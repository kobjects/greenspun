package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType

interface FuncInterface : Exportable {
    val index: Int
    val type: FuncType
    val localContextSize: Int

    fun call(context: LocalRuntimeContext): Any

    operator fun invoke(vararg node: Any) =
        Call(this, *node.map { Node.of(it) }.toTypedArray())

    fun toString(writer: CodeWriter)

    override fun writeExport(writer: WasmWriter) {
        writer.writeByte(0)
        writer.writeU32(index)
    }
}