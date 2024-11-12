package codegen.java;

import java.util.Map;
import java.util.Optional;

import codegen.MIR;
import id.Id;
import magic.Magic;

/// @deprecated Part of the old Java codegen system. See [JavaTarget] for the new one.
record TypeIds(JavaMagicImpls magic, StringIds id){
  public String getTName(MIR.MT t, boolean isRet) {
    var res= switch (t) {
      case MIR.MT.Any ignored -> "Object";
      case MIR.MT.Plain plain -> auxGetTName(plain.id());
      case MIR.MT.Usual usual -> auxGetTName(usual.it().name());
    };
    return isRet ? boxOf(res) : res;
  }
  public String auxGetTName(Id.DecId name) {
    return switch (name.name()) {
      case "base.Int", "base.Nat" -> "long";
      case "base.Float" -> "double";
      case "base.Byte" -> "byte";
      case "base.Str" -> "rt.Str";
      default -> magicName(name);
      };
    }
  private String magicName(Id.DecId name){
    if (magic.isMagic(Magic.Int, name)) { return "long"; }
    if (magic.isMagic(Magic.Nat, name)) { return "long"; }
    if (magic.isMagic(Magic.Float, name)) { return "double"; }
    if (magic.isMagic(Magic.Byte, name)) { return "byte"; }
    if (magic.isMagic(Magic.Str, name)) { return "rt.Str"; }
    return id.getFullName(name);
  }
  public String boxOf(String s){
    return Optional.ofNullable(boxed.get(s)).orElse(s);
  }
  private static final Map<String,String> boxed= Map.of(
    "int","Integer",
    "float","Float",
    "double","Double",
    "char","Character",
    "byte","Byte",
    "short","Short",
    "long","Long",
    "boolean","Boolean"
  );
}