// Generated from /Users/nick/Programming/PhD/fearless/grammar/antlrGrammars/Fearless.g4 by ANTLR 4.12.0
package generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class FearlessLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Mut=1, Lent=2, Read=3, ReadOnly=4, Iso=5, RecMdf=6, Mdf=7, Imm=8, Eq=9, 
		Alias=10, As=11, OC=12, CC=13, OS=14, CS=15, OR=16, CR=17, Comma=18, Colon=19, 
		Arrow=20, Underscore=21, X=22, SelfX=23, MName=24, BlockComment=25, LineComment=26, 
		SysInM=27, FullCN=28, Whitespace=29, Pack=30;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"Mut", "Lent", "Read", "ReadOnly", "Iso", "RecMdf", "Mdf", "Imm", "Eq", 
			"Alias", "As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "IdUp", "IdLow", "IdChar", "CHAR", "CHARInStringSingle", 
			"CHARInStringMulti", "StringMultiOpen", "StringMultiClose", "StringMultiLine", 
			"FStringMulti", "FStringSingle", "FNumber", "FIdLow", "FIdUp", "X", "SelfX", 
			"MName", "BlockComment", "LineComment", "SyInM", "SyInMExtra", "SysInM", 
			"PX", "FullCN", "Whitespace", "Pack"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'mut'", "'lent'", "'read'", "'readOnly'", "'iso'", "'recMdf'", 
			"'mdf'", "'imm'", "'='", "'alias'", "'as'", "'{'", "'}'", "'['", "']'", 
			"'('", "')'", "','", "':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Mut", "Lent", "Read", "ReadOnly", "Iso", "RecMdf", "Mdf", "Imm", 
			"Eq", "Alias", "As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "X", "SelfX", "MName", "BlockComment", "LineComment", 
			"SysInM", "FullCN", "Whitespace", "Pack"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public FearlessLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Fearless.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u001e\u017a\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a"+
		"\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d"+
		"\u0002\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!"+
		"\u0007!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002"+
		"&\u0007&\u0002\'\u0007\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002"+
		"+\u0007+\u0002,\u0007,\u0002-\u0007-\u0002.\u0007.\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001"+
		"\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0015\u0005\u0015\u00ab\b\u0015\n\u0015\f\u0015\u00ae"+
		"\t\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0005\u0016\u00b3\b\u0016"+
		"\n\u0016\f\u0016\u00b6\t\u0016\u0001\u0016\u0001\u0016\u0004\u0016\u00ba"+
		"\b\u0016\u000b\u0016\f\u0016\u00bb\u0001\u0016\u0003\u0016\u00bf\b\u0016"+
		"\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019"+
		"\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001c\u0005\u001c\u00d0\b\u001c\n\u001c"+
		"\f\u001c\u00d3\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001d\u0005\u001d\u00da\b\u001d\n\u001d\f\u001d\u00dd\t\u001d\u0001"+
		"\u001d\u0001\u001d\u0005\u001d\u00e1\b\u001d\n\u001d\f\u001d\u00e4\t\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0004\u001e\u00ea\b\u001e"+
		"\u000b\u001e\f\u001e\u00eb\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f"+
		"\u0005\u001f\u00f2\b\u001f\n\u001f\f\u001f\u00f5\t\u001f\u0001\u001f\u0001"+
		"\u001f\u0001 \u0003 \u00fa\b \u0001 \u0001 \u0005 \u00fe\b \n \f \u0101"+
		"\t \u0001!\u0001!\u0005!\u0105\b!\n!\f!\u0108\t!\u0001!\u0005!\u010b\b"+
		"!\n!\f!\u010e\t!\u0001\"\u0001\"\u0005\"\u0112\b\"\n\"\f\"\u0115\t\"\u0001"+
		"\"\u0005\"\u0118\b\"\n\"\f\"\u011b\t\"\u0001#\u0001#\u0001$\u0001$\u0001"+
		"$\u0001%\u0001%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001&\u0005&\u012a"+
		"\b&\n&\f&\u012d\t&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001\'"+
		"\u0001\'\u0001\'\u0005\'\u0138\b\'\n\'\f\'\u013b\t\'\u0001\'\u0003\'\u013e"+
		"\b\'\u0001\'\u0001\'\u0001(\u0001(\u0001)\u0001)\u0001*\u0005*\u0147\b"+
		"*\n*\f*\u014a\t*\u0001*\u0001*\u0001+\u0001+\u0005+\u0150\b+\n+\f+\u0153"+
		"\t+\u0001,\u0001,\u0001,\u0005,\u0158\b,\n,\f,\u015b\t,\u0001,\u0001,"+
		"\u0001,\u0001,\u0003,\u0161\b,\u0001-\u0001-\u0001-\u0001-\u0001.\u0001"+
		".\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001"+
		".\u0005.\u0173\b.\n.\f.\u0176\t.\u0001.\u0001.\u0001.\u0002\u012b\u0139"+
		"\u0000/\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b"+
		"\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b"+
		"\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0000"+
		"-\u0000/\u00001\u00003\u00005\u00007\u00009\u0000;\u0000=\u0000?\u0000"+
		"A\u0000C\u0000E\u0000G\u0016I\u0017K\u0018M\u0019O\u001aQ\u0000S\u0000"+
		"U\u001bW\u0000Y\u001c[\u001d]\u001e\u0001\u0000\b\u0004\u000009AZ__az"+
		"\u0002\u0000\n\n ~\u0002\u0000 !#~\u0004\u0000..09__uu\u0001\u0001\n\n"+
		"\n\u0000!!#&*+--//<@\\\\^^||~~\u000b\u0000!!#&*+--//::<@\\\\^^||~~\u0002"+
		"\u0000\t\n  \u0181\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001"+
		"\u0000\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001"+
		"\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000"+
		"\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000"+
		"\u0000\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000"+
		"\u0000\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000"+
		"\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000"+
		"\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000"+
		"\u0000\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000"+
		"%\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001"+
		"\u0000\u0000\u0000\u0000G\u0001\u0000\u0000\u0000\u0000I\u0001\u0000\u0000"+
		"\u0000\u0000K\u0001\u0000\u0000\u0000\u0000M\u0001\u0000\u0000\u0000\u0000"+
		"O\u0001\u0000\u0000\u0000\u0000U\u0001\u0000\u0000\u0000\u0000Y\u0001"+
		"\u0000\u0000\u0000\u0000[\u0001\u0000\u0000\u0000\u0000]\u0001\u0000\u0000"+
		"\u0000\u0001_\u0001\u0000\u0000\u0000\u0003c\u0001\u0000\u0000\u0000\u0005"+
		"h\u0001\u0000\u0000\u0000\u0007m\u0001\u0000\u0000\u0000\tv\u0001\u0000"+
		"\u0000\u0000\u000bz\u0001\u0000\u0000\u0000\r\u0081\u0001\u0000\u0000"+
		"\u0000\u000f\u0085\u0001\u0000\u0000\u0000\u0011\u0089\u0001\u0000\u0000"+
		"\u0000\u0013\u008b\u0001\u0000\u0000\u0000\u0015\u0091\u0001\u0000\u0000"+
		"\u0000\u0017\u0094\u0001\u0000\u0000\u0000\u0019\u0096\u0001\u0000\u0000"+
		"\u0000\u001b\u0098\u0001\u0000\u0000\u0000\u001d\u009a\u0001\u0000\u0000"+
		"\u0000\u001f\u009c\u0001\u0000\u0000\u0000!\u009e\u0001\u0000\u0000\u0000"+
		"#\u00a0\u0001\u0000\u0000\u0000%\u00a2\u0001\u0000\u0000\u0000\'\u00a4"+
		"\u0001\u0000\u0000\u0000)\u00a7\u0001\u0000\u0000\u0000+\u00ac\u0001\u0000"+
		"\u0000\u0000-\u00be\u0001\u0000\u0000\u0000/\u00c0\u0001\u0000\u0000\u0000"+
		"1\u00c2\u0001\u0000\u0000\u00003\u00c4\u0001\u0000\u0000\u00005\u00c6"+
		"\u0001\u0000\u0000\u00007\u00c8\u0001\u0000\u0000\u00009\u00d1\u0001\u0000"+
		"\u0000\u0000;\u00db\u0001\u0000\u0000\u0000=\u00e7\u0001\u0000\u0000\u0000"+
		"?\u00ef\u0001\u0000\u0000\u0000A\u00f9\u0001\u0000\u0000\u0000C\u0102"+
		"\u0001\u0000\u0000\u0000E\u010f\u0001\u0000\u0000\u0000G\u011c\u0001\u0000"+
		"\u0000\u0000I\u011e\u0001\u0000\u0000\u0000K\u0121\u0001\u0000\u0000\u0000"+
		"M\u0124\u0001\u0000\u0000\u0000O\u0133\u0001\u0000\u0000\u0000Q\u0141"+
		"\u0001\u0000\u0000\u0000S\u0143\u0001\u0000\u0000\u0000U\u0148\u0001\u0000"+
		"\u0000\u0000W\u014d\u0001\u0000\u0000\u0000Y\u0160\u0001\u0000\u0000\u0000"+
		"[\u0162\u0001\u0000\u0000\u0000]\u0166\u0001\u0000\u0000\u0000_`\u0005"+
		"m\u0000\u0000`a\u0005u\u0000\u0000ab\u0005t\u0000\u0000b\u0002\u0001\u0000"+
		"\u0000\u0000cd\u0005l\u0000\u0000de\u0005e\u0000\u0000ef\u0005n\u0000"+
		"\u0000fg\u0005t\u0000\u0000g\u0004\u0001\u0000\u0000\u0000hi\u0005r\u0000"+
		"\u0000ij\u0005e\u0000\u0000jk\u0005a\u0000\u0000kl\u0005d\u0000\u0000"+
		"l\u0006\u0001\u0000\u0000\u0000mn\u0005r\u0000\u0000no\u0005e\u0000\u0000"+
		"op\u0005a\u0000\u0000pq\u0005d\u0000\u0000qr\u0005O\u0000\u0000rs\u0005"+
		"n\u0000\u0000st\u0005l\u0000\u0000tu\u0005y\u0000\u0000u\b\u0001\u0000"+
		"\u0000\u0000vw\u0005i\u0000\u0000wx\u0005s\u0000\u0000xy\u0005o\u0000"+
		"\u0000y\n\u0001\u0000\u0000\u0000z{\u0005r\u0000\u0000{|\u0005e\u0000"+
		"\u0000|}\u0005c\u0000\u0000}~\u0005M\u0000\u0000~\u007f\u0005d\u0000\u0000"+
		"\u007f\u0080\u0005f\u0000\u0000\u0080\f\u0001\u0000\u0000\u0000\u0081"+
		"\u0082\u0005m\u0000\u0000\u0082\u0083\u0005d\u0000\u0000\u0083\u0084\u0005"+
		"f\u0000\u0000\u0084\u000e\u0001\u0000\u0000\u0000\u0085\u0086\u0005i\u0000"+
		"\u0000\u0086\u0087\u0005m\u0000\u0000\u0087\u0088\u0005m\u0000\u0000\u0088"+
		"\u0010\u0001\u0000\u0000\u0000\u0089\u008a\u0005=\u0000\u0000\u008a\u0012"+
		"\u0001\u0000\u0000\u0000\u008b\u008c\u0005a\u0000\u0000\u008c\u008d\u0005"+
		"l\u0000\u0000\u008d\u008e\u0005i\u0000\u0000\u008e\u008f\u0005a\u0000"+
		"\u0000\u008f\u0090\u0005s\u0000\u0000\u0090\u0014\u0001\u0000\u0000\u0000"+
		"\u0091\u0092\u0005a\u0000\u0000\u0092\u0093\u0005s\u0000\u0000\u0093\u0016"+
		"\u0001\u0000\u0000\u0000\u0094\u0095\u0005{\u0000\u0000\u0095\u0018\u0001"+
		"\u0000\u0000\u0000\u0096\u0097\u0005}\u0000\u0000\u0097\u001a\u0001\u0000"+
		"\u0000\u0000\u0098\u0099\u0005[\u0000\u0000\u0099\u001c\u0001\u0000\u0000"+
		"\u0000\u009a\u009b\u0005]\u0000\u0000\u009b\u001e\u0001\u0000\u0000\u0000"+
		"\u009c\u009d\u0005(\u0000\u0000\u009d \u0001\u0000\u0000\u0000\u009e\u009f"+
		"\u0005)\u0000\u0000\u009f\"\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005"+
		",\u0000\u0000\u00a1$\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005:\u0000"+
		"\u0000\u00a3&\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005-\u0000\u0000\u00a5"+
		"\u00a6\u0005>\u0000\u0000\u00a6(\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005"+
		"_\u0000\u0000\u00a8*\u0001\u0000\u0000\u0000\u00a9\u00ab\u0005_\u0000"+
		"\u0000\u00aa\u00a9\u0001\u0000\u0000\u0000\u00ab\u00ae\u0001\u0000\u0000"+
		"\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000"+
		"\u0000\u00ad\u00af\u0001\u0000\u0000\u0000\u00ae\u00ac\u0001\u0000\u0000"+
		"\u0000\u00af\u00b0\u0002AZ\u0000\u00b0,\u0001\u0000\u0000\u0000\u00b1"+
		"\u00b3\u0005_\u0000\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b4\u00b5"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b7\u0001\u0000\u0000\u0000\u00b6\u00b4"+
		"\u0001\u0000\u0000\u0000\u00b7\u00bf\u0002az\u0000\u00b8\u00ba\u0005_"+
		"\u0000\u0000\u00b9\u00b8\u0001\u0000\u0000\u0000\u00ba\u00bb\u0001\u0000"+
		"\u0000\u0000\u00bb\u00b9\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u00bf\u000209\u0000"+
		"\u00be\u00b4\u0001\u0000\u0000\u0000\u00be\u00b9\u0001\u0000\u0000\u0000"+
		"\u00bf.\u0001\u0000\u0000\u0000\u00c0\u00c1\u0007\u0000\u0000\u0000\u00c1"+
		"0\u0001\u0000\u0000\u0000\u00c2\u00c3\u0007\u0001\u0000\u0000\u00c32\u0001"+
		"\u0000\u0000\u0000\u00c4\u00c5\u0007\u0002\u0000\u0000\u00c54\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c7\u0002 ~\u0000\u00c76\u0001\u0000\u0000\u0000"+
		"\u00c8\u00c9\u0005\"\u0000\u0000\u00c9\u00ca\u0005\"\u0000\u0000\u00ca"+
		"\u00cb\u0005\"\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc\u00cd"+
		"\u0005\n\u0000\u0000\u00cd8\u0001\u0000\u0000\u0000\u00ce\u00d0\u0005"+
		" \u0000\u0000\u00cf\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d3\u0001\u0000"+
		"\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d4\u0001\u0000\u0000\u0000\u00d3\u00d1\u0001\u0000"+
		"\u0000\u0000\u00d4\u00d5\u0005\"\u0000\u0000\u00d5\u00d6\u0005\"\u0000"+
		"\u0000\u00d6\u00d7\u0005\"\u0000\u0000\u00d7:\u0001\u0000\u0000\u0000"+
		"\u00d8\u00da\u0005 \u0000\u0000\u00d9\u00d8\u0001\u0000\u0000\u0000\u00da"+
		"\u00dd\u0001\u0000\u0000\u0000\u00db\u00d9\u0001\u0000\u0000\u0000\u00db"+
		"\u00dc\u0001\u0000\u0000\u0000\u00dc\u00de\u0001\u0000\u0000\u0000\u00dd"+
		"\u00db\u0001\u0000\u0000\u0000\u00de\u00e2\u0005|\u0000\u0000\u00df\u00e1"+
		"\u00035\u001a\u0000\u00e0\u00df\u0001\u0000\u0000\u0000\u00e1\u00e4\u0001"+
		"\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001"+
		"\u0000\u0000\u0000\u00e3\u00e5\u0001\u0000\u0000\u0000\u00e4\u00e2\u0001"+
		"\u0000\u0000\u0000\u00e5\u00e6\u0005\n\u0000\u0000\u00e6<\u0001\u0000"+
		"\u0000\u0000\u00e7\u00e9\u00037\u001b\u0000\u00e8\u00ea\u0003;\u001d\u0000"+
		"\u00e9\u00e8\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000"+
		"\u00eb\u00e9\u0001\u0000\u0000\u0000\u00eb\u00ec\u0001\u0000\u0000\u0000"+
		"\u00ec\u00ed\u0001\u0000\u0000\u0000\u00ed\u00ee\u00039\u001c\u0000\u00ee"+
		">\u0001\u0000\u0000\u0000\u00ef\u00f3\u0005\"\u0000\u0000\u00f0\u00f2"+
		"\u00033\u0019\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001"+
		"\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001"+
		"\u0000\u0000\u0000\u00f4\u00f6\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f7\u0005\"\u0000\u0000\u00f7@\u0001\u0000"+
		"\u0000\u0000\u00f8\u00fa\u0005-\u0000\u0000\u00f9\u00f8\u0001\u0000\u0000"+
		"\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa\u00fb\u0001\u0000\u0000"+
		"\u0000\u00fb\u00ff\u000209\u0000\u00fc\u00fe\u0007\u0003\u0000\u0000\u00fd"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fe\u0101\u0001\u0000\u0000\u0000\u00ff"+
		"\u00fd\u0001\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100"+
		"B\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0102\u0106"+
		"\u0003-\u0016\u0000\u0103\u0105\u0003/\u0017\u0000\u0104\u0103\u0001\u0000"+
		"\u0000\u0000\u0105\u0108\u0001\u0000\u0000\u0000\u0106\u0104\u0001\u0000"+
		"\u0000\u0000\u0106\u0107\u0001\u0000\u0000\u0000\u0107\u010c\u0001\u0000"+
		"\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0109\u010b\u0005\'\u0000"+
		"\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010b\u010e\u0001\u0000\u0000"+
		"\u0000\u010c\u010a\u0001\u0000\u0000\u0000\u010c\u010d\u0001\u0000\u0000"+
		"\u0000\u010dD\u0001\u0000\u0000\u0000\u010e\u010c\u0001\u0000\u0000\u0000"+
		"\u010f\u0113\u0003+\u0015\u0000\u0110\u0112\u0003/\u0017\u0000\u0111\u0110"+
		"\u0001\u0000\u0000\u0000\u0112\u0115\u0001\u0000\u0000\u0000\u0113\u0111"+
		"\u0001\u0000\u0000\u0000\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0119"+
		"\u0001\u0000\u0000\u0000\u0115\u0113\u0001\u0000\u0000\u0000\u0116\u0118"+
		"\u0005\'\u0000\u0000\u0117\u0116\u0001\u0000\u0000\u0000\u0118\u011b\u0001"+
		"\u0000\u0000\u0000\u0119\u0117\u0001\u0000\u0000\u0000\u0119\u011a\u0001"+
		"\u0000\u0000\u0000\u011aF\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000"+
		"\u0000\u0000\u011c\u011d\u0003C!\u0000\u011dH\u0001\u0000\u0000\u0000"+
		"\u011e\u011f\u0005\'\u0000\u0000\u011f\u0120\u0003C!\u0000\u0120J\u0001"+
		"\u0000\u0000\u0000\u0121\u0122\u0005.\u0000\u0000\u0122\u0123\u0003C!"+
		"\u0000\u0123L\u0001\u0000\u0000\u0000\u0124\u0125\u0005/\u0000\u0000\u0125"+
		"\u0126\u0005*\u0000\u0000\u0126\u012b\u0001\u0000\u0000\u0000\u0127\u012a"+
		"\u0003M&\u0000\u0128\u012a\t\u0000\u0000\u0000\u0129\u0127\u0001\u0000"+
		"\u0000\u0000\u0129\u0128\u0001\u0000\u0000\u0000\u012a\u012d\u0001\u0000"+
		"\u0000\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012b\u0129\u0001\u0000"+
		"\u0000\u0000\u012c\u012e\u0001\u0000\u0000\u0000\u012d\u012b\u0001\u0000"+
		"\u0000\u0000\u012e\u012f\u0005*\u0000\u0000\u012f\u0130\u0005/\u0000\u0000"+
		"\u0130\u0131\u0001\u0000\u0000\u0000\u0131\u0132\u0006&\u0000\u0000\u0132"+
		"N\u0001\u0000\u0000\u0000\u0133\u0134\u0005/\u0000\u0000\u0134\u0135\u0005"+
		"/\u0000\u0000\u0135\u0139\u0001\u0000\u0000\u0000\u0136\u0138\t\u0000"+
		"\u0000\u0000\u0137\u0136\u0001\u0000\u0000\u0000\u0138\u013b\u0001\u0000"+
		"\u0000\u0000\u0139\u013a\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000"+
		"\u0000\u0000\u013a\u013d\u0001\u0000\u0000\u0000\u013b\u0139\u0001\u0000"+
		"\u0000\u0000\u013c\u013e\u0007\u0004\u0000\u0000\u013d\u013c\u0001\u0000"+
		"\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0140\u0006\'\u0000"+
		"\u0000\u0140P\u0001\u0000\u0000\u0000\u0141\u0142\u0007\u0005\u0000\u0000"+
		"\u0142R\u0001\u0000\u0000\u0000\u0143\u0144\u0007\u0006\u0000\u0000\u0144"+
		"T\u0001\u0000\u0000\u0000\u0145\u0147\u0003S)\u0000\u0146\u0145\u0001"+
		"\u0000\u0000\u0000\u0147\u014a\u0001\u0000\u0000\u0000\u0148\u0146\u0001"+
		"\u0000\u0000\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u014b\u0001"+
		"\u0000\u0000\u0000\u014a\u0148\u0001\u0000\u0000\u0000\u014b\u014c\u0003"+
		"Q(\u0000\u014cV\u0001\u0000\u0000\u0000\u014d\u0151\u0003-\u0016\u0000"+
		"\u014e\u0150\u0003/\u0017\u0000\u014f\u014e\u0001\u0000\u0000\u0000\u0150"+
		"\u0153\u0001\u0000\u0000\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0151"+
		"\u0152\u0001\u0000\u0000\u0000\u0152X\u0001\u0000\u0000\u0000\u0153\u0151"+
		"\u0001\u0000\u0000\u0000\u0154\u0155\u0003W+\u0000\u0155\u0156\u0005."+
		"\u0000\u0000\u0156\u0158\u0001\u0000\u0000\u0000\u0157\u0154\u0001\u0000"+
		"\u0000\u0000\u0158\u015b\u0001\u0000\u0000\u0000\u0159\u0157\u0001\u0000"+
		"\u0000\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a\u015c\u0001\u0000"+
		"\u0000\u0000\u015b\u0159\u0001\u0000\u0000\u0000\u015c\u0161\u0003E\""+
		"\u0000\u015d\u0161\u0003=\u001e\u0000\u015e\u0161\u0003?\u001f\u0000\u015f"+
		"\u0161\u0003A \u0000\u0160\u0159\u0001\u0000\u0000\u0000\u0160\u015d\u0001"+
		"\u0000\u0000\u0000\u0160\u015e\u0001\u0000\u0000\u0000\u0160\u015f\u0001"+
		"\u0000\u0000\u0000\u0161Z\u0001\u0000\u0000\u0000\u0162\u0163\u0007\u0007"+
		"\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000\u0164\u0165\u0006-\u0000"+
		"\u0000\u0165\\\u0001\u0000\u0000\u0000\u0166\u0167\u0005p\u0000\u0000"+
		"\u0167\u0168\u0005a\u0000\u0000\u0168\u0169\u0005c\u0000\u0000\u0169\u016a"+
		"\u0005k\u0000\u0000\u016a\u016b\u0005a\u0000\u0000\u016b\u016c\u0005g"+
		"\u0000\u0000\u016c\u016d\u0005e\u0000\u0000\u016d\u016e\u0005 \u0000\u0000"+
		"\u016e\u0174\u0001\u0000\u0000\u0000\u016f\u0170\u0003W+\u0000\u0170\u0171"+
		"\u0005.\u0000\u0000\u0171\u0173\u0001\u0000\u0000\u0000\u0172\u016f\u0001"+
		"\u0000\u0000\u0000\u0173\u0176\u0001\u0000\u0000\u0000\u0174\u0172\u0001"+
		"\u0000\u0000\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175\u0177\u0001"+
		"\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177\u0178\u0003"+
		"W+\u0000\u0178\u0179\u0005\n\u0000\u0000\u0179^\u0001\u0000\u0000\u0000"+
		"\u0019\u0000\u00ac\u00b4\u00bb\u00be\u00d1\u00db\u00e2\u00eb\u00f3\u00f9"+
		"\u00ff\u0106\u010c\u0113\u0119\u0129\u012b\u0139\u013d\u0148\u0151\u0159"+
		"\u0160\u0174\u0001\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}