package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.type.Type

interface GlobalInterface : Exportable {
    val index: Int
    val mutable: Boolean
    val type: Type


    override fun writeExport(writer: WasmWriter) {
        writer.writeByte(3)
        writer.writeU32(index)
    }
}