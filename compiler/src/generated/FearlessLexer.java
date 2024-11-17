// Generated from C:\Users\sonta\Documents\GitHub\Fearless\grammar\antlrGrammars\Fearless.g4 by ANTLR 4.12.0
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
		T__0=1, Mut=2, MutH=3, Read=4, ReadImm=5, ReadH=6, Iso=7, Imm=8, Eq=9, 
		Alias=10, As=11, OC=12, CC=13, OS=14, CS=15, OR=16, CR=17, Comma=18, Colon=19, 
		Arrow=20, Underscore=21, X=22, SelfX=23, MName=24, CCMName=25, FStringMulti=26, 
		BlockComment=27, LineComment=28, SysInM=29, FullCN=30, Whitespace=31, 
		Pack=32;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "Mut", "MutH", "Read", "ReadImm", "ReadH", "Iso", "Imm", "Eq", 
			"Alias", "As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "CHAR", "CHARInStringSingle", "CHARInStringDouble", 
			"CHARInStringMulti", "StringMultiLine", "FStringSingle", "FStringDouble", 
			"Unders", "NumSym", "NumStart", "UpStart", "LowStart", "Start", "IdUnit", 
			"TypeName", "FIdLow", "X", "SelfX", "MName", "CCMName", "FStringMulti", 
			"BlockComment", "LineComment", "SyInM", "SyInMExtra", "SysInM", "FullCN", 
			"Whitespace", "Pack"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'::'", "'mut'", "'mutH'", "'read'", "'read/imm'", "'readH'", "'iso'", 
			"'imm'", "'='", "'alias'", "'as'", "'{'", "'}'", "'['", "']'", "'('", 
			"')'", "','", "':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "Mut", "MutH", "Read", "ReadImm", "ReadH", "Iso", "Imm", 
			"Eq", "Alias", "As", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "X", "SelfX", "MName", "CCMName", "FStringMulti", 
			"BlockComment", "LineComment", "SysInM", "FullCN", "Whitespace", "Pack"
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
		"\u0004\u0000 \u0184\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
		"\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002"+
		"\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002"+
		"\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007"+
		"!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007"+
		"&\u0002\'\u0007\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007"+
		"+\u0002,\u0007,\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u0007"+
		"0\u00021\u00071\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u00b4\b\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00ba\b\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0019\u0004\u0019\u00bf\b\u0019\u000b\u0019\f\u0019\u00c0"+
		"\u0001\u0019\u0001\u0019\u0005\u0019\u00c5\b\u0019\n\u0019\f\u0019\u00c8"+
		"\t\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a\u0005\u001a\u00ce"+
		"\b\u001a\n\u001a\f\u001a\u00d1\t\u001a\u0001\u001a\u0001\u001a\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u00d7\b\u001b\n\u001b\f\u001b\u00da\t\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001c\u0005\u001c\u00df\b\u001c\n\u001c\f\u001c"+
		"\u00e2\t\u001c\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u00e8\b\u001e\n\u001e\f\u001e\u00eb\t\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0005\u001f\u00f1\b\u001f\n\u001f\f\u001f\u00f4\t\u001f"+
		"\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0001"+
		"!\u0003!\u00ff\b!\u0001\"\u0001\"\u0003\"\u0103\b\"\u0001#\u0001#\u0005"+
		"#\u0107\b#\n#\f#\u010a\t#\u0001#\u0005#\u010d\b#\n#\f#\u0110\t#\u0001"+
		"$\u0001$\u0005$\u0114\b$\n$\f$\u0117\t$\u0001%\u0001%\u0005%\u011b\b%"+
		"\n%\f%\u011e\t%\u0001&\u0001&\u0001&\u0001\'\u0001\'\u0001\'\u0005\'\u0126"+
		"\b\'\n\'\f\'\u0129\t\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0005(\u0130"+
		"\b(\n(\f(\u0133\t(\u0001)\u0004)\u0136\b)\u000b)\f)\u0137\u0001*\u0001"+
		"*\u0001*\u0001*\u0001*\u0005*\u013f\b*\n*\f*\u0142\t*\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0001+\u0001+\u0001+\u0001+\u0005+\u014d\b+\n+\f+\u0150"+
		"\t+\u0001+\u0003+\u0153\b+\u0001+\u0001+\u0001,\u0001,\u0001-\u0001-\u0001"+
		".\u0005.\u015c\b.\n.\f.\u015f\t.\u0001.\u0001.\u0001/\u0001/\u0001/\u0005"+
		"/\u0166\b/\n/\f/\u0169\t/\u0001/\u0001/\u00010\u00010\u00010\u00010\u0001"+
		"1\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u0001"+
		"1\u00011\u00051\u017d\b1\n1\f1\u0180\t1\u00011\u00011\u00011\u0002\u0140"+
		"\u014e\u00002\u0001\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005"+
		"\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019"+
		"\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015"+
		"+\u0000-\u0000/\u00001\u00003\u00005\u00007\u00009\u0000;\u0000=\u0000"+
		"?\u0000A\u0000C\u0000E\u0000G\u0000I\u0000K\u0016M\u0017O\u0018Q\u0019"+
		"S\u001aU\u001bW\u001cY\u0000[\u0000]\u001d_\u001ea\u001fc \u0001\u0000"+
		"\u000b\u0002\u0000\n\n ~\u0007\u0000 !#&([]]az||~~\u0006\u0000\"\"\'\'"+
		"\\\\^_{{}}\u0003\u0000 !#&(~\u0002\u0000\"\"``\u0002\u0000++-/\u0004\u0000"+
		"09AZ__az\u0001\u0001\n\n\n\u0000!!#&*+--//<@\\\\^^||~~\u000b\u0000!!#"+
		"&*+--//::<@\\\\^^||~~\u0002\u0000\t\n  \u018d\u0000\u0001\u0001\u0000"+
		"\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000"+
		"\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000"+
		"\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000"+
		"\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000"+
		"\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000"+
		"\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000"+
		"\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000"+
		"\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000"+
		"#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001"+
		"\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000K\u0001\u0000\u0000"+
		"\u0000\u0000M\u0001\u0000\u0000\u0000\u0000O\u0001\u0000\u0000\u0000\u0000"+
		"Q\u0001\u0000\u0000\u0000\u0000S\u0001\u0000\u0000\u0000\u0000U\u0001"+
		"\u0000\u0000\u0000\u0000W\u0001\u0000\u0000\u0000\u0000]\u0001\u0000\u0000"+
		"\u0000\u0000_\u0001\u0000\u0000\u0000\u0000a\u0001\u0000\u0000\u0000\u0000"+
		"c\u0001\u0000\u0000\u0000\u0001e\u0001\u0000\u0000\u0000\u0003h\u0001"+
		"\u0000\u0000\u0000\u0005l\u0001\u0000\u0000\u0000\u0007q\u0001\u0000\u0000"+
		"\u0000\tv\u0001\u0000\u0000\u0000\u000b\u007f\u0001\u0000\u0000\u0000"+
		"\r\u0085\u0001\u0000\u0000\u0000\u000f\u0089\u0001\u0000\u0000\u0000\u0011"+
		"\u008d\u0001\u0000\u0000\u0000\u0013\u008f\u0001\u0000\u0000\u0000\u0015"+
		"\u0095\u0001\u0000\u0000\u0000\u0017\u0098\u0001\u0000\u0000\u0000\u0019"+
		"\u009a\u0001\u0000\u0000\u0000\u001b\u009c\u0001\u0000\u0000\u0000\u001d"+
		"\u009e\u0001\u0000\u0000\u0000\u001f\u00a0\u0001\u0000\u0000\u0000!\u00a2"+
		"\u0001\u0000\u0000\u0000#\u00a4\u0001\u0000\u0000\u0000%\u00a6\u0001\u0000"+
		"\u0000\u0000\'\u00a8\u0001\u0000\u0000\u0000)\u00ab\u0001\u0000\u0000"+
		"\u0000+\u00ad\u0001\u0000\u0000\u0000-\u00b3\u0001\u0000\u0000\u0000/"+
		"\u00b9\u0001\u0000\u0000\u00001\u00bb\u0001\u0000\u0000\u00003\u00be\u0001"+
		"\u0000\u0000\u00005\u00cb\u0001\u0000\u0000\u00007\u00d4\u0001\u0000\u0000"+
		"\u00009\u00e0\u0001\u0000\u0000\u0000;\u00e3\u0001\u0000\u0000\u0000="+
		"\u00e5\u0001\u0000\u0000\u0000?\u00ee\u0001\u0000\u0000\u0000A\u00f7\u0001"+
		"\u0000\u0000\u0000C\u00fe\u0001\u0000\u0000\u0000E\u0102\u0001\u0000\u0000"+
		"\u0000G\u0104\u0001\u0000\u0000\u0000I\u0111\u0001\u0000\u0000\u0000K"+
		"\u0118\u0001\u0000\u0000\u0000M\u011f\u0001\u0000\u0000\u0000O\u0122\u0001"+
		"\u0000\u0000\u0000Q\u012a\u0001\u0000\u0000\u0000S\u0135\u0001\u0000\u0000"+
		"\u0000U\u0139\u0001\u0000\u0000\u0000W\u0148\u0001\u0000\u0000\u0000Y"+
		"\u0156\u0001\u0000\u0000\u0000[\u0158\u0001\u0000\u0000\u0000]\u015d\u0001"+
		"\u0000\u0000\u0000_\u0167\u0001\u0000\u0000\u0000a\u016c\u0001\u0000\u0000"+
		"\u0000c\u0170\u0001\u0000\u0000\u0000ef\u0005:\u0000\u0000fg\u0005:\u0000"+
		"\u0000g\u0002\u0001\u0000\u0000\u0000hi\u0005m\u0000\u0000ij\u0005u\u0000"+
		"\u0000jk\u0005t\u0000\u0000k\u0004\u0001\u0000\u0000\u0000lm\u0005m\u0000"+
		"\u0000mn\u0005u\u0000\u0000no\u0005t\u0000\u0000op\u0005H\u0000\u0000"+
		"p\u0006\u0001\u0000\u0000\u0000qr\u0005r\u0000\u0000rs\u0005e\u0000\u0000"+
		"st\u0005a\u0000\u0000tu\u0005d\u0000\u0000u\b\u0001\u0000\u0000\u0000"+
		"vw\u0005r\u0000\u0000wx\u0005e\u0000\u0000xy\u0005a\u0000\u0000yz\u0005"+
		"d\u0000\u0000z{\u0005/\u0000\u0000{|\u0005i\u0000\u0000|}\u0005m\u0000"+
		"\u0000}~\u0005m\u0000\u0000~\n\u0001\u0000\u0000\u0000\u007f\u0080\u0005"+
		"r\u0000\u0000\u0080\u0081\u0005e\u0000\u0000\u0081\u0082\u0005a\u0000"+
		"\u0000\u0082\u0083\u0005d\u0000\u0000\u0083\u0084\u0005H\u0000\u0000\u0084"+
		"\f\u0001\u0000\u0000\u0000\u0085\u0086\u0005i\u0000\u0000\u0086\u0087"+
		"\u0005s\u0000\u0000\u0087\u0088\u0005o\u0000\u0000\u0088\u000e\u0001\u0000"+
		"\u0000\u0000\u0089\u008a\u0005i\u0000\u0000\u008a\u008b\u0005m\u0000\u0000"+
		"\u008b\u008c\u0005m\u0000\u0000\u008c\u0010\u0001\u0000\u0000\u0000\u008d"+
		"\u008e\u0005=\u0000\u0000\u008e\u0012\u0001\u0000\u0000\u0000\u008f\u0090"+
		"\u0005a\u0000\u0000\u0090\u0091\u0005l\u0000\u0000\u0091\u0092\u0005i"+
		"\u0000\u0000\u0092\u0093\u0005a\u0000\u0000\u0093\u0094\u0005s\u0000\u0000"+
		"\u0094\u0014\u0001\u0000\u0000\u0000\u0095\u0096\u0005a\u0000\u0000\u0096"+
		"\u0097\u0005s\u0000\u0000\u0097\u0016\u0001\u0000\u0000\u0000\u0098\u0099"+
		"\u0005{\u0000\u0000\u0099\u0018\u0001\u0000\u0000\u0000\u009a\u009b\u0005"+
		"}\u0000\u0000\u009b\u001a\u0001\u0000\u0000\u0000\u009c\u009d\u0005[\u0000"+
		"\u0000\u009d\u001c\u0001\u0000\u0000\u0000\u009e\u009f\u0005]\u0000\u0000"+
		"\u009f\u001e\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005(\u0000\u0000\u00a1"+
		" \u0001\u0000\u0000\u0000\u00a2\u00a3\u0005)\u0000\u0000\u00a3\"\u0001"+
		"\u0000\u0000\u0000\u00a4\u00a5\u0005,\u0000\u0000\u00a5$\u0001\u0000\u0000"+
		"\u0000\u00a6\u00a7\u0005:\u0000\u0000\u00a7&\u0001\u0000\u0000\u0000\u00a8"+
		"\u00a9\u0005-\u0000\u0000\u00a9\u00aa\u0005>\u0000\u0000\u00aa(\u0001"+
		"\u0000\u0000\u0000\u00ab\u00ac\u0005_\u0000\u0000\u00ac*\u0001\u0000\u0000"+
		"\u0000\u00ad\u00ae\u0007\u0000\u0000\u0000\u00ae,\u0001\u0000\u0000\u0000"+
		"\u00af\u00b4\u0007\u0001\u0000\u0000\u00b0\u00b1\u0005\\\u0000\u0000\u00b1"+
		"\u00b4\u0005`\u0000\u0000\u00b2\u00b4\u0007\u0002\u0000\u0000\u00b3\u00af"+
		"\u0001\u0000\u0000\u0000\u00b3\u00b0\u0001\u0000\u0000\u0000\u00b3\u00b2"+
		"\u0001\u0000\u0000\u0000\u00b4.\u0001\u0000\u0000\u0000\u00b5\u00ba\u0007"+
		"\u0003\u0000\u0000\u00b6\u00b7\u0005\\\u0000\u0000\u00b7\u00ba\u0005\""+
		"\u0000\u0000\u00b8\u00ba\u0005\'\u0000\u0000\u00b9\u00b5\u0001\u0000\u0000"+
		"\u0000\u00b9\u00b6\u0001\u0000\u0000\u0000\u00b9\u00b8\u0001\u0000\u0000"+
		"\u0000\u00ba0\u0001\u0000\u0000\u0000\u00bb\u00bc\u0002 ~\u0000\u00bc"+
		"2\u0001\u0000\u0000\u0000\u00bd\u00bf\u0005|\u0000\u0000\u00be\u00bd\u0001"+
		"\u0000\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00be\u0001"+
		"\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c6\u0007\u0004\u0000\u0000\u00c3\u00c5\u0003"+
		"1\u0018\u0000\u00c4\u00c3\u0001\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c9\u0001\u0000\u0000\u0000\u00c8\u00c6\u0001\u0000"+
		"\u0000\u0000\u00c9\u00ca\u0005\n\u0000\u0000\u00ca4\u0001\u0000\u0000"+
		"\u0000\u00cb\u00cf\u0005`\u0000\u0000\u00cc\u00ce\u0003-\u0016\u0000\u00cd"+
		"\u00cc\u0001\u0000\u0000\u0000\u00ce\u00d1\u0001\u0000\u0000\u0000\u00cf"+
		"\u00cd\u0001\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0"+
		"\u00d2\u0001\u0000\u0000\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000\u00d2"+
		"\u00d3\u0005`\u0000\u0000\u00d36\u0001\u0000\u0000\u0000\u00d4\u00d8\u0005"+
		"\"\u0000\u0000\u00d5\u00d7\u0003/\u0017\u0000\u00d6\u00d5\u0001\u0000"+
		"\u0000\u0000\u00d7\u00da\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000"+
		"\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\u00db\u0001\u0000"+
		"\u0000\u0000\u00da\u00d8\u0001\u0000\u0000\u0000\u00db\u00dc\u0005\"\u0000"+
		"\u0000\u00dc8\u0001\u0000\u0000\u0000\u00dd\u00df\u0005_\u0000\u0000\u00de"+
		"\u00dd\u0001\u0000\u0000\u0000\u00df\u00e2\u0001\u0000\u0000\u0000\u00e0"+
		"\u00de\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1"+
		":\u0001\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000\u0000\u0000\u00e3\u00e4"+
		"\u0007\u0005\u0000\u0000\u00e4<\u0001\u0000\u0000\u0000\u00e5\u00e9\u0003"+
		"9\u001c\u0000\u00e6\u00e8\u0003;\u001d\u0000\u00e7\u00e6\u0001\u0000\u0000"+
		"\u0000\u00e8\u00eb\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000"+
		"\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec\u0001\u0000\u0000"+
		"\u0000\u00eb\u00e9\u0001\u0000\u0000\u0000\u00ec\u00ed\u000209\u0000\u00ed"+
		">\u0001\u0000\u0000\u0000\u00ee\u00f2\u00039\u001c\u0000\u00ef\u00f1\u0003"+
		";\u001d\u0000\u00f0\u00ef\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000"+
		"\u0000\u0000\u00f2\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f5\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000"+
		"\u0000\u0000\u00f5\u00f6\u0002AZ\u0000\u00f6@\u0001\u0000\u0000\u0000"+
		"\u00f7\u00f8\u00039\u001c\u0000\u00f8\u00f9\u0002az\u0000\u00f9B\u0001"+
		"\u0000\u0000\u0000\u00fa\u00ff\u0003?\u001f\u0000\u00fb\u00ff\u0003=\u001e"+
		"\u0000\u00fc\u00ff\u00035\u001a\u0000\u00fd\u00ff\u00037\u001b\u0000\u00fe"+
		"\u00fa\u0001\u0000\u0000\u0000\u00fe\u00fb\u0001\u0000\u0000\u0000\u00fe"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fe\u00fd\u0001\u0000\u0000\u0000\u00ff"+
		"D\u0001\u0000\u0000\u0000\u0100\u0103\u0003C!\u0000\u0101\u0103\u0003"+
		"A \u0000\u0102\u0100\u0001\u0000\u0000\u0000\u0102\u0101\u0001\u0000\u0000"+
		"\u0000\u0103F\u0001\u0000\u0000\u0000\u0104\u0108\u0003C!\u0000\u0105"+
		"\u0107\u0003E\"\u0000\u0106\u0105\u0001\u0000\u0000\u0000\u0107\u010a"+
		"\u0001\u0000\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0108\u0109"+
		"\u0001\u0000\u0000\u0000\u0109\u010e\u0001\u0000\u0000\u0000\u010a\u0108"+
		"\u0001\u0000\u0000\u0000\u010b\u010d\u0005\'\u0000\u0000\u010c\u010b\u0001"+
		"\u0000\u0000\u0000\u010d\u0110\u0001\u0000\u0000\u0000\u010e\u010c\u0001"+
		"\u0000\u0000\u0000\u010e\u010f\u0001\u0000\u0000\u0000\u010fH\u0001\u0000"+
		"\u0000\u0000\u0110\u010e\u0001\u0000\u0000\u0000\u0111\u0115\u0003A \u0000"+
		"\u0112\u0114\u0007\u0006\u0000\u0000\u0113\u0112\u0001\u0000\u0000\u0000"+
		"\u0114\u0117\u0001\u0000\u0000\u0000\u0115\u0113\u0001\u0000\u0000\u0000"+
		"\u0115\u0116\u0001\u0000\u0000\u0000\u0116J\u0001\u0000\u0000\u0000\u0117"+
		"\u0115\u0001\u0000\u0000\u0000\u0118\u011c\u0003I$\u0000\u0119\u011b\u0005"+
		"\'\u0000\u0000\u011a\u0119\u0001\u0000\u0000\u0000\u011b\u011e\u0001\u0000"+
		"\u0000\u0000\u011c\u011a\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000"+
		"\u0000\u0000\u011dL\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000"+
		"\u0000\u011f\u0120\u0005\'\u0000\u0000\u0120\u0121\u0003I$\u0000\u0121"+
		"N\u0001\u0000\u0000\u0000\u0122\u0123\u0005.\u0000\u0000\u0123\u0127\u0003"+
		"I$\u0000\u0124\u0126\u0005\'\u0000\u0000\u0125\u0124\u0001\u0000\u0000"+
		"\u0000\u0126\u0129\u0001\u0000\u0000\u0000\u0127\u0125\u0001\u0000\u0000"+
		"\u0000\u0127\u0128\u0001\u0000\u0000\u0000\u0128P\u0001\u0000\u0000\u0000"+
		"\u0129\u0127\u0001\u0000\u0000\u0000\u012a\u012b\u0005:\u0000\u0000\u012b"+
		"\u012c\u0005:\u0000\u0000\u012c\u012d\u0001\u0000\u0000\u0000\u012d\u0131"+
		"\u0003I$\u0000\u012e\u0130\u0005\'\u0000\u0000\u012f\u012e\u0001\u0000"+
		"\u0000\u0000\u0130\u0133\u0001\u0000\u0000\u0000\u0131\u012f\u0001\u0000"+
		"\u0000\u0000\u0131\u0132\u0001\u0000\u0000\u0000\u0132R\u0001\u0000\u0000"+
		"\u0000\u0133\u0131\u0001\u0000\u0000\u0000\u0134\u0136\u00033\u0019\u0000"+
		"\u0135\u0134\u0001\u0000\u0000\u0000\u0136\u0137\u0001\u0000\u0000\u0000"+
		"\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000\u0000\u0000"+
		"\u0138T\u0001\u0000\u0000\u0000\u0139\u013a\u0005/\u0000\u0000\u013a\u013b"+
		"\u0005*\u0000\u0000\u013b\u0140\u0001\u0000\u0000\u0000\u013c\u013f\u0003"+
		"U*\u0000\u013d\u013f\t\u0000\u0000\u0000\u013e\u013c\u0001\u0000\u0000"+
		"\u0000\u013e\u013d\u0001\u0000\u0000\u0000\u013f\u0142\u0001\u0000\u0000"+
		"\u0000\u0140\u0141\u0001\u0000\u0000\u0000\u0140\u013e\u0001\u0000\u0000"+
		"\u0000\u0141\u0143\u0001\u0000\u0000\u0000\u0142\u0140\u0001\u0000\u0000"+
		"\u0000\u0143\u0144\u0005*\u0000\u0000\u0144\u0145\u0005/\u0000\u0000\u0145"+
		"\u0146\u0001\u0000\u0000\u0000\u0146\u0147\u0006*\u0000\u0000\u0147V\u0001"+
		"\u0000\u0000\u0000\u0148\u0149\u0005/\u0000\u0000\u0149\u014a\u0005/\u0000"+
		"\u0000\u014a\u014e\u0001\u0000\u0000\u0000\u014b\u014d\t\u0000\u0000\u0000"+
		"\u014c\u014b\u0001\u0000\u0000\u0000\u014d\u0150\u0001\u0000\u0000\u0000"+
		"\u014e\u014f\u0001\u0000\u0000\u0000\u014e\u014c\u0001\u0000\u0000\u0000"+
		"\u014f\u0152\u0001\u0000\u0000\u0000\u0150\u014e\u0001\u0000\u0000\u0000"+
		"\u0151\u0153\u0007\u0007\u0000\u0000\u0152\u0151\u0001\u0000\u0000\u0000"+
		"\u0153\u0154\u0001\u0000\u0000\u0000\u0154\u0155\u0006+\u0000\u0000\u0155"+
		"X\u0001\u0000\u0000\u0000\u0156\u0157\u0007\b\u0000\u0000\u0157Z\u0001"+
		"\u0000\u0000\u0000\u0158\u0159\u0007\t\u0000\u0000\u0159\\\u0001\u0000"+
		"\u0000\u0000\u015a\u015c\u0003[-\u0000\u015b\u015a\u0001\u0000\u0000\u0000"+
		"\u015c\u015f\u0001\u0000\u0000\u0000\u015d\u015b\u0001\u0000\u0000\u0000"+
		"\u015d\u015e\u0001\u0000\u0000\u0000\u015e\u0160\u0001\u0000\u0000\u0000"+
		"\u015f\u015d\u0001\u0000\u0000\u0000\u0160\u0161\u0003Y,\u0000\u0161^"+
		"\u0001\u0000\u0000\u0000\u0162\u0163\u0003I$\u0000\u0163\u0164\u0005."+
		"\u0000\u0000\u0164\u0166\u0001\u0000\u0000\u0000\u0165\u0162\u0001\u0000"+
		"\u0000\u0000\u0166\u0169\u0001\u0000\u0000\u0000\u0167\u0165\u0001\u0000"+
		"\u0000\u0000\u0167\u0168\u0001\u0000\u0000\u0000\u0168\u016a\u0001\u0000"+
		"\u0000\u0000\u0169\u0167\u0001\u0000\u0000\u0000\u016a\u016b\u0003G#\u0000"+
		"\u016b`\u0001\u0000\u0000\u0000\u016c\u016d\u0007\n\u0000\u0000\u016d"+
		"\u016e\u0001\u0000\u0000\u0000\u016e\u016f\u00060\u0000\u0000\u016fb\u0001"+
		"\u0000\u0000\u0000\u0170\u0171\u0005p\u0000\u0000\u0171\u0172\u0005a\u0000"+
		"\u0000\u0172\u0173\u0005c\u0000\u0000\u0173\u0174\u0005k\u0000\u0000\u0174"+
		"\u0175\u0005a\u0000\u0000\u0175\u0176\u0005g\u0000\u0000\u0176\u0177\u0005"+
		"e\u0000\u0000\u0177\u0178\u0005 \u0000\u0000\u0178\u017e\u0001\u0000\u0000"+
		"\u0000\u0179\u017a\u0003I$\u0000\u017a\u017b\u0005.\u0000\u0000\u017b"+
		"\u017d\u0001\u0000\u0000\u0000\u017c\u0179\u0001\u0000\u0000\u0000\u017d"+
		"\u0180\u0001\u0000\u0000\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017e"+
		"\u017f\u0001\u0000\u0000\u0000\u017f\u0181\u0001\u0000\u0000\u0000\u0180"+
		"\u017e\u0001\u0000\u0000\u0000\u0181\u0182\u0003I$\u0000\u0182\u0183\u0005"+
		"\n\u0000\u0000\u0183d\u0001\u0000\u0000\u0000\u001a\u0000\u00b3\u00b9"+
		"\u00c0\u00c6\u00cf\u00d8\u00e0\u00e9\u00f2\u00fe\u0102\u0108\u010e\u0115"+
		"\u011c\u0127\u0131\u0137\u013e\u0140\u014e\u0152\u015d\u0167\u017e\u0001"+
		"\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}