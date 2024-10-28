use crate::dec_id::{AstDecId, DecId, ExplicitDecId};
use crate::schema_capnp;
use crate::schema_capnp::e::WhichReader;
use crate::schema_capnp::RC;
use anyhow::{anyhow, bail, Result};
use capnp::message::{ReaderOptions, TypedReader};
use hashbrown::{HashMap, HashSet};
use itertools::Itertools;
use std::borrow::Cow;
use std::hash::{Hash, Hasher};

#[derive(Debug, Clone)]
struct ParseCtx<'mir> {
	bindings: HashMap<&'mir [u8], u32>,
	next_binding: u32,
}
impl<'mir> ParseCtx<'mir> {
	fn new() -> Self {
		Self {
			bindings: Default::default(),
			next_binding: 0,
		}
	}
	fn get_x(&self, reader: capnp::text::Reader<'mir>) -> Result<u32> {
		self.bindings.get(reader.as_bytes())
			.copied()
			.ok_or_else(|| anyhow!("Binding not found: {:?}", reader))
	}
	fn add_x(&mut self, reader: capnp::text::Reader<'mir>) -> Result<u32> {
		let entry = self.bindings.entry(reader.as_bytes());
		Ok(*entry.or_insert_with(|| {
			let x = self.next_binding;
			self.next_binding = self.next_binding
				.checked_add(1)
				.expect("Binding limit exceeded (u32)");
			x
		}))
		// TODO: we seem to get issues with this
		// match entry {
		// 	hashbrown::hash_map::Entry::Occupied(n) =>
		// 		bail!("Duplicate binding: {:?} = {}", reader, n.get()),
		// 	hashbrown::hash_map::Entry::Vacant(_) => {
		// 		let x = self.next_binding;
		// 		self.next_binding = self.next_binding
		// 			.checked_add(1)
		// 			.expect("Binding limit exceeded (u32)");
		// 		entry.insert(x);
		// 		Ok(x)
		// 	}
		// }
	}
}

#[derive(Debug, Clone)]
pub struct Program {
	defs: HashMap<blake3::Hash, TypeDef>,
	funs: HashMap<blake3::Hash, Fun>,
}
impl Program {
	pub fn new() -> Self {
		Self {
			defs: Default::default(),
			funs: Default::default(),
		}
	}
	pub fn add_pkg(&mut self, raw: &[u8]) -> Result<()> {
		let msg_reader = capnp::serialize::read_message_from_flat_slice(
			&mut &*raw,
			ReaderOptions::new()
		)?;
		let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
		let reader = reader.get()?;
		for def in reader.get_defs()? {
			let def = TypeDef::parse(def)?;
			self.defs.insert(def.name.unique_hash(), def);
		}
		for fun in reader.get_funs()? {
			let fun = Fun::parse(fun)?;
			self.funs.insert(fun.name.unique_hash, fun);
		}
		Ok(())
	}
	pub fn pkg_names(&self) -> impl Iterator<Item = &str> {
		self.defs.values()
			.map(|def| def.name.package())
			.unique()
	}
	pub fn lookup<Id: DecId>(&self, id: &Id) -> Option<&TypeDef> {
		let key = id.unique_hash();
		self.defs.get(&key)
	}
}

#[derive(Debug, Clone)]
pub struct TypeDef {
	name: ExplicitDecId<'static>,
	impls: HashSet<blake3::Hash>,
	sigs: HashMap<blake3::Hash, Sig>,
	singleton_instance: Option<CreateObj>,
}
impl TypeDef {
	fn parse(reader: schema_capnp::type_def::Reader) -> Result<Self> {
		let impls = reader.get_impls()?.iter()
			.map(|reader| AstDecId(reader).unique_hash())
			.collect();
		let mut ctx = ParseCtx::new();
		let sigs = reader.get_sigs()?.iter()
			.map(|reader| {
				let sig = Sig::parse(&mut ctx, reader)?;
				Ok((sig.name.hash, sig))
			})
			.collect::<Result<_>>()?;
		let name = AstDecId(reader.get_name()?);
		let singleton_instance = match reader.get_singleton_instance().which()? {
			schema_capnp::type_def::singleton_instance::Instance(e) => match e?.which()? {
				WhichReader::CreateObj(create_obj) => {
					let ty = Type { rc: RC::Mut, rt: RawType::Plain(name.unique_hash()) };
					Some(CreateObj::parse(&mut ctx, create_obj?, ty)?)
				},
				_ => bail!("Expected a CreateObj expression for singletonInstance on {:?}", name),
			},
			schema_capnp::type_def::singleton_instance::Empty(_) => None,
		};
		Ok(Self {
			name: name.into(),
			impls,
			sigs,
			singleton_instance,
		})
	}
}

#[derive(Debug, Clone)]
pub struct Fun {
	name: FunName,
	args: Vec<TypePair>,
	ret: Type,
	body: E,
}
impl Fun {
	fn parse(reader: schema_capnp::fun::Reader) -> Result<Self> {
		let name = FunName::parse(reader.get_name()?)?;
		let mut ctx = ParseCtx::new();
		let args = reader.get_args()?.iter()
			.map(|reader| TypePair::parse_binding(&mut ctx, reader))
			.collect::<Result<_>>()?;
		let ret = Type::parse(reader.get_ret()?)?;
		let body = E::parse(&mut ctx, reader.get_body()?)?;
		Ok(Self {
			name,
			args,
			ret,
			body,
		})
	}
}

#[derive(Debug, Clone)]
pub struct FunName {
	d: blake3::Hash,
	rc: RC,
	m: blake3::Hash,
	captures_self: bool,
	unique_hash: blake3::Hash,
}
impl FunName {
	fn parse(reader: schema_capnp::fun::name::Reader) -> Result<FunName> {
		Ok(FunName {
			d: AstDecId(reader.get_d()?).unique_hash(),
			rc: reader.get_rc()?,
			m: MethName::parse_hash(reader.get_m()?)?,
			captures_self: reader.get_captures_self(),
			unique_hash: Self::parse_hash(reader)?,
		})
	}
	fn parse_hash(reader: schema_capnp::fun::name::Reader) -> Result<blake3::Hash> {
		let slice = reader.get_hash()?;
		let arr: [u8; 32] = slice.try_into().map_err(|_| anyhow::anyhow!("Cannot read hash on MethName: {:?}", slice))?;
		Ok(blake3::Hash::from(arr))
	}
}

#[derive(Debug, Clone)]
pub struct MethName<'a> {
	rc: RC,
	name: Cow<'a, str>,
	arity: u32,
	hash: blake3::Hash,
}
impl<'a> MethName<'a> {
	fn parse(reader: schema_capnp::meth_name::Reader<'a>) -> Result<MethName<'a>> {
		Ok(MethName {
			rc: reader.get_rc()?,
			name: reader.get_name()?.to_str()?.into(),
			arity: reader.get_arity(),
			hash: Self::parse_hash(reader)?,
		})
	}
	fn parse_owned(reader: schema_capnp::meth_name::Reader<'a>) -> Result<MethName<'static>> {
		Ok(MethName {
			rc: reader.get_rc()?,
			name: reader.get_name()?.to_string()?.into(),
			arity: reader.get_arity(),
			hash: Self::parse_hash(reader)?,
		})
	}
	fn parse_hash(reader: schema_capnp::meth_name::Reader<'a>) -> Result<blake3::Hash> {
		let slice = reader.get_hash()?;
		let arr: [u8; 32] = slice.try_into().map_err(|_| anyhow::anyhow!("Cannot read hash on MethName: {:?}", slice))?;
		Ok(blake3::Hash::from(arr))
	}
}

pub trait HasType {
	fn t(&self) -> &Type;
}
#[derive(Debug, Clone, Eq, PartialEq)]
pub struct Type {
	pub rc: RC,
	pub rt: RawType
}
impl Type {
	fn parse(reader: schema_capnp::t::Reader) -> Result<Self> {
		Ok(Self {
			rc: reader.get_rc()?,
			rt: match reader.which()? {
				schema_capnp::t::Plain(plain) => RawType::Plain(AstDecId(plain?).unique_hash()),
				schema_capnp::t::Any(_) => RawType::Any,
			}
		})
	}
}
impl Hash for Type {
	fn hash<H: Hasher>(&self, state: &mut H) {
		debug_assert_eq!(size_of_val(&self.rc), size_of::<u16>());
		state.write_u16(self.rc as u16);
		self.rt.hash(state);
	}
}
#[derive(Debug, Clone, Hash, Eq, PartialEq)]
pub enum RawType {
	Plain(blake3::Hash),
	Any,
}
impl HasType for Type {
	fn t(&self) -> &Type {
		self
	}
}

#[derive(Debug, Clone, Hash, Eq, PartialEq)]
pub struct TypePair {
	pub x: u32,
	pub t: Type
}
impl TypePair {
	fn parse_binding<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::type_pair::Reader<'ctx>) -> Result<Self> {
		Ok(Self {
			x: ctx.add_x(reader.get_name()?)?,
			t: Type::parse(reader.get_t()?)?,
		})
	}
	fn parse_ref<'ctx>(ctx: &ParseCtx<'ctx>, reader: schema_capnp::type_pair::Reader<'ctx>) -> Result<Self> {
		Ok(Self {
			x: ctx.get_x(reader.get_name()?)?,
			t: Type::parse(reader.get_t()?)?,
		})
	}
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
impl E {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::Reader<'ctx>) -> Result<Self> {
		let t = Type::parse(reader.get_t()?)?;
		Ok(match reader.which()? {
			schema_capnp::e::X(x) => E::X(TypePair {
				x: ctx.get_x(x?.get_name()?)?,
				t,
			}),
			schema_capnp::e::MCall(c) => E::MCall(MCall::parse(ctx, c?, t)?),
			schema_capnp::e::CreateObj(k) => E::CreateObj(CreateObj::parse(ctx, k?, t)?),
		})
	}
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
}
impl MCall {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::m_call::Reader<'ctx>, return_type: Type) -> Result<Self> {
		let name = MethName::parse_owned(reader.get_name()?)?;
		let args = reader.get_args()?.iter()
			.map(|reader| E::parse(ctx, reader))
			.collect::<Result<_>>()?;
		Ok(Self {
			rc: reader.get_rc()?,
			name,
			args,
			return_type,
		})
	}
}
impl HasType for MCall {
	fn t(&self) -> &Type {
		&self.return_type
	}
}

#[derive(Debug, Clone)]
pub struct CreateObj {
	ty: Type,
	self_name: u32,
	meths: HashMap<blake3::Hash, Meth>,
	captures: HashSet<TypePair>,
}
impl CreateObj {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::create_obj::Reader<'ctx>, ty: Type) -> Result<Self> {
		let self_name = ctx.add_x(reader.get_self_name()?)?;
		let captures = reader.get_captures()?.iter()
			.map(|reader| TypePair::parse_ref(ctx, reader))
			.collect::<Result<HashSet<_>>>()?;
		let meths = reader.get_meths()?.iter()
			.map(|reader| {
				let meth = Meth::parse(&mut ctx.clone(), reader)?;
				Ok((meth.sig.name.hash, meth))
			})
			.collect::<Result<_>>()?;
		Ok(Self {
			ty,
			self_name,
			meths,
			captures,
		})
	}
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
impl Sig {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::sig::Reader<'ctx>) -> Result<Self> {
		let xs = reader.get_xs()?.iter()
			.map(|reader| TypePair::parse_binding(ctx, reader))
			.collect::<Result<Vec<TypePair>>>()?;
		Ok(Sig {
			name: MethName::parse_owned(reader.get_name()?)?,
			xs,
			return_type: Type::parse(reader.get_rt()?)?,
		})
	}
}
impl HasType for Sig {
	fn t(&self) -> &Type {
		&self.return_type
	}
}

#[derive(Debug, Clone)]
pub struct Meth {
	origin: blake3::Hash,
	sig: Sig,
	captures_self: bool,
	captures: HashSet<u32>,
	fun_name: Option<blake3::Hash>,
}
impl Meth {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::meth::Reader<'ctx>) -> Result<Self> {
		let captures = reader.get_captures()?.iter()
			.map(|reader| ctx.get_x(reader?))
			.collect::<Result<_>>()?;
		Ok(Self {
			origin: AstDecId(reader.get_origin()?).unique_hash(),
			sig: Sig::parse(&mut ctx.clone(), reader.get_sig()?)?,
			captures_self: reader.get_captures_self(),
			captures,
			fun_name: match reader.get_f_name().which()? {
				schema_capnp::meth::f_name::Instance(f_name) =>
					Some(FunName::parse(f_name?)?.unique_hash),
				schema_capnp::meth::f_name::Empty(_) => None,
			},
		})
	}
}
impl HasType for Meth {
	fn t(&self) -> &Type {
		self.sig.t()
	}
}
