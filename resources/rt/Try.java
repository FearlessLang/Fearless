package rt;

import userCode.FProgram;

public class Try implements FProgram.base.Try_0 {
	public static final Try _$self = new Try();
	@Override public FProgram.base.Res_2 $35$imm$(FProgram.base.Try_1 try$) {
		try { return FProgram.base.Res_0._$self.ok$imm$(try$.$35$read$()); }
		catch(FearlessError _$err) { return FProgram.base.Res_0._$self.err$imm$(_$err.info); }
		catch(ArithmeticException _$err) { return FProgram.base.Res_0._$self.err$imm$(FProgram.base.FInfo_0._$self.msg$imm$(rt.Str.fromJavaStr(_$err.getMessage()))); }
	}
}
