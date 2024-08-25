package rt;

import base.False_0;
import base.True_0;
import base._StrHelpers_0;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface Str extends base.Str_0 {
	byte[] utf8();
	int[] graphemes();

	static Str fromJavaStr(String str) {
		var utf8 = str.getBytes(StandardCharsets.UTF_8);
		return fromTrustedUtf8(utf8);
	}
	static Str fromUtf8(byte[] utf8) {
		NativeRuntime.validateStringOrThrow(utf8);
		return fromTrustedUtf8(utf8);
	}
	static Str fromTrustedUtf8(byte[] utf8) {
		return new Str(){
			private int[] GRAPHEMES = null;
			@Override public byte[] utf8() { return utf8; }
			@Override public int[] graphemes() {
				if (GRAPHEMES == null) { GRAPHEMES = NativeRuntime.indexString(utf8); }
				return GRAPHEMES;
			}
		};
	}

	@Override default Str str$read() {
		return this;
	}
	@Override default base.Bool_0 $equals$equals$imm(Str other$) {
		return Arrays.equals(this.utf8(), other$.utf8()) ? True_0.$self : False_0.$self;
	}
	@Override default Str $plus$imm(Str other$) {
		var a = this.utf8();
		var b = other$.utf8();
		var res = new byte[a.length + b.length];
		System.arraycopy(a, 0, res, 0, a.length);
		System.arraycopy(b, 0, res, a.length, b.length);
		return fromTrustedUtf8(res);
	}
	@Override default Long size$imm() {
		return (long) this.graphemes().length;
	}
	@Override default base.Void_0 assertEq$imm(Str other$) {
		return _StrHelpers_0.$self.assertEq$imm(this, other$);
	}
	@Override default base.Void_0 assertEq$imm(Str message$, Str other$) {
		return _StrHelpers_0.$self.assertEq$imm(message$, this, other$);
	}
	@Override default base.Bool_0 isEmpty$read() {
		return this.utf8().length == 0 ? True_0.$self : False_0.$self;
	}
}
