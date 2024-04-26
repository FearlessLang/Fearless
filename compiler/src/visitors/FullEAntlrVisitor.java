package visitors;

import astFull.E;
import astFull.Package;
import astFull.T;
import files.Pos;
import generated.FearlessParser.*;
import id.Id;
import id.Id.MethName;
import id.Mdf;
import failure.Fail;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import astFull.E.Lambda.LambdaId;
import utils.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<Id.IT<T>>> resolve;
  public StringBuilder errors = new StringBuilder();
  private String pkg;
  private Map<Id.GX<T>, Set<Mdf>> xbs = Map.of();
  public List<T.Alias> inlineNames = new ArrayList<>();
  public FullEAntlrVisitor(Path fileName,Function<String,Optional<Id.IT<T>>> resolve){
    this.fileName=fileName;
    this.resolve=resolve;
  }
  Pos pos(ParserRuleContext prc){
    return pos(fileName,prc);
    }
  public static Pos pos(Path fileName,ParserRuleContext prc){
    return Pos.of(fileName.toUri(),prc.getStart().getLine(),prc.getStart().getCharPositionInLine());
    }

  void check(ParserRuleContext ctx){
    if(ctx.children==null){ throw new ParserFailed(); }
    }
  @Override public Void visit(ParseTree arg0){ throw Bug.unreachable(); }
  @Override public Void visitChildren(RuleNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitErrorNode(ErrorNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitTerminal(TerminalNode arg0){ throw Bug.unreachable(); }
  @Override public Object visitRoundE(RoundEContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitGenDecl(GenDeclContext ctx) { throw Bug.unreachable();}
  @Override public Object visitMGen(MGenContext ctx) { throw Bug.unreachable(); }

  @Override public Object visitBblock(BblockContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitPOp(POpContext ctx){ throw Bug.unreachable(); }
  @Override public T.Dec visitTopDec(TopDecContext ctx) { throw Bug.unreachable(); }
  @Override public Object visitNudeX(NudeXContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeM(NudeMContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeFullCN(NudeFullCNContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitM(MContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitBlock(BlockContext ctx){ throw Bug.unreachable(); }
  @Override public Call visitCallOp(CallOpContext ctx) {
    check(ctx);
    return new Call(
      new MethName(Optional.empty(), ctx.m().getText(),1),
      visitMGen(ctx.mGen(), true),
      Optional.ofNullable(ctx.x()).map(this::visitX),
      List.of(visitPostE(ctx.postE())),
      pos(ctx)
    );
  }

  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);
    return visitE(ctx.e());
  }

  @Override public E visitE(EContext ctx){
    check(ctx);
    E root = visitPostE(ctx.postE());
    var calls = ctx.callOp();
    if(calls.isEmpty()){ return root; }
    var res = calls.stream()
      .flatMap(callOpCtx->{
        var head = new Call(
          new MethName(Optional.empty(), callOpCtx.m().getText(),1),
          visitMGen(callOpCtx.mGen(), true),
          Optional.ofNullable(callOpCtx.x()).map(this::visitX),
          List.of(visitAtomE(callOpCtx.postE().atomE())),
          pos(callOpCtx)
        );
        return Stream.concat(Stream.of(head), callOpCtx.postE().pOp().stream().flatMap(pOp->fromPOp(pOp).stream()));
      })
      .toList();

    return desugar(root, res);
    }

  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  @Override public E visitPostE(PostEContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    return desugar(root, ctx.pOp().stream().flatMap(pOp->fromPOp(pOp).stream()).toList());
  }
  record Call(MethName m, Optional<List<T>> mGen, Optional<E.X> x, List<E> es, Pos pos){}
  List<Call> fromPOp(POpContext ctx) {
    var call = new Call(
      new MethName(Optional.empty(), ctx.m().getText(),ctx.e().size()),
      visitMGen(ctx.mGen(), true),
      Optional.ofNullable(ctx.x()).map(this::visitX),
      ctx.e().stream().map(this::visitE).toList(),
      pos(ctx)
    );
    return Push.of(call, ctx.callOp().stream().map(this::visitCallOp).toList());
  }
  E desugar(E root,List<Call> tail){
    if(tail.isEmpty()){ return root; }
    var head = tail.get(0);
    var newTail = Pop.left(tail);
    var m = head.m();
    var ts=head.mGen();
    if(head.x().isPresent()){
      var x = head.x().get();
      assert head.es().size() == 1;
      var e = head.es().get(0);
      var freshRoot = new E.X(T.infer);
      var rest = desugar(freshRoot, newTail);

      var contMs = List.of(new E.Meth(Optional.empty(), Optional.empty(), List.of(x.name(), freshRoot.name()), Optional.of(rest), Optional.of(head.pos())));
      var cont = new E.Lambda(
        new E.Lambda.LambdaId(Id.DecId.fresh(pkg, 0), List.of(), this.xbs),
        Optional.empty(),
        List.of(),
        null,
        contMs,
        Optional.empty(),
        Optional.of(head.pos())
      );
      return new E.MCall(root, new MethName(Optional.empty(), m.name(), 2), ts, List.of(e, cont), T.infer, Optional.of(head.pos()));
    }
    var es=head.es();
    E res=new E.MCall(root, new MethName(Optional.empty(), m.name(), es.size()), ts, es, T.infer, Optional.of(head.pos()));
    return desugar(res,newTail);
  }
  public Optional<List<T>> visitMGen(MGenContext ctx, boolean isDecl){
    if(ctx.children==null){ return Optional.empty(); }//subsumes check(ctx);
    var noTs = ctx.genDecl()==null || ctx.genDecl().isEmpty();
    if(ctx.OS() == null){ return Optional.empty(); }
    if(noTs){ return Optional.of(List.of()); }
    return Optional.of(ctx.genDecl().stream().map(declCtx->{
      var t = visitT(declCtx.t(), isDecl);
      if (declCtx.mdf() == null || declCtx.mdf().isEmpty()) { return t; }
      var gx = t.gxOrThrow();
      return new T(t.mdf(), new Id.GX<>(gx.name()));
    }).toList());
  }
  public record GenericParams(List<Id.GX<T>> gxs, Map<Id.GX<astFull.T>, Set<Mdf>> bounds) {}
  public Optional<GenericParams> visitMGenParams(MGenContext ctx){
    var mGens = this.visitMGen(ctx, false);
    return mGens
      .map(ts->ts.stream()
        .map(t->t.match(
          gx->gx,
          it->{throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx));}
        ))
        .toList())
      .map(gxs -> {
        Map<Id.GX<astFull.T>, Set<Mdf>> boundsMap = Mapper.of(acc->Streams.zip(ctx.genDecl(), gxs)
          .filter((declCtx, gx)->declCtx.mdf() != null && !declCtx.mdf().isEmpty())
          .forEach((declCtx, gx) -> {
            var bounds = declCtx.mdf().stream().map(this::visitGenMdf).collect(Collectors.toSet());
            acc.put(gx, bounds);
          }));

        return new GenericParams(gxs, boundsMap);
      });
  }
  @Override public E.X visitX(XContext ctx){
    check(ctx);
    var name = ctx.getText();
    name = name.equals("_") ? E.X.freshName() : name;
    return new E.X(name, T.infer, Optional.of(pos(ctx)));
  }
  @Override public E visitAtomE(AtomEContext ctx){
    check(ctx);
    return OneOr.of("",Stream.of(
        opt(ctx.x(),xCtx->{
          if (xCtx.getText().equals("_")) { throw Fail.ignoredIdentInExpr(); }
          return this.visitX(xCtx);
        }),
        opt(ctx.lambda(),this::visitLambda),
        opt(ctx.roundE(),(re->this.visitE(re.e()))))
        .filter(Objects::nonNull));
  }
  @Override public E.Lambda visitLambda(LambdaContext ctx){

    var oldXbs = this.xbs;
    E.Lambda res;
    if (ctx.topDec() != null) {
      String cName = visitFullCN(ctx.topDec().fullCN());
      if (cName.contains(".")) {
        throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
      }
      assert this.pkg != null;
      cName = this.pkg + "." + cName;

      var mGen = Optional.ofNullable(ctx.topDec().mGen())
        .flatMap(this::visitMGenParams)
        .orElse(new GenericParams(List.of(), Map.of()));
      this.xbs = mGen.bounds;
      var name = Optional.of(new E.Lambda.LambdaId(new Id.DecId(cName, mGen.gxs.size()), mGen.gxs, mGen.bounds));
      res = visitBlock(ctx.topDec().block(), Optional.ofNullable(visitExplicitMdf(ctx.mdf())), name);
    } else {
      res = visitBlock(ctx.block(), Optional.ofNullable(visitExplicitMdf(ctx.mdf())), Optional.empty());
      if (res.its().isEmpty() && !ctx.mdf().getText().isEmpty()) { throw Fail.modifierOnInferredLambda().pos(pos(ctx)); }
    }

    this.xbs = oldXbs;
    return res;
  }
  public E.Lambda visitBlock(BlockContext ctx, Optional<Mdf> mdf, Optional<E.Lambda.LambdaId> name){
    check(ctx);
    var _its = Optional.ofNullable(ctx.t())
      .map(its->its.stream().map(this::visitIT).toList());
    var its = _its.orElse(List.of());
    if (name.isEmpty() && mdf.isPresent()) {
      throw Fail.mustProvideImplsIfMdfProvided().pos(pos(ctx));
    }
    if (name.isPresent() && mdf.isEmpty()) { mdf = Optional.of(Mdf.imm); }
    assert mdf.filter(Mdf::isMdf).isEmpty();
    Supplier<E.Lambda.LambdaId> emptyTopName = ()->new E.Lambda.LambdaId(Id.DecId.fresh(pkg, 0), List.of(), this.xbs);
    LambdaId id= name.orElseGet(emptyTopName);
    boolean givenName= mdf.isPresent() && !id.id().isFresh();
    var inferredOpt= Optional.<Id.IT<T>>empty();
    if(givenName){
      Id.IT<astFull.T> nameId= new Id.IT<>(id.id(),
        id.gens().stream().map(gx->new T(Mdf.mdf, gx)).toList());
      inferredOpt= Optional.of(nameId);
    }
    //TODO: inferredOpt may itself disappear since we have nameId in id.
    var bb = ctx.bblock();
    if(bb==null || bb.children==null){
      return new E.Lambda(name.orElseGet(emptyTopName),
        mdf, its, null, List.of(), inferredOpt, Optional.of(pos(ctx)));
    }
    var _x=bb.SelfX();
    var _n=_x==null?null:_x.getText().substring(1);
    var _ms=opt(bb.meth(),ms->ms.stream().map(this::visitMeth).toList());
    var _singleM=opt(bb.singleM(),this::visitSingleM);
    List<E.Meth> mms=_ms==null?List.of():_ms;
    if(mms.isEmpty()&&_singleM!=null){ mms=List.of(_singleM); }
    var meths = mms;
    return new E.Lambda(id, mdf, its, _n,
      meths,inferredOpt, Optional.of(pos(ctx)));
  }
  @Override public String visitFullCN(FullCNContext ctx) {
    return ctx.getText();
  }

  @Override public Mdf visitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.imm; }
    if (ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitExplicitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return null; }
    if (ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitGenMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.mdf; }
    if (ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }

  public Id.IT<T> visitIT(TContext ctx) {
    T t=visitT(ctx,false);
    return t.match(gx->{throw Fail.expectedConcreteType(t).pos(pos(ctx));}, it->it);
  }
  @Override public T visitNudeT(NudeTContext ctx) { return visitT(ctx.t()); }

  @Override public T visitT(TContext ctx) { return visitT(ctx,true); }
  public T visitT(TContext ctx, boolean canMdf) {
    if(!canMdf && !ctx.mdf().getText().isEmpty()){
      throw Fail.noMdfInFormalParams(ctx.getText()).pos(pos(ctx));
    }
    String name = visitFullCN(ctx.fullCN());
    var isFullName = name.contains(".");
    var mGen=visitMGen(ctx.mGen(), true);
    Optional<Id.IT<T>> resolved = isFullName ? Optional.empty() : resolve.apply(name);
    var isIT = isFullName || resolved.isPresent();
    Mdf mdf = isIT ? visitMdf(ctx.mdf()) : visitGenMdf(ctx.mdf());
    if(!isIT){
      var t = new T(mdf, new Id.GX<>(name));
      if(mGen.isPresent()){ throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx)); }
      return t;
    }
    // TODO: TEST alias generic merging
    var ts = mGen.orElse(List.of());
    if(resolved.isEmpty()){return new T(mdf,new Id.IT<>(name,ts));}
    var res = resolved.get();
    ts = Push.of(res.ts(),ts);
    return new T(mdf,res.withTs(ts));
  }
  @Override
  public E.Meth visitSingleM(SingleMContext ctx) {
    check(ctx);
    var _xs = opt(ctx.x(), xs->xs.stream()
      .map(this::visitX).map(E.X::name).toList());
    _xs = _xs==null?List.of():_xs;
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    return new E.Meth(Optional.empty(), Optional.empty(), _xs, body, Optional.of(pos(ctx)));
  }
  @Override
  public E.Meth visitMeth(MethContext ctx) {
    check(ctx);
    var oldXbs = this.xbs;
    var mh = Optional.ofNullable(ctx.sig()).map(this::visitSig);
    mh.ifPresent(header->
      this.xbs = Mapper.of(xbs_->{
        xbs_.putAll(oldXbs);
        xbs_.putAll(header.bounds);
      }));
    var xs = mh.map(MethHeader::xs).orElseGet(()->{
      var _xs = opt(ctx.x(), xs1->xs1.stream().map(this::visitX).toList());
      return _xs==null?List.of():_xs;
    });
    var name = mh.map(MethHeader::name)
        .orElseGet(()->new MethName(mh.map(MethHeader::mdf), ctx.m().getText(),xs.size()));
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    var sig = mh.map(h->new E.Sig(h.gens(), h.bounds(), xs.stream().map(E.X::t).toList(), h.ret(), Optional.of(pos(ctx))));
    var res = new E.Meth(sig, Optional.of(name), xs.stream().map(E.X::name).toList(), body, Optional.of(pos(ctx)));
    this.xbs = oldXbs;
    return res;
  }
  public record MethHeader(Mdf mdf, MethName name, List<Id.GX<T>> gens, Map<Id.GX<astFull.T>, Set<Mdf>> bounds, List<E.X> xs, T ret){}
  @Override
  public MethHeader visitSig(SigContext ctx) {
    check(ctx);
    var mdf = this.visitMdf(ctx.mdf());
    var gens = this.visitMGenParams(ctx.mGen()).orElse(new GenericParams(List.of(), Map.of()));
    var xs = Optional.ofNullable(ctx.gamma()).map(this::visitGamma).orElse(List.of());
    var name = new MethName(Optional.of(mdf), ctx.m().getText(), xs.size());
    var ret = this.visitT(ctx.t());
    return new MethHeader(mdf, name, gens.gxs, gens.bounds, xs, ret);
  }
  @Override
  public List<E.X> visitGamma(GammaContext ctx) {
    return Streams.zip(ctx.x(), ctx.t())
      .map((xCtx, tCtx)->new E.X(xCtx.getText(), this.visitT(tCtx), Optional.of(pos(xCtx))))
      .toList();
  }
  public T.Dec visitTopDec(TopDecContext ctx, String pkg, boolean shallow) {
    check(ctx);
    String cName = visitFullCN(ctx.fullCN());
    if (cName.contains(".")) {
      throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
    }
    cName = pkg + "." + cName;
    this.pkg = pkg;

    var mGen = Optional.ofNullable(ctx.mGen())
      .flatMap(this::visitMGenParams)
      .orElse(new GenericParams(List.of(), Map.of()));
    var id = new Id.DecId(cName,mGen.gxs.size());

    var inlineNamesVisitor = new InlineDecNamesAntlrVisitor(this, pkg);
    inlineNamesVisitor.visitTopDec(ctx);
    this.inlineNames.addAll(inlineNamesVisitor.inlineDecs);

    var oldXbs = this.xbs;
    this.xbs = Map.copyOf(mGen.bounds);
    var body = shallow ? null : visitBlock(ctx.block(), Optional.empty(), Optional.of(new E.Lambda.LambdaId(id, mGen.gxs, mGen.bounds)));
    if (body != null) {
//      assert body.it().isEmpty();
      body = body.withT(Optional.empty());
    }
    this.xbs = oldXbs;
    return new T.Dec(id, mGen.gxs(), mGen.bounds(), body, Optional.of(pos(ctx)));
  }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.mGen(0), mGenCtx->visitMGen(mGenCtx, true));
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new Id.IT<>(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    var outG = ctx.mGen(1);
    if(!outG.genDecl().isEmpty()){ throw Bug.of("No gen on out Alias"); }
    return new T.Alias(inT, out, Optional.of(pos(ctx)));
  }
  @Override public Package visitNudeProgram(NudeProgramContext ctx) {
    String name = ctx.Pack().getText();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    name = name.substring("package ".length(),name.length()-1);
    var as = ctx.alias().stream().map(this::visitAlias).toList();

    var decs = List.copyOf(ctx.topDec());
    return new Package(name,as,decs,decs.stream().map(e->fileName).toList());
  }
}