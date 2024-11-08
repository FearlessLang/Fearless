use crate::ast::{Program, E};
use crate::interp::{Interpreter, Value};

pub fn apply(program: &mut Program) {
	let mut replacements: Vec<Replacement> = Vec::new();
	let mut interp = Interpreter::with(program, 1000);
	for fun in program.funs.values() {
		if let Some(call_fun_hash) = fun.body.get_static_call() {
			let call_fun = program.lookup_fun_by_hash(&call_fun_hash).unwrap();
			let body = &call_fun.body;
			if let Ok(inlined) = interp.inline(body) {
				replacements.push(Replacement {
					fun_hash: fun.name.unique_hash,
					new_body: inlined,
				});
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
