use std::hint::unreachable_unchecked;
use crate::schema_capnp;
use capnp::message::TypedReader;
use capnp::serialize::OwnedSegments;
use hashbrown::HashMap;
use crate::state::{AstDecId, ExplicitDecId};

pub struct Program {
	raw: HashMap<String, TypedReader<OwnedSegments, schema_capnp::package::Owned>>,
	literals: HashMap<ExplicitDecId, usize>,
	methods: HashMap<(ExplicitDecId, String), usize>,
}
impl Program {
	pub fn new(readers: HashMap<String, TypedReader<OwnedSegments, schema_capnp::package::Owned>>) -> Program {
		let (type_defs, method_def) = Program::add_types(&readers);
		println!("{:?}", type_defs);
		Program {
			raw: readers,
			literals: type_defs,
			methods: method_def,
		}
	}
	pub fn package_names(&self) -> impl Iterator<Item = &String> {
		self.raw.keys()
	}
	// pub fn get_expr<Recv: DecId>(&self, recv: Recv) -> schema_capnp::e::Reader {
	// 	let pkg = self.raw.get(recv.package()).unwrap();
	// }

	fn add_types(readers: &HashMap<String, TypedReader<OwnedSegments, schema_capnp::package::Owned>>) -> (HashMap<ExplicitDecId, usize>, HashMap<(ExplicitDecId, String), usize>) {
		let mut type_defs = HashMap::new();
		let mut method_defs = HashMap::new();
		for pkg_reader in readers.values() {
			let reader = pkg_reader.get().unwrap();
			let defs_reader = reader.get_defs().unwrap();
			defs_reader.iter().enumerate()
				.filter(|(_, def)| def.get_singleton_instance().has_instance())
				.for_each(|(i, def)| {
					let dec_id = AstDecId(def.get_name().unwrap());
					type_defs.insert(dec_id.into(), i);
					// match def.get_singleton_instance().which().unwrap() {
					// 	schema_capnp::type_def::singleton_instance::Which::Instance(lit_reader) => {
					// 		debug_assert!(lit_reader.unwrap().has_create_obj());
					// 		// match lit_reader.unwrap().which().unwrap() {
					// 		// 	schema_capnp::e::Which::CreateObj(create_obj_reader) => {
					// 		// 		create_obj_reader.unwrap().get_meths().unwrap().iter().enumerate()
					// 		// 			.for_each(|(j, meth_reader)| {
					// 		// 				let name = meth_reader.reborrow().unwrap().to_string();
					// 		// 				method_defs.insert((dec_id.into(), name), j);
					// 		// 			});
					// 		// 	},
					// 		// }
					// 	},
					// 	// Safety: checked in the filter earlier
					// 	_ => unsafe { unreachable_unchecked() },
					// };
				});
		}
		(type_defs, method_defs)
	}
}

// #[repr(transparent)]
// struct TypeTable<'mir> {
// 	types: HashMap<ExplicitDecId, usize>
// }
// impl<'mir> TypeTable<'mir> {
// 	fn new() -> Self {
// 		Self {
// 			types: HashMap::new(),
// 		}
// 	}
// 	fn add(&mut self, def: schema_capnp::type_def::Reader<'mir>) -> Result<()> {
// 		let key = state::DecId(def.get_name()?);
// 		if self.types.contains_key(&key) {
// 			bail!("Invalid MIR: duplicate type definition: {:?}", key);
// 		}
// 		self.types.insert(key, def);
// 		Ok(())
// 	}
// }
