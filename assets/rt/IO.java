package rt;

import base.List_1;
import base.caps.ReadPath_0;
import base.caps.ReadWritePath_0;
import base.caps.WritePath_0;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class IO implements base.caps.IO_0 {
	public static final IO $self = new IO();
	@Override public base.Void_0 printlnErr$mut(Str msg$) {
		NativeRuntime.printlnErr(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 println$mut(Str msg$) {
		NativeRuntime.println(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 print$mut(Str msg$) {
		NativeRuntime.print(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public base.Void_0 printErr$mut(Str msg$) {
		NativeRuntime.printErr(msg$.utf8());
		return base.Void_0.$self;
	}
	@Override public ReadPath_0 accessR$mut(List_1 path_m$) {
		return new rt.fs.ReadWritePath(strListToPath(Path.of("").toAbsolutePath(), path_m$));
	}
	@Override public WritePath_0 accessW$mut(List_1 path_m$) {
		return new rt.fs.ReadWritePath(strListToPath(Path.of("").toAbsolutePath(), path_m$));
	}
	@Override public ReadWritePath_0 accessRW$mut(List_1 path_m$) {
		return new rt.fs.ReadWritePath(strListToPath(Path.of("").toAbsolutePath(), path_m$));
	}
	@Override public IO clone$mut() { return this; }

	public static Path strListToPath(Path root, List_1 path) {
		assert root.isAbsolute();
		@SuppressWarnings("unchecked") // validated by the Fearless type system
		var pathList = ((ListK.ListImpl<Str>)path);
		return pathList.inner().stream()
			.map(str -> rt.Str.toJavaStr(str.utf8()))
			.reduce(root, Path::resolve, (_,_)->{throw new UnsupportedOperationException("not associative");})
			.toAbsolutePath();
	}
}
