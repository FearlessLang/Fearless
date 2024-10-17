use anyhow::Result;
use std::fs;
use std::ops::Index;
use crate::ast::Program;

mod proto;
mod schema_capnp;
mod interp;
mod state;
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

	// let readers = raw.iter()
	// 	.map(|raw_pkg| -> Result<_> {
	// 		let msg_reader = capnp::serialize::read_message(
	// 			raw_pkg.as_slice(),
	// 			ReaderOptions::new()
	// 		)?;
	// 		let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
	// 		Ok((reader.get()?.get_name()?.to_string()?, reader))
	// 	})
	// 	.collect::<Result<HashMap<_, _>>>()?;
	
	// let program = Program::new(readers);
	// for reader in readers {
	// 	// let r = reader.get()?;
	// 	// p.load_package(r)?;
	// }

	// let mut program = Program::new();
	// program.load_package().unwrap();
	// for i in 0..len {
	// }
	
	
	println!("{:?}", program.pkg_names().collect::<Vec<_>>());
	Ok(())
}
