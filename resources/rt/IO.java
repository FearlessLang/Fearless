package rt;

import userCode.FProgram;

public final class IO implements FProgram.base$46caps.IO_0 {
	public static final IO _$self = new IO();
	@Override public FProgram.base.Void_0 printlnErr$mut$(Str msg$) {
		NativeRuntime.printlnErr(msg$.utf8());
		return FProgram.base.Void_0._$self;
	}
	@Override public FProgram.base.Void_0 println$mut$(Str msg$) {
		NativeRuntime.println(msg$.utf8());
		return FProgram.base.Void_0._$self;
	}
	@Override public FProgram.base.Void_0 print$mut$(Str msg$) {
		NativeRuntime.print(msg$.utf8());
		return FProgram.base.Void_0._$self;
	}
	@Override public FProgram.base.Void_0 printErr$mut$(Str msg$) {
		NativeRuntime.printErr(msg$.utf8());
		return FProgram.base.Void_0._$self;
	}
}
