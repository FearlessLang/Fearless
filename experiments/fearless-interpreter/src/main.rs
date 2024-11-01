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

	println!("{:?}", program);
	let entry: ExplicitDecId = "test.Test/0".try_into()?;
	let entry = program.lookup_type(&entry).unwrap();
	assert!(entry.has_singleton());
	let entry_recv = E::SummonObj(SummonObj { def: entry.name.unique_hash(), rc: RC::Mut });
	// let entry_ret = program.lookup_type::<ExplicitDecId>(&"test.Num/0".try_into().unwrap()).unwrap();
	// let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/0".as_bytes()), vec![], entry_ret.t());
	let entry_ret = program.lookup_type::<ExplicitDecId>(&"base.Void/0".try_into().unwrap()).unwrap();
	let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/1".as_bytes()), vec![
		E::SummonObj(SummonObj { rc: RC::Imm, def: program.lookup_type::<ExplicitDecId>(&"base.LList/1".try_into().unwrap()).unwrap().name.unique_hash() })
	], entry_ret.t());
	let mut interp = Interpreter::new(program, 0);
	interp.run(entry_call)?;
	println!("\nStack trace:\n{}", interp);
	
	// println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
