use crate::ast::{HasType, Program};
use anyhow::Result;
use std::fs;
use std::ops::Index;
use crate::dec_id::ExplicitDecId;
use crate::interp::Interpreter;
use crate::schema_capnp::RC;

mod proto;
mod schema_capnp;
mod interp;
mod dec_id;
mod ast;

#[global_allocator]
static ALLOC: mimalloc::MiMalloc = mimalloc::MiMalloc;

#[tokio::main]
async fn main() -> Result<()> {
	let paths = ["/tmp/test.fear.pkg.mearless", "/tmp/base.fear.pkg.mearless"];
	let program = {
		let mut program = Program::new();
		for path in paths.iter() {
			let raw = fs::read(path)?;
			program.add_pkg(&raw)?;
		}
		program
	};

	let entry: ExplicitDecId = "test.Usage/0".try_into()?;
	println!("{:?}", entry);
	let entry = program.lookup(&entry).unwrap();
	assert!(entry.has_singleton());
	let entry_recv = ast::E::SummonObj(entry.t());
	let entry_ret = program.lookup::<ExplicitDecId>(&"test.Num/0".try_into().unwrap()).unwrap();
	let entry_call = ast::E::MCall(ast::MCall::new(entry_recv, RC::Imm, blake3::hash("#".as_bytes()), vec![], entry_ret.t()));
	let mut interp = Interpreter::new(program);
	interp.eval(&entry_call)?;
	
	// println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
