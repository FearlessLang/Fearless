mod nat;
#[allow(clippy::module_inception)] // base.Magic is a type called "Magic" :P
mod magic;
mod magic_meths;

use crate::ast::{Meth, Program, SummonObj};
use crate::dec_id::{AstDecId, DecId};
use crate::interp::Value;
use crate::schema_capnp::RC;
use anyhow::{bail, Result};
use regex::Regex;
use std::fmt::{Display, Formatter};
use std::sync::OnceLock;
use hashbrown::HashMap;

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
	pub fn meth<'a>(&self, meth_hash: &blake3::Hash, program: &'a Program) -> &'a Meth {
		match self {
			MagicType::Nat(_) => nat::Impl.meth(meth_hash, program),
			MagicType::Magic => magic::Impl.meth(meth_hash, program),
			_ => panic!("No methods for {:?}", self),
		}
	}
	pub fn def(&self) -> blake3::Hash {
		match self {
			MagicType::String(_) => blake3::hash("base.String/0".as_bytes()),
			MagicType::Int(_) => blake3::hash("base.Int/0".as_bytes()),
			MagicType::Nat(_) => blake3::hash(nat::TYPE_NAME.as_bytes()),
			MagicType::Float(_) => blake3::hash("base.Float/0".as_bytes()),
			MagicType::Byte(_) => blake3::hash("base.Byte/0".as_bytes()),
			MagicType::Magic => blake3::hash(magic::TYPE_NAME.as_bytes()),
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
	
	if is_literal(name) { return true; }
	if dec_id.full_name() == "base.Magic" && dec_id.arity() == 0 { return true; }
	false
}

pub fn parse_type(dec_id: AstDecId) -> Result<MagicType> {
	let name = dec_id.full_name();
	if name == "base.Magic" && dec_id.arity() == 0 { return Ok(MagicType::Magic) }
	
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

trait MagicImpl {
	fn cell(&self) -> &'static OnceLock<HashMap<blake3::Hash, Meth>>;
	fn instance_name(&self) -> &str;
	fn meth<'a>(&self, meth_hash: &blake3::Hash, program: &'a Program) -> &'a Meth {
		let meths = self.cell().get_or_init(|| {
			let Some(def) = program.lookup_type_by_hash(&blake3::hash(self.instance_name().as_bytes())) else {
				panic!("No {}", self.instance_name())
			};
			let obj = match def.singleton_instance {
				Some(ref obj) => obj,
				None => panic!("No singleton instance for {}", def.name),
			};
			self.add_overrides(self.instance_name(), obj.meths.clone())
		});
		let Some(meth) = meths.get(meth_hash) else { panic!("No method for {} in base.Nat/0", meth_hash); };
		meth
	}
	fn add_overrides(&self, type_name: &str, meths: HashMap<blake3::Hash, Meth>) -> HashMap<blake3::Hash, Meth>;
}

impl From<bool> for Value {
	fn from(b: bool) -> Self {
		thread_local! {
			static TRUE: Value = Value::summoned_obj(&SummonObj {
				rc: RC::Imm,
				def: blake3::hash(b"base.True/0"),
			});
			static FALSE: Value = Value::summoned_obj(&SummonObj {
				rc: RC::Imm,
				def: blake3::hash(b"base.False/0"),
			});
		}

		if b {
			TRUE.with(|v| v.clone())
		} else {
			FALSE.with(|v| v.clone())
		}
	}
}
