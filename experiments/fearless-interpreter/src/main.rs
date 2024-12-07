use crate::ast::{CallTarget, HasType, Program, SummonObj, E};
use crate::dec_id::{DecId, ExplicitDecId};
use crate::interp::Interpreter;
use crate::schema_capnp::RC;
use std::fs;

mod schema_capnp;
mod interp;
mod dec_id;
mod ast;
mod magic;
mod rc;
mod pretty_print;
mod optimisations;
mod jit;

#[global_allocator]
static ALLOC: mimalloc::MiMalloc = mimalloc::MiMalloc;

// #[tokio::main]
fn main() {
	let paths = ["/tmp/test.fear.pkg.mearless", "/tmp/base.fear.pkg.mearless"];

	// let mut jit = jit::Jit::default();
	// let _ = jit::lower::Translator::translate_program(&mut jit, paths.iter()).unwrap();

	let program = {
		let mut program = Program::new();
		for path in paths.iter() {
			let raw = fs::read(path).unwrap();
			program.add_pkg(&raw).unwrap();
		}
		program.compute_effectively_final_defs();
		optimisations::devirtualise_final::apply(&mut program);
		// optimisations::inline_static_calls::apply(&mut program);
		program
	};
	
	// println!("{:#?}", program);
	let entry: ExplicitDecId = "test.Usage/0".try_into().unwrap();
	let entry = program.lookup_type(&entry).unwrap();
	assert!(entry.has_singleton());
	let entry_recv = E::SummonObj(SummonObj { def: entry.name.unique_hash(), rc: RC::Imm });
	let entry_ret = program.lookup_type::<ExplicitDecId>(&"test.Num/0".try_into().unwrap()).unwrap();
	let entry_call = ast::MCall::new(
		entry_recv,
		RC::Imm,
		CallTarget::Meth(blake3::hash(b"imm #/0")),
		vec![],
		entry_ret.t()
	);
	// let entry_ret = program.lookup_type::<ExplicitDecId>(&"base.Str/0".try_into().unwrap()).unwrap();
	// let entry_call = ast::MCall::new(entry_recv, RC::Imm, CallTarget::Meth(blake3::hash("imm #/1".as_bytes())), vec![
	// 	E::SummonObj(SummonObj { rc: RC::Imm, def: program.lookup_type::<ExplicitDecId>(&"base.LList/1".try_into().unwrap()).unwrap().name.unique_hash() })
	// ], entry_ret.t());
	let mut interpreter = Interpreter::new(program, 0);
	interpreter.run(&entry_call).unwrap();
	println!("\nStack trace:\n{}", interpreter);
	
	// println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
}
