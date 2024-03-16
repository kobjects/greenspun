package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.control.AbstractBlockBuilder
import org.kobjects.greenspun.core.module.ModuleBuilder

class FuncBuilder(
    val moduleBuilder: ModuleBuilder,
    val returnType: Type
) : AbstractBlockBuilder(mutableListOf()) {

    internal var paramCount = 0

    fun Param(type: Type): LocalReference {

        if (paramCount != variables.size) {
            throw IllegalStateException("Parameters can't be declared after local variables.")
        }

        if (statements.isNotEmpty()) {
            throw IllegalStateException("Parameters can't be declared after statements.")
        }

        val variable = LocalReference(variables.size, type)
        variables.add(type)

        paramCount++

        return variable
    }

    internal fun build() = Func(
        index = moduleBuilder.funcs.size,
        type = moduleBuilder.getFuncType(returnType, variables.subList(0, paramCount)),
        locals = variables.subList(paramCount, variables.size),
        body = Block(*statements.toTypedArray())
    )
}