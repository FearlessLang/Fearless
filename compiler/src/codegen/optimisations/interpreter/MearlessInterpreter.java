package codegen.optimisations.interpreter;

import codegen.MIR;
import failure.CompileError;
import failure.FailOr;
import id.Id;
import id.Mdf;
import magic.Magic;
import utils.Box;
import utils.Streams;
import visitors.MIRVisitor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public class MearlessInterpreter implements MIRVisitor<MIR.E> {
  private final MIR.Program p;
  private final Map<MIR.FName, MIR.Fun> funs;
  private Map<String, MIR.E> locals = new HashMap<>();
  private final Map<MIR.CreateObj, Map<String, MIR.E>> objLocals = new WeakHashMap<>();
  private final InterpreterMagicImpl magic;

  public MearlessInterpreter(MIR.Program p) {
    this.p = p;
    this.funs = p.pkgs().stream()
      .flatMap(pkg -> pkg.funs().stream())
      .collect(Collectors.toMap(MIR.Fun::name, Function.identity()));
    this.magic = new InterpreterMagicImpl(p, this);
  }

  public FailOr<MIR.E> safely(Function<MearlessInterpreter, MIR.E> f) {
    try {
      return FailOr.res(f.apply(this));
    } catch (StackOverflowError e) {
      return FailOr.err(()->CompileError.of("Stack overflowed while interpreting the program. This is likely due to a recursive call that does not terminate."));
    }
  }

  public MIR.E run(Id.DecId main) {
    var typeDef = p.of(main);
    var mainObj = this.visitCreateObj(typeDef.singletonInstance().orElseThrow(), true);
    var system = p.of(new Id.DecId("base.caps._System", 0)).singletonInstance().orElseThrow();

    var mainMethod = mainObj.meths().stream().filter(m -> m.sig().name().equals(new Id.MethName(Optional.of(Mdf.imm), "#", 1)))
      .findFirst()
      .orElseThrow(() -> new NoSuchElementException("Main method not found: " + main));
    var call = new MIR.MCall(
      mainObj,
      mainMethod.sig().name(),
      List.of(system),
      new MIR.MT.Plain(Mdf.imm, new Id.DecId("base.Void", 0)),
      Mdf.imm,
      EnumSet.of(MIR.MCall.CallVariant.Standard)
    );
    return this.visitMCall(call, true);
  }

  @Override public MIR.CreateObj visitCreateObj(MIR.CreateObj createObj, boolean checkMagic) {
    if (createObj.captures().isEmpty()) { return createObj; }
    Map<String, MIR.E> captures = createObj.captures().stream().collect(Collectors.toMap(MIR.X::name, x->locals.get(x.name())));
    objLocals.put(createObj, captures);
    return createObj;
  }

  @Override public MIR.E visitX(MIR.X x, boolean checkMagic) {
    return requireNonNull(locals.get(x.name()));
  }

  @Override public MIR.E visitMCall(MIR.MCall call, boolean checkMagic) {
    var oldLocals = locals;
    try {
      // Reduce the recv until we get to a value.
      var recv = call.recv().accept(this, checkMagic);
      while (!(recv instanceof MIR.CreateObj recvK)) {
        recv = recv.accept(this, checkMagic);
      }

      var magicImpl = magic.get(recvK);
      if (magicImpl.isPresent()) {
        var impl = magicImpl.get().call(call.name(), call.args(), call.variant(), call.t());
        if (impl.isPresent()) {
          return impl.get().accept(this, true);
        }
      }

      var singletonInstance = p.of(recvK.concreteT().id()).singletonInstance();
      if (singletonInstance.isPresent()) {
        recvK = visitCreateObj(singletonInstance.get(), true);
      }

      if (recvK.concreteT().id().equals(Magic.MagicAbort)) {
        if (call.name().equals(new Id.MethName(Mdf.imm, "!", 0)) || call.name().equals(new Id.MethName(Mdf.imm, "!", 1))) {
          throw new InterpreterException.NonDeterministicExecutionException(call);
        }
      }

      // Get the method
      var funName = recvK.meths().stream()
        .filter(m -> m.sig().name().equals(call.name()))
        .findFirst()
        .flatMap(MIR.Meth::fName)
        .orElseThrow();
      var fun = requireNonNull(funs.get(funName));
      locals = new HashMap<>(locals);

      Map<String, MIR.E> captures = requireNonNullElse(objLocals.get(recvK), Map.of());

      // Reduce the args until we get to values and add them as locals.
      Streams.zip(fun.args().subList(0, call.args().size()), call.args())
        .forEach(((x, e) -> locals.put(x.name(), e.accept(this, true))));
      var idx = new Box<>(call.args().size());

      // Self capture is always added for now
      locals.put(fun.args().get(idx.thenUpdate(i->i+1)).name(), recvK);

      var hasCaptures = fun.args().size() > idx.get();
      if (hasCaptures) {
        var meth = recvK.meths().stream()
          .filter(m -> m.sig().name().equals(funName.m()))
          .findFirst()
          .orElseThrow(() -> new NoSuchElementException("Method not found: " + funName));
        assert !meth.captures().isEmpty();
        meth.captures().forEach(x -> locals.put(fun.args().get(idx.thenUpdate(i->i+1)).name(), captures.get(x).accept(this, true)));
      }

      return fun.body().accept(this, checkMagic);
    } finally {
      locals = oldLocals;
    }
  }

  @Override public MIR.E visitBoolExpr(MIR.BoolExpr expr, boolean checkMagic) {
    return MIRVisitor.super.visitBoolExpr(expr, checkMagic);
  }

  private sealed static class InterpreterException extends RuntimeException {
    InterpreterException(String message) {
      super(message);
    }
    final static class NonDeterministicExecutionException extends InterpreterException {
      public NonDeterministicExecutionException(MIR.MCall ignored) {
        super("Call to a non-deterministic method.");
      }
    }
  }
}
