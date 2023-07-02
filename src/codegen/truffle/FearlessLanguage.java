package codegen.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import id.Id;
import utils.Bug;

import java.util.Map;

@TruffleLanguage.Registration(id = "fearless", name = "Fearless", interactive = false)
public class FearlessLanguage extends TruffleLanguage<FearlessContext> {
  public final Map<Id.DecId, FearlessTrait> ts;
  public FearlessLanguage(Map<Id.DecId, FearlessTrait> ts) {
    this.ts = ts;
  }

  @Override protected FearlessContext createContext(Env env) {
    return new FearlessContext(this.ts);
  }

  @CompilerDirectives.TruffleBoundary
  @Override protected CallTarget parse(ParsingRequest request) {
    CompilerAsserts.neverPartOfCompilation();
    throw Bug.todo();
//    var src = request.getSource();
//    var code = new BufferedReader(src.getReader()).lines().collect(Collectors.joining("\n"));
//    var pkg = new Parser(Path.of(src.getPath()), code).parseFile(CompileError::err);
//    var p = new astFull.Program(pkg.parse());
//    new WellFormednessFullShortCircuitVisitor().visitProgram(p).ifPresent(err->{ throw err; });
//    var inferredSigs = p.inferSignaturesToCore();
//    var inferred = new InferBodies(inferredSigs).inferAll(p);
//    new WellFormednessShortCircuitVisitor(inferred).visitProgram(inferred).ifPresent(err->{ throw err; });
//    inferred.typeCheck();
//
//    return
  }


}
