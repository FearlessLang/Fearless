use crate::ast::{CreateObj, MCall, Meth, MethImpl, Program, RawType, SummonObj, Type, TypePair, E, THIS_X};
use crate::magic::MagicType;
use crate::pretty_print::{Format, PrettyPrint};
use crate::rc::format_rc;
use crate::schema_capnp::RC;
use anyhow::anyhow;
use hashbrown::HashMap;
use itertools::{zip_eq, Itertools};
use std::borrow::Cow;
// use std::collections::HashMap;
use std::collections::VecDeque;
use std::error::Error;
use std::fmt::{Display, Formatter, Write};
use std::rc::Rc;

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
pub type Result<T> = std::result::Result<T, InterpreterError>;

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
	pub fn new(program: Program, max_depth: u32) -> Self {
		Self {
			program: Rc::new(program),
			stack: Default::default(),
			depth: 0,
			max_depth,
		}
	}

	pub fn run(&mut self, entry: MCall) -> Result<()> {
		let entry = Self::allocate_captures(&CaptureContext::new(), &E::MCall(entry), self.program.clone())?;
		let InterpreterE::MCall(entry) = entry else { unreachable!("non-call main expression") };
		let res = self.eval_call(entry);
		match res {
			Ok(res) => {
				match res {
					Value::Obj(obj) => {
						let def = self.program.lookup_type_by_hash(&obj.k.def()).unwrap();
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
		if self.max_depth > 0 && self.depth > self.max_depth {
			return Err(InterpreterError::Internal(anyhow!("Stack overflowed")));
		}

		self.stack.push_front(StackFrame::new(call.recv.def().unwrap_or(blake3::hash(b"")), call.meth));
		let recv = call.recv.eval(self)?;
		let args: Vec<Value> = call.args.into_iter()
			.map(|arg| arg.clone().eval(self))
			.collect::<Result<_>>()?;
		let meth = recv.meth(&call.meth, &self.program)?;
		let gamma = &meth.sig.xs;
		
		match &meth.body {
			MethImpl::Fun(fun_hash) => {
				let fun = self.program
					.lookup_fun_by_hash(fun_hash)
					.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_hash)))?;

				let fun_ctx = {
					let fun_ctx_len = fun.args.len() + (if fun.name.captures_self { 1 } else { 0 }) + meth.captures.len();
					let mut fun_ctx = CaptureContext::with_capacity(fun_ctx_len);

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

					let fun_args = args.iter()
						.chain(std::iter::once(&recv))
						.chain(meth.captures.iter().map(|x| capture_context.lookup(*x).unwrap()));
					for (v, fun_xt) in zip_eq(fun_args, fun.args.iter()) {
						fun_ctx.add(fun_xt.x, v.clone());
					}

					self.stack.pop_front();
					self.stack.push_front(StackFrame::new(recv.def(), call.meth));
					fun_ctx
				};

				let body = fun.body.clone();
				println!("please eval: {}", Format(|f| body.pretty_print(f, &self.program)));
				let body = Self::allocate_captures(&fun_ctx, &body, self.program.clone())?;
				match body {
					InterpreterE::Value(value) => {
						self.stack.pop_front();
						Ok(value)
					},
					InterpreterE::MCall(next_call) => Ok(self.eval_call(next_call)?),
				}
			},
			MethImpl::Magic(magic) => {
				self.stack.pop_front();
				self.stack.push_front(StackFrame::new(recv.def(), call.meth));
				let body = magic(recv, args)?;
				match body {
					InterpreterE::Value(value) => {
						self.stack.pop_front();
						Ok(value)
					},
					InterpreterE::MCall(next_call) => Ok(self.eval_call(next_call)?),
				}
			},
			MethImpl::Abstract => {
				let type_name = type_name_or_hash(&self.program, &recv);
				Err(InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{}'", call.meth, type_name)))
			},
		}
	}

	fn allocate_captures(c: &CaptureContext, e: &E, p: Rc<Program>) -> Result<InterpreterE> {
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
				let recv_value = Self::allocate_captures(c, &call.recv, p.clone())?;
				println!("Recv for: {}", Format(|f| recv_value.pretty_print(f, &p)));
				let args: Vec<InterpreterE> = call.args.iter()
					.map(|arg| Self::allocate_captures(c, arg, p.clone()))
					.collect::<Result<_>>()?;
				let allocated_call = AllocatedMCall {
					recv: Box::new(recv_value),
					rc: call.rc,
					meth: call.meth,
					args: args.into_iter().collect(),
					return_type: call.return_type.clone(),
				};
				
				// debug_assert!({
        //   let Some(recv) = p.lookup_type_by_hash(&allocated_call.recv.def().unwrap())
        //   else { unreachable!("No type found for {:?}", allocated_call.recv) };
        //   recv.sigs.contains_key(&allocated_call.meth)
        // }, "Method not found: {:?}", allocated_call.meth);
				
				let bad_meth = blake3::hash(b"imm -/1");
				let bad_dec = blake3::hash(b"test.Fib/0");
				if allocated_call.recv.def().map(|def| def == bad_dec).unwrap_or(false) && call.meth == bad_meth {
					println!("Bad call! {}\n\n", Format(|f| allocated_call.pretty_print(f, &p)));
				} else {
					println!("Acceptable: {}\n\n", Format(|f| allocated_call.pretty_print(f, &p)));
				}
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
}
impl Display for Interpreter {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		for frame in self.stack.iter() {
				match self.program.lookup_type_by_hash(&frame.def) {
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

fn type_name_or_hash(program: &Program, obj: &Value) -> String {
	program.lookup_type_by_hash(&obj.def())
		.map(|def| def.name.to_string())
		.unwrap_or(obj.def().to_string())
}

#[derive(Debug, Clone)]
pub enum Value {
	Obj(Obj),
	Magic(MagicType),
}
impl Value {
	pub fn obj(k: &CreateObj) -> Self {
		Value::Obj(Obj {
			k: Literal::obj(k.clone()),
			fat_meths: k.meths.iter()
				.filter(|(_, meth)| meth.origin == k.def)
				.map(|(meth_id, _)| (*meth_id, CaptureContext::new()))
				.collect()
		})
	}
	pub fn summoned_obj(k: &SummonObj) -> Result<Self> {
		Ok(Value::Obj(Obj {
			k: Literal::Summoned(k.clone()),
			fat_meths: Default::default()
		}))
	}
	fn meth<'a>(&'a self, meth_hash: &blake3::Hash, program: &'a Program) -> Result<&'a Meth> {
		match self {
			Value::Obj(obj) => obj.k.meths(program)?.get(meth_hash)
				.ok_or_else(|| {
					let type_name = type_name_or_hash(program, self);
					let visible = obj.k.meths(program).unwrap().values().map(|meth| &meth.sig.name).collect::<Vec<_>>();
					InterpreterError::Internal(anyhow!("Method '{}' not found on '{}'\nVisible: {:?}", meth_hash, type_name, visible))
				}),
			Value::Magic(m) =>
				m.meth(meth_hash, program).map_err(InterpreterError::Internal),
		}
	}
	fn def(&self) -> blake3::Hash {
		match self {
			Value::Obj(obj) => obj.k.def(),
			Value::Magic(magic) => magic.def(),
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
impl PrettyPrint for Value {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		match self {
			Value::Obj(obj) => obj.k.pretty_print(f, program),
			Value::Magic(ty) => ty.pretty_print(f, program),
		}
	}
}

#[derive(Debug, Clone)]
pub enum InterpreterE {
	Value(Value),
	MCall(AllocatedMCall),
}
impl InterpreterE {
	fn eval(self, interpreter: &mut Interpreter) -> Result<Value> {
		match self {
			InterpreterE::Value(v) => Ok(v),
			InterpreterE::MCall(call) => {
				interpreter.eval_call(call)
			}
		}
	}
	fn def(&self) -> Option<blake3::Hash> {
		match self {
			InterpreterE::Value(v) => Some(v.def()),
			InterpreterE::MCall(call) => match &call.return_type.rt {
				RawType::Plain(def) => Some(*def),
				RawType::Any => None,
				RawType::Magic(magic) => Some(magic.def()),
			},
		}
	}
}
impl PrettyPrint for InterpreterE {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		match self {
			InterpreterE::Value(v) => v.pretty_print(f, program),
			InterpreterE::MCall(call) => call.pretty_print(f, program),
		}
	}
}

#[derive(Debug, Clone)]
pub struct AllocatedMCall {
	recv: Box<InterpreterE>,
	rc: RC,
	meth: blake3::Hash,
	args: Vec<InterpreterE>,
	return_type: Type,
}
impl PrettyPrint for AllocatedMCall {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		let recv = program.lookup_type_by_hash(&self.recv.def().unwrap());
		let recv = match recv {
			Some(def) => def,
			None => panic!("No type found for {:?} @ {:?}", self.recv, self.recv.def()),
		};
		let meth = recv.sigs.get(&self.meth);
		f.write_str(format_rc(self.rc))?;
		f.write_str(" ")?;
		write!(f, "{} ", recv.name)?;
		match meth {
			Some(meth) => {
				write!(f, "{}(", meth.name)?;
			},
			None => write!(f, "{}(", self.meth)?,
		}
		for (i, arg) in self.args.iter().enumerate() {
			arg.pretty_print(f, program)?;
			f.write_str(": ")?;
			let t = &program.lookup_type_by_hash(&arg.def().unwrap()).unwrap().name;
			t.pretty_print(f, program)?;
			if i < self.args.len() - 1 {
				f.write_str(", ")?;
			}
		}
		f.write_str("): ")?;
		self.return_type.pretty_print(f, program)?;
		Ok(())
	}
}

#[derive(Debug, Clone)]
struct Obj {
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
				match program.lookup_type_by_hash(&k.def) {
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
impl PrettyPrint for Literal {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		let t = program.lookup_type_by_hash(&self.def()).unwrap();
		let n_captures = if let Literal::Obj(k) = self { k.captures.len() } else { 0 };
		write!(f, "{}[{}]", t.name, n_captures)
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
