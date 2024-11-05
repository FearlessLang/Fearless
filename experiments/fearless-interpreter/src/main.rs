#![feature(once_cell_try)]

use crate::ast::{HasType, Program, SummonObj, E};
use anyhow::Result;
use std::fs;
use crate::dec_id::{DecId, ExplicitDecId};
use crate::interp::Interpreter;
use crate::schema_capnp::RC;

mod schema_capnp;
mod interp;
mod dec_id;
mod ast;
mod magic;
mod rc;
mod pretty_print;

#[global_allocator]
static ALLOC: mimalloc::MiMalloc = mimalloc::MiMalloc;

// #[tokio::main]
fn main() -> Result<()> {
	let paths = ["tests/fib/test.fear.pkg.mearless", "tests/fib/base.fear.pkg.mearless"];
	let program = {
		let mut program = Program::new();
		for path in paths.iter() {
			let raw = fs::read(path)?;
			program.add_pkg(&raw)?;
		}
		program
	};
	
	// println!("{:#?}", program);
	let entry: ExplicitDecId = "test.Test/0".try_into()?;
	let entry = program.lookup_type(&entry).unwrap();
	assert!(entry.has_singleton());
	let entry_recv = E::SummonObj(SummonObj { def: entry.name.unique_hash(), rc: RC::Mut });
	// let entry_ret = program.lookup_type::<ExplicitDecId>(&"test.Num/0".try_into().unwrap()).unwrap();
	// let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/0".as_bytes()), vec![], entry_ret.t());
	// let entry_ret = program.lookup_type::<ExplicitDecId>(&"base.Void/0".try_into().unwrap()).unwrap();
	// let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/1".as_bytes()), vec![
	// 	E::SummonObj(SummonObj { rc: RC::Imm, def: program.lookup_type::<ExplicitDecId>(&"base.LList/1".try_into().unwrap()).unwrap().name.unique_hash() })
	// ], entry_ret.t());
	// let mut interpreter = Interpreter::new(program, 0);
	// interpreter.run(entry_call.clone())?;
	// println!("\nStack trace:\n{}", interpreter);

	
	let mut ps = Vec::new();
	let mut happy = 0;
	let mut sad = 0;
	for _ in 0..100 {
		let paths = ["tests/fib/test.fear.pkg.mearless", "tests/fib/base.fear.pkg.mearless"];
		let program = {
			let mut program = Program::new();
			for path in paths.iter() {
				let raw = fs::read(path)?;
				program.add_pkg(&raw)?;
			}
			program
		};
		if !ps.iter().any(|p| p == &program) {
			ps.push(program.clone());
		}
	
		// println!("{:#?}", program);
		let entry: ExplicitDecId = "test.Test/0".try_into()?;
		let program = &program;
		let entry = program.lookup_type(&entry).unwrap();
		assert!(entry.has_singleton());
		let entry_recv = E::SummonObj(SummonObj { def: entry.name.unique_hash(), rc: RC::Mut });
		let entry_ret = program.lookup_type::<ExplicitDecId>(&"base.Str/0".try_into().unwrap()).unwrap();
		let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/1".as_bytes()), vec![
			E::SummonObj(SummonObj { rc: RC::Imm, def: program.lookup_type::<ExplicitDecId>(&"base.LList/1".try_into().unwrap()).unwrap().name.unique_hash() })
		], entry_ret.t());
		let mut interp = Interpreter::new(program.clone(), 0);
		let res = interp.run(entry_call.clone())?;
		// match res {
		// 	Ok(_) => happy += 1,
		// 	Err(_) => sad += 1,
		// }
	}
	
	println!("{:?}", ps.len());
	println!("{} {}", happy, sad);
	
	// println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
