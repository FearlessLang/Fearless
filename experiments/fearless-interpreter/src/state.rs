use std::borrow::Cow;
use std::hash::{Hash, Hasher};
use std::ops::Deref;
use crate::schema_capnp;

pub trait DecId {
	fn package(&self) -> &str;
	fn full_name(&self) -> &str;
	fn short_name(&self) -> &str;
	fn arity(&self) -> u32;
}

#[derive(Debug, Clone, PartialEq, Eq, Hash)]
pub struct ExplicitDecId<'a> {
	full_name: Cow<'a, str>,
	arity: u32,
}
impl<'mir> From<AstDecId<'mir>> for ExplicitDecId<'static> {
	fn from(id: AstDecId<'mir>) -> Self {
		Self {
			full_name: id.full_name().to_string().into(),
			arity: id.arity(),
		}
	}
}
impl DecId for ExplicitDecId<'_> {
	fn package(&self) -> &str {
		self.full_name.split_once('.').unwrap().0
	}
	fn full_name(&self) -> &str {
		&self.full_name
	}
	fn short_name(&self) -> &str {
		self.full_name.split_once('.').unwrap().1
	}
	fn arity(&self) -> u32 {
		self.arity
	}
}

#[derive(Copy, Clone, Debug)]
#[repr(transparent)]
pub struct AstDecId<'mir>(pub schema_capnp::dec_id::Reader<'mir>);
impl<'mir> DecId for AstDecId<'mir> {
	fn package(&self) -> &str {
		self.full_name().split_once('.').unwrap().0
	}
	fn full_name(&self) -> &str {
		std::str::from_utf8(self.0.get_name().unwrap().0).unwrap()
	}
	fn short_name(&self) -> &str {
		self.full_name().split_once('.').unwrap().1
	}
	fn arity(&self) -> u32 {
		self.0.get_gens()
	}
}

impl PartialEq for AstDecId<'_> {
	fn eq(&self, other: &Self) -> bool {
		let name_eq = self.0.get_name().unwrap().0 == other.0.get_name().unwrap().0;
		if !name_eq { return false; }
		let gens_eq = self.0.get_gens() == other.0.get_gens();
		if !gens_eq { return false; }
		true
	}
}
impl Eq for AstDecId<'_> {}
impl Hash for AstDecId<'_> {
	fn hash<H: Hasher>(&self, state: &mut H) {
		if self.0.get_name().is_err() {
			panic!("DecId::hash: missing name");
		}
		self.0.get_name().unwrap().0.hash(state);
		self.0.get_gens().hash(state);
	}
}
impl<'mir> Deref for AstDecId<'mir> {
	type Target = schema_capnp::dec_id::Reader<'mir>;
	fn deref(&self) -> &Self::Target {
		&self.0
	}
}
