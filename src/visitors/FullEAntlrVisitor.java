package visitors;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import astFull.PosMap;
import main.CompileError;
import main.Fail;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import ast.Mdf;
import astFull.E;
import astFull.T;
import files.Pos;
import generated.FearlessParser.AliasContext;
import generated.FearlessParser.AtomEContext;
import generated.FearlessParser.BblockContext;
import generated.FearlessParser.BlockContext;
import generated.FearlessParser.EContext;
import generated.FearlessParser.FullCNContext;
import generated.FearlessParser.GammaContext;
import generated.FearlessParser.LambdaContext;
import generated.FearlessParser.MContext;
import generated.FearlessParser.MGenContext;
import generated.FearlessParser.MdfContext;
import generated.FearlessParser.MethContext;
import generated.FearlessParser.NudeEContext;
import generated.FearlessParser.NudeProgramContext;
import generated.FearlessParser.POpContext;
import generated.FearlessParser.PostEContext;
import generated.FearlessParser.RoundEContext;
import generated.FearlessParser.SigContext;
import generated.FearlessParser.SingleMContext;
import generated.FearlessParser.TContext;
import generated.FearlessParser.TopDecContext;
import generated.FearlessParser.XContext;
import utils.*;
import astFull.Package;

@SuppressWarnings("serial")
class ParserFailed extends RuntimeException{}

public class FullEAntlrVisitor implements generated.FearlessVisitor<Object>{
  public final Path fileName;
  public final Function<String,Optional<T.IT>> resolve;
  public StringBuilder errors = new StringBuilder();
  public FullEAntlrVisitor(Path fileName,Function<String,Optional<T.IT>> resolve){ 
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
  @Override public Object visitBblock(BblockContext ctx){ throw Bug.unreachable(); }
  @Override public Object visitPOp(POpContext ctx){ throw Bug.unreachable(); }
  @Override public T.Dec visitTopDec(TopDecContext ctx) { throw Bug.unreachable(); }
  @Override public E visitNudeE(NudeEContext ctx){
    check(ctx);    
    return visitE(ctx.e());
  } 
  
  @Override public E visitE(EContext ctx){
    check(ctx);
    var es = ctx.postE();
    E root = visitPostE(es.get(0));
    if(es.size()==1){ return root; }
    if (ctx.x() != null && !ctx.x().isEmpty()) {
      throw Bug.todo(); // TODO: = sugar without brackets
    }
    throw Bug.of();
    /*
    E e=visitE(ctx.e());
    var m=visitM(ctx.m());
    var mGen=visitMGen(ctx.mGen());
    if(ctx.x()!=null) { throw Bug.of(); }
    return new E.MCall(root,m,mGen,List.of(e),T.infer);
    */
    }
  static <A,B> B opt(A a,Function<A,B> f){
    if(a==null){return null;}
    return f.apply(a);
    }
  @Override public E visitPostE(PostEContext ctx){
    check(ctx);
    E root = visitAtomE(ctx.atomE());
    return desugar(root,ctx.pOp());
  }
  E desugar(E root,List<POpContext> tail){
    if(tail.isEmpty()){ return root; }
    var top = tail.get(0);
    var pop = Pop.left(tail);
    var m = visitM(top.m());
    var ts=visitMGen(top.mGen());
    if(top.x()!=null){
      var x = this.visitX(top.x());
      assert top.e().size() == 1;
      var e = this.visitE(top.e().get(0));
      var freshRoot = new E.X(T.infer);
      var rest = desugar(freshRoot, pop);
      var cont = new E.Lambda(Mdf.mdf, List.of(), null, List.of(
        new E.Meth(Optional.empty(), Optional.empty(), List.of(x, freshRoot), Optional.of(rest))
      ), T.infer);
      return new E.MCall(root, m, ts, List.of(e, cont), T.infer);
    }
    var es=top.e().stream().map(this::visitE).toList();
    E res=new E.MCall(root,m, ts, es,T.infer);
    return desugar(res,pop);
  }
  @Override public Optional<List<T>> visitMGen(MGenContext ctx){
    if(ctx.children==null){ return Optional.empty(); }//subsumes check(ctx);
    var noTs = ctx.t()==null || ctx.t().isEmpty();
    if(ctx.OS() == null){ return Optional.empty(); }
    if(noTs){ return Optional.of(List.of()); }

    return Optional.of(ctx.t().stream().map(this::visitT).toList());
  }
  public Optional<List<T.GX>> visitMGenParams(MGenContext ctx){
    var mGens = this.visitMGen(ctx);
    return mGens.map(ts->ts.stream()
      .map(t->t.match(
        gx->gx,
        it->{throw Fail.concreteTypeInFormalParams(t).pos(pos(ctx));}
      ))
      .toList()
    );
  }
  @Override public E.MethName visitM(MContext ctx){
    check(ctx);
    return new E.MethName(ctx.getText());
  }
  @Override public E.X visitX(XContext ctx){
    check(ctx);
    var name = ctx.getText();
    if (name.startsWith(E.X.RESERVED)) {
      throw Fail.reservedIdentifierUsed(name).pos(pos(ctx));
    }
    return PosMap.add(new E.X(name,T.infer),pos(ctx));
  }
  @Override public E visitAtomE(AtomEContext ctx){
    check(ctx);
    return OneOr.of("",Stream.of(
        opt(ctx.x(),this::visitX),
        opt(ctx.lambda(),this::visitLambda),
        opt(ctx.roundE(),(re->this.visitE(re.e()))))
        .filter(a->a!=null));
  }
  @Override public E.Lambda visitLambda(LambdaContext ctx){
    Mdf mdf=visitMdf(ctx.mdf());
    var res=visitBlock(ctx.block());
    return PosMap.add(new E.Lambda(mdf,res.its(), res.selfName(), res.meths(), T.infer),pos(ctx));
    }
  @Override public E.Lambda visitBlock(BlockContext ctx){
    check(ctx);
    var _ts=opt(ctx.t(),ts->ts.stream().map(this::visitIT).toList());
    _ts=_ts==null?List.of():_ts;
    if(ctx.bblock()==null){
      return new E.Lambda(Mdf.mdf,_ts,null,List.of(),T.infer);
      }
    var bb = ctx.bblock();
    if(bb.children==null){ return new E.Lambda(Mdf.mdf, _ts, null, List.of(), T.infer); }
    var _x=opt(bb.x(),this::visitX);
    var _n=_x==null?null:_x.name();
    var _ms=opt(bb.meth(),ms->ms.stream().map(this::visitMeth).toList());
    var _singleM=opt(bb.singleM(),this::visitSingleM);
    List<E.Meth> mms=_ms==null?List.of():_ms;
    if(mms.isEmpty()&&_singleM!=null){ mms=List.of(_singleM); }
    return new E.Lambda(Mdf.mdf,_ts,_n,mms,T.infer);
    }
  @Override
  public String visitFullCN(FullCNContext ctx) {
    return ctx.getText();
  }
  @Override
  public Mdf visitMdf(MdfContext ctx) {
    if(ctx.getText().isEmpty()){ return Mdf.imm; }
    return Mdf.valueOf(ctx.getText());
  }
  public T.IT visitIT(TContext ctx) {
    T t=visitT(ctx,false);
    return t.match(gx->{throw Bug.todo();}, it->it);
  }
  @Override
  public T visitT(TContext ctx) {
    return visitT(ctx,true);
  }
  public T visitT(TContext ctx, boolean canMdf) {
    if(!canMdf && !ctx.mdf().getText().isEmpty()){
      throw Bug.todo();
    }
    Mdf mdf = visitMdf(ctx.mdf());
    String name = visitFullCN(ctx.fullCN());
    var mGen=visitMGen(ctx.mGen());
    var resolved=resolve.apply(name);
    var isIT = name.contains(".") || resolved.isPresent();
    if(!isIT){ return new T(mdf,new T.GX(name)); }
    var ts = mGen.orElse(List.of());
    if(resolved.isEmpty()){return new T(mdf,new T.IT(name,ts));}
    var res = resolved.get();
    ts = Push.of(res.ts(),ts);
    return new T(mdf,res.withTs(ts));
  }
  @Override
  public E.Meth visitSingleM(SingleMContext ctx) {
    check(ctx);
    var _xs = opt(ctx.x(), xs->xs.stream().map(this::visitX).toList());
    _xs = _xs==null?List.of():_xs;
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    return PosMap.add(new E.Meth(Optional.empty(), Optional.empty(), _xs, body), pos(ctx));
  }
  @Override
  public E.Meth visitMeth(MethContext ctx) {
    check(ctx);
    var mh = Optional.ofNullable(ctx.sig()).map(this::visitSig);
    var name = mh.map(MethHeader::name).orElseGet(()->this.visitM(ctx.m()));
    var xs = mh.map(MethHeader::xs).orElseGet(()->{
      var _xs = opt(ctx.x(), xs1->xs1.stream().map(this::visitX).toList());
      return _xs==null?List.of():_xs;
    });
    var body = Optional.ofNullable(ctx.e()).map(this::visitE);
    var sig = mh.map(h->new E.Sig(h.mdf(), h.gens(), h.ret()));
    return PosMap.add(new E.Meth(sig, Optional.of(name), xs, body), pos(ctx));
  }
  private record MethHeader(Mdf mdf, E.MethName name, List<T.GX> gens, List<E.X> xs, T ret){}
  @Override
  public MethHeader visitSig(SigContext ctx) {
    check(ctx);
    var mdf = this.visitMdf(ctx.mdf());
    var name = this.visitM(ctx.m());
    var gens = this.visitMGenParams(ctx.mGen()).orElse(List.of());
    var xs = Optional.ofNullable(ctx.gamma()).map(this::visitGamma).orElse(List.of());
    var ret = this.visitT(ctx.t());
    return new MethHeader(mdf, name, gens, xs, ret);
  }
  @Override
  public List<E.X> visitGamma(GammaContext ctx) {
    return Streams.zip(ctx.x(), ctx.t())
      .map((xCtx, tCtx)->new E.X(xCtx.getText(), this.visitT(tCtx)))
      .toList();
  }
  public T.Dec visitTopDec(TopDecContext ctx, String pkg, boolean shallow) {
    check(ctx);
    String cName = visitFullCN(ctx.fullCN());
    if (cName.contains(".")) {
      throw Bug.of("You may not declare a trait in a different package than the package the declaration is in.");
    }
    cName = pkg + "." + cName;

    var mGen = Optional.ofNullable(ctx.mGen())
      .flatMap(this::visitMGenParams)
      .orElse(List.of());
    var body = shallow ? null : visitBlock(ctx.block());
    return PosMap.add(new T.Dec(cName, mGen, body),pos(ctx));
  }
  @Override
  public T.Alias visitAlias(AliasContext ctx) {
    check(ctx);
    var in = visitFullCN(ctx.fullCN(0));
    var _inG = opt(ctx.mGen(0),this::visitMGen);
    var inG = Optional.ofNullable(_inG).flatMap(e->e).orElse(List.of());
    var inT=new T.IT(in,inG);
    var out = visitFullCN(ctx.fullCN(1));
    var outG = ctx.mGen(1);
    if(!outG.t().isEmpty()){ throw Bug.of("No gen on out Alias"); }
    return PosMap.add(new T.Alias(inT, out), pos(ctx));
  }
  @Override
  public Package visitNudeProgram(NudeProgramContext ctx) {
    String name = ctx.Pack().toString();
    assert name.startsWith("package ");
    assert name.endsWith("\n");
    name=name.substring("package ".length(),name.length()-1);
    var as=ctx.alias().stream().map(this::visitAlias).toList();
    var decs=List.copyOf(ctx.topDec());
    return new Package(name,as,decs,decs.stream().map(e->fileName).toList());
  } 
}