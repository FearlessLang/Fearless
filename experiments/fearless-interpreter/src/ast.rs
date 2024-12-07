use crate::dec_id::{AstDecId, DecId, ExplicitDecId};
use crate::interp::Value;
use crate::pretty_print::PrettyPrint;
use crate::rc::format_rc;
use crate::schema_capnp::e::WhichReader;
use crate::schema_capnp::RC;
use crate::{interp, magic, schema_capnp};
use anyhow::{anyhow, bail, Result};
use capnp::message::{ReaderOptions, TypedReader};
use hashbrown::{HashMap, HashSet};
use itertools::{zip_eq, Itertools};
use std::borrow::Cow;
use std::fmt::{Display, Formatter};

pub(crate) const THIS_X: u32 = 0;

#[derive(Debug)]
struct ParseCtx<'mir> {
	bindings: HashMap<&'mir [u8], u32>,
	singletons: HashSet<blake3::Hash>,
	next_binding: u32,
}
impl<'mir> ParseCtx<'mir> {
	fn new() -> Self {
		let mut ctx = Self {
			bindings: Default::default(),
			singletons: Default::default(),
			next_binding: 0,
		};
		let this_x = ctx.add_x(b"this").unwrap();
		assert_eq!(this_x, THIS_X);
		ctx
	}
	fn get_x(&self, reader: &'mir [u8]) -> Result<u32> {
		self.bindings.get(reader)
			.copied()
			.ok_or_else(|| anyhow!("Binding not found: {}", std::str::from_utf8(reader).unwrap_or("?")))
	}
	fn add_x(&mut self, reader: &'mir [u8]) -> Result<u32> {
		let entry = self.bindings.entry(reader);
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

#[derive(Debug, Clone, PartialEq)]
pub struct Program {
	pub defs: HashMap<blake3::Hash, TypeDef>,
	pub funs: HashMap<blake3::Hash, Fun>,
	pub effectively_final_defs: HashSet<blake3::Hash>,
}
impl Program {
	pub fn new() -> Self {
		Self {
			defs: Default::default(),
			funs: Default::default(),
			effectively_final_defs: Default::default(),
		}
	}
	pub fn add_pkg(&mut self, raw: &[u8]) -> Result<()> {
		let msg_reader = capnp::serialize::read_message_from_flat_slice(
			&mut &*raw,
			ReaderOptions::new()
		)?;
		let reader = TypedReader::<_, schema_capnp::package::Owned>::new(msg_reader);
		let reader = reader.get()?;
		let mut ctx = ParseCtx::new();
		for def in reader.get_defs()? {
			let def = TypeDef::parse(&mut ctx, def)?;
			if def.singleton_instance.is_some() {
				ctx.singletons.insert(def.name.unique_hash());
			}
			self.defs.insert(def.name.unique_hash(), def);
		}
		for fun in reader.get_funs()? {
			let fun = Fun::parse(&mut ctx, fun)?;
			self.funs.insert(fun.name.unique_hash, fun);
		}
		// println!("Bindings for package: {}", reader.get_name()?.to_str()?);
		// for (name, binding) in ctx.bindings.iter() {
		// 	println!("{} = {}", std::str::from_utf8(name).unwrap_or("?"), binding);
		// }
		Ok(())
	}
	pub fn pkg_names(&self) -> impl Iterator<Item = &str> {
		self.defs.values()
			.map(|def| def.name.package())
			.unique()
	}
	pub fn lookup_type<Id: DecId>(&self, id: &Id) -> Option<&TypeDef> {
		let key = id.unique_hash();
		self.lookup_type_by_hash(&key)
	}
	pub fn lookup_type_by_hash(&self, hash: &blake3::Hash) -> Option<&TypeDef> {
		self.defs.get(hash)
	}
	pub fn lookup_fun_by_hash(&self, hash: &blake3::Hash) -> Option<&Fun> {
		self.funs.get(hash)
	}
	pub fn compute_effectively_final_defs(&mut self) {
		let mut all = self.defs.keys().copied().collect::<HashSet<_>>();
		self.defs.values()
			.flat_map(|def| def.impls.iter())
			.for_each(|super_type| {
				all.remove(super_type);
			});
		self.effectively_final_defs = all;
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct TypeDef {
	pub(crate) name: ExplicitDecId<'static>,
	pub(crate) impls: HashSet<blake3::Hash>,
	pub(crate) sigs: HashMap<blake3::Hash, Sig>,
	pub(crate) singleton_instance: Option<CreateObj>,
}
impl TypeDef {
	pub fn has_singleton(&self) -> bool {
		self.singleton_instance.is_some()
	}
	fn parse<'a>(ctx: &mut ParseCtx<'a>, reader: schema_capnp::type_def::Reader<'a>) -> Result<Self> {
		let impls = reader.get_impls()?.iter()
			.map(|reader| AstDecId(reader).unique_hash())
			.collect();
		let sigs = reader.get_sigs()?.iter()
			.map(|reader| {
				let sig = Sig::parse(ctx, reader)?;
				Ok((sig.name.hash, sig))
			})
			.collect::<Result<_>>()?;
		let name = AstDecId(reader.get_name()?);
		let singleton_instance = match reader.get_singleton_instance().which()? {
			schema_capnp::type_def::singleton_instance::Instance(e) => match e?.which()? {
				WhichReader::CreateObj(create_obj) => {
					let ty = Type { rc: RC::Mut, rt: RawType::Plain(name.unique_hash()) };
					Some(CreateObj::parse(ctx, create_obj?, ty)?)
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
impl HasType for TypeDef {
	fn t(&self) -> Type {
		Type {
			rc: RC::Mut,
			rt: RawType::Plain(self.name.unique_hash())
		}
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct Fun {
	pub(crate) name: FunName,
	pub(crate) args: Vec<TypePair>,
	pub(crate) ret: Type,
	pub(crate) body: E,
}
impl Fun {
	fn parse<'a>(ctx: &mut ParseCtx<'a>, reader: schema_capnp::fun::Reader<'a>) -> Result<Self> {
		let name = FunName::parse(reader.get_name()?)?;
		let args = reader.get_args()?.iter()
			.map(|reader| TypePair::parse_binding(ctx, reader))
			.collect::<Result<_>>()?;
		let ret = Type::parse(reader.get_ret()?)?;
		let body = E::parse(ctx, reader.get_body()?)?;
		Ok(Self {
			name,
			args,
			ret,
			body,
		})
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct FunName {
	pub(crate) d: blake3::Hash,
	pub(crate) rc: RC,
	pub(crate) m: blake3::Hash,
	pub(crate) captures_self: bool,
	pub(crate) unique_hash: blake3::Hash,
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

#[derive(Debug, Clone, PartialEq)]
pub struct MethName<'a> {
	pub(crate) rc: RC,
	pub(crate) name: Cow<'a, str>,
	pub(crate) arity: u32,
	pub(crate) hash: blake3::Hash,
}
impl<'a> MethName<'a> {
	pub fn new(rc: RC, name: &'a str, arity: u32) -> Self {
		Self {
			rc,
			name: name.into(),
			arity,
			hash: blake3::hash(format!("{} {}/{}", format_rc(rc), name, arity).as_bytes()),
		}
	}
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
impl Display for MethName<'_> {
	fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
		write!(f, "{}/{}", self.name, self.arity)?;
		Ok(())
	}
}

pub trait HasType {
	fn t(&self) -> Type;
}
#[derive(Debug, Clone, PartialEq)]
pub struct Type {
	pub rc: RC,
	pub rt: RawType
}
impl Type {
	pub(crate) fn parse(reader: schema_capnp::t::Reader) -> Result<Self> {
		Ok(Self {
			rc: reader.get_rc()?,
			rt: match reader.which()? {
				schema_capnp::t::Plain(plain) => {
					let dec_id = AstDecId(plain?);
					if magic::is_magic_type(dec_id) {
						Self::parse_magic_type(dec_id)?
					} else {
						RawType::Plain(dec_id.unique_hash())
					}
				},
				schema_capnp::t::Any(_) => RawType::Any,
			}
		})
	}
	fn parse_magic_type(dec_id: AstDecId) -> Result<RawType> {
		Ok(RawType::Magic(magic::parse_type(dec_id)?))
	}
}
#[derive(Debug, Clone, PartialEq)]
pub enum RawType {
	Plain(blake3::Hash),
	Any,
	Magic(magic::MagicType),
}
impl HasType for Type {
	fn t(&self) -> Type {
		self.clone()
	}
}
impl PrettyPrint for Type {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		match &self.rt {
			RawType::Plain(hash) => {
				let def = program.lookup_type_by_hash(hash).unwrap();
				write!(f, "{}", def.name)
			},
			RawType::Magic(magic) => write!(f, "{}", magic),
			RawType::Any => f.write_str("Any"),
		}
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct TypePair {
	pub x: u32,
	pub t: Type
}
impl TypePair {
	fn parse_binding<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::type_pair::Reader<'ctx>) -> Result<Self> {
		Ok(Self {
			x: ctx.add_x(reader.get_name()?.as_bytes())?,
			t: Type::parse(reader.get_t()?)?,
		})
	}
	fn parse_ref<'ctx>(ctx: &ParseCtx<'ctx>, reader: schema_capnp::type_pair::Reader<'ctx>) -> Result<Self> {
		Ok(Self {
			x: ctx.get_x(reader.get_name()?.as_bytes())?,
			t: Type::parse(reader.get_t()?)?,
		})
	}
}
impl PrettyPrint for TypePair {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		write!(f, "x{}:", self.x)?;
		self.t.pretty_print(f, program)
	}
}
impl HasType for TypePair {
	fn t(&self) -> Type {
		self.t.clone()
	}
}

#[derive(Debug, Clone, PartialEq)]
pub enum E {
	X(TypePair),
	MCall(MCall),
	CreateObj(CreateObj),
	SummonObj(SummonObj),
	MagicValue(RC, magic::MagicType),
	Computed(Value),
}
impl E {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::Reader<'ctx>) -> Result<Self> {
		let t = Type::parse(reader.get_t()?)?;
		Ok(match reader.which()? {
			schema_capnp::e::X(x) => E::X(TypePair {
				x: ctx.get_x(x?.get_name()?.as_bytes())?,
				t,
			}),
			schema_capnp::e::MCall(c) => E::MCall(MCall::parse(ctx, c?, t)?),
			schema_capnp::e::CreateObj(k) => {
				match t.rt {
					RawType::Plain(ty) => {
						if ctx.singletons.contains(&ty) {
							E::SummonObj(SummonObj { rc: t.rc, def: ty })
						} else {
							E::CreateObj(CreateObj::parse(ctx, k?, t)?)
						}
					},
					RawType::Magic(ty) => E::MagicValue(t.rc, ty),
					_ => bail!("Expected a plain type for CreateObj: {:?}", t),
				}
			},
		})
	}
	pub fn get_static_call(&self) -> Option<blake3::Hash> {
		match self {
			E::MCall(call) =>
				if let CallTarget::Fun(fun) = call.meth { Some(fun) } else { None },
			_ => None,
		}
	}
}
impl HasType for E {
	fn t(&self) -> Type {
		match self {
			E::X(x) => x.t(),
			E::MCall(c) => c.t(),
			E::CreateObj(k) => k.t(),
			E::SummonObj(k) => k.t(),
			E::MagicValue(rc, ty) => Type { rc: *rc, rt: RawType::Magic(ty.clone()) },
			E::Computed(_) => unimplemented!("Computed value types should gotten in the interpreter"),
		}
	}
}
impl PrettyPrint for E {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		match self {
			E::X(x) => x.pretty_print(f, program),
			E::MCall(call) => call.pretty_print(f, program),
			E::CreateObj(k) => {
				f.write_str("CreateObj(")?;
				k.t().pretty_print(f, program)?;
				f.write_str(")")
			},
			E::SummonObj(k) => {
				f.write_str("CreateObj(")?;
				k.t().pretty_print(f, program)?;
				f.write_str(")")
			},
			E::MagicValue(_, ty) => ty.pretty_print(f, program),
			E::Computed(v) => v.pretty_print(f, program),
		}
	}
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct SummonObj {
	pub(crate) rc: RC,
	pub(crate) def: blake3::Hash,
}
impl HasType for SummonObj {
	fn t(&self) -> Type {
		Type {
			rc: self.rc,
			rt: RawType::Plain(self.def)
		}
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct MCall {
	pub recv: Box<E>,
	pub rc: RC,
	pub meth: CallTarget,
	pub args: Vec<E>,
	pub return_type: Type,
}
impl MCall {
	pub fn new(recv: E, rc: RC, meth: CallTarget, args: Vec<E>, return_type: Type) -> Self {
		Self { recv: Box::new(recv), rc, meth, args, return_type }
	}
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::m_call::Reader<'ctx>, return_type: Type) -> Result<Self> {
		let meth = MethName::parse_hash(reader.get_name()?)?;
		let recv = Box::new(E::parse(ctx, reader.get_recv()?)?);
		let args = reader.get_args()?.iter()
			.map(|reader| E::parse(ctx, reader))
			.collect::<Result<_>>()?;

		Ok(Self {
			recv,
			rc: reader.get_rc()?,
			meth: CallTarget::Meth(meth),
			args,
			return_type,
		})
	}
}
impl PrettyPrint for MCall {
	fn pretty_print(&self, f: &mut Formatter<'_>, program: &Program) -> std::fmt::Result {
		let recv = match self.recv.t().rt {
			RawType::Plain(hash) => program.lookup_type_by_hash(&hash),
			RawType::Magic(magic) => program.lookup_type_by_hash(&magic.def()),
			RawType::Any => unreachable!(),
		}.unwrap();
		match self.meth {
			CallTarget::Meth(meth) => {
				let meth = recv.sigs.get(&meth).unwrap();
				self.recv.pretty_print(f, program)?;
				write!(f, " {}(", meth.name)?;
				for (i, (arg, xt)) in zip_eq(self.args.iter(), meth.xs.iter()).enumerate() {
					arg.pretty_print(f, program)?;
					f.write_str(" as ")?;
					xt.t().pretty_print(f, program)?;

					if i < self.args.len() - 1 {
						f.write_str(", ")?;
					}
				}
				f.write_str("): ")?;
				self.return_type.pretty_print(f, program)?;
			}
			CallTarget::Fun(fun) => {
				let fun = program.funs.get(&fun).unwrap();
				write!(f, "fun[{:?}]", fun.name)?;
			}
		}
		Ok(())
	}
}
impl HasType for MCall {
	fn t(&self) -> Type {
		self.return_type.clone()
	}
}
#[derive(Debug, Copy, Clone, Eq, PartialEq)]
pub enum CallTarget {
	Meth(blake3::Hash),
	Fun(blake3::Hash),
}

#[derive(Debug, Clone, PartialEq)]
pub struct CreateObj {
	pub(crate) rc: RC,
	pub(crate) def: blake3::Hash,
	pub(crate) self_name: u32,
	pub(crate) meths: HashMap<blake3::Hash, Meth>,
	pub(crate) captures: Vec<TypePair>,
}
impl CreateObj {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::e::create_obj::Reader<'ctx>, ty: Type) -> Result<Self> {
		let rc = ty.rc;
		let def = match ty.rt {
			RawType::Plain(def) => def,
			_ => bail!("Expected a plain type for CreateObj: {:?}", ty),
		};
		let self_name = ctx.add_x(reader.get_self_name()?.as_bytes())?;
		let meths = reader.get_meths()?.iter()
			.map(|reader| {
				let meth = Meth::parse(ctx, reader)?;
				Ok((meth.sig.name.hash, meth))
			})
			.collect::<Result<_>>()?;
		let captures = reader.get_captures()?.iter()
			.map(|reader| TypePair::parse_ref(ctx, reader))
			.collect::<Result<Vec<_>>>()?;
		Ok(Self {
			rc,
			def,
			self_name,
			meths,
			captures,
		})
	}
}
impl HasType for CreateObj {
	fn t(&self) -> Type {
		Type {
			rc: self.rc,
			rt: RawType::Plain(self.def)
		}
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct Sig {
	pub(crate) name: MethName<'static>,
	pub(crate) xs: Vec<TypePair>,
	pub(crate) return_type: Type,
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
	fn t(&self) -> Type {
		self.return_type.clone()
	}
}

#[derive(Debug, Clone, PartialEq)]
pub struct Meth {
	pub(crate) origin: blake3::Hash,
	pub(crate) sig: Sig,
	pub(crate) captures_self: bool,
	pub(crate) captures: Vec<u32>,
	pub(crate) body: MethImpl,
}
impl Meth {
	fn parse<'ctx>(ctx: &mut ParseCtx<'ctx>, reader: schema_capnp::meth::Reader<'ctx>) -> Result<Self> {
		let captures_self = reader.get_captures_self();
		let captures = reader.get_captures()?.iter()
			.map(|reader| ctx.get_x(reader?.as_bytes()))
			.collect::<Result<_>>()?;
		Ok(Self {
			origin: AstDecId(reader.get_origin()?).unique_hash(),
			sig: Sig::parse(ctx, reader.get_sig()?)?,
			captures_self,
			captures,
			body: match reader.get_f_name().which()? {
				schema_capnp::meth::f_name::Instance(f_name) =>
					MethImpl::Fun(FunName::parse(f_name?)?.unique_hash),
				schema_capnp::meth::f_name::Empty(_) => MethImpl::Abstract,
			},
		})
	}
}
impl HasType for Meth {
	fn t(&self) -> Type {
		self.sig.t()
	}
}
#[derive(Debug, Clone, PartialEq)]
pub enum MethImpl {
	Fun(blake3::Hash),
	Magic(fn(Value, Vec<Value>) -> interp::Result<interp::InterpreterE>),
	Abstract
}
