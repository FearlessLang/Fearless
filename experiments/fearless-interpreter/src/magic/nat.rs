use crate::ast::{Meth, MethImpl, MethName, Program};
use crate::interp;
use crate::interp::{InterpreterE, Value};
use crate::magic::magic_meths::add_override;
use crate::magic::MagicType;
use crate::schema_capnp::RC;
use anyhow::{anyhow, Result};
use hashbrown::HashMap;
use itertools::Itertools;
use std::sync::OnceLock;

const TYPE_NAME: &str = "base.Nat/0";
const INSTANCE_NAME: &str = "base._NatInstance/0";
static METHS: OnceLock<HashMap<blake3::Hash, Meth>> = OnceLock::new();

pub fn meth<'a>(meth_hash: &blake3::Hash, program: &'a Program) ->  Result<&'a Meth> {
	let meths = METHS.get_or_try_init(|| {
		let def = program.lookup_type_by_hash(&blake3::hash(INSTANCE_NAME.as_bytes()))
			.ok_or(anyhow!("No {}", INSTANCE_NAME))?;
		let obj = match def.singleton_instance {
			Some(ref obj) => obj,
			None => return Err(anyhow!("No singleton instance for {}", def.name)),
		};
		Ok(add_overrides(INSTANCE_NAME, obj.meths.clone()))
	})?;
	let meth = meths.get(meth_hash)
		.ok_or(anyhow!("No method for {} in base.Nat/0", meth_hash))?;
	Ok(meth)
}

fn add_overrides(type_name: &str, mut meths: HashMap<blake3::Hash, Meth>) -> HashMap<blake3::Hash, Meth> {
	let origin = blake3::hash(type_name.as_bytes());
	add_override(MethName::new(RC::Imm, "<=", 1), MethImpl::Magic(leq), origin, &mut meths);
	add_override(MethName::new(RC::Imm, "+", 1), MethImpl::Magic(plus), origin, &mut meths);
	add_override(MethName::new(RC::Imm, "-", 1), MethImpl::Magic(minus), origin, &mut meths);
	add_override(MethName::new(RC::Read, ".str", 0), MethImpl::Magic(str), origin, &mut meths);
	meths
}
fn leq(lhs: Value, args: Vec<Value>) -> interp::Result<InterpreterE> {
	let rhs = &args[0];
	let Value::Magic(lhs) = lhs else { unreachable!("{:?} was not magical", lhs) };
	let Value::Magic(rhs) = rhs else { unreachable!("{:?} was not magical", rhs) };
	let MagicType::Nat(lhs) = lhs else { unreachable!("{:?} was not a Nat", lhs) };
	let MagicType::Nat(rhs) = rhs else { unreachable!("{:?} was not a Nat", rhs) };
	Ok(InterpreterE::Value(Value::from(lhs <= *rhs)))
}
fn plus(lhs: Value, args: Vec<Value>) -> interp::Result<InterpreterE> {
	let rhs = &args[0];
	let Value::Magic(lhs) = lhs else { unreachable!("{:?} was not magical", lhs) };
	let Value::Magic(rhs) = rhs else { unreachable!("{:?} was not magical", rhs) };
	let MagicType::Nat(lhs) = lhs else { unreachable!("{:?} was not a Nat", lhs) };
	let MagicType::Nat(rhs) = rhs else { unreachable!("{:?} was not a Nat", rhs) };
	Ok(InterpreterE::Value(Value::Magic(MagicType::Nat(lhs + *rhs))))
}
fn minus(lhs: Value, args: Vec<Value>) -> interp::Result<InterpreterE> {
	let rhs = &args[0];
	let Value::Magic(lhs) = lhs else { unreachable!("{:?} was not magical", lhs) };
	let Value::Magic(rhs) = rhs else { unreachable!("{:?} was not magical", rhs) };
	let MagicType::Nat(lhs) = lhs else { unreachable!("{:?} was not a Nat", lhs) };
	let MagicType::Nat(rhs) = rhs else { unreachable!("{:?} was not a Nat", rhs) };
	Ok(InterpreterE::Value(Value::Magic(MagicType::Nat(lhs - *rhs))))
}
fn str(recv: Value, _: Vec<Value>) -> interp::Result<InterpreterE> {
	let Value::Magic(recv) = recv else { unreachable!("{:?} was not magical", recv) };
	let MagicType::Nat(recv) = recv else { unreachable!("{:?} was not a Nat", recv) };
	Ok(InterpreterE::Value(Value::Magic(MagicType::String(recv.to_string()))))
}
