use crate::ast::{CreateObj, MCall, SummonObj, TypePair, E};
use crate::magic::MagicType;
use crate::schema_capnp::RC;

pub(super) trait ExprVisitor {
	fn x(&mut self, x: &TypePair) {}
	fn m_call(&mut self, mcall: &MCall) {}
	fn create_obj(&mut self, k: &CreateObj) {}
	fn summon_obj(&mut self, k: &SummonObj) {}
	fn magic_value(&mut self, rc: RC, ty: &MagicType) {}
}
pub(super) fn walk_expr<V: ExprVisitor>(visitor: &mut V, e: &E) {
	match e {
		E::X(x) => visitor.x(x),
		E::MCall(mcall) => {
			visitor.m_call(mcall);
			walk_expr(visitor, &mcall.recv);
			for arg in mcall.args.iter() {
				walk_expr(visitor, arg);
			}
		},
		E::CreateObj(k) => visitor.create_obj(k),
		E::SummonObj(k) => visitor.summon_obj(k),
		E::MagicValue(rc, ty) => visitor.magic_value(*rc, ty),
	}
}

pub(super) trait ExprVisitorMut {
	fn x(&mut self, x: &mut TypePair) {}
	fn m_call(&mut self, mcall: &mut MCall) {}
	fn create_obj(&mut self, k: &mut CreateObj) {}
	fn summon_obj(&mut self, k: &mut SummonObj) {}
	fn magic_value(&mut self, rc: RC, ty: &mut MagicType) {}
}
pub(super) fn walk_expr_mut<V: ExprVisitorMut>(visitor: &mut V, e: &mut E) {
	match e {
		E::X(x) => visitor.x(x),
		E::MCall(mcall) => {
			visitor.m_call(mcall);
			walk_expr_mut(visitor, &mut mcall.recv);
			for arg in mcall.args.iter_mut() {
				walk_expr_mut(visitor, arg);
			}
		},
		E::CreateObj(k) => visitor.create_obj(k),
		E::SummonObj(k) => visitor.summon_obj(k),
		E::MagicValue(rc, ty) => visitor.magic_value(*rc, ty),
	}
}
