use crate::ast::{CreateObj, MCall, Meth, Program, SummonObj, Type, TypeDef, TypePair, E, THIS_X};
use anyhow::anyhow;
use hashbrown::HashMap;
use itertools::{zip_eq, Itertools};
use std::borrow::Cow;
// use std::collections::HashMap;
use std::collections::VecDeque;
use std::error::Error;
use std::fmt::{Display, Formatter};
use std::rc::Rc;
use crate::magic::MagicType;
use crate::schema_capnp::RC;

#[derive(Debug)]
pub enum InterpreterError {
	Fearless(Box<Value>),
	NonDeterministic(Box<Value>),
	Internal(anyhow::Error),
}

impl Display for InterpreterError {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		match self {
			InterpreterError::Fearless(_) => todo!(),
			InterpreterError::NonDeterministic(_) => todo!(),
			InterpreterError::Internal(error) => error.fmt(f),
		}
	}
}
impl Error for InterpreterError {}
type Result<T> = std::result::Result<T, InterpreterError>;

#[derive(Debug, Copy, Clone)]
struct StackFrame { def: blake3::Hash, meth: blake3::Hash }
impl StackFrame {
	fn new(def: blake3::Hash, meth: blake3::Hash) -> Self {
		Self { def, meth }
	}
}

#[derive(Debug, Clone)]
pub struct Interpreter {
	program: Rc<Program>,
	stack: VecDeque<StackFrame>,
	depth: u32,
	max_depth: u32,
}
impl Interpreter {
	pub fn new(program: Program) -> Self {
		Self {
			program: Rc::new(program),
			stack: Default::default(),
			depth: 0,
			max_depth: 1000,
		}
	}

	pub fn run(&mut self, entry: MCall) -> Result<()> {
		let entry = Self::allocate_captures(&CaptureContext::new(), &E::MCall(entry))?;
		let InterpreterE::MCall(entry) = entry else { unreachable!("non-call main expression") };
		let res = self.eval_call(entry);
		match res {
			Ok(res) => {
				match res {
					Value::Obj(obj) => {
						let def = self.program.lookup_type_by_hash(obj.k.def()).unwrap();
						println!("{}", def.name);
					},
					Value::Magic(ty) => {
						println!("{}", ty);
					}
				};
				Ok(())
			},
			Err(ref e) => {
				eprintln!("Stack trace:\n{}", self);
				match e {
					InterpreterError::Fearless(_) => todo!(),
					InterpreterError::NonDeterministic(_) => todo!(),
					InterpreterError::Internal(_) => res.map(|_| ()),
				}
			},
		}
	}

	fn eval_call(&mut self, call: AllocatedMCall) -> Result<Value> {
		self.depth += 1;
		if self.depth > self.max_depth {
			return Err(InterpreterError::Internal(anyhow!("Stack overflowed")));
		}

		let recv = call.recv.eval(self)?;
		let args: Vec<Value> = call.args.into_iter()
			.map(|arg| arg.clone().eval(self))
			.collect::<Result<_>>()?;
		let meth = recv.meths(&self.program)?.get(&call.meth)
			.ok_or_else(|| {
				let type_name = self.type_name_or_hash(&recv);
				InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, type_name))
			})?;
		let gamma = &meth.sig.xs;
		let fun = meth
			.fun_name
			.ok_or_else(|| {
				let type_name = self.type_name_or_hash(&recv);
				InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{}'", call.meth, type_name))
			})
			.and_then(|fun_name| self.program
				.lookup_fun_by_hash(fun_name)
				.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_name)))
			)?;

		let capture_context = if is_meth_in_obj(&recv, &call.meth) {
			let ctx = &*recv.meth_captures(&call.meth)?;
			let mut ctx = CaptureContext::to_owned(ctx);
			ctx.and_zip(args.clone(), gamma);
			ctx
		} else {
			let mut ctx = CaptureContext::zip(args.clone(), gamma);
			ctx.add(THIS_X, recv.clone());
			ctx
		};

		self.stack.push_front(StackFrame::new(recv.def(), call.meth));

		let fun_ctx = {
			let fun_ctx_len = fun.args.len() + (if fun.name.captures_self { 1 } else { 0 }) + meth.captures.len();
			let mut fun_ctx = CaptureContext::with_capacity(fun_ctx_len);

			let fun_args = args.iter()
				.chain(std::iter::once(&recv))
				.chain(meth.captures.iter().map(|x| capture_context.lookup(*x).unwrap()));
			for (v, fun_xt) in zip_eq(fun_args, fun.args.iter()) {
				fun_ctx.add(fun_xt.x, v.clone());
			}
			fun_ctx
		};

		let body = fun.body.clone();
		let body = Self::allocate_captures(&fun_ctx, &body)?;
		match body {
			InterpreterE::Value(value) => {
				self.stack.pop_front();
				Ok(value)
			},
			InterpreterE::MCall(next_call) => Ok(self.eval_call(next_call)?),
		}
	}

	fn allocate_captures(c: &CaptureContext, e: &E) -> Result<InterpreterE> {
		match e {
			E::X(xt) => match c.lookup(xt.x) {
				Some(value) => {
					let value = value.clone();
					// Ok((c, value))
					Ok(InterpreterE::Value(value))
				},
				None => {
					Err(InterpreterError::Internal(anyhow!("X not found: {:?}", xt)))
				},
			},
			E::MCall(call) => {
				let recv_value = Self::allocate_captures(c, &call.recv)?;
				let args: Vec<InterpreterE> = call.args.iter()
					.map(|arg| Self::allocate_captures(c, arg))
					.collect::<Result<_>>()?;
				let allocated_call = AllocatedMCall {
					recv: Box::new(recv_value),
					rc: call.rc,
					meth: call.meth,
					args: args.into_iter().collect(),
					return_type: call.return_type.clone(),
				};
				Ok(InterpreterE::MCall(allocated_call))
			},
			E::CreateObj(k) => {
				let fat_meths = k.meths.iter()
					.map(|(meth_id, meth)| {
						let mut ctx = CaptureContext::with_capacity(meth.captures.len());
						for x in meth.captures.iter() {
							let capture = c.lookup(*x)
								.ok_or_else(|| InterpreterError::Internal(anyhow!("Capture not found: {:?}", x)))?;
							ctx.add(*x, capture.clone());
						}
						Ok((*meth_id, ctx))
					})
					.collect::<Result<HashMap<_,_>>>()?;
				let obj = Value::Obj(Obj {
					k: Literal::obj(k.clone()),
					fat_meths
				});
				Ok(InterpreterE::Value(obj))
			},
			E::SummonObj(k) => Ok(InterpreterE::Value(Value::summoned_obj(k)?)),
			E::MagicValue(_, ty) => Ok(InterpreterE::Value(Value::Magic(ty.clone()))),
		}
	}

	fn type_name_or_hash(&self, obj: &Value) -> String {
		self.program.lookup_type_by_hash(obj.def())
			.map(|def| def.name.to_string())
			.unwrap_or(obj.def().to_string())
	}
}
impl Display for Interpreter {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		for frame in self.stack.iter() {
				match self.program.lookup_type_by_hash(frame.def) {
					None => writeln!(f, "<unknown>: <unknown>")?,
					Some(def) => {
						let meth: Cow<'static, str> = def.sigs.get(&frame.meth)
							.map(|sig| sig.name.to_string().into())
							.unwrap_or("<unknown>".into());
						writeln!(f, "{}: {}", def.name, meth)?;
					},
				}
			}
		Ok(())
	}
}

fn is_meth_in_obj(obj: &Value, meth: &blake3::Hash) -> bool {
	match obj {
		Value::Obj(obj) => obj.fat_meths.contains_key(meth),
		Value::Magic(_) => false,
	}
}

#[derive(Debug, Clone)]
pub enum Value {
	Obj(Obj),
	Magic(MagicType),
}
impl Value {
	fn obj(k: &CreateObj) -> Self {
		Value::Obj(Obj {
			k: Literal::obj(k.clone()),
			fat_meths: k.meths.iter()
				.filter(|(_, meth)| meth.origin == k.def)
				.map(|(meth_id, _)| (*meth_id, CaptureContext::new()))
				.collect()
		})
	}
	fn summoned_obj(k: &SummonObj) -> Result<Self> {
		Ok(Value::Obj(Obj {
			k: Literal::Summoned(k.clone()),
			fat_meths: Default::default()
		}))
	}
	fn meths<'a>(&'a self, program: &'a Program) -> Result<&'a HashMap<blake3::Hash, Meth>> {
		match self {
			Value::Obj(obj) => obj.k.meths(program),
			Value::Magic(_) => todo!("magic methods"),
		}
	}
	fn def(&self) -> blake3::Hash {
		match self {
			Value::Obj(obj) => obj.k.def(),
			Value::Magic(_) => blake3::hash(&[]),
		}
	}
	fn meth_captures(&self, meth_name: &blake3::Hash) -> Result<Cow<CaptureContext>> {
		match self {
			Value::Obj(obj) => {
				let fat_meth = obj.fat_meths.get(meth_name)
					.ok_or_else(|| InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", meth_name, obj.k.def())))?;
				Ok(Cow::Borrowed(fat_meth))
			},
			Value::Magic(_) => Ok(Cow::Owned(CaptureContext::new())),
		}
	}
}

#[derive(Debug, Clone)]
enum InterpreterE {
	Value(Value),
	MCall(AllocatedMCall),
}
impl InterpreterE {
	fn eval(self, interpreter: &mut Interpreter) -> Result<Value> {
		match self {
			InterpreterE::Value(v) => Ok(v),
			InterpreterE::MCall(call) => interpreter.eval_call(call)
		}
	}
}
#[derive(Debug, Clone)]
struct AllocatedMCall {
	recv: Box<InterpreterE>,
	rc: RC,
	meth: blake3::Hash,
	args: Vec<InterpreterE>,
	return_type: Type,
}

#[derive(Debug, Clone)]
pub struct Obj {
	k: Literal,
	/// `mc` from our reduction, the capture context per-method
	fat_meths: HashMap<blake3::Hash, CaptureContext>
}
#[derive(Debug, Clone)]
enum Literal {
	Summoned(SummonObj),
	Obj(Box<CreateObj>),
}
impl Literal {
	fn obj(obj: CreateObj) -> Self {
		Literal::Obj(Box::new(obj))
	}
	fn def(&self) -> blake3::Hash {
		match self {
			Literal::Summoned(k) => k.def,
			Literal::Obj(k) => k.def,
		}
	}
	fn meths<'a>(&'a self, program: &'a Program) -> Result<&'a HashMap<blake3::Hash, Meth>> {
		match self {
			Literal::Summoned(k) => {
				match program.lookup_type_by_hash(k.def) {
					Some(def) => match &def.singleton_instance {
						Some(obj) => Ok(&obj.meths),
						None => Err(InterpreterError::Internal(anyhow!("No singleton instance on type")))?,
					},
					None =>
						Err(InterpreterError::Internal(anyhow!("Method call on non-existent type '{}' from:\n{:?}", k.def, k)))?,
				}
			},
			Literal::Obj(k) => Ok(&k.meths),
		}
	}
}

/// `c` from our reduction, a capture context
#[derive(Debug, Clone)]
#[repr(transparent)]
struct CaptureContext {
	inner: HashMap<u32, Value>
}
impl CaptureContext {
	fn new() -> Self {
		Self {
			inner: HashMap::new()
		}
	}
	fn with_capacity(n: usize) -> Self {
		Self {
			inner: HashMap::with_capacity(n)
		}
	}
	fn lookup(&self, key: u32) -> Option<&Value> {
		self.inner.get(&key)
	}
	fn add(&mut self, key: u32, value: Value) {
		self.inner.insert(key, value);
	}
	fn zip(vs: Vec<Value>, gamma: &[TypePair]) -> Self {
		let mut inner = HashMap::with_capacity(vs.len());
		vs.into_iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				inner.insert(xt.x, v);
			});
		Self { inner }
	}
	fn and_zip(&mut self, vs: Vec<Value>, gamma: &[TypePair]) {
		vs.into_iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				self.inner.insert(xt.x, v);
			});
	}
}
