package codegen.truffle;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import id.Id;

import java.util.Map;

public record FearlessContext(Map<Id.DecId, FearlessTrait> ts) {
}

@ExportLibrary(InteropLibrary.class)
record TraitTable(Map<String, FearlessTrait> ts) {}
