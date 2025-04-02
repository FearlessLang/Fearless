package codegen.optimisations

import codegen.MIR
import codegen.MIR.FName
import codegen.MIR.Fun
import codegen.MIRCloneVisitor
import magic.MagicImpls
import utils.Bug

class InliningOptimisation(private val magic: MagicImpls<*>) : MIRCloneVisitor {
  private var funs: Map<FName, Fun>? = null
  private var gamma: MutableMap<String, MIR.MT> = mutableMapOf()

  override fun visitProgram(p: MIR.Program): MIR.Program {
    this.funs = p.pkgs
      .flatMap { it.funs }
      .associateBy { it.name }
    return super.visitProgram(p)
  }

  override fun visitCreateObj(createObj: MIR.CreateObj, checkMagic: Boolean): MIR.CreateObj {
    gamma = createObj.captures.associate { Pair(it.name, it.t) }.toMutableMap()
    return super.visitCreateObj(createObj, checkMagic)
  }

  override fun visitMeth(meth: MIR.Meth): MIR.Meth {
    meth.sig.xs.forEach { gamma[it.name] = it.t }
    return super.visitMeth(meth)
  }

  override fun visitMCall(call: MIR.MCall, checkMagic: Boolean): MIR.E {
    return super.visitMCall(call, checkMagic)
  }

  private fun inline(body: Fun, call: MIR.MCall): MIR.E {
//    val args = call.args.map { it.accept(this) }
//    val newBody = body.body.map { it.accept(this) }
//    return MIR.MCall(call.name, args, newBody)
    throw Bug.todo()
  }
}
