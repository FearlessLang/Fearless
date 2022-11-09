// Generated from java-escape by ANTLR 4.11.1
package generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link FearlessParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface FearlessVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link FearlessParser#fullCN}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullCN(FearlessParser.FullCNContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#x}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitX(FearlessParser.XContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#m}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitM(FearlessParser.MContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#mdf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMdf(FearlessParser.MdfContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#roundE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoundE(FearlessParser.RoundEContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#mGen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMGen(FearlessParser.MGenContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#lambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(FearlessParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(FearlessParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#bblock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBblock(FearlessParser.BblockContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#t}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitT(FearlessParser.TContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#singleM}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleM(FearlessParser.SingleMContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#meth}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeth(FearlessParser.MethContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#sig}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSig(FearlessParser.SigContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#gamma}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGamma(FearlessParser.GammaContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#topDec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopDec(FearlessParser.TopDecContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(FearlessParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#atomE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomE(FearlessParser.AtomEContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#postE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostE(FearlessParser.PostEContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#pOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPOp(FearlessParser.POpContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#e}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitE(FearlessParser.EContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#nudeE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNudeE(FearlessParser.NudeEContext ctx);
	/**
	 * Visit a parse tree produced by {@link FearlessParser#nudeProgram}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNudeProgram(FearlessParser.NudeProgramContext ctx);
}