use std::fmt::Display;
use crate::ast::Program;

pub trait PrettyPrint {
	fn pretty_print(&self, f: &mut std::fmt::Formatter<'_>, program: &Program) -> std::fmt::Result;
}
impl<T: Display> PrettyPrint for T {
	fn pretty_print(&self, f: &mut std::fmt::Formatter<'_>, _: &Program) -> std::fmt::Result {
		write!(f, "{}", self)
	}
}

#[repr(transparent)]
pub struct Format<F: Fn(&mut std::fmt::Formatter<'_>) -> std::fmt::Result>(pub F);
impl<F: Fn(&mut std::fmt::Formatter<'_>) -> std::fmt::Result> Display for Format<F> {
	fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
		self.0(f)
	}
}
