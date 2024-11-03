use crate::ast::{Meth, MethImpl, MethName, Program};
use crate::interp;
use crate::interp::{InterpreterError, Value};
use crate::magic::magic_meths::add_override;
use crate::schema_capnp::RC;
use anyhow::anyhow;
use hashbrown::HashMap;
use std::sync::OnceLock;

pub(crate) const TYPE_NAME: &str = "base.Magic/0";
static METHS: OnceLock<HashMap<blake3::Hash, Meth>> = OnceLock::new();

pub fn meth<'a>(meth_hash: &blake3::Hash, program: &'a Program) -> anyhow::Result<&'a Meth> {
	let meths = METHS.get_or_try_init(|| {
		let def = program.lookup_type_by_hash(&blake3::hash(TYPE_NAME.as_bytes()))
			.ok_or(anyhow!("No base.Magic/0"))?;
		let obj = match def.singleton_instance {
			Some(ref obj) => obj,
			None => return Err(anyhow!("No singleton instance for {}", def.name)),
		};
		Ok(add_overrides(TYPE_NAME, obj.meths.clone()))
	})?;
	let meth = meths.get(meth_hash)
		.ok_or(anyhow!("No method for {} in base.Nat/0", meth_hash))?;
	Ok(meth)
}

fn add_overrides(type_name: &str, mut meths: HashMap<blake3::Hash, Meth>) -> HashMap<blake3::Hash, Meth> {
	let origin = blake3::hash(type_name.as_bytes());
	add_override(MethName::new(RC::Imm, "!", 0), MethImpl::Magic(bang), origin, &mut meths);
	meths
}
fn bang(_recv: Value, _args: Vec<Value>) -> interp::Result<interp::InterpreterE> {
	Err(InterpreterError::Internal(anyhow!("No magic implementation for this method was found.")))
}
