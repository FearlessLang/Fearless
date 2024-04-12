package rt;

import userCode.FProgram;

public final class CapTry implements FProgram.base$46caps.CapTry_0 {
	public static final CapTry _$self = new CapTry();
	@Override public FProgram.base.Res_2 $35$mut$(FProgram.base$46caps.TryBody_1 try$) {
		try { return FProgram.base.Res_0._$self.ok$imm$(try$.$35$mut$()); }
		catch(FearlessError _$err) { return FProgram.base.Res_0._$self.err$imm$(_$err.info); }
		catch(java.lang.StackOverflowError _$err) { return FProgram.base.Res_0._$self.err$imm$(FProgram.base.FInfo_0._$self.msg$imm$(StackOverflowedErrStr._self$)); }
		catch(Throwable _$err) { return FProgram.base.Res_0._$self.err$imm$(FProgram.base.FInfo_0._$self.msg$imm$(Str.fromJavaStr(_$err.getMessage()))); }
	}
	private static class StackOverflowedErrStr implements Str {
		private static final Str _self$ = new StackOverflowedErrStr();
		private static final byte[] UTF8 = new byte[]{83, 116, 97, 99, 107, 32, 111, 118, 101, 114, 102, 108, 111, 119, 101, 100};
		private static final int[] GRAPHEMES = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
		@Override public byte[] utf8() { return UTF8; }
		@Override public int[] graphemes() { return GRAPHEMES; }
	}
}
