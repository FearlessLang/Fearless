use std::borrow::Cow;
use crate::ast::{CreateObj, MCall, Program, SummonObj, TypePair, E, THIS_X};
use anyhow::anyhow;
use hashbrown::HashMap;
use itertools::Itertools;
use std::collections::VecDeque;
use std::error::Error;
use std::fmt::{Display, Formatter};
use std::os::unix::fs::lchown;
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
	// gamma: HashMap<u32, Value>,
	program: Rc<Program>,
	stack: VecDeque<StackFrame>,
}
impl Interpreter {
	pub fn new(program: Program) -> Self {
		Self {
			program: Rc::new(program),
			stack: Default::default(),
		}
	}

	pub fn run(&mut self, entry: &MCall) -> Result<()> {
		let res = self.eval_call(entry);
		match res {
			Ok(res) => {
				let def = match res {
					Value::Obj(obj) => self.program.lookup_type_by_hash(obj.k.def)
						.unwrap(),
				};
				println!("{}", def.name);
				Ok(())
			},
			Err(ref e) => match e {
				InterpreterError::Fearless(_) => todo!(),
				InterpreterError::NonDeterministic(_) => todo!(),
				InterpreterError::Internal(_) => res.map(|_| ()),
			},
		}
	}

	fn eval(&mut self, expr: &E) -> Result<Value> {
		match expr {
			E::X(_) => todo!("X"),
			E::MCall(call) => self.eval_call(call),
			E::CreateObj(_) => todo!("CreateObj"),
			E::SummonObj(k) => Value::summoned_obj(k, &self.program),
			E::InterpreterValue(value) => Ok(value.clone()),
		}
	}
	fn eval_call(&mut self, call: &MCall) -> Result<Value> {
		let recv = self.eval(&call.recv)?;
		let args: Vec<Value> = call.args.iter()
			.map(|arg| self.eval(arg))
			.collect::<Result<_>>()?;
		let Value::Obj(recv_obj) = &recv;
		let meth = recv_obj.k.meths.get(&call.meth)
			.ok_or_else(|| {
				let type_name = self.program.lookup_type_by_hash(recv_obj.k.def)
					.map(|def| def.name.to_string())
					.unwrap_or(recv_obj.k.def.to_string());
				InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, type_name))
			})?;
		let gamma = &meth.sig.xs;
		let fun = meth
			.fun_name
			.ok_or_else(|| {
				let type_name = self.program.lookup_type_by_hash(recv_obj.k.def)
					.map(|def| def.name.to_string())
					.unwrap_or(recv_obj.k.def.to_string());
				InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{:?}'", call.meth, type_name))
			})
			.and_then(|fun_name| self.program
				.lookup_fun_by_hash(fun_name)
				.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_name)))
			)?;

		let capture_context = if is_meth_in_obj(recv_obj, &call.meth) {
			let mut ctx = recv_obj.fat_meths.get(&call.meth)
				.ok_or_else(|| InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, recv_obj.k.def)))?
				.clone();
			ctx.and_zip(&args, gamma);
			ctx
		} else {
			let mut ctx = CaptureContext::zip(&args, gamma);
			ctx.add(THIS_X, recv.clone());
			ctx
		};

		self.stack.push_front(StackFrame::new(recv_obj.k.def, call.meth));

		let body = fun.body.clone();
		let (_, body) = self.allocate_captures(capture_context, &body)?;

		Ok(body)
		// match recv {
		// 	E::Obj(obj) => {
		// 		let method = obj.lookup_method(method)?;
		// 		let ret = method.call(args)?;
		// 		Ok(E::Obj(ret))
		// 	}
		// 	_ => todo!(),
		// }
	}

	// I'm currently ignoring CaptureContext to see if I can do this without it
	fn allocate_captures(&mut self, mut c: CaptureContext, e: &E) -> Result<(CaptureContext, Value)> {
		match e {
			E::X(xt) => match c.lookup(xt.x) {
				Some(value) => {
					let value = value.clone();
					// Ok((c, value))
					Ok((CaptureContext::new(), value))
				},
				None => Err(InterpreterError::Internal(anyhow!("Capture not found: {:?}", xt))),
			},
			E::MCall(call) => {
				let (recv_ctx, recv_value) = self.allocate_captures(c.clone(), &call.recv)?;
				let args: Vec<(CaptureContext, Value)> = call.args.iter()
					.map(|arg| self.allocate_captures(c.clone(), arg))
					.collect::<Result<_>>()?;
				let reduced_call = MCall {
					recv: Box::new(E::InterpreterValue(recv_value)),
					rc: call.rc,
					meth: call.meth,
					args: args.into_iter().map(|(_, v)| E::InterpreterValue(v)).collect(),
					return_type: call.return_type,
				};
				let res = self.eval_call(&reduced_call)?;
				Ok((CaptureContext::new(), res))
			},
			E::CreateObj(k) => {
				let fat_meths = k.meths.iter()
					.map(|(meth_id, meth)| {
						let mut ctx = CaptureContext::with_capacity(meth.captures.len());
						c.add(k.self_name, Value::obj(k));
						for x in meth.captures.iter() {
							let capture = c.lookup(*x)
								.ok_or_else(|| InterpreterError::Internal(anyhow!("Capture not found: {:?}", x)))?;
							ctx.add(*x, capture.clone());
						}
						Ok((*meth_id, ctx))
					})
					.collect::<Result<HashMap<_,_>>>()?;
				let obj = Value::Obj(Obj {
					k: k.clone(),
					fat_meths
				});
				Ok((CaptureContext::new(), obj))
			},
			E::SummonObj(k) => Ok((CaptureContext::new(), Value::summoned_obj(k, &self.program)?)),
			E::InterpreterValue(_) => todo!(),
		}
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

fn is_meth_in_obj(obj: &Obj, meth: &blake3::Hash) -> bool {
	obj.fat_meths.contains_key(meth)
}

#[derive(Debug, Clone)]
pub enum Value {
	Obj(Obj),
}
impl Value {
	fn obj(k: &CreateObj) -> Self {
		Value::Obj(Obj {
			k: k.clone(),
			fat_meths: k.meths.iter()
				.filter(|(_, meth)| meth.origin == k.def)
				.map(|(meth_id, _)| (*meth_id, CaptureContext::new()))
				.collect()
		})
	}
	fn summoned_obj(k: &SummonObj, program: &Program) -> Result<Self> {
		let k = match program.lookup_type_by_hash(k.def) {
			Some(def) => match &def.singleton_instance {
				Some(obj) => obj,
				None => Err(InterpreterError::Internal(anyhow!("No singleton instance on type")))?,
			},
			None =>
				Err(InterpreterError::Internal(anyhow!("Method call on non-existent type '{}' from:\n{:?}", k.def, k)))?,
		};
		Ok(Value::Obj(Obj {
			k: k.clone(),
			fat_meths: Default::default()
		}))
	}
}

#[derive(Debug, Clone)]
pub struct Obj {
	k: CreateObj,
	/// `mc` from our reduction, the capture context per-method
	fat_meths: HashMap<blake3::Hash, CaptureContext>
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
	fn zip(vs: &[Value], gamma: &[TypePair]) -> Self {
		let mut inner = HashMap::with_capacity(vs.len());
		vs.iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				inner.insert(xt.x, v.clone());
			});
		Self { inner }
	}

	fn and_zip(&mut self, vs: &[Value], gamma: &[TypePair]) {
		vs.iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				self.inner.insert(xt.x, v.clone());
			});
	}
}
