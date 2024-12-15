package rt;

import java.nio.ByteBuffer;

public record CapTry(base.caps.System_0 sys) implements base.caps.CapTry_0 {
	@Override public Fallible $hash$mut(base.F_1 try$) {
		return res -> {
			try { return res.ok$mut(try$.$hash$read()); }
			catch(FearlessError _$err) { return res.info$mut(_$err.info); }
			catch(java.lang.StackOverflowError _$err) { return res.info$mut(base.Infos_0.$self.msg$imm(StackOverflowedErrStr.$self)); }
			catch(Throwable _$err) { return res.info$mut(base.Infos_0.$self.msg$imm(Str.fromJavaStr(_$err.getMessage()))); }
		};
	}
	@Override public Fallible $hash$mut(Object data, base.F_2 try$) {
		return res -> {
			try { return res.ok$mut(try$.$hash$read(data)); }
			catch(FearlessError _$err) { return res.info$mut(_$err.info); }
			catch(java.lang.StackOverflowError _$err) { return res.info$mut(base.Infos_0.$self.msg$imm(StackOverflowedErrStr.$self)); }
			catch(Throwable _$err) { return res.info$mut(base.Infos_0.$self.msg$imm(Str.fromJavaStr(_$err.getMessage()))); }
		};
	}
	@Override public CapTry clone$mut() { return this; }
	private static class StackOverflowedErrStr implements Str {
		private static final Str $self = new StackOverflowedErrStr();
		private static final ByteBuffer UTF8 = Str.wrap(new byte[]{83, 116, 97, 99, 107, 32, 111, 118, 101, 114, 102, 108, 111, 119, 101, 100});
		private static final int[] GRAPHEMES = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
		@Override public ByteBuffer utf8() { return UTF8; }
		@Override public int[] graphemes() { return GRAPHEMES; }
	}
}
