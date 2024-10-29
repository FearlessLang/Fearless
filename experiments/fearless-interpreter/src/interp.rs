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
				match self.program.lookup_type_by_hash(obj.def) {
					Some(def) => match &def.singleton_instance {
						Some(obj) => obj,
						None => Err(InterpreterError::Internal(anyhow!("No singleton instance on type")))?,
					},
					None =>
						Err(InterpreterError::Internal(anyhow!("Method call on non-existent type '{}' from:\n{:?}", obj.def, recv)))?,
				}
			},
			_ => Err(InterpreterError::Internal(anyhow!("Method call on non-obj type: {:?}", recv)))?,
		};
		let meth = recv_obj.meths.get(&call.meth)
			.ok_or_else(|| {
				let type_name = self.program.lookup_type_by_hash(recv_obj.def)
					.map(|def| def.name.to_string())
					.unwrap_or(recv_obj.def.to_string());
				InterpreterError::Internal(anyhow!("Method '{}' not found on '{:?}'", call.meth, type_name))
			})?;
		let fun = meth
			.fun_name
			.ok_or_else(|| {
				let type_name = self.program.lookup_type_by_hash(recv_obj.def)
					.map(|def| def.name.to_string())
					.unwrap_or(recv_obj.def.to_string());
				InterpreterError::Internal(anyhow!("Illegal call to abstract method '{}' on '{:?}'", call.meth, type_name))
			})
			.and_then(|fun_name| self.program
				.lookup_fun_by_hash(fun_name)
				.ok_or_else(|| InterpreterError::Internal(anyhow!("Function '{}' not found", fun_name)))
			)?;

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
