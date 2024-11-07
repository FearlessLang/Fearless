use crate::ast::{Meth, MethImpl, MethName, Program};
use crate::interp;
use crate::interp::{InterpreterError, Value};
use crate::magic::magic_meths::add_override;
use crate::schema_capnp::RC;
use hashbrown::HashMap;
use std::sync::OnceLock;
use blake3::Hash;
use crate::magic::MagicImpl;

pub(super) const TYPE_NAME: &str = "base.Magic/0";
static METHS: OnceLock<HashMap<blake3::Hash, Meth>> = OnceLock::new();

pub(super) struct Impl;
impl MagicImpl for Impl {
	fn cell(&self) -> &'static OnceLock<HashMap<Hash, Meth>> { &METHS}
	fn instance_name(&self) -> &str { TYPE_NAME }
	fn add_overrides(&self, type_name: &str, mut meths: HashMap<Hash, Meth>) -> HashMap<Hash, Meth> {
		let origin = blake3::hash(type_name.as_bytes());
		add_override(MethName::new(RC::Imm, "!", 0), MethImpl::Magic(bang), origin, &mut meths);
		meths
	}
}

fn bang(_recv: Value, _args: Vec<Value>) -> interp::Result<interp::InterpreterE> {
	Err(InterpreterError::Internal("No magic implementation for this method was found.".into()))
}
