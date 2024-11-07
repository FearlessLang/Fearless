use crate::ast::{Meth, MethImpl, MethName};
use crate::interp;
use crate::interp::{InterpreterE, Value};
use crate::magic::magic_meths::add_override;
use crate::magic::{MagicImpl, MagicType};
use crate::schema_capnp::RC;
use blake3::Hash;
use hashbrown::HashMap;
use std::sync::OnceLock;

pub(super) const TYPE_NAME: &str = "base.Nat/0";
const INSTANCE_NAME: &str = "base._NatInstance/0";
static METHS: OnceLock<HashMap<Hash, Meth>> = OnceLock::new();

pub(super) struct Impl;
impl MagicImpl for Impl {
	fn cell(&self) -> &'static OnceLock<HashMap<Hash, Meth>> { &METHS }
	fn instance_name(&self) -> &str { INSTANCE_NAME }
	fn add_overrides(&self, type_name: &str, mut meths: HashMap<Hash, Meth>) -> HashMap<Hash, Meth> {
		let origin = blake3::hash(type_name.as_bytes());
		add_override(MethName::new(RC::Imm, "<=", 1), MethImpl::Magic(leq), origin, &mut meths);
		add_override(MethName::new(RC::Imm, "+", 1), MethImpl::Magic(plus), origin, &mut meths);
		add_override(MethName::new(RC::Imm, "-", 1), MethImpl::Magic(minus), origin, &mut meths);
		add_override(MethName::new(RC::Read, ".str", 0), MethImpl::Magic(str), origin, &mut meths);
		meths
	}
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
