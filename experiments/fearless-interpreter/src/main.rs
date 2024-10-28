use crate::ast::Program;
use anyhow::Result;
use std::fs;
use std::ops::Index;
use crate::dec_id::ExplicitDecId;

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
	
	println!("{:?}", entry);
	// println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
