use hashbrown::HashMap;
use crate::ast::{Meth, MethImpl, MethName};

pub(crate) fn add_override(meth_name: MethName, body: MethImpl, origin: blake3::Hash, meths: &mut HashMap<blake3::Hash, Meth>) {
	meths.insert(meth_name.hash, get_override(meth_name, body, origin, meths));
}

pub(crate) fn get_override(meth_name: MethName, body: MethImpl, origin: blake3::Hash, meths: &HashMap<blake3::Hash, Meth>) -> Meth {
	let prev = meths.get(&meth_name.hash).unwrap();
	Meth {
		origin,
		captures: Default::default(),
		captures_self: false,
		body,
		sig: prev.sig.clone(),
	}
}