package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmSection
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.FuncImpl
import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.global.GlobalInterface
import org.kobjects.greenspun.core.global.GlobalImport
import org.kobjects.greenspun.core.instance.ImportObject
import org.kobjects.greenspun.core.instance.Instance
import org.kobjects.greenspun.core.memory.DataImpl
import org.kobjects.greenspun.core.memory.MemoryImpl
import org.kobjects.greenspun.core.memory.MemoryImport
import org.kobjects.greenspun.core.memory.MemoryInterface
import org.kobjects.greenspun.core.table.ElementImpl
import org.kobjects.greenspun.core.table.TableImport
import org.kobjects.greenspun.core.table.TableInterface
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType

class Module(
    val types: List<FuncType>,
    val funcs: List<FuncInterface>,
    val tables: List<TableInterface>,
    val memory: MemoryInterface?,
    val globals: List<GlobalInterface>,
    val exports: List<Export>,
    val start: Int?,
    val elements: List<ElementImpl>,
    val datas: List<DataImpl>,
) {

    fun imports() = (
            funcs.filterIsInstance<FuncImport>() +
                    globals.filterIsInstance<MemoryImport>() +
                    tables.filterIsInstance<TableImport>() +
                    (if (memory == null) emptyList() else listOf(memory))
            ) as List<Imported>


    fun instantiate(importObject: ImportObject = ImportObject()) = Instance(this, importObject)

    private fun writeTypes(writer: ModuleWriter) {
        writer.writeU32(types.size)
        for (type in types) {
            type.toWasm(writer)
        }
    }

    private fun writeImports(writer: ModuleWriter) {
        val imports = imports()
        if (imports.isNotEmpty()) {
            writer.writeU32(imports.size)
            for (i in imports) {
                writer.writeName(i.module)
                writer.writeName(i.name)
                i.writeImportDescription(writer)
            }
        }
    }

    private fun writeFunctions(writer: ModuleWriter) {
        val funcs = funcs.filterIsInstance<FuncImpl>()
        writer.writeU32(funcs.size)
        for (func in funcs) {
            writer.writeU32(func.type.index)
        }
    }

    private fun writeCode(writer: ModuleWriter) {
        val funcs = funcs.filterIsInstance<FuncImpl>()
        writer.writeU32(funcs.size)
        for (func in funcs) {
            val funcWriter = ModuleWriter(this)
            func.writeBody(funcWriter)
            val bytes = funcWriter.toByteArray()

            writer.writeU32(bytes.size)
            writer.writeBytes(bytes)
        }
    }

    private fun writeExports(writer: ModuleWriter) {
        if (exports.isNotEmpty()) {
            writer.writeU32(exports.size)
            for (export in exports) {
                writer.writeName(export.name)
                export.value.writeExportDescription(writer)
            }
        }
    }

    private fun writeGlobals(writer: ModuleWriter) {
        val globals = globals.filterIsInstance<GlobalImpl>()
        if (globals.isNotEmpty()) {
            writer.writeU32(globals.size)
            for (global in globals) {
                global.type.toWasm(writer)
                writer.writeByte(if (global.mutable) 1 else 0)
                global.initializer.toWasm(writer)
                writer.write(WasmOpcode.END)
            }
        }
    }

    private fun writeMemory(writer: ModuleWriter) {
        if (memory is MemoryImpl) {
            writer.writeU32(1)
            memory.writeType(writer)
        }
    }

    private fun writeDataCount(writer: ModuleWriter) {
        if (datas.isNotEmpty()) {
            writer.writeU32(datas.size)
        }
    }

    private fun writeElements(writer: ModuleWriter) {
        if (elements.isNotEmpty()) {
            writer.writeU32(elements.size)
            for (element in elements) {
                writer.writeU32(element.tableIdx)
                element.offset.toWasm(writer)
                writer.write(WasmOpcode.END)
                writer.writeU32(element.funcs.size)
                for (func in element.funcs) {
                    writer.writeU32(func.index)
                }
            }
        }
    }

    private fun writeDatas(writer: ModuleWriter) {
        if (datas.isNotEmpty()) {
            writer.writeU32(datas.size)
            for (data in datas) {
                if (data.offset == null) {
                    writer.writeU32(1)
                } else {
                    writer.writeU32(0)
                    data.offset.toWasm(writer)
                    writer.write(WasmOpcode.END)
                }
                writer.writeU32(data.data.size)
                writer.writeBytes(data.data)
            }
        }
    }

    private fun writeTables(writer: ModuleWriter) {
        val tables = tables.filterIsInstance<TableInterface>()
        if (tables.isNotEmpty()) {
            writer.writeU32(tables.size)
            for (table in tables) {
                table.type.toWasm(writer)
                val max = table.max
                if (max == null) {
                    writer.writeByte(0)
                    writer.writeU32(table.min)
                } else {
                    writer.writeByte(1)
                    writer.writeU32(table.min)
                    writer.writeU32(max)
                }
            }
        }
    }

    private fun writeSection(writer: ModuleWriter, section: WasmSection, writeSection: (ModuleWriter) -> Unit) {
        val sectionWriter = ModuleWriter(this)
        writeSection(sectionWriter)
        val bytes = sectionWriter.toByteArray()

        if (bytes.isNotEmpty()) {
            writer.writeByte(section.id)
            writer.writeU32(bytes.size)
            writer.writeBytes(bytes)
        }
    }


    fun toWasm(): ByteArray {
        val writer = ModuleWriter(this)

        // Magic
        writer.writeByte(0)
        writer.writeByte(0x61)
        writer.writeByte(0x73)
        writer.writeByte(0x6d)

        // Version
        writer.writeInt(1)

        // Sections
        writeSection(writer, WasmSection.TYPE, ::writeTypes)
        writeSection(writer, WasmSection.IMPORT, ::writeImports)
        writeSection(writer, WasmSection.FUNCTION, ::writeFunctions)
        writeSection(writer, WasmSection.TABLE, ::writeTables)
        writeSection(writer, WasmSection.MEMORY, ::writeMemory)
        writeSection(writer, WasmSection.GLOBAL, ::writeGlobals)
        writeSection(writer, WasmSection.EXPORT, ::writeExports)
        // Start
        // Elements
        writeSection(writer, WasmSection.DATA_COUNT, ::writeDataCount)
        writeSection(writer, WasmSection.CODE, ::writeCode)
        writeSection(writer, WasmSection.DATA, ::writeDatas)

        return writer.toByteArray()
    }


    fun toString(writer: CodeWriter) {
        writer.write("Module {")
        val inner = writer.indented()

        if (memory != null) {
            inner.newLine()
            if (memory is MemoryImport) {
                inner.write("ImportMemory(")
                inner.writeQuoted(memory.module)
                inner.write(", ")
                inner.writeQuoted(memory.name)
                inner.write(", ")
            } else {
                inner.write("Memory(")
            }
            inner.write(memory.min)
            if (memory.max != null) {
                inner.write(", ${memory.max}")
            }
            inner.write(")")
        }

        for (global in globals.filterIsInstance<GlobalImport>()) {
            global.writeImport(writer)
        }

        for (func in funcs.filterIsInstance<FuncImport>()) {
            func.writeImport(writer)
        }

        for (global in globals.filterIsInstance<GlobalImpl>()) {
            global.toString(writer)
        }


        for (func in funcs.filterIsInstance<FuncImpl>()) {
            func.toString(inner)
        }

        writer.newLine()

        for (export in exports) {
            writer.newLine()
            writer.write("Export(")
            writer.writeQuoted(export.name)
            writer.write(", ")
            export.value.writeExportDescription(writer)
            writer.write(")")
        }

        writer.newLine()
        writer.write("}\n")
    }

    override fun toString(): String {
        val writer = CodeWriter()
        toString(writer)
        return writer.toString()
    }
}