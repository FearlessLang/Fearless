use hashbrown::HashMap;
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
	let paths = ["/tmp/test.fear.pkg.mearless", "/tmp/base.fear.pkg.mearless"];
	let raw = paths.iter()
		.map(|path| Ok(fs::read(path)?))
		.collect::<Result<Vec<_>>>()?;

	let readers = raw.iter()
		.map(|raw_pkg| -> Result<_> {
			let msg_reader = capnp::serialize::read_message(
				raw_pkg.as_slice(),
				ReaderOptions::new()
			)?;
			let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
			Ok((reader.get()?.get_name()?.to_string()?, reader))
		})
		.collect::<Result<HashMap<_, _>>>()?;
	
	let program = Program::new(readers);
	// for reader in readers {
	// 	// let r = reader.get()?;
	// 	// p.load_package(r)?;
	// }

	// let mut program = Program::new();
	// program.load_package().unwrap();
	// for i in 0..len {
	// }
	println!("{:?}", program.package_names().collect::<Vec<_>>());
	Ok(())
}
