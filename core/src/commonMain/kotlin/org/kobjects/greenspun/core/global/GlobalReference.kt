package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.type.Type

class GlobalReference(val global: GlobalInterface) : AbstractLeafNode() {

    override fun eval(context: LocalRuntimeContext) =
        context.instance.getGlobal(global.index)

    override fun toString(writer: CodeWriter) =
        writer.write("global${global.index}")

    override fun toWasm(writer: ModuleWriter) {
        TODO("Not yet implemented")
    }

    override val returnType: Type
        get() = global.type
}