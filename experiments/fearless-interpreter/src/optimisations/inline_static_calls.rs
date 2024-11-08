use crate::ast::{Program, E};

pub fn apply(program: &mut Program) {
	let mut replacements: Vec<Replacement> = Vec::new();
	for fun in program.funs.values() {
		if let Some(call_fun_hash) = fun.body.get_static_call() {
			let call_fun = program.lookup_fun_by_hash(&call_fun_hash).unwrap();
			let body = &call_fun.body;
			// TODO: rename X's in body to apply fun args
			
			replacements.push(Replacement {
				fun_hash: fun.name.unique_hash,
				new_body: body.clone(),
			});
		}
	}
	for replacement in replacements {
		program.funs.get_mut(&replacement.fun_hash).unwrap().body = replacement.new_body;
	}
}

struct Replacement {
	fun_hash: blake3::Hash,
	new_body: E,
}
