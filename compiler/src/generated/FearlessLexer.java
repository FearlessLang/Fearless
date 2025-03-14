// Generated from /home/nick/Programming/uni/fearless/grammar/antlrGrammars/Fearless.g4 by ANTLR 4.12.0
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
		Mut=1, MutH=2, Read=3, ReadImm=4, ReadH=5, Iso=6, Imm=7, Eq=8, Alias=9, 
		As=10, ColonColon=11, OC=12, CC=13, OS=14, CS=15, OR=16, CR=17, Comma=18, 
		Colon=19, Arrow=20, Underscore=21, X=22, SelfX=23, MName=24, CCMName=25, 
		FStringMulti=26, BlockComment=27, LineComment=28, SysInM=29, FullCN=30, 
		Whitespace=31, Pack=32;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"Mut", "MutH", "Read", "ReadImm", "ReadH", "Iso", "Imm", "Eq", "Alias", 
			"As", "ColonColon", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", "Colon", 
			"Arrow", "Underscore", "CHAR", "EscapeSequence", "CHARInString", "CHARInStringSingle", 
			"CHARInStringDouble", "FStringSingle", "FStringDouble", "CHARInStringMulti", 
			"StringMultiLine", "Unders", "NumSym", "NumStart", "UpStart", "LowStart", 
			"Start", "IdUnit", "TypeName", "FIdLow", "X", "SelfX", "MName", "CCMName", 
			"FStringMulti", "BlockComment", "LineComment", "SyInM", "SyInMExtra", 
			"SysInM", "FullCN", "Whitespace", "Pack"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'mut'", "'mutH'", "'read'", "'read/imm'", "'readH'", "'iso'", 
			"'imm'", "'='", "'alias'", "'as'", "'::'", "'{'", "'}'", "'['", "']'", 
			"'('", "')'", "','", "':'", "'->'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Mut", "MutH", "Read", "ReadImm", "ReadH", "Iso", "Imm", "Eq", 
			"Alias", "As", "ColonColon", "OC", "CC", "OS", "CS", "OR", "CR", "Comma", 
			"Colon", "Arrow", "Underscore", "X", "SelfX", "MName", "CCMName", "FStringMulti", 
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
		"\u0004\u0000 \u01a6\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
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
		"0\u00021\u00071\u00022\u00072\u00023\u00073\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t"+
		"\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0004\u0016\u00ba\b\u0016\u000b\u0016\f\u0016\u00bb\u0001\u0016"+
		"\u0003\u0016\u00bf\b\u0016\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0003\u0018\u00c6\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0003\u0019\u00cb\b\u0019\u0001\u001a\u0001\u001a\u0005\u001a\u00cf\b"+
		"\u001a\n\u001a\f\u001a\u00d2\t\u001a\u0001\u001a\u0001\u001a\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u00d8\b\u001b\n\u001b\f\u001b\u00db\t\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0004\u001d\u00e2"+
		"\b\u001d\u000b\u001d\f\u001d\u00e3\u0001\u001d\u0001\u001d\u0005\u001d"+
		"\u00e8\b\u001d\n\u001d\f\u001d\u00eb\t\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001e\u0005\u001e\u00f0\b\u001e\n\u001e\f\u001e\u00f3\t\u001e\u0001\u001f"+
		"\u0001\u001f\u0001 \u0001 \u0005 \u00f9\b \n \f \u00fc\t \u0001 \u0001"+
		" \u0001!\u0001!\u0005!\u0102\b!\n!\f!\u0105\t!\u0001!\u0001!\u0001\"\u0001"+
		"\"\u0001\"\u0001#\u0001#\u0001#\u0001#\u0003#\u0110\b#\u0001$\u0001$\u0003"+
		"$\u0114\b$\u0001%\u0001%\u0005%\u0118\b%\n%\f%\u011b\t%\u0001%\u0005%"+
		"\u011e\b%\n%\f%\u0121\t%\u0001&\u0001&\u0005&\u0125\b&\n&\f&\u0128\t&"+
		"\u0001\'\u0001\'\u0005\'\u012c\b\'\n\'\f\'\u012f\t\'\u0001(\u0001(\u0001"+
		"(\u0001)\u0001)\u0001)\u0005)\u0137\b)\n)\f)\u013a\t)\u0001*\u0001*\u0001"+
		"*\u0001*\u0001*\u0005*\u0141\b*\n*\f*\u0144\t*\u0001+\u0004+\u0147\b+"+
		"\u000b+\f+\u0148\u0001,\u0001,\u0001,\u0001,\u0001,\u0005,\u0150\b,\n"+
		",\f,\u0153\t,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001-\u0001-\u0001-"+
		"\u0001-\u0005-\u015e\b-\n-\f-\u0161\t-\u0001-\u0003-\u0164\b-\u0001-\u0001"+
		"-\u0001.\u0001.\u0001/\u0005/\u016b\b/\n/\f/\u016e\t/\u0001/\u0001/\u0001"+
		"0\u00050\u0173\b0\n0\f0\u0176\t0\u00010\u00010\u00040\u017a\b0\u000b0"+
		"\f0\u017b\u00010\u00040\u017f\b0\u000b0\f0\u0180\u00030\u0183\b0\u0001"+
		"1\u00011\u00011\u00051\u0188\b1\n1\f1\u018b\t1\u00011\u00011\u00012\u0001"+
		"2\u00012\u00012\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u0001"+
		"3\u00013\u00013\u00013\u00013\u00053\u019f\b3\n3\f3\u01a2\t3\u00013\u0001"+
		"3\u00013\u0002\u0151\u015f\u00004\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015"+
		"\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012"+
		"%\u0013\'\u0014)\u0015+\u0000-\u0000/\u00001\u00003\u00005\u00007\u0000"+
		"9\u0000;\u0000=\u0000?\u0000A\u0000C\u0000E\u0000G\u0000I\u0000K\u0000"+
		"M\u0000O\u0016Q\u0017S\u0018U\u0019W\u001aY\u001b[\u001c]\u0000_\u0000"+
		"a\u001dc\u001ee\u001fg \u0001\u0000\t\u0002\u0000\n\n ~\b\u0000\"\"\\"+
		"\\``bbffnnrrtt\u0004\u0000 !#[]_a~\u0002\u0000\"\"``\u0002\u0000++-/\u0004"+
		"\u000009AZ__az\u0001\u0001\n\n\n\u0000!!#&++--//<@\\\\^^||~~\u0002\u0000"+
		"\t\n  \u01b4\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000"+
		"\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"+
		"\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"+
		"\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"+
		"\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"+
		"\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000"+
		"\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000"+
		"\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000"+
		"\u0000!\u0001\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%"+
		"\u0001\u0000\u0000\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001"+
		"\u0000\u0000\u0000\u0000O\u0001\u0000\u0000\u0000\u0000Q\u0001\u0000\u0000"+
		"\u0000\u0000S\u0001\u0000\u0000\u0000\u0000U\u0001\u0000\u0000\u0000\u0000"+
		"W\u0001\u0000\u0000\u0000\u0000Y\u0001\u0000\u0000\u0000\u0000[\u0001"+
		"\u0000\u0000\u0000\u0000a\u0001\u0000\u0000\u0000\u0000c\u0001\u0000\u0000"+
		"\u0000\u0000e\u0001\u0000\u0000\u0000\u0000g\u0001\u0000\u0000\u0000\u0001"+
		"i\u0001\u0000\u0000\u0000\u0003m\u0001\u0000\u0000\u0000\u0005r\u0001"+
		"\u0000\u0000\u0000\u0007w\u0001\u0000\u0000\u0000\t\u0080\u0001\u0000"+
		"\u0000\u0000\u000b\u0086\u0001\u0000\u0000\u0000\r\u008a\u0001\u0000\u0000"+
		"\u0000\u000f\u008e\u0001\u0000\u0000\u0000\u0011\u0090\u0001\u0000\u0000"+
		"\u0000\u0013\u0096\u0001\u0000\u0000\u0000\u0015\u0099\u0001\u0000\u0000"+
		"\u0000\u0017\u009c\u0001\u0000\u0000\u0000\u0019\u009e\u0001\u0000\u0000"+
		"\u0000\u001b\u00a0\u0001\u0000\u0000\u0000\u001d\u00a2\u0001\u0000\u0000"+
		"\u0000\u001f\u00a4\u0001\u0000\u0000\u0000!\u00a6\u0001\u0000\u0000\u0000"+
		"#\u00a8\u0001\u0000\u0000\u0000%\u00aa\u0001\u0000\u0000\u0000\'\u00ac"+
		"\u0001\u0000\u0000\u0000)\u00af\u0001\u0000\u0000\u0000+\u00b1\u0001\u0000"+
		"\u0000\u0000-\u00b3\u0001\u0000\u0000\u0000/\u00c0\u0001\u0000\u0000\u0000"+
		"1\u00c5\u0001\u0000\u0000\u00003\u00ca\u0001\u0000\u0000\u00005\u00cc"+
		"\u0001\u0000\u0000\u00007\u00d5\u0001\u0000\u0000\u00009\u00de\u0001\u0000"+
		"\u0000\u0000;\u00e1\u0001\u0000\u0000\u0000=\u00f1\u0001\u0000\u0000\u0000"+
		"?\u00f4\u0001\u0000\u0000\u0000A\u00f6\u0001\u0000\u0000\u0000C\u00ff"+
		"\u0001\u0000\u0000\u0000E\u0108\u0001\u0000\u0000\u0000G\u010f\u0001\u0000"+
		"\u0000\u0000I\u0113\u0001\u0000\u0000\u0000K\u0115\u0001\u0000\u0000\u0000"+
		"M\u0122\u0001\u0000\u0000\u0000O\u0129\u0001\u0000\u0000\u0000Q\u0130"+
		"\u0001\u0000\u0000\u0000S\u0133\u0001\u0000\u0000\u0000U\u013b\u0001\u0000"+
		"\u0000\u0000W\u0146\u0001\u0000\u0000\u0000Y\u014a\u0001\u0000\u0000\u0000"+
		"[\u0159\u0001\u0000\u0000\u0000]\u0167\u0001\u0000\u0000\u0000_\u016c"+
		"\u0001\u0000\u0000\u0000a\u0182\u0001\u0000\u0000\u0000c\u0189\u0001\u0000"+
		"\u0000\u0000e\u018e\u0001\u0000\u0000\u0000g\u0192\u0001\u0000\u0000\u0000"+
		"ij\u0005m\u0000\u0000jk\u0005u\u0000\u0000kl\u0005t\u0000\u0000l\u0002"+
		"\u0001\u0000\u0000\u0000mn\u0005m\u0000\u0000no\u0005u\u0000\u0000op\u0005"+
		"t\u0000\u0000pq\u0005H\u0000\u0000q\u0004\u0001\u0000\u0000\u0000rs\u0005"+
		"r\u0000\u0000st\u0005e\u0000\u0000tu\u0005a\u0000\u0000uv\u0005d\u0000"+
		"\u0000v\u0006\u0001\u0000\u0000\u0000wx\u0005r\u0000\u0000xy\u0005e\u0000"+
		"\u0000yz\u0005a\u0000\u0000z{\u0005d\u0000\u0000{|\u0005/\u0000\u0000"+
		"|}\u0005i\u0000\u0000}~\u0005m\u0000\u0000~\u007f\u0005m\u0000\u0000\u007f"+
		"\b\u0001\u0000\u0000\u0000\u0080\u0081\u0005r\u0000\u0000\u0081\u0082"+
		"\u0005e\u0000\u0000\u0082\u0083\u0005a\u0000\u0000\u0083\u0084\u0005d"+
		"\u0000\u0000\u0084\u0085\u0005H\u0000\u0000\u0085\n\u0001\u0000\u0000"+
		"\u0000\u0086\u0087\u0005i\u0000\u0000\u0087\u0088\u0005s\u0000\u0000\u0088"+
		"\u0089\u0005o\u0000\u0000\u0089\f\u0001\u0000\u0000\u0000\u008a\u008b"+
		"\u0005i\u0000\u0000\u008b\u008c\u0005m\u0000\u0000\u008c\u008d\u0005m"+
		"\u0000\u0000\u008d\u000e\u0001\u0000\u0000\u0000\u008e\u008f\u0005=\u0000"+
		"\u0000\u008f\u0010\u0001\u0000\u0000\u0000\u0090\u0091\u0005a\u0000\u0000"+
		"\u0091\u0092\u0005l\u0000\u0000\u0092\u0093\u0005i\u0000\u0000\u0093\u0094"+
		"\u0005a\u0000\u0000\u0094\u0095\u0005s\u0000\u0000\u0095\u0012\u0001\u0000"+
		"\u0000\u0000\u0096\u0097\u0005a\u0000\u0000\u0097\u0098\u0005s\u0000\u0000"+
		"\u0098\u0014\u0001\u0000\u0000\u0000\u0099\u009a\u0005:\u0000\u0000\u009a"+
		"\u009b\u0005:\u0000\u0000\u009b\u0016\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0005{\u0000\u0000\u009d\u0018\u0001\u0000\u0000\u0000\u009e\u009f\u0005"+
		"}\u0000\u0000\u009f\u001a\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005[\u0000"+
		"\u0000\u00a1\u001c\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005]\u0000\u0000"+
		"\u00a3\u001e\u0001\u0000\u0000\u0000\u00a4\u00a5\u0005(\u0000\u0000\u00a5"+
		" \u0001\u0000\u0000\u0000\u00a6\u00a7\u0005)\u0000\u0000\u00a7\"\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a9\u0005,\u0000\u0000\u00a9$\u0001\u0000\u0000"+
		"\u0000\u00aa\u00ab\u0005:\u0000\u0000\u00ab&\u0001\u0000\u0000\u0000\u00ac"+
		"\u00ad\u0005-\u0000\u0000\u00ad\u00ae\u0005>\u0000\u0000\u00ae(\u0001"+
		"\u0000\u0000\u0000\u00af\u00b0\u0005_\u0000\u0000\u00b0*\u0001\u0000\u0000"+
		"\u0000\u00b1\u00b2\u0007\u0000\u0000\u0000\u00b2,\u0001\u0000\u0000\u0000"+
		"\u00b3\u00be\u0005\\\u0000\u0000\u00b4\u00bf\u0007\u0001\u0000\u0000\u00b5"+
		"\u00b6\u0005u\u0000\u0000\u00b6\u00b7\u0005{\u0000\u0000\u00b7\u00b9\u0001"+
		"\u0000\u0000\u0000\u00b8\u00ba\u000209\u0000\u00b9\u00b8\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00b9\u0001\u0000\u0000"+
		"\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000"+
		"\u0000\u00bd\u00bf\u0005}\u0000\u0000\u00be\u00b4\u0001\u0000\u0000\u0000"+
		"\u00be\u00b5\u0001\u0000\u0000\u0000\u00bf.\u0001\u0000\u0000\u0000\u00c0"+
		"\u00c1\u0007\u0002\u0000\u0000\u00c10\u0001\u0000\u0000\u0000\u00c2\u00c6"+
		"\u0003/\u0017\u0000\u00c3\u00c6\u0003-\u0016\u0000\u00c4\u00c6\u0005\""+
		"\u0000\u0000\u00c5\u00c2\u0001\u0000\u0000\u0000\u00c5\u00c3\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c4\u0001\u0000\u0000\u0000\u00c62\u0001\u0000\u0000"+
		"\u0000\u00c7\u00cb\u0003/\u0017\u0000\u00c8\u00cb\u0003-\u0016\u0000\u00c9"+
		"\u00cb\u0005`\u0000\u0000\u00ca\u00c7\u0001\u0000\u0000\u0000\u00ca\u00c8"+
		"\u0001\u0000\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000\u0000\u00cb4\u0001"+
		"\u0000\u0000\u0000\u00cc\u00d0\u0005`\u0000\u0000\u00cd\u00cf\u00031\u0018"+
		"\u0000\u00ce\u00cd\u0001\u0000\u0000\u0000\u00cf\u00d2\u0001\u0000\u0000"+
		"\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000\u0000"+
		"\u0000\u00d1\u00d3\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000"+
		"\u0000\u00d3\u00d4\u0005`\u0000\u0000\u00d46\u0001\u0000\u0000\u0000\u00d5"+
		"\u00d9\u0005\"\u0000\u0000\u00d6\u00d8\u00033\u0019\u0000\u00d7\u00d6"+
		"\u0001\u0000\u0000\u0000\u00d8\u00db\u0001\u0000\u0000\u0000\u00d9\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000\u0000\u0000\u00da\u00dc"+
		"\u0001\u0000\u0000\u0000\u00db\u00d9\u0001\u0000\u0000\u0000\u00dc\u00dd"+
		"\u0005\"\u0000\u0000\u00dd8\u0001\u0000\u0000\u0000\u00de\u00df\u0002"+
		" ~\u0000\u00df:\u0001\u0000\u0000\u0000\u00e0\u00e2\u0005|\u0000\u0000"+
		"\u00e1\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000"+
		"\u00e3\u00e1\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000"+
		"\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5\u00e9\u0007\u0003\u0000\u0000"+
		"\u00e6\u00e8\u00039\u001c\u0000\u00e7\u00e6\u0001\u0000\u0000\u0000\u00e8"+
		"\u00eb\u0001\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00e9"+
		"\u00ea\u0001\u0000\u0000\u0000\u00ea\u00ec\u0001\u0000\u0000\u0000\u00eb"+
		"\u00e9\u0001\u0000\u0000\u0000\u00ec\u00ed\u0005\n\u0000\u0000\u00ed<"+
		"\u0001\u0000\u0000\u0000\u00ee\u00f0\u0005_\u0000\u0000\u00ef\u00ee\u0001"+
		"\u0000\u0000\u0000\u00f0\u00f3\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001"+
		"\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000\u0000\u0000\u00f2>\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f4\u00f5\u0007\u0004"+
		"\u0000\u0000\u00f5@\u0001\u0000\u0000\u0000\u00f6\u00fa\u0003=\u001e\u0000"+
		"\u00f7\u00f9\u0003?\u001f\u0000\u00f8\u00f7\u0001\u0000\u0000\u0000\u00f9"+
		"\u00fc\u0001\u0000\u0000\u0000\u00fa\u00f8\u0001\u0000\u0000\u0000\u00fa"+
		"\u00fb\u0001\u0000\u0000\u0000\u00fb\u00fd\u0001\u0000\u0000\u0000\u00fc"+
		"\u00fa\u0001\u0000\u0000\u0000\u00fd\u00fe\u000209\u0000\u00feB\u0001"+
		"\u0000\u0000\u0000\u00ff\u0103\u0003=\u001e\u0000\u0100\u0102\u0003?\u001f"+
		"\u0000\u0101\u0100\u0001\u0000\u0000\u0000\u0102\u0105\u0001\u0000\u0000"+
		"\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000\u0000"+
		"\u0000\u0104\u0106\u0001\u0000\u0000\u0000\u0105\u0103\u0001\u0000\u0000"+
		"\u0000\u0106\u0107\u0002AZ\u0000\u0107D\u0001\u0000\u0000\u0000\u0108"+
		"\u0109\u0003=\u001e\u0000\u0109\u010a\u0002az\u0000\u010aF\u0001\u0000"+
		"\u0000\u0000\u010b\u0110\u0003C!\u0000\u010c\u0110\u0003A \u0000\u010d"+
		"\u0110\u00035\u001a\u0000\u010e\u0110\u00037\u001b\u0000\u010f\u010b\u0001"+
		"\u0000\u0000\u0000\u010f\u010c\u0001\u0000\u0000\u0000\u010f\u010d\u0001"+
		"\u0000\u0000\u0000\u010f\u010e\u0001\u0000\u0000\u0000\u0110H\u0001\u0000"+
		"\u0000\u0000\u0111\u0114\u0003G#\u0000\u0112\u0114\u0003E\"\u0000\u0113"+
		"\u0111\u0001\u0000\u0000\u0000\u0113\u0112\u0001\u0000\u0000\u0000\u0114"+
		"J\u0001\u0000\u0000\u0000\u0115\u0119\u0003G#\u0000\u0116\u0118\u0003"+
		"I$\u0000\u0117\u0116\u0001\u0000\u0000\u0000\u0118\u011b\u0001\u0000\u0000"+
		"\u0000\u0119\u0117\u0001\u0000\u0000\u0000\u0119\u011a\u0001\u0000\u0000"+
		"\u0000\u011a\u011f\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000"+
		"\u0000\u011c\u011e\u0005\'\u0000\u0000\u011d\u011c\u0001\u0000\u0000\u0000"+
		"\u011e\u0121\u0001\u0000\u0000\u0000\u011f\u011d\u0001\u0000\u0000\u0000"+
		"\u011f\u0120\u0001\u0000\u0000\u0000\u0120L\u0001\u0000\u0000\u0000\u0121"+
		"\u011f\u0001\u0000\u0000\u0000\u0122\u0126\u0003E\"\u0000\u0123\u0125"+
		"\u0007\u0005\u0000\u0000\u0124\u0123\u0001\u0000\u0000\u0000\u0125\u0128"+
		"\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0126\u0127"+
		"\u0001\u0000\u0000\u0000\u0127N\u0001\u0000\u0000\u0000\u0128\u0126\u0001"+
		"\u0000\u0000\u0000\u0129\u012d\u0003M&\u0000\u012a\u012c\u0005\'\u0000"+
		"\u0000\u012b\u012a\u0001\u0000\u0000\u0000\u012c\u012f\u0001\u0000\u0000"+
		"\u0000\u012d\u012b\u0001\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000"+
		"\u0000\u012eP\u0001\u0000\u0000\u0000\u012f\u012d\u0001\u0000\u0000\u0000"+
		"\u0130\u0131\u0005\'\u0000\u0000\u0131\u0132\u0003M&\u0000\u0132R\u0001"+
		"\u0000\u0000\u0000\u0133\u0134\u0005.\u0000\u0000\u0134\u0138\u0003M&"+
		"\u0000\u0135\u0137\u0005\'\u0000\u0000\u0136\u0135\u0001\u0000\u0000\u0000"+
		"\u0137\u013a\u0001\u0000\u0000\u0000\u0138\u0136\u0001\u0000\u0000\u0000"+
		"\u0138\u0139\u0001\u0000\u0000\u0000\u0139T\u0001\u0000\u0000\u0000\u013a"+
		"\u0138\u0001\u0000\u0000\u0000\u013b\u013c\u0005:\u0000\u0000\u013c\u013d"+
		"\u0005:\u0000\u0000\u013d\u013e\u0001\u0000\u0000\u0000\u013e\u0142\u0003"+
		"M&\u0000\u013f\u0141\u0005\'\u0000\u0000\u0140\u013f\u0001\u0000\u0000"+
		"\u0000\u0141\u0144\u0001\u0000\u0000\u0000\u0142\u0140\u0001\u0000\u0000"+
		"\u0000\u0142\u0143\u0001\u0000\u0000\u0000\u0143V\u0001\u0000\u0000\u0000"+
		"\u0144\u0142\u0001\u0000\u0000\u0000\u0145\u0147\u0003;\u001d\u0000\u0146"+
		"\u0145\u0001\u0000\u0000\u0000\u0147\u0148\u0001\u0000\u0000\u0000\u0148"+
		"\u0146\u0001\u0000\u0000\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149"+
		"X\u0001\u0000\u0000\u0000\u014a\u014b\u0005/\u0000\u0000\u014b\u014c\u0005"+
		"*\u0000\u0000\u014c\u0151\u0001\u0000\u0000\u0000\u014d\u0150\u0003Y,"+
		"\u0000\u014e\u0150\t\u0000\u0000\u0000\u014f\u014d\u0001\u0000\u0000\u0000"+
		"\u014f\u014e\u0001\u0000\u0000\u0000\u0150\u0153\u0001\u0000\u0000\u0000"+
		"\u0151\u0152\u0001\u0000\u0000\u0000\u0151\u014f\u0001\u0000\u0000\u0000"+
		"\u0152\u0154\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000"+
		"\u0154\u0155\u0005*\u0000\u0000\u0155\u0156\u0005/\u0000\u0000\u0156\u0157"+
		"\u0001\u0000\u0000\u0000\u0157\u0158\u0006,\u0000\u0000\u0158Z\u0001\u0000"+
		"\u0000\u0000\u0159\u015a\u0005/\u0000\u0000\u015a\u015b\u0005/\u0000\u0000"+
		"\u015b\u015f\u0001\u0000\u0000\u0000\u015c\u015e\t\u0000\u0000\u0000\u015d"+
		"\u015c\u0001\u0000\u0000\u0000\u015e\u0161\u0001\u0000\u0000\u0000\u015f"+
		"\u0160\u0001\u0000\u0000\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u0160"+
		"\u0163\u0001\u0000\u0000\u0000\u0161\u015f\u0001\u0000\u0000\u0000\u0162"+
		"\u0164\u0007\u0006\u0000\u0000\u0163\u0162\u0001\u0000\u0000\u0000\u0164"+
		"\u0165\u0001\u0000\u0000\u0000\u0165\u0166\u0006-\u0000\u0000\u0166\\"+
		"\u0001\u0000\u0000\u0000\u0167\u0168\u0007\u0007\u0000\u0000\u0168^\u0001"+
		"\u0000\u0000\u0000\u0169\u016b\u0005:\u0000\u0000\u016a\u0169\u0001\u0000"+
		"\u0000\u0000\u016b\u016e\u0001\u0000\u0000\u0000\u016c\u016a\u0001\u0000"+
		"\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000\u016d\u016f\u0001\u0000"+
		"\u0000\u0000\u016e\u016c\u0001\u0000\u0000\u0000\u016f\u0170\u0003].\u0000"+
		"\u0170`\u0001\u0000\u0000\u0000\u0171\u0173\u0003_/\u0000\u0172\u0171"+
		"\u0001\u0000\u0000\u0000\u0173\u0176\u0001\u0000\u0000\u0000\u0174\u0172"+
		"\u0001\u0000\u0000\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175\u0179"+
		"\u0001\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177\u017a"+
		"\u0003].\u0000\u0178\u017a\u0005*\u0000\u0000\u0179\u0177\u0001\u0000"+
		"\u0000\u0000\u0179\u0178\u0001\u0000\u0000\u0000\u017a\u017b\u0001\u0000"+
		"\u0000\u0000\u017b\u0179\u0001\u0000\u0000\u0000\u017b\u017c\u0001\u0000"+
		"\u0000\u0000\u017c\u0183\u0001\u0000\u0000\u0000\u017d\u017f\u0003_/\u0000"+
		"\u017e\u017d\u0001\u0000\u0000\u0000\u017f\u0180\u0001\u0000\u0000\u0000"+
		"\u0180\u017e\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000\u0000\u0000"+
		"\u0181\u0183\u0001\u0000\u0000\u0000\u0182\u0174\u0001\u0000\u0000\u0000"+
		"\u0182\u017e\u0001\u0000\u0000\u0000\u0183b\u0001\u0000\u0000\u0000\u0184"+
		"\u0185\u0003M&\u0000\u0185\u0186\u0005.\u0000\u0000\u0186\u0188\u0001"+
		"\u0000\u0000\u0000\u0187\u0184\u0001\u0000\u0000\u0000\u0188\u018b\u0001"+
		"\u0000\u0000\u0000\u0189\u0187\u0001\u0000\u0000\u0000\u0189\u018a\u0001"+
		"\u0000\u0000\u0000\u018a\u018c\u0001\u0000\u0000\u0000\u018b\u0189\u0001"+
		"\u0000\u0000\u0000\u018c\u018d\u0003K%\u0000\u018dd\u0001\u0000\u0000"+
		"\u0000\u018e\u018f\u0007\b\u0000\u0000\u018f\u0190\u0001\u0000\u0000\u0000"+
		"\u0190\u0191\u00062\u0000\u0000\u0191f\u0001\u0000\u0000\u0000\u0192\u0193"+
		"\u0005p\u0000\u0000\u0193\u0194\u0005a\u0000\u0000\u0194\u0195\u0005c"+
		"\u0000\u0000\u0195\u0196\u0005k\u0000\u0000\u0196\u0197\u0005a\u0000\u0000"+
		"\u0197\u0198\u0005g\u0000\u0000\u0198\u0199\u0005e\u0000\u0000\u0199\u019a"+
		"\u0005 \u0000\u0000\u019a\u01a0\u0001\u0000\u0000\u0000\u019b\u019c\u0003"+
		"M&\u0000\u019c\u019d\u0005.\u0000\u0000\u019d\u019f\u0001\u0000\u0000"+
		"\u0000\u019e\u019b\u0001\u0000\u0000\u0000\u019f\u01a2\u0001\u0000\u0000"+
		"\u0000\u01a0\u019e\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000"+
		"\u0000\u01a1\u01a3\u0001\u0000\u0000\u0000\u01a2\u01a0\u0001\u0000\u0000"+
		"\u0000\u01a3\u01a4\u0003M&\u0000\u01a4\u01a5\u0005\n\u0000\u0000\u01a5"+
		"h\u0001\u0000\u0000\u0000!\u0000\u00bb\u00be\u00c5\u00ca\u00d0\u00d9\u00e3"+
		"\u00e9\u00f1\u00fa\u0103\u010f\u0113\u0119\u011f\u0126\u012d\u0138\u0142"+
		"\u0148\u014f\u0151\u015f\u0163\u016c\u0174\u0179\u017b\u0180\u0182\u0189"+
		"\u01a0\u0001\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}