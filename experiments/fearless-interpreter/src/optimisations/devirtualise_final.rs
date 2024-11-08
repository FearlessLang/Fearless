use crate::ast::{CallTarget, HasType, MCall, MethImpl, Program, RawType, TypeDef, E};
use hashbrown::{HashMap, HashSet};

pub fn apply(program: &mut Program) {
	for fun in program.funs.values_mut() {
		visit_expr(&program.effectively_final_defs, &program.defs, &mut fun.body);
	}
}

fn visit_expr(
	final_types: &HashSet<blake3::Hash>,
	defs: &HashMap<blake3::Hash, TypeDef>,
	expr: &mut E
) {
	if let E::MCall(call) = expr { devirtualise_call(final_types, defs, call) }
}

fn devirtualise_call(
	final_types: &HashSet<blake3::Hash>,
	defs: &HashMap<blake3::Hash, TypeDef>,
	call: &mut MCall
) {
	match call.meth {
		CallTarget::Fun(_) => {},
		CallTarget::Meth(hash) => {
			let type_hash = match call.recv.t().rt {
				RawType::Plain(hash) => hash,
				RawType::Magic(mt) => mt.def(),
				RawType::Any => unreachable!("Method call on an unknown type"),
			};
			if final_types.contains(&type_hash) {
				let fun = &defs[&type_hash];
				if let Some(k) = &fun.singleton_instance {
					if let Some(meth) = k.meths.get(&hash) {
						if let MethImpl::Fun(fun_hash) = meth.body {
							debug_assert!(k.captures.is_empty());
							debug_assert!(meth.sig.xs.len() == call.args.len());
							call.meth = CallTarget::Fun(fun_hash);
						}
					}
				}
			}
		}
	}
	visit_expr(final_types, defs, &mut call.recv);
	call.args.iter_mut().for_each(|arg| visit_expr(final_types, defs, arg));
}

/*
or to include things with captures (will not work without some renaming or storing a capture map)
pub fn apply(program: &mut Program) {
	for fun in program.funs.values_mut() {
		visit_expr(&program.effectively_final_defs, &program.defs, &mut Default::default(), &mut fun.body);
	}
}

fn visit_expr(
	final_types: &HashSet<blake3::Hash>,
	defs: &HashMap<blake3::Hash, TypeDef>,
	meths_to_funs: &mut HashMap<blake3::Hash, HashMap<blake3::Hash, blake3::Hash>>,
	expr: &mut E
) {
	match expr {
		E::MCall(call) => devirtualise_call(final_types, defs, meths_to_funs, call),
		E::CreateObj(k) if final_types.contains(&k.def) => {
			unreachable!()
			let ms = meths_to_funs.entry(k.def).or_default();
			k.meths.values().for_each(|meth| {
				if let MethImpl::Fun(fun_hash) = meth.body {
					ms.insert(meth.sig.name.hash, fun_hash);
				}
			});
		},
		_ => {},
	}
}

fn devirtualise_call(
	final_types: &HashSet<blake3::Hash>,
	defs: &HashMap<blake3::Hash, TypeDef>,
	meths_to_funs: &mut HashMap<blake3::Hash, HashMap<blake3::Hash, blake3::Hash>>,
	call: &mut MCall
) {
	match call.meth {
		CallTarget::Fun(_) => {},
		CallTarget::Meth(hash) => {
			let type_hash = match call.recv.t().rt {
				RawType::Plain(hash) => hash,
				RawType::Magic(mt) => mt.def(),
				RawType::Any => unreachable!("Method call on an unknown type"),
			};
			if final_types.contains(&type_hash) {
				let fun = &defs[&type_hash];
				let fun_hash = match &fun.singleton_instance {
					None => meths_to_funs.get(&type_hash).and_then(|ms| ms.get(&hash).copied()),
					Some(k) => k.meths.get(&hash).and_then(|meth|
						if let MethImpl::Fun(fun_hash) = meth.body {
							Some(fun_hash)
						} else {
							None
						}
					),
				};
				if let Some(fun_hash) = fun_hash {
					call.meth = CallTarget::Fun(fun_hash);
				}
			}
		}
	}
	visit_expr(final_types, defs, meths_to_funs, &mut call.recv);
	call.args.iter_mut().for_each(|arg| visit_expr(final_types, defs, meths_to_funs, arg));
}
 */
