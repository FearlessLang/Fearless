use std::error::Error;
use std::fmt::{Display, Formatter};
use crate::ast::{CreateObj, HasType, MCall, Program, RawType, Type, E};
use hashbrown::HashMap;
use std::rc::Rc;
use anyhow::{anyhow, Context};
use blake3::hash;

#[derive(Debug)]
pub enum InterpreterError {
	Fearless(Value),
	NonDeterministic(Value),
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
pub struct Interpreter {
	// gamma: HashMap<u32, Value>,
	program: Rc<Program>,
	stack: Vec<()>,
}
impl Interpreter {
	pub fn new(program: Program) -> Self {
		Self {
			program: Rc::new(program),
			stack: Vec::new(),
		}
	}
	pub fn eval(&mut self, expr: &E) -> Result<E> {
		match expr {
			E::X(_) => todo!("X"),
			E::MCall(call) => self.eval_call(call),
			E::CreateObj(_) => todo!("CreateObj"),
			E::SummonObj(_) => Ok(expr.clone()),
			E::InterpreterValue(_) => Ok(expr.clone()),
		}
	}

	fn eval_call(&mut self, call: &MCall) -> Result<E> {
		let recv = self.eval(&call.recv)?;
		let args: Vec<E> = call.args.iter().map(|arg| self.eval(arg)).collect::<Result<_>>()?;
		let recv_obj: &CreateObj = match &recv {
			E::CreateObj(obj) => obj,
			E::SummonObj(obj) => {
				match self.program.lookup_by_hash(hash_from_type(obj)?) {
					Some(def) => match &def.singleton_instance {
						Some(obj) => obj,
						None => Err(InterpreterError::Internal(anyhow!("No singleton instance on type")))?,
					},
					None =>
						Err(InterpreterError::Internal(anyhow!("Method call on non-existent type '{}' from:\n{:?}", hash_from_type(&obj)?, recv)))?,
				}
			},
			_ => Err(InterpreterError::Internal(anyhow!("Method call on non-obj type: {:?}", recv)))?,
		};
		
		todo!()
		// match recv {
		// 	E::Obj(obj) => {
		// 		let method = obj.lookup_method(method)?;
		// 		let ret = method.call(args)?;
		// 		Ok(E::Obj(ret))
		// 	}
		// 	_ => todo!(),
		// }
	}
}

fn hash_from_type(ty: &Type) -> Result<blake3::Hash> {
	match ty.rt {
		RawType::Plain(ty) => Ok(ty),
		RawType::Any => Err(InterpreterError::Internal(anyhow!("Type lookup on non-concrete type"))),
	}
}

#[derive(Debug, Clone)]
pub enum Value {
	Obj(Obj),
}
#[derive(Debug, Clone)]
struct Obj {
	type_def: blake3::Hash,
	fat_meths: HashMap<blake3::Hash, FatMeth>
}
#[derive(Debug, Clone)]
struct FatMeth {
	inner: HashMap<u32, Value>
}
