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
