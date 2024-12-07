//! Lower Mearless (MIR) to Cranelift IL

#![allow(unused)]

use crate::ast::RawType;
use crate::jit::Jit;
use crate::magic::MagicType;
use crate::{ast, schema_capnp};
use anyhow::Result;
use capnp::message::{ReaderOptions, TypedReader};
use core::default::Default;
use cranelift::prelude::*;
use cranelift_module::Module;
use hashbrown::HashSet;
use std::fs;
use std::path::Path;

pub struct Translator<'jit> {
	jit: &'jit mut Jit,
	non_final_defs: HashSet<blake3::Hash>
}
impl<'jit> Translator<'jit> {
	pub fn translate_program<P: AsRef<Path>, Ps: Iterator<Item=P>>(jit: &'jit mut Jit, paths: Ps) -> Result<Self> {
		let mut translator = Self {
			jit,
			non_final_defs: Default::default()
		};
		for path in paths {
			let raw = fs::read(path)?;
			translator.translate_pkg(&raw)?;
		}
		Ok(translator)
	}
	fn translate_pkg(&mut self, raw: &[u8]) -> Result<()> {
		let msg_reader = capnp::serialize::read_message_from_flat_slice(
			&mut &*raw,
			ReaderOptions::new()
		)?;
		let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
		let reader = reader.get()?;
		// TODO: defs?
		for fun in reader.get_funs()? {
			let _ = self.translate_fun(fun);
		}
		Ok(())
	}
	fn translate_fun(&mut self, fun: schema_capnp::fun::Reader) -> Result<()> {
		// TODO: will need to manage stack slots, etc. for representing objects as structs
		let ptr_t = self.jit.module.target_config().pointer_type();
		let ret = ast::Type::parse(fun.get_ret()?)?;
		let ret_t = match ret.rt {
			RawType::Plain(plain) => {
				if plain == blake3::hash(b"base.Nat/0") || plain == blake3::hash(b"base.Int/0") {
					types::I64
				} else if plain == blake3::hash(b"base.Float/0") {
					types::F64
				} else if plain == blake3::hash(b"base.Byte/0") {
					types::I8
				} else {
					todo!()
				}
			},
			RawType::Any => todo!(),
			RawType::Magic(magic) => match magic {
				MagicType::String(_) => todo!(),
				MagicType::Magic => todo!(),
				MagicType::Int(_) => types::I64,
				MagicType::Nat(_) => types::I64,
				MagicType::Float(_) => types::F64,
				MagicType::Byte(_) => types::I8,
			}
		};

		self.jit.ctx.func.signature.params.push(AbiParam::new(ptr_t));
		self.jit.ctx.func.signature.returns.push(AbiParam::new(ret_t));

		let mut builder = FunctionBuilder::new(&mut self.jit.ctx.func, &mut self.jit.builder_ctx);
		let entry_block = builder.create_block();
		builder.append_block_params_for_function_params(entry_block);
		builder.switch_to_block(entry_block);
		builder.seal_block(entry_block);
		
		
		// println!("{:?}", fun);
		Ok(())
	}
}
