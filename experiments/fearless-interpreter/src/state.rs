use std::hash::{Hash, Hasher};
use std::ops::Deref;
use crate::schema_capnp;

#[derive(Copy, Clone, Debug)]
#[repr(transparent)]
pub struct DecId<'mir>(pub schema_capnp::dec_id::Reader<'mir>);
impl PartialEq for DecId<'_> {
	fn eq(&self, other: &Self) -> bool {
		let name_eq = self.0.get_name().unwrap().0 == other.0.get_name().unwrap().0;
		if !name_eq { return false; }
		let gens_eq = self.0.get_gens() == other.0.get_gens();
		if !gens_eq { return false; }
		true
	}
}
impl Eq for DecId<'_> {}
impl Hash for DecId<'_> {
	fn hash<H: Hasher>(&self, state: &mut H) {
		if self.0.get_name().is_err() {
			panic!("DecId::hash: missing name");
		}
		self.0.get_name().unwrap().0.hash(state);
		self.0.get_gens().hash(state);
	}
}
impl<'mir> Deref for DecId<'mir> {
	type Target = schema_capnp::dec_id::Reader<'mir>;
	fn deref(&self) -> &Self::Target {
		&self.0
	}
}
