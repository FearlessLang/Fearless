package rt;

public class Try implements base.Try_0 {
	public static final Try $self = new Try();
	@Override public Fallible $hash$imm(base.Try_1 try$) {
		return res -> {
			try { return res.ok$mut(try$.$hash$read()); }
			catch(FearlessError _$err) { return res.info$mut(_$err.info); }
			catch(ArithmeticException _$err) { return res.info$mut(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(_$err.getMessage()))); }
		};
	}
}
