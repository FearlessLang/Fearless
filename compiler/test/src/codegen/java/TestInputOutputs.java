package codegen.java;

import main.InputOutput;
import utils.IoErr;
import utils.Push;
import utils.ResolveResource;

import java.nio.file.Files;
import java.util.List;


public interface TestInputOutputs {
	static InputOutput explicit(List<String> files, String entry){
		return explicit(files, entry, List.of());
	}
	static InputOutput explicit(List<String> files, String entry, List<String> args){
		var workingDir = ResolveResource.freshTmpPath();
		IoErr.of(()->Files.createDirectories(workingDir));
		args = Push.of(entry, args);
		return InputOutput.programmatic(
			entry,
			args,
			files,
			workingDir,
			workingDir
		);
	}
	static InputOutput programmaticImm(List<String> files){
		return programmaticImm(files, List.of());
	}
	static InputOutput programmaticImm(List<String> files, List<String> args){
		var workingDir = ResolveResource.freshTmpPath();
		IoErr.of(()->Files.createDirectories(workingDir));
		args = Push.of("test.Test", args);
		return InputOutput.programmaticImm(
			"test.Test",
			args,
			files,
			workingDir,
			workingDir
		);
	}
}
