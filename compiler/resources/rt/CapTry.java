package rt;

public final class CapTry implements base.caps.CapTry_0 {
	public static final CapTry $self = new CapTry();
	@Override public base.Res_2 $hash$mut(base.caps.TryBody_1 try$) {
		try { return base.Res_0.$self.ok$imm(try$.$hash$mut()); }
		catch(FearlessError _$err) { return base.Res_0.$self.err$imm(_$err.info); }
		catch(java.lang.StackOverflowError _$err) { return base.Res_0.$self.err$imm(base.FInfo_0.$self.msg$imm(StackOverflowedErrStr.$self)); }
		catch(Throwable _$err) { return base.Res_0.$self.err$imm(base.FInfo_0.$self.msg$imm(Str.fromJavaStr(_$err.getMessage()))); }
	}
	private static class StackOverflowedErrStr implements Str {
		private static final Str $self = new StackOverflowedErrStr();
		private static final byte[] UTF8 = new byte[]{83, 116, 97, 99, 107, 32, 111, 118, 101, 114, 102, 108, 111, 119, 101, 100};
		private static final int[] GRAPHEMES = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
		@Override public byte[] utf8() { return UTF8; }
		@Override public int[] graphemes() { return GRAPHEMES; }
	}
}
