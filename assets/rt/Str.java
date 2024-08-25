package rt;

import base.*;
import base.flows.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public interface Str extends base.Str_0 {
	byte[] utf8();
	int[] graphemes();

	Str EMPTY = fromTrustedUtf8(new byte[0]);
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
	@Override default base.Bool_0 $exclamation$equals$imm(Str other$) {
		return Arrays.equals(this.utf8(), other$.utf8()) ? False_0.$self : True_0.$self;
	}
	@Override default Str $plus$imm(base.Stringable_0 other$) {
		var a = this.utf8();
		var b = other$.str$read().utf8();
		var res = new byte[a.length + b.length];
		System.arraycopy(a, 0, res, 0, a.length);
		System.arraycopy(b, 0, res, a.length, b.length);
		return fromTrustedUtf8(res);
	}
	@Override default Str append$mut(base.Stringable_0 other$) { throw new java.lang.Error("Unreachable code"); }
	@Override default base.Void_0 clear$mut() { throw new java.lang.Error("Unreachable code"); }
	@Override default Long size$imm() {
		return (long) this.graphemes().length;
	}
	@Override default base.Void_0 assertEq$imm(Str other$) {
		return _StrHelpers_0.$self.assertEq$imm(this, other$);
	}
	@Override default base.Void_0 assertEq$imm(Str other$, Str message$) {
		return _StrHelpers_0.$self.assertEq$imm(this, other$, message$);
	}
	@Override default base.Bool_0 isEmpty$read() {
		return this.utf8().length == 0 ? True_0.$self : False_0.$self;
	}
	@Override default Str join$imm(Flow_1 flow_m$) {
		return (Str) flow_m$.fold$mut(new MutStr(EMPTY), (_acc, _str) -> {
			var acc = (MutStr) _acc;
			var str = (Str) _str;
			return acc.isEmpty$read() == True_0.$self ? acc.append$mut(str) : acc.append$mut(this).append$mut(str);
		});
	}

	@Override default Str substring$imm(long start_m$, long end_m$) {
		if (start_m$ > end_m$) {
			throw new FearlessError(base.Infos_0.$self.msg$imm(fromJavaStr("Start index must be less than end index")));
		}
		if (start_m$ < 0) {
			throw new FearlessError(base.Infos_0.$self.msg$imm(fromJavaStr("Start index must be greater than or equal to 0")));
		}
		if (end_m$ > this.size$imm()) {
			throw new FearlessError(base.Infos_0.$self.msg$imm(fromJavaStr("End index must be less than the size of the string")));
		}
		return new SubStr(this, (int) start_m$, (int) end_m$);
	}

	@Override default Str charAt$imm(long index_m$) {
		return substring$imm(index_m$, index_m$ + 1);
	}

	@Override default Str normalise$imm() {
		return fromTrustedUtf8(NativeRuntime.normaliseString(this.utf8()));
	}

	@Override default Flow_1 flow$imm() {
		return Flow_0.$self.fromOp$imm(this._flow$imm(), size$imm());
	}
	@Override default FlowOp_1 _flow$imm() {
		return this._flow$imm(0, size$imm());
	}
	@Override default FlowOp_1 _flow$imm(long start, long end_) {
		return new FlowOp_1() {
			long cur = start;
			long end = end_;
			@Override public Bool_0 isFinite$mut() {
				return True_0.$self;
			}
			@Override public Void_0 step$mut(_Sink_1 sink_m$) {
				if (this.cur >= this.end) {
					sink_m$.stop$mut();
					return Void_0.$self;
				}
				var ch = charAt$imm(this.cur++);
				sink_m$.$hash$mut(ch);
				return Void_0.$self;
			}
			@Override public Void_0 stop$mut() {
				this.cur = size$imm();
				return Void_0.$self;
			}
			@Override public Bool_0 isRunning$mut() {
				return this.cur >= this.end ? False_0.$self : True_0.$self;
			}
			@Override public Void_0 forRemaining$mut(_Sink_1 downstream_m$) {
				for (; this.cur < end; ++this.cur) {
					downstream_m$.$hash$mut(charAt$imm(this.cur));
				}
				return Void_0.$self;
			}
			@Override public Opt_1 split$mut() {
				var size = this.end - this.cur;
				if (size <= 1) { return Opt_1.$self; }
				var mid = this.cur + (size / 2);
				var end_ = this.end;
				this.end = mid;
				return Opts_0.$self.$hash$imm(_flow$imm(mid, end_));
			}
			@Override public Bool_0 canSplit$read() {
				return this.end - this.cur > 1 ? True_0.$self : False_0.$self;
			}
		};
	}

	@Override default base.Hasher_0 hash$read(base.Hasher_0 hasher) {
		hasher.str$mut(this);
		return hasher;
	}

	final class SubStr implements Str {
		private int[] GRAPHEMES;
		private final byte[] UTF8;
		private final long size;
		public SubStr(Str all, int start, int end) {
			var graphemes = all.graphemes();
			var utf8 = all.utf8();
			var startIdx = start == graphemes.length ? utf8.length : graphemes[start];
			var endIdx = end == graphemes.length ? utf8.length : graphemes[end];
			this.UTF8 = Arrays.copyOfRange(utf8, startIdx, endIdx);
			this.size = end - start;
		}
		@Override public byte[] utf8() {
			return UTF8;
		}
		@Override public int[] graphemes() {
			if (GRAPHEMES == null) { GRAPHEMES = NativeRuntime.indexString(UTF8); }
			return GRAPHEMES;
		}
		@Override public Long size$imm() {
			return this.size;
		}
		@Override public Bool_0 isEmpty$read() {
			return UTF8.length == 0 ? True_0.$self : False_0.$self;
		}
	}
}
