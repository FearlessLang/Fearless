package codegen.optimisations

import codegen.MIR
import codegen.MIRCloneVisitor
import id.Id
import id.Mdf
import magic.MagicImpls
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse

class InliningOptimisation(private val magic: MagicImpls<*>) : MIRCloneVisitor {
  private var program: MIR.Program? = null
  private var funs: Map<MIR.FName, MIR.Fun>? = null
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

  override fun visitFun(f: MIR.Fun): MIR.Fun {
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
    assert(checkMagic)
    if (!call.variant.contains(MIR.MCall.CallVariant.Standard) || magic.get(call.recv).isPresent) {
      return super.visitMCall(call, checkMagic)
    }

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
      return attemptInlining(recv, refinedCall)
    }

    val recvType = refinedCall.recv.t().name().getOrElse { return super.visitMCall(refinedCall, checkMagic) }
    val impl = program!!.of(recvType).singletonInstance.getOrElse {
      return super.visitMCall(refinedCall, checkMagic)
    }
    return attemptInlining(impl, refinedCall)
  }

  private fun attemptInlining(recv: MIR.CreateObj, refinedCall: MIR.MCall): MIR.E {
    recv.meths.find { it.fName.isPresent && it.sig.name == refinedCall.name }?.let { meth ->
      val body = funs!![meth.fName.get()]!!
      seen.add(refinedCall)
      val result = inline(refinedCall, meth, body)
      if (result.fullyInlined) {
        return result.expr
      }
      seen.add(refinedCall)
      return super.visitMCall(result.expr as MIR.MCall, true)
    }
    seen.add(refinedCall)
    return super.visitMCall(refinedCall, true)
  }

  private data class InlineResult(val expr: MIR.E, val fullyInlined: Boolean)
  private fun inline(call: MIR.MCall, meth: MIR.Meth, impl: MIR.Fun): InlineResult {
    if (impl.name.capturesSelf) {
      return InlineResult(call, true)
    }
    println("attempting to inline ${impl.name}")
//    val args = call.args.map { it.accept(this) }
//    val newBody = body.body.map { it.accept(this) }
//    return MIR.MCall(call.name, args, newBody)

    val expr = impl.body.accept(AlphaRenamer(gamma), true)
    val funParams: List<String> = (
      meth.sig.xs.map { it.name } +
      listOf("this") +
      meth.captures
      )
    val funArgs: List<MIR.E> = (
      call.args +
      listOf(FearlessNull) +
      meth.captures.map { MIR.X(it, gamma[it]!!) }
      )
    val args = funParams.zip(funArgs).associate { (k, v) -> k to v }

    val inlined = expr.accept(Inliner(args), true)
    val fullyInlined = inlined !is MIR.MCall
    return InlineResult(inlined, fullyInlined)
  }
}

private class AlphaRenamer(private val gamma: Map<String, MIR.MT>) : MIRCloneVisitor {
  private val renamed = mutableMapOf<String, String>()
  override fun visitX(x: MIR.X, checkMagic: Boolean): MIR.E {
    if (x.name !in gamma) {
      return super.visitX(x, checkMagic)
    }
    val newName = renamed.getOrPut(x.name) { astFull.E.X.freshName() }
    return super.visitX(MIR.X(newName, x.t), checkMagic)
  }
}

private class Inliner(private val args: Map<String, MIR.E>) : MIRCloneVisitor {
  override fun visitX(x: MIR.X, checkMagic: Boolean): MIR.E {
    args[x.name]?.let { return it }
    return super.visitX(x, checkMagic)
  }

  override fun visitCreateObj(createObj: MIR.CreateObj, checkMagic: Boolean): MIR.CreateObj {
    return MIR.CreateObj(
      this.visitMT(createObj.t),
      createObj.selfName,
      createObj.meths.stream().map { meth -> this.visitMeth(meth) }.toList(),
      createObj.unreachableMs.stream().map { meth -> this.visitMeth(meth) }.toList(),
      createObj.captures,
    )
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

private val FearlessNull = MIR.CreateObj(Mdf.iso, Id.DecId("base.codegen._Null", 0))

//private class Evaluator(program: MIR.Program) : MIRCloneVisitor {
//
//}
