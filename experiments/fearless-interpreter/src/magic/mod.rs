mod nat;
#[allow(clippy::module_inception)] // base.Magic is a type called "Magic" :P
mod magic;
mod magic_meths;

use crate::ast::{Meth, Program, SummonObj, E};
use crate::dec_id::{AstDecId, DecId};
use anyhow::{bail, Result};
use regex::Regex;
use std::fmt::{Display, Formatter};
use crate::interp::Value;
use crate::schema_capnp::RC;

#[derive(Debug, Clone, PartialEq)]
pub enum MagicType {
	String(String),
	Int(i64),
	Nat(u64),
	Float(f64),
	Byte(u8),
	Magic,
}
impl MagicType {
	pub fn meth<'a>(&self, meth_hash: &blake3::Hash, program: &'a Program) -> Result<&'a Meth> {
		match self {
			MagicType::Nat(_) => nat::meth(meth_hash, program),
			MagicType::Magic => magic::meth(meth_hash, program),
			_ => bail!("No methods for {:?}", self),
		}
	}
}

impl Display for MagicType {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		match self {
			MagicType::String(s) => f.write_str(s),
			MagicType::Int(i) => write!(f, "{}", i),
			MagicType::Nat(n) => write!(f, "{}", n),
			MagicType::Float(float) => write!(f, "{}", float),
			MagicType::Byte(b) => write!(f, "{}", b),
			MagicType::Magic => f.write_str("Magic"),
		}
	}
}

thread_local! {
	static IS_INT: Regex = Regex::new(r"[+-][\d_]*\d+$").unwrap();
	static IS_FLOAT: Regex = Regex::new(r"[+-]?[\d_]*\d+\.[\d_]*\d+$").unwrap();
	static IS_NAT: Regex = Regex::new(r"[\d_]*\d+$").unwrap();
}

pub fn is_magic_type(dec_id: AstDecId) -> bool {
	let name = dec_id.full_name();
	
	if is_numeric_literal(name) || is_string_literal(name) { return true; }
	if dec_id.full_name() == "base.Magic" && dec_id.arity() == 0 { return true; }
	false
}

pub fn parse_type(dec_id: AstDecId) -> Result<MagicType> {
	let name = dec_id.full_name();
	if name == "base.Magic" && dec_id.arity() == 0 {
		return Ok(MagicType::Magic)
	}
	
	if is_numeric_literal(name) {
		if IS_INT.with(|re| re.is_match(name)) {
			let name = name.strip_prefix('+').unwrap_or(name);
			Ok(MagicType::Int(name.parse::<i64>()?))
		} else if IS_NAT.with(|re| re.is_match(name)) {
			let name = name.strip_prefix('+').unwrap_or(name);
			Ok(MagicType::Nat(name.parse::<u64>()?))
		} else if IS_FLOAT.with(|re| re.is_match(name)) {
			let name = name.strip_prefix('+').unwrap_or(name);
			Ok(MagicType::Float(name.parse::<f64>()?))
		} else {
			bail!("Unknown numeric type: {}", name)
		}
	} else if is_string_literal(name) {
		Ok(MagicType::String(name[1..name.len()-1].to_string()))
	} else {
		bail!("Unknown magic type: {}", name)
	}
}

pub fn is_literal(dec_name: &str) -> bool {
	is_string_literal(dec_name) || is_numeric_literal(dec_name)
}

pub fn is_string_literal(dec_name: &str) -> bool {
	dec_name.starts_with("\"") && dec_name.ends_with("\"")
}

pub fn is_numeric_literal(dec_name: &str) -> bool {
	dec_name.starts_with(['-', '+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'])
}

impl From<bool> for Value {
	fn from(b: bool) -> Self {
		let ty = if b { "base.True/0" } else { "base.False/0" };
		Value::summoned_obj(&SummonObj {
			rc: RC::Imm,
			def: blake3::hash(ty.as_bytes()),
		}).unwrap()
	}
}
