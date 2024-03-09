package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.types.Type

class BlockBuilder(variables: MutableList<Type>) : AbstractBlockBuilder(variables) {
    fun build() = Block(*statements.toTypedArray())
}