@0xd450c7e171226aae;
using Java = import "/capnp/java.capnp";
$Java.package("org.fearlang.mearless.proto");
$Java.outerClassname("Mearless");

struct Program @0xf5604a35edc2f94a {
  pkgs @0 :List(Package);
}

struct Package @0xb19d1d751e5320c1 {
  name @0 :Text;
  defs @1 :List(TypeDef);
  funs @2 :List(Fun);
}

struct TypeDef @0x850214d014c54577 {
  name @0 :DecId;
  impls @1 :List(DecId);
  sigs @2 :List(Sig);
  singletonInstance :union {
    empty @3 :Void;
    instance @4 :E; # must be E.CreateObj
  }
}

struct Fun @0xda20b7fafa989ab8 {
  name @0 :Fun.Name;
  args @1 :List(TypePair);
  ret @2 :T;
  body @3 :E;

  struct Name @0xa447c9f0f5521b5e {
    d @0 :DecId;
    m @1 :MethName;
    capturesSelf @2 :Bool;
    rc @3 :RC;
    hash @4 :Data;
  }
}

struct Sig @0xfd71105e28399dc6 {
  name @0 :MethName;
  xs @1 :List(TypePair);
  rt @2 :T;
}

struct Meth @0xddb148bf8fb4ae9d {
  origin @0 :DecId;
  sig @1 :Sig;
  capturesSelf @2 :Bool;
  captures @3 :List(Text);
  fName :union {
    empty @4 :Void;
    instance @5 :Fun.Name;
  }
}

struct T @0xdbf7b6f1e0cf6cda {
  rc @0 :RC;
  union {
    plain @1 :DecId;
    any @2 :Void;
  }
}

struct DecId @0xcea8d4b5671efbed {
  name @0 :Text;
  gens @1 :UInt32;
  hash @2 :Data;
}

# struct Hash256 @0x95554ac1a8a8f090 {
# 	a @0 :UInt64;
# 	b @1 :UInt64;
# 	c @2 :UInt64;
# 	d @3 :UInt64;
# }

enum RC @0xd79bf80b86c31d5a {
  iso @0;
  imm @1;
  mut @2;
  mutH @3;
  read @4;
  readH @5;
}

struct MethName {
  rc @0 :RC;
  name @1 :Text;
  arity @2 :UInt32;
  hash @3 :Data;
}

struct E @0xd08f47af381da063 {
  t @0 :T;
  union {
    x @1 :X;
    mCall @2 :MCall;
    createObj @3 :CreateObj;
  }

  struct X @0x9d0bc4c5ce4dd1cf {
    name @0 :Text;
  }
  struct MCall @0xd71ae1db5ac9f57c {
    recv @0 :E;
    name @1 :MethName;
    args @2 :List(E);
    originalRet @3 :T;
    rc @4 :RC;
    variant @5 :List(E.MCall.Variant);

    enum Variant {
      standard @0;
      pipelineParallelFlow @1;
      dataParallelFlow @2;
      safeMutSourceFlow @3;
    }
  }
  struct CreateObj @0xfd0043dc0961c2db {
    selfName @0 :Text;
    meths @1 :List(Meth);
    # unreachableMs @2 :List(Meth);
    captures @2 :List(TypePair);
  }
}

struct TypePair {
  t @0 :T;
  name @1 :Text;
}

struct Map(Key, Value) {
  entries @0 :List(Entry);
  struct Entry @0xfe0ac8cd35afd5fa {
    key @0 :Key;
    value @1 :Value;
  }
}
