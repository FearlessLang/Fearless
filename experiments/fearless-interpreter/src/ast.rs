use crate::schema_capnp;
use crate::schema_capnp::RC;
use crate::state::{AstDecId, ExplicitDecId};
use anyhow::Result;
use capnp::message::{ReaderOptions, TypedReader};
use hashbrown::{HashMap, HashSet};
use std::borrow::Cow;

#[derive(Debug, Clone)]
pub struct Program {
	pkgs: HashMap<Cow<'static, str>, Package>
}
impl Program {
	pub fn new() -> Self {
		Self {
			pkgs: HashMap::new()
		}
	}
	pub fn add_pkg(&mut self, raw: &[u8]) -> Result<()> {
		let pkg = Package::read_raw(raw)?;
		self.pkgs.insert(pkg.name.clone(), pkg);
		Ok(())
	}
	pub fn pkg_names(&self) -> impl Iterator<Item = &Cow<'static, str>> {
		self.pkgs.keys()
	}
}

#[derive(Debug, Clone)]
struct Package {
	name: Cow<'static, str>,
	defs: HashMap<ExplicitDecId<'static>, TypeDef>,
	funs: HashMap<FunName<'static>, TypeDef>,
}
impl Package {
	fn read_raw(raw: &[u8]) -> Result<Self> {
		let msg_reader = capnp::serialize::read_message_from_flat_slice(
			&mut &*raw,
			ReaderOptions::new()
		)?;
		let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
		let reader = reader.get()?;
		let name = reader.get_name()?.to_string()?.into();
		let defs = reader.get_defs()?.iter()
			.map(|reader| {
				let def = TypeDef::parse(reader)?;
				let name = def.name.clone();
				Ok((name, def))
			})
			.collect::<Result<_>>()?;
		println!("{:?}", defs);
		Ok(Self {
			name,
			defs,
			funs: HashMap::new(),
		})
	}
}

#[derive(Debug, Clone)]
struct TypeDef {
	name: ExplicitDecId<'static>,
	impls: HashSet<ExplicitDecId<'static>>,
	sigs: HashMap<MethName<'static>, Sig>,
	singleton_instance: Option<CreateObj>,
}
impl TypeDef {
	fn parse(reader: schema_capnp::type_def::Reader) -> Result<Self> {
		let impls = reader.get_impls()?.iter()
			.map(|reader| AstDecId(reader).into())
			.collect();
		Ok(Self {
			name: AstDecId(reader.get_name()?).into(),
			impls,
			sigs: HashMap::new(),
			singleton_instance: None,
		})
	}
}

#[derive(Debug, Clone)]
pub struct Fun {
	name: FunName<'static>,
	args: Vec<TypePair>,
	ret: Type,
	body: E
}

#[derive(Debug, Clone)]
pub struct FunName<'a> {
	d: ExplicitDecId<'a>,
	rc: RC,
	m: MethName<'a>,
	captures_self: bool,
}

#[derive(Debug, Clone)]
pub struct MethName<'a> {
	rc: RC,
	name: Cow<'a, str>,
	arity: u32,
}

pub trait HasType {
	fn t(&self) -> &Type;
}
#[derive(Debug, Clone)]
pub struct Type {
	pub rc: RC,
	pub rt: RawType
}
#[derive(Debug, Clone)]
pub enum RawType {
	Plain(ExplicitDecId<'static>),
	Any,
}
impl HasType for Type {
	fn t(&self) -> &Type {
		self
	}
}

#[derive(Debug, Clone)]
pub struct TypePair {
	pub x: Cow<'static, str>,
	pub t: Type
}
impl HasType for TypePair {
	fn t(&self) -> &Type {
		&self.t
	}
}

#[derive(Debug, Clone)]
pub enum E {
	X(TypePair),
	MCall(MCall),
	CreateObj(CreateObj),
}
impl HasType for E {
	fn t(&self) -> &Type {
		match self {
			E::X(x) => x.t(),
			E::MCall(c) => c.t(),
			E::CreateObj(k) => k.t(),
		}
	}
}

#[derive(Debug, Clone)]
pub struct MCall {
	rc: RC,
	name: MethName<'static>,
	args: Vec<E>,
	return_type: Type,
	variant: schema_capnp::e::m_call::Variant,
}
impl HasType for MCall {
	fn t(&self) -> &Type {
		&self.return_type
	}
}

#[derive(Debug, Clone)]
pub struct CreateObj {
	ty: Type,
	self_name: Cow<'static, String>,
	meths: HashMap<MethName<'static>, Meth>,
	captures: HashSet<TypePair>,
}
impl HasType for CreateObj {
	fn t(&self) -> &Type {
		&self.ty
	}
}

#[derive(Debug, Clone)]
pub struct Sig {
	name: MethName<'static>,
	xs: Vec<TypePair>,
	return_type: Type,
}
impl HasType for Sig {
	fn t(&self) -> &Type {
		&self.return_type
	}
}

#[derive(Debug, Clone)]
pub struct Meth {
	origin: ExplicitDecId<'static>,
	sig: Sig,
	captures_self: bool,
	captures: HashSet<Cow<'static, str>>,
	fun_name: Option<FunName<'static>>,
}
impl HasType for Meth {
	fn t(&self) -> &Type {
		self.sig.t()
	}
}
