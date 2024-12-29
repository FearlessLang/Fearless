package visitors;

import astFull.E;
import astFull.Package;
import astFull.T;
import files.Pos;
import generated.FearlessParser.*;
import id.Id;
import id.Id.GX;
import id.Id.MethName;
import id.Mdf;
import failure.Fail;
import magic.LiteralKind;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import astFull.E.Lambda.LambdaId;
import astFull.E.Meth;
import astFull.E.X;
import parser.ParseDirectLambdaCall;
import utils.*;
import wellFormedness.FullUndefinedGXsVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<Id.IT<T>>> resolve;
  public StringBuilder errors = new StringBuilder();
  private String pkg;
  private Map<Id.GX<T>, Set<Mdf>> xbs = Map.of();
  public FullEAntlrVisitor(Path fileName,String pkg,Function<String,Optional<Id.IT<T>>> resolve){
    this.fileName= fileName;
    this.resolve= resolve;
    this.pkg= pkg;
  }
  Pos pos(ParserRuleContext prc){
    return pos(fileName,prc);
    }
  public static Pos pos(Path fileName,ParserRuleContext prc){
    return Pos.of(fileName.toUri(),prc.getStart().getLine(),prc.getStart().getCharPositionInLine());
    }

  void check(ParserRuleContext ctx){
    assert this.pkg!=null;
    if(ctx.children==null){ throw new ParserFailed(); }
    }
  @Override public Void visit(ParseTree arg0){ throw Bug.unreachable(); }
  @Override public Void visitChildren(RuleNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitErrorNode(ErrorNode arg0){ throw Bug.unreachable(); }
  @Override public Void visitTerminal(TerminalNode arg0){ throw Bug.unreachable(); }
  @Override public Object visitRoundE(RoundEContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitGenDecl(GenDeclContext ctx) { throw Bug.unreachable();}
  @Override public Object visitNudeX(NudeXContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeM(NudeMContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitNudeFullCN(NudeFullCNContext ctx){ throw Bug.unreachable(); }
  @Override public MethName visitM(MContext ctx){ throw Bug.unreachable(); }
  @Override public Package visitNudeProgram(NudeProgramContext ctx){ throw Bug.unreachable(); }

  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);
    return visitE(ctx.e());
  }
  @Override public E visitE(EContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    var calls = ctx.pOp();
    if(calls.isEmpty()){ return root; }
    root = ParseDirectLambdaCall.of(root, xbs);
    return visitAllPOp(calls).apply(root);
    }
  public Function<E,E> visitAllPOp(List<POpContext> pops){
    return pops.stream()
      .map(popi->this.visitPOp(popi))
      .reduce(x->x,Function::andThen);
    }
  E.MCall buildMCall(E recv, ActualGenContext mGen, List<EContext> e, AtomEContext atomE, String mName, XContext x, List<POpContext> pops, Pos pos){
    var gen= visitActualGen(mGen);
    var es=  e.stream().map(this::visitE).toList();
    var optAtomE= Optional.ofNullable(atomE).map(this::visitAtomE);
    if(optAtomE.isPresent()){ es = Push.of(optAtomE.get(),es); }
    var m=   new MethName(Optional.empty(), mName, es.size());
    var mcall= new E.MCall(recv,m,gen,es,T.infer,Optional.of(pos));
    Optional<X> xOpt= Optional.ofNullable(x).map(this::visitX);
    Function<E,E> pOp= this.visitAllPOp(pops);
    return xOpt
      .map(xi->buildEqSugar(mcall,xi,pOp))
      .orElse(mcall);
    }
  @Override public Function<E,E> visitPOp(POpContext ctx){
    check(ctx);
    return recv->buildMCall(
      recv, ctx.actualGen(), ctx.e(), ctx.atomE(),ctx.m().getText(),
      ctx.x(),ctx.pOp(),pos(ctx));
    }
  E.MCall buildEqSugar(E.MCall m, X x,Function<E,E> pops){
    assert m.es().size() == 1;
    var atom= m.es().get(0);
    var freshSelf= new E.X(T.infer);
    var body= Optional.of(pops.apply(freshSelf));
    var pos= atom.pos();
    var id= new E.Lambda.LambdaId(Id.DecId.fresh(pkg, 0), List.of(), this.xbs);
    var xs= List.of(x.name(), freshSelf.name());
    var contM = new E.Meth(Optional.empty(), Optional.empty(), xs, body, pos);
    var cont = new E.Lambda(id, Optional.empty(), List.of(), null,
      List.of(contM), Optional.empty(), pos);
    List<E> es=List.of(atom,cont);
    var n=new MethName(m.name().name(),2);
    return new E.MCall(m.receiver(), n, m.ts(), es, m.t(),m.pos());
  }
  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }


  @Override public Optional<List<T>> visitActualGen(ActualGenContext ctx) {
    if(ctx.OS()==null){ return Optional.empty(); }
    return Optional.of(ctx.t().stream().map(this::visitT).toList());
    }
  public record GenericParams(List<Id.GX<T>> gxs, Map<Id.GX<T>, Set<Mdf>> bounds) {
    public GenericParams{
      //No, it must be allowed so that it can fails on well formedness assert gxs.size()==bounds.size():gxs+" "+bounds;
      assert bounds.keySet().stream().allMatch(k->gxs.contains(k)):gxs+" "+bounds;
    }
  }
  private Set<Mdf> mdfStarSugar(GenDeclContext gen){
    if (gen.SysInM()==null){ return Set.of(Mdf.imm); }
    String text= gen.SysInM().getText();
    var res= new LinkedHashSet<Mdf>();
    res.add(Mdf.imm);res.add(Mdf.mut);res.add(Mdf.read);
    if (text.equals("*")){return res;}
    if (!text.equals("**")){ throw Bug.todo("better error"); }
    res.add(Mdf.iso);res.add(Mdf.mutH);res.add(Mdf.readH);
    return res;
  }
  Id.GX<T> buildGenX(FullCNContext ctx){
    return buildGenX(visitFullCN(ctx),pos(ctx));
  }
  Id.GX<T> buildGenX(String name, Pos pos){
    var canBeGen =  isGenOk(name);
    if (!canBeGen){ throw Bug.todo("better error for invalid syntax for gen declared name"); }
    var shadows= resolve.apply(name);
    if (shadows.isPresent()){ throw Fail.concreteTypeInFormalParams(shadows.get().toString()).pos(pos); }
    //TODO: above can be improved "better error for generic name shadows type name"
    return new Id.GX<T>(name);
  }
  @Override public Optional<GenericParams> visitMGen(MGenContext ctx) {
    if (ctx.OS()==null){ return Optional.empty(); }//subsumes check(ctx);
    List<GX<T>> gxs= ctx.genDecl().stream()
      .map(gd->buildGenX(gd.fullCN()))
      .toList();
    Map<Id.GX<astFull.T>, Set<Mdf>> boundsMap = Mapper.of(acc->
      Streams.zip(ctx.genDecl(), gxs)
        .forEach((declCtx, gx) -> {
          var bounds = !some(declCtx.mdf())
            ?mdfStarSugar(declCtx)
            :new LinkedHashSet<>(declCtx.mdf().stream()
              .map(this::visitMdf)
              .toList());
          acc.put(gx, bounds);
          }));
    return Optional.of(new GenericParams(gxs, boundsMap));
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
  Map<Id.GX<T>, Set<Mdf>> freeXbs(List<Id.IT<T>> its,List<Meth> ms){
    var visitor = new FullUndefinedGXsVisitor(Set.copyOf(xbs.keySet()));
    its.forEach(visitor::visitIT);
    ms.forEach(visitor::visitMeth);
    var vres= visitor.res();
    return Mapper.of(m->vres.forEach(b->m.put(b, xbs.get(b))));
  }
  E.Lambda visitUnnamedLambda(LambdaContext ctx){
    T t= opt(ctx.t(),this::visitT);
    assert t==null || !t.isInfer();
    Mdf mdf=opt(ctx.mdf(),this::visitExplicitMdf);
    if (mdf==null && t!=null){ mdf = visitMdf(ctx.t().mdf()); }
    Id.IT<T> it= opt(t,ti->ti.match(gx->{throw Fail.expectedConcreteType(ti).pos(pos(ctx));}, iti->iti));
    List<Id.IT<T>> its= it==null?List.of():List.of(it);
    BBlock block= opt(ctx.bblock(),this::visitBblock);
    var decId= Id.DecId.fresh(pkg, 0);
    Map<Id.GX<T>, Set<Mdf>> idBounds=block==null?Map.of():freeXbs(List.of(),block.ms());
    var id= new E.Lambda.LambdaId(decId, List.of(), idBounds);
    if(block==null){
      assert it!=null;
      return new E.Lambda(
          id,                       //LambdaId id
          Optional.ofNullable(mdf), //Optional<Mdf> mdf
          its,                      //List<Id.IT<T>>its
          null,                     //String selfName
          List.of(),                //List<Meth> meths
          Optional.of(it),          //Optional<Id.IT<T>> it
          Optional.of(pos(ctx))     //Optional<Pos> pos
        );
    }
    assert block!=null;
    return new E.Lambda(
        id,                       //LambdaId id
        Optional.ofNullable(mdf), //Optional<Mdf> mdf
        its,                      //List<Id.IT<T>>its
        block.selfName(),         //String selfName
        block.ms(),                //List<Meth> meths
        Optional.ofNullable(it),  //Optional<Id.IT<T>> it
        Optional.of(pos(ctx))     //Optional<Pos> pos
      );
  }
  E.Lambda visitNamedLambda(LambdaContext ctx){
    Mdf mdf= opt(ctx.mdf(),this::visitMdf);
    return visitNamedLambda(mdf,ctx.topDec());
  }
  E.Lambda visitNamedLambda(Mdf mdf,TopDecContext ctx){
    GenericParams mGen= Optional.ofNullable(ctx.mGen())
        .flatMap(this::visitMGen)
        .orElse(new GenericParams(List.of(), Map.of()));
    return withXBs(mGen.bounds,()->visitNamedLambda(mGen,mdf,ctx));
  }
  E.Lambda visitNamedLambda(GenericParams mGen, Mdf mdf, TopDecContext ctx){
    String cName= completeDeclName(visitFullCN(ctx.fullCN()), pos(ctx));
    var decId= new Id.DecId(cName, mGen.gxs.size());
    LambdaId id= new LambdaId(decId, mGen.gxs, mGen.bounds);
    List<T> nameTs= id.gens().stream()
      .map(gx->new T(Mdf.mdf, gx))
      .toList();
    Id.IT<T> nameId= new Id.IT<T>(decId,nameTs);
    List<Id.IT<T>> its= ctx.t() == null
      ?List.of()
      :ctx.t().stream()
        .map(ti->visitTNoMdf(ti))
        .map(ti->ti.match(
          gx->{
            throw Fail.expectedConcreteType(ti).pos(pos(ctx)); },
          iti->iti))
        .toList();
    var block= visitBblock(ctx.bblock());
    return new E.Lambda(
      id,                       //LambdaId id
      Optional.ofNullable(mdf), //Optional<Mdf> mdf
      its,                      //List<Id.IT<T>>its
      block.selfName(),         //String selfName
      block.ms(),                //List<Meth> meths
      Optional.ofNullable(nameId),  //Optional<Id.IT<T>> it
      Optional.of(pos(ctx))     //Optional<Pos> pos
    );
  }
  record BBlock(String selfName, List<Meth> ms){};
  public BBlock visitMethsBlock(BblockContext ctx){
    var selfName=opt(ctx.SelfX(),xi->xi.getText().substring(1));
    List<Meth> ms= ctx.meth().stream().map(this::visitMeth).toList();
    return new BBlock(selfName,ms);
  }
  public BBlock visitSingleBlock(BblockContext ctx){
    var selfName=opt(ctx.SelfX(),xi->xi.getText().substring(1));
    List<Meth> ms=List.of(visitSingleM(ctx.singleM()));
    return new BBlock(selfName,ms);
  }
  String sugarName(String mName, String opName){
    if (mName!=null){
      assert mName.startsWith("::");
      return "."+mName.substring(2);
      }
    assert opName!=null;
    if (!opName.startsWith("::")){ throw Bug.todo("better error"); }
    return opName.substring(2);
  }
  record XE(X x,E e){}
  XE computeSugarBlockStart(BblockContext ctx){
    var x= new E.X(T.infer);
    boolean id= ctx.ColonColon()!=null;
    if (id){ return new XE(x,x); }
    String mName=sugarName(opt(ctx.CCMName(),s->s.getText()), opt(ctx.SysInM(),s->s.getText()));
    E e= buildMCall(
      x, ctx.actualGen(), ctx.e(), ctx.atomE(),mName,
      ctx.x(),ctx.pOp(),pos(ctx));
    return new XE(x,e);
  }
  public BBlock visitSugarBlock(BblockContext ctx){
    XE xe=computeSugarBlockStart(ctx);
    List<String> xs= List.of(xe.x().name());
    List<POpContext> pop= ctx.pOp()==null?List.of():ctx.pOp();
    var popAlreadyAdded= ctx.x()!=null;
    E e= xe.e();
    if (!popAlreadyAdded){ e = visitAllPOp(pop).apply(e); }
    Meth m= new E.Meth(
      Optional.empty(), Optional.empty(), xs,
      Optional.of(e), Optional.of(pos(ctx)));
    return new BBlock(null,List.of(m));
  }
  @Override public BBlock visitBblock(BblockContext ctx){
    //NO: can be empty check(ctx);
    if (ctx.children==null){ new BBlock(null,List.of()); }
    boolean sugar= ctx.ColonColon()!=null || ctx.CCMName() !=null|| ctx.SysInM()!=null;
    if (sugar){ return visitSugarBlock(ctx); }
    if (ctx.singleM()!=null){return visitSingleBlock(ctx);}
    assert ctx.meth()!=null;
    return visitMethsBlock(ctx);
  }
  @Override public E.Lambda visitLambda(LambdaContext ctx){
    check(ctx);
    return ctx.topDec() == null
      ? visitUnnamedLambda(ctx)
      : visitNamedLambda(ctx);
  }
  private <R> R withXBs(Map<Id.GX<T>, Set<Mdf>> xbs, Supplier<R> s){
    var oldXbs = this.xbs;
    this.xbs = Map.copyOf(xbs);
    try{ return s.get(); }
    finally { this.xbs = oldXbs; }
  }
  @Override public String visitFullCN(FullCNContext ctx) {
    return ctx.getText();
  }
  @Override public Mdf visitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.imm; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitExplicitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return null; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }
  public Mdf visitGenricMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.mdf; }
    if(ctx.getText().equals("read/imm")) { return Mdf.readImm; }
    return Mdf.valueOf(ctx.getText());
  }

  public Id.IT<T> visitIT(TContext ctx) {
    T t=visitTNoMdf(ctx);
    return t.match(gx->{throw Fail.expectedConcreteType(t).pos(pos(ctx));}, it->it);
  }
  @Override public T visitNudeT(NudeTContext ctx) { return visitT(ctx.t()); }

  // TODO: this RegEx looks wrong to me, I think something like _foo. will match
  private static final Pattern regexPkg = Pattern.compile("^(_*[a-z][0-9A-Za-z_]*(?:\\._*[a-z][0-9A-Za-z_]*)*)");
  private static final Pattern regexGenName = Pattern.compile("^_*[A-Z][0-9A-Za-z_]*'*$");

  public static String extractPackageName(String name) {
    Matcher matcher = regexPkg.matcher(name);
    if (matcher.find()) { return matcher.group(1); }
    else { return ""; }
  }
  private boolean isGenOk(String name){
    return regexGenName.matcher(name).matches();
  }
  public T visitTNoMdf(TContext ctx) {
    if(ctx.mdf().getText().isEmpty()){ return visitT(ctx); }
    throw Fail.noMdfInImplementedT(ctx.mdf().getText(), ctx.fullCN().getText()).pos(pos(ctx));
  }
  Optional<Id.IT<T>> fullNameIt(String name, List<T> ts){
    String pkgName= extractPackageName(name);
    if(pkgName.isEmpty()){ return Optional.empty(); }
    return Optional.of(new Id.IT<>(name,ts));
  }
  Optional<Id.IT<T>> aliasIt(String name, List<T> ts){
    return resolve.apply(name)
      .map(it-> it.withTs(Push.of(it.ts(),ts)));
  }
  @Override public T visitT(TContext ctx) {
    String name = visitFullCN(ctx.fullCN());
    var ts= visitActualGen(ctx.actualGen()).orElse(List.of());
    return fullNameIt(name,ts)
      .or(()->aliasIt(name,ts))
      .or(()->LiteralKind.toFullIt(name,ts))
      .map(it->new T(visitMdf(ctx.mdf()),it))
      .orElseGet(()->new T(visitGenricMdf(ctx.mdf()), new Id.GX<>(name)));
  }
  @Override
  public E.Meth visitSingleM(SingleMContext ctx) {
    check(ctx);
    var xs = ctx.x() == null
      ?List.<String>of()
      :ctx.x().stream()
        .map(this::visitX)
        .map(E.X::name)
        .toList();
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    return new E.Meth(Optional.empty(), Optional.empty(), xs, body, Optional.of(pos(ctx)));
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
    var gens = this.visitMGen(ctx.mGen()).orElse(new GenericParams(List.of(), Map.of()));
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
  String completeDeclName(String cName, Pos pos){
    String pkgName= extractPackageName(cName);
    if (!pkgName.isEmpty()){ throw Fail.crossPackageDeclaration().pos(pos); }
    Optional<String> litFullName= LiteralKind.toFullName(cName);
    if (litFullName.isPresent()) {
      //TODO: will be changed when we add the alias pkg.* syntax
      throw Fail.syntaxError(pkg + "." + cName + " is not a valid type name.").pos(pos);
    }
    assert pkg!=null;
    return pkg+"."+cName;
  }
  @Override public T.Dec visitTopDec(TopDecContext ctx) {
    check(ctx);
    E.Lambda l= visitNamedLambda(Mdf.mut,ctx);
    var pos= Optional.of(pos(ctx));
    return new T.Dec(l.id().id(), l.id().gens(), l.id().bounds(), l, pos);
  }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.actualGen(), g->visitActualGen(g));
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new Id.IT<>(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    return new T.Alias(inT, out, Optional.of(pos(ctx)));
  }
  public static Package fileToPackage(
      Path fileName,Function<String,Optional<Id.IT<T>>> resolve, NudeProgramContext ctx){
    String name = ctx.Pack().getText();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    name = name.substring("package ".length(),name.length()-1);
    FullEAntlrVisitor self=new FullEAntlrVisitor(fileName,name,resolve);
    var as = ctx.alias().stream().map(self::visitAlias).toList();
    var decs = List.copyOf(ctx.topDec());
    return new Package(name,as,decs,decs.stream().map(e->fileName).toList());
  }
  @Override
  public Object visitFStringMulti(FStringMultiContext arg0) {
    throw Bug.unreachable();
  }
  private boolean some(Object ctx){
    if (ctx==null){ return false; }
    if (ctx instanceof Collection<?> c && c.isEmpty()){ return false; }
    return true;
  }
}