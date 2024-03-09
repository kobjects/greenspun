package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.FuncType

interface Callable {
    val type: FuncType
    val localContextSize: Int
    fun call(context: LocalRuntimeContext): Any

    operator fun invoke(vararg node: Any) =
        Call(this, *node.map { Node.of(it) }.toTypedArray())
}