use std::fmt::{Debug, Formatter, Write};
use std::mem::MaybeUninit;
use std::ptr::addr_of_mut;
use crate::{schema_capnp, state};
use anyhow::{bail, Result};
use capnp::message;
use capnp::message::{ReaderOptions, TypedReader};
use capnp::serialize::BufferSegments;
use hashbrown::HashMap;

// #[derive(Debug)]
pub struct Program<'mir> {
	// msg_reader: message::Reader<BufferSegments<&'mir [u8]>>,
	raw: Vec<Vec<u8>>,
	types: TypeTable<'mir>,
}
impl<'mir> Debug for Program<'mir> {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		f.write_fmt(format_args!("Program {{ types: {:?} }}", self.types))
	}
}
impl<'mir> Program<'mir> {
	pub fn new(packages: Vec<Vec<u8>>) -> Program<'mir> {
		Program {
			raw: packages,
			types: TypeTable::new(),
		}
	}
	pub fn load_package(&mut self, ) -> Result<()> {

		let err = self.packages[idx].reader.get_defs()?
			.iter()
			.find_map(|def| self.types.add(def).err());
		match err {
			Some(e) => Err(e),
			None => Ok(()),
		}
	}
}

#[derive(Debug)]
#[repr(transparent)]
struct TypeTable<'mir> {
	types: HashMap<state::DecId<'mir>, schema_capnp::type_def::Reader<'mir>>
}
impl<'mir> TypeTable<'mir> {
	fn new() -> Self {
		Self {
			types: HashMap::new(),
		}
	}
	fn add(&mut self, def: schema_capnp::type_def::Reader<'mir>) -> Result<()> {
		let key = state::DecId(def.get_name()?);
		if self.types.contains_key(&key) {
			bail!("Invalid MIR: duplicate type definition: {:?}", key);
		}
		self.types.insert(key, def);
		Ok(())
	}
}
