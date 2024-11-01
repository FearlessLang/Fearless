use crate::schema_capnp::RC;

pub fn format_rc(rc: RC) -> &'static str {
	match rc {
		RC::Iso => "iso",
		RC::Imm => "imm",
		RC::Mut => "mut",
		RC::MutH => "mutH",
		RC::Read => "read",
		RC::ReadH => "readH",
		RC::ReadImm => "read/imm",
		RC::Generic => ""
	}
}
