use std::fs;
use std::ops::Index;
use capnp::message::{ReaderOptions, TypedReader};
use anyhow::Result;
use crate::interp::Program;

mod proto;
mod schema_capnp;
mod interp;
mod state;

#[global_allocator]
static ALLOC: mimalloc::MiMalloc = mimalloc::MiMalloc;

fn main() -> Result<()> {
	let paths = ["/tmp/test.fear.pkg.mearless"];
	let raw = paths.iter()
		.map(|path| Ok(fs::read(path)?))
		.collect::<Result<Vec<_>>>()?;

	let readers = raw.iter()
		.map(|raw_pkg| -> Result<_> {
			let msg_reader = capnp::serialize::read_message(
				raw_pkg.as_slice(),
				ReaderOptions::new()
			)?;
			Ok(TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader))
		})
		.collect::<Result<Vec<_>>>()?;
	
	let mut p = Program::new(raw);
	for reader in readers {
		let r = reader.get()?;
		p.load_package()?;
	}

	let mut program = interp::Program::new(raw);
	program.load_package(0).unwrap();
	// for i in 0..len {
	// }
	// println!("Hello, world! {:?}", program);
	Ok(())
}
