package codegen.optimisations

import codegen.MIR
import codegen.MIR.FName
import codegen.MIR.Fun
import codegen.MIRCloneVisitor
import id.Id
import magic.MagicImpls
import utils.Bug
import visitors.MIRVisitor
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse

class InliningOptimisation(private val magic: MagicImpls<*>) : MIRCloneVisitor {
  private var program: MIR.Program? = null
  private var funs: Map<FName, Fun>? = null
  private var finalTypes: Set<Id.DecId>? = null
  private var gamma: MutableMap<String, MIR.MT> = mutableMapOf()
  private val seen = mutableSetOf<MIR.MCall>()

  override fun visitProgram(p: MIR.Program): MIR.Program {
    this.program = p
    this.funs = p.pkgs
      .flatMap { it.funs }
      .associateBy { it.name }
    this.finalTypes = collectEffectivelyFinalTypes(p)
    return super.visitProgram(p)
  }

  override fun visitCreateObj(createObj: MIR.CreateObj, checkMagic: Boolean): MIR.CreateObj {
//    gamma = createObj.captures.associate { Pair(it.name, it.t) }.toMutableMap()
    return super.visitCreateObj(createObj, checkMagic)
  }

  override fun visitMeth(meth: MIR.Meth): MIR.Meth {
//    meth.sig.xs.forEach { gamma[it.name] = it.t }
    return super.visitMeth(meth)
  }

  override fun visitFun(f: Fun): Fun {
    gamma = f.args.associate { Pair(it.name, it.t) }.toMutableMap()
    return super.visitFun(f)
  }

  override fun visitX(x: MIR.X, checkMagic: Boolean): MIR.E {
    val associatedType = x.t().name().getOrElse {
      return super.visitX(x, checkMagic)
    }
    if (associatedType in finalTypes!!) {
      return super.visitX(x, checkMagic)
    }

    val gammaType = (gamma[x.name] ?: return super.visitX(x, checkMagic))
    val gammaTypeName = gammaType.name().getOrElse {
      return super.visitX(x, checkMagic)
    }
    if (gammaTypeName in finalTypes!!) {
      return super.visitX(MIR.X(x.name(), gammaType), checkMagic)
    }

    return super.visitX(x, checkMagic)
  }

  override fun visitMCall(call: MIR.MCall, checkMagic: Boolean): MIR.E {
//    if (call.isConcrete()) {
//      return super.visitMCall(call, checkMagic)
//    }
    val hasAttemptedToInline = seen.contains(call)
    if (hasAttemptedToInline) {
      return super.visitMCall(call, checkMagic)
    }

    var refinedCall = call
    refinedCall = refinedCall.withRecv(call.recv.accept(this, true))
    refinedCall = refinedCall.withArgs(refinedCall.args.map{
      it.accept(this, true)
    })

    val recv = refinedCall.recv()
    if (recv is MIR.CreateObj) {
      recv.meths.find { it.fName.isPresent && it.sig.name == call.name }?.let { meth ->
        val body = funs!![meth.fName.get()]!!
        seen.add(refinedCall)
        val result = inline(body, refinedCall)
        if (result.fullyInlined) {
          return result.expr
        }
      }
      seen.add(refinedCall)
      return super.visitMCall(refinedCall, checkMagic)
    }

    val recvType = refinedCall.recv.t().name().getOrElse { return super.visitMCall(refinedCall, checkMagic) }
    val impl = program!!.of(recvType).singletonInstance.getOrElse {
      return super.visitMCall(refinedCall, checkMagic)
    }
    impl.meths.find { it.fName.isPresent && it.sig.name == call.name }?.let { meth ->
      val body = funs!![meth.fName.get()]!!
      seen.add(refinedCall)
      val result = inline(body, refinedCall)
      if (result.fullyInlined) {
        return result.expr
      }
    }
    seen.add(refinedCall)
    return super.visitMCall(refinedCall, checkMagic)
  }

  private data class InlineResult(val expr: MIR.E, val fullyInlined: Boolean)
  private fun inline(body: Fun, call: MIR.MCall): InlineResult {
//    val args = call.args.map { it.accept(this) }
//    val newBody = body.body.map { it.accept(this) }
//    return MIR.MCall(call.name, args, newBody)
    throw Bug.todo()
  }
}

//private object IsConcrete : MIRVisitor<Boolean> {
//  override fun visitCreateObj(createObj: MIR.CreateObj, checkMagic: Boolean): Boolean = true
//
//  override fun visitX(x: MIR.X, checkMagic: Boolean): Boolean = false
//
//  override fun visitMCall(call: MIR.MCall, checkMagic: Boolean): Boolean = false
//}
//private fun MIR.E.isConcrete() =
//  this.accept(IsConcrete, true)

private fun collectEffectivelyFinalTypes(p: MIR.Program): Set<Id.DecId> {
  val program = p.p
  val inheritedTypes = mutableSetOf<Id.DecId>()
  Stream.concat(program.ds().values.stream(), program.inlineDs().values.stream())
    .forEach { d -> inheritedTypes.addAll(d.lambda.its.map { it.name }) }
  return Stream.concat(program.ds().values.stream(), program.inlineDs().values.stream())
    .map { it.name() }
    .filter { it !in inheritedTypes }
    .collect(Collectors.toUnmodifiableSet())
}

//private class Evaluator(program: MIR.Program) : MIRCloneVisitor {
//
//}
