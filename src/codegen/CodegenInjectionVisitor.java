package codegen;

import ast.T;
import id.Id;
import id.Mdf;
import utils.Bug;
import utils.Push;
import visitors.Visitor;

import java.util.*;
import java.util.stream.Collectors;

public class CodegenInjectionVisitor implements Visitor<E> {
  public E.MCall visitMCall(ast.E.MCall e) {
    return new E.MCall(
      e.receiver().accept(this),
      e.name(),
      e.ts().stream().map(this::visitT).toList(),
      e.es().stream().map(ei->ei.accept(this)).toList(),
      e.pos()
    );
  }

  public E.X visitX(ast.E.X e){
    return new E.X(e.name());
  }

  public E.Lambda visitLambda(ast.E.Lambda e, HashSet<String> usedGXNames){
    return new E.Lambda(
      e.mdf(),
    );

    var base = e.it().map(List::of).orElseGet(List::of);
    var its = Push.of(base, e.its());
    return new E.Lambda(
      e.mdf().orElseThrow(),
      its.stream().map(this::visitIT).toList(),
      Optional.ofNullable(e.selfName()).orElseGet(ast.E.X::freshName),
      e.meths().stream().map(this::visitMeth).toList(),
      e.pos()
    );
  }

  public E.Dec visitDec(ast.T.Dec d){
    return new ast.T.Dec(
      d.name(),
      d.gxs().stream().map(gx->new Id.GX<ast.T>(gx.name())).toList(),
      this.visitLambda(d.lambda().withITs(Push.of(d.toIT(), d.lambda().its()))),
      d.pos()
    );
  }

  public ast.T visitT(ast.T t){
    return t;
  }

  public Id.IT<ast.T> visitIT(Id.IT<ast.T> t){
    return new Id.IT<>(
      t.name(),
      t.ts().stream().map(this::visitT).toList()
    );
  }

  public E.Meth visitMeth(ast.E.Meth m){
    return new E.Meth(
      visitSig(m.sig().orElseThrow()),
      m.name().orElseThrow(),
      m.xs(),
      m.body().map(b->b.accept(this)),
      m.pos()
    );
  }

  public E.Sig visitSig(ast.E.Sig s){
    return new E.Sig(
      s.mdf(),
      s.gens().stream().map(this::visitGX).toList(),
      s.ts().stream().map(this::visitT).toList(),
      this.visitT(s.ret()),
      s.pos()
    );
  }

  public String visitDecId(Id.DecId name, HashSet<String> usedNames) {
    var baseName = name.name() + "_" + name.gen();
    return codeGenName(baseName, usedNames);
  }

  public List<E.Dec> visitProgram(ast.Program p){
    var usedDecNames = new HashSet<String>();
    var usedGXNames = new HashSet<String>();
    return p.ds().values().stream()
      .map(dec->new E.Dec(
        visitDecId(dec.name(), usedDecNames),
        dec.gxs().stream().map(gx->this.visitGX(gx, usedGXNames)).toList(),
        visitLambda(dec.lambda(), usedGXNames)
      ))
      .toList();
  }

  private Id.GX<ast.T> visitGX(Id.GX<ast.T> gx, HashSet<String> usedNames){
     return new Id.GX<>(codeGenName(gx.name(), usedNames));
  }

  private String codeGenName(String baseName, HashSet<String> usedNames) {
    var codegenName = baseName;
    int i = 1;
    while (usedNames.contains(codegenName)) {
      codegenName = baseName + "_" + (i++);
    }
    usedNames.add(codegenName);
    return codegenName;
  }
}
