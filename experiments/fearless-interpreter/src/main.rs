use crate::ast::{HasType, Program, SummonObj};
use anyhow::Result;
use std::fs;
use crate::dec_id::{DecId, ExplicitDecId};
use crate::interp::Interpreter;
use crate::schema_capnp::RC;

mod proto;
mod schema_capnp;
mod interp;
mod dec_id;
mod ast;

#[global_allocator]
static ALLOC: mimalloc::MiMalloc = mimalloc::MiMalloc;

// #[tokio::main]
fn main() -> Result<()> {
	let paths = ["/tmp/test.fear.pkg.mearless", "/tmp/base.fear.pkg.mearless"];
	let program = {
		let mut program = Program::new();
		for path in paths.iter() {
			let raw = fs::read(path)?;
			program.add_pkg(&raw)?;
		}
		program
	};

	// println!("{:?}", program);
	let entry: ExplicitDecId = "test.Usage/0".try_into()?;
	let entry = program.lookup_type(&entry).unwrap();
	assert!(entry.has_singleton());
	let entry_recv = ast::E::SummonObj(SummonObj { def: entry.name.unique_hash(), rc: RC::Mut });
	let entry_ret = program.lookup_type::<ExplicitDecId>(&"test.Num/0".try_into().unwrap()).unwrap();
	let entry_call = ast::MCall::new(entry_recv, RC::Imm, blake3::hash("imm #/0".as_bytes()), vec![], entry_ret.t());
	let mut interp = Interpreter::new(program);
	interp.run(&entry_call)?;
	println!("\nStack trace:\n{}", interp);

	println!("{}", size_of::<interp::Value>());
	
	// println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
