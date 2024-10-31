use crate::ast::{CreateObj, MCall, Meth, Program, SummonObj, Type, TypePair, E, THIS_X};
use anyhow::anyhow;
use hashbrown::HashMap;
use itertools::{zip_eq, Itertools};
use std::borrow::Cow;
// use std::collections::HashMap;
use std::collections::VecDeque;
use std::error::Error;
use std::fmt::{Display, Formatter};
use std::mem::{ManuallyDrop, MaybeUninit};
use std::rc::Rc;
use crate::bounded_stack::BoundedStack;
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

#[derive(Debug, Clone)]
struct StackFrame {
	def: Option<blake3::Hash>,
	meth: Option<blake3::Hash>,
	fun_ctx: CaptureContext,
	fun_body: InterpreterE,
}
impl StackFrame {
	fn new(def: blake3::Hash, meth: blake3::Hash, fun_ctx: CaptureContext, fun_body: InterpreterE) -> Self {
		Self {
			def: Some(def),
			meth: Some(meth),
			fun_ctx,
			fun_body,
		}
	}
	fn new_magic_frame(fun_ctx: CaptureContext, fun_body: InterpreterE) -> Self {
		Self {
			def: None,
			meth: None,
			fun_ctx,
			fun_body,
		}
	}
	fn extend(&self, fun_ctx: CaptureContext, fun_body: InterpreterE) -> Self {
		Self {
			def: self.def,
			meth: self.meth,
			fun_ctx,
			fun_body,
		}
	}
}

const STACK_SIZE: usize = 8388608 / size_of::<StackFrame>();
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
			stack: VecDeque::new(),
		}
	}

	pub fn run(&mut self, entry: MCall) -> Result<()> {
		let main_expr = allocate_captures(&CaptureContext::new(), &E::MCall(entry))?;
		let entry_frame = StackFrame::new_magic_frame(CaptureContext::new(), main_expr);
		self.push_frame(entry_frame);
		let res = self.eval_loop();

		match res {
			Ok(res) => {
				let def = match res {
					Value::Obj(obj) => self.program.lookup_type_by_hash(obj.k.def())
						.unwrap(),
				};
				println!("{}", def.name);
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

	// fn eval<'a>(&mut self, expr: &'a E) -> Result<Cow<'a, Value>> {
	// 	match expr {
	// 		E::SummonObj(k) => Ok(Cow::Owned(Value::summoned_obj(k)?)),
	// 		// E::InterpreterE(value) => Ok(Cow::Borrowed(value)),
	// 		E::InterpreterE(value) => todo!(),
	// 		E::MCall(_) => unreachable!("MCall is not a valid free expression"),
	// 		E::X(_) => unreachable!("X is not a valid free expression"),
	// 		E::CreateObj(_) => unreachable!("CreateObj is not a valid free expression"),
	// 	}
	// }

	fn push_frame(&mut self, frame: StackFrame) {
		if self.stack.len() == STACK_SIZE {
			todo!("Stack overflow");
		}
		self.stack.push_front(frame);
	}

	fn pop_frame(&mut self) -> Option<StackFrame> {
		self.stack.pop_front()
	}

	fn eval_loop(&mut self) -> Result<Value> {
		let mut res: Option<Value> = None;
		loop {
			let frame = self.pop_frame().unwrap();
			// let body = self.allocate_captures(&frame.fun_ctx, &frame.fun_body)?;
			match &frame.fun_body {
				InterpreterE::Value(v) => {
					res = Some(v.clone());
					return Ok(res.unwrap());
				},
				InterpreterE::MCall(ref call) => self.eval_call2(&frame, call)?,
			};
			if self.stack.is_empty() {
				return Ok(res.unwrap());
			}
		};
	}

	fn eval_call2(&mut self, frame: &StackFrame, call: &AllocatedMCall) -> Result<Value> {
		let recv = match &*call.recv {
			InterpreterE::Value(recv) => recv,
			InterpreterE::MCall(recv) => {
				self.push_frame(frame.extend(frame.fun_ctx.clone(), InterpreterE::MCall(recv.clone())));
				// self.push_frame()
				println!("recv: {}", self);
				return Ok(());
			},
		};

		let mut is_reduced = true;
		call.args.iter()
			.filter_map(|arg| match arg {
				InterpreterE::Value(_) => None,
				InterpreterE::MCall(arg) => Some(frame.extend(frame.fun_ctx.clone(), InterpreterE::MCall(arg.clone())))
			})
			.rev()
			.for_each(|frame| {
				is_reduced = false;
				self.push_frame(frame);
				println!("arg: {}", self);
			});
		if !is_reduced {
			return Ok(());
		}

		let mut args: Vec<&Value> = Vec::with_capacity(call.args.len());
		for arg in call.args.iter() {
			match arg {
				InterpreterE::Value(arg) => args.push(arg),
				InterpreterE::MCall(_) => unreachable!(),
			}
		}

		let meth = recv.meths(&self.program)?.get(&call.meth)
			.ok_or_else(|| {
				let type_name = self.type_name_or_hash(recv);
				InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, type_name))
			})?;
		let gamma = &meth.sig.xs;
		let fun = meth
			.fun_name
			.ok_or_else(|| {
				let type_name = self.type_name_or_hash(recv);
				InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{}'", call.meth, type_name))
			})
			.and_then(|fun_name| self.program
				.lookup_fun_by_hash(fun_name)
				.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_name)))
			)?;

		let capture_context = if is_meth_in_obj(recv, &call.meth) {
			let mut ctx = recv.meth_captures(&call.meth)?.clone();
			ctx.and_zip(args.as_slice(), gamma);
			ctx
		} else {
			let mut ctx = CaptureContext::zip(&args, gamma);
			ctx.add(THIS_X, Value::clone(recv));
			ctx
		};

		let fun_ctx = {
			let fun_ctx_len = fun.args.len() + (if fun.name.captures_self { 1 } else { 0 }) + meth.captures.len();
			let mut fun_ctx = CaptureContext::with_capacity(fun_ctx_len);

			let fun_args = args.iter()
				.map(|value| &**value)
				.chain(std::iter::once(recv))
				.chain(meth.captures.iter().map(|x| capture_context.lookup(*x).unwrap()));
			for (v, fun_xt) in zip_eq(fun_args, fun.args.iter()) {
				fun_ctx.add(fun_xt.x, v.clone());
			}
			fun_ctx
		};
		let body = allocate_captures(&fun_ctx, &fun.body)?;
		self.push_frame(StackFrame::new(recv.def(), call.meth, fun_ctx, body));
		println!("body: {}", self);
		Ok(())
	}

	// fn eval_call(&mut self, call: &MCall) -> Result<Value> {
	// 	let frame: &StackFrame;
	// 	{
	// 		let recv = self.eval(&call.recv)?;
	// 		let args: Vec<Cow<Value>> = call.args.iter()
	// 			.map(|arg| self.eval(arg))
	// 			.collect::<Result<_>>()?;
	// 		let Value::Obj(recv_obj) = &*recv;
	// 		let meth = recv_obj.k.meths(&self.program)?.get(&call.meth)
	// 			.ok_or_else(|| {
	// 				let type_name = self.type_name_or_hash(recv_obj);
	// 				InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, type_name))
	// 			})?;
	// 		let gamma = &meth.sig.xs;
	// 		let fun = meth
	// 			.fun_name
	// 			.ok_or_else(|| {
	// 				let type_name = self.type_name_or_hash(recv_obj);
	// 				InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{}'", call.meth, type_name))
	// 			})
	// 			.and_then(|fun_name| self.program
	// 				.lookup_fun_by_hash(fun_name)
	// 				.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_name)))
	// 			)?;
	// 
	// 		let capture_context = if is_meth_in_obj(recv_obj, &call.meth) {
	// 			let mut ctx = recv_obj.fat_meths.get(&call.meth)
	// 				.ok_or_else(|| InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, recv_obj.k.def())))?
	// 				.clone();
	// 			ctx.and_zip(&args, gamma);
	// 			ctx
	// 		} else {
	// 			let mut ctx = CaptureContext::zip(&args, gamma);
	// 			ctx.add(THIS_X, Value::clone(&*recv));
	// 			ctx
	// 		};
	// 
	// 		let fun_ctx = {
	// 			let fun_ctx_len = fun.args.len() + (if fun.name.captures_self { 1 } else { 0 }) + meth.captures.len();
	// 			let mut fun_ctx = CaptureContext::with_capacity(fun_ctx_len);
	// 
	// 			let fun_args = args.iter()
	// 				.map(|value| &**value)
	// 				.chain(std::iter::once(&*recv))
	// 				.chain(meth.captures.iter().map(|x| capture_context.lookup(*x).unwrap()));
	// 			for (v, fun_xt) in zip_eq(fun_args, fun.args.iter()) {
	// 				fun_ctx.add(fun_xt.x, v.clone());
	// 			}
	// 			fun_ctx
	// 		};
	// 
	// 		self.stack.push_front(StackFrame::new(recv_obj.k.def(), call.meth, fun_ctx, fun.body.clone()));
	// 	};
	//
	// 	let frame = &self.stack[0];
	// 	let body = self.allocate_captures(&frame.fun_ctx, &frame.fun_body)?;
	// 	match body {
	// 		InterpreterE::Value(v) => {
	// 			self.stack.pop_front();
	// 			return Ok(v);
	// 		},
	// 		InterpreterE::MCall(call) => {
	//
	// 		}
	// 	};
	// 	self.stack.pop_front();
	//
	// 	// Ok(body)
	// 	todo!()
	// }

	fn type_name_or_hash(&self, obj: &Value) -> String {
		let Value::Obj(obj) = obj;
		self.program.lookup_type_by_hash(obj.k.def())
			.map(|def| def.name.to_string())
			.unwrap_or(obj.k.def().to_string())
	}
}
impl Display for Interpreter {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		for frame in self.stack.iter() {
			match frame.def.and_then(|def| self.program.lookup_type_by_hash(def)) {
				None => writeln!(f, "<unknown>: <unknown>")?,
				Some(def) => {
					let meth: Cow<'static, str> = frame.meth
						.and_then(|meth| def.sigs.get(&meth))
						.map(|sig| sig.name.to_string().into())
						.unwrap_or("<unknown>".into());
					writeln!(f, "{}: {}", def.name, meth)?;
				},
			}
		}
		Ok(())
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
			let recv_value = allocate_captures(c, &call.recv)?;
			let args: Vec<InterpreterE> = call.args.iter()
				.map(|arg| allocate_captures(c, arg))
				.collect::<Result<_>>()?;
			let allocated_call = AllocatedMCall {
				recv: Box::new(recv_value),
				rc: call.rc,
				meth: call.meth,
				args: args.into_iter().collect(),
				return_type: call.return_type,
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
		E::InterpreterE(_) => unreachable!(),
	}
}

fn is_meth_in_obj(obj: &Value, meth: &blake3::Hash) -> bool {
	match obj {
		Value::Obj(obj) => obj.fat_meths.contains_key(meth),
	}
}

#[derive(Debug, Clone)]
pub enum InterpreterE {
	Value(Value),
	MCall(AllocatedMCall),
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
pub enum Value {
	Obj(Obj),
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
		}
	}
	fn def(&self) -> blake3::Hash {
		match self {
			Value::Obj(obj) => obj.k.def(),
		}
	}
	fn meth_captures(&self, meth_name: &blake3::Hash) -> Result<&CaptureContext> {
		match self {
			Value::Obj(obj) => {
				let fat_meth = obj.fat_meths.get(meth_name)
					.ok_or_else(|| InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", meth_name, obj.k.def())))?;
				Ok(fat_meth)
			},
		}
	}
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
	fn zip(vs: &[&Value], gamma: &[TypePair]) -> Self {
		let mut inner = HashMap::with_capacity(vs.len());
		vs.iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				inner.insert(xt.x, Value::clone(v));
			});
		Self { inner }
	}
	fn and_zip(&mut self, vs: &[&Value], gamma: &[TypePair]) {
		vs.iter()
			.zip_eq(gamma)
			.for_each(|(v, xt)| {
				self.inner.insert(xt.x, Value::clone(v));
			});
	}
}
