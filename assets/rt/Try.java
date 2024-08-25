package rt;

public class Try implements base.Try_0 {
	public static final Try $self = new Try();
	@Override public base.Res_2 $hash$imm(base.Try_1 try$) {
		try { return base.Res_0.$self.ok$imm(try$.$hash$read()); }
		catch(FearlessError _$err) { return base.Res_0.$self.err$imm(_$err.info); }
		catch(ArithmeticException _$err) { return base.Res_0.$self.err$imm(base.Infos_0.$self.msg$imm(rt.Str.fromJavaStr(_$err.getMessage()))); }
	}
}
