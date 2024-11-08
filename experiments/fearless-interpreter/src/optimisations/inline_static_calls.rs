use crate::ast::{CallTarget, Program, E};
use crate::interp::{Interpreter, Value};

pub fn apply(program: &mut Program) {
	let mut replacements: Vec<Replacement> = Vec::new();
	let mut interpreter = Interpreter::with(program, 1000);
	for fun in program.funs.values() {
		if let E::MCall(call) = &fun.body {
			if let CallTarget::Fun(call_fun_hash) = call.meth {
				let call_fun = program.funs.get(&call_fun_hash).unwrap();
				if call_fun.name.captures_self { continue; }
				
				if let Ok(inlined) = interpreter.inline(call) {
					replacements.push(Replacement {
						fun_hash: fun.name.unique_hash,
						new_body: inlined,
					});
				}
			}
		}
	}
	for replacement in replacements {
		program.funs.get_mut(&replacement.fun_hash).unwrap().body = E::Computed(replacement.new_body);
	}
}

struct Replacement {
	fun_hash: blake3::Hash,
	new_body: Value,
}
