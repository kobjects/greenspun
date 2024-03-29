package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.type.F64
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.I64
import org.kobjects.greenspun.core.type.Type

enum class UnaryOperator(
    val typeSupport: TypeSupport = TypeSupport.ALL,
    val deviantResultType: Type? = null
) {
    ABS(TypeSupport.FLOAT_ONLY),

    CEIL(TypeSupport.FLOAT_ONLY),
    CLZ(TypeSupport.INT_ONLY),
    CTZ(TypeSupport.INT_ONLY),

    FLOOR(TypeSupport.FLOAT_ONLY),
    POPCNT(TypeSupport.INT_ONLY),

    NEG,
    NEAREST(TypeSupport.FLOAT_ONLY),
    NOT(TypeSupport.INT_ONLY),

    SQRT(TypeSupport.FLOAT_ONLY),

    TO_I32(TypeSupport.ALL, I32),
    TO_I64(TypeSupport.ALL, I64),
    TO_F64(TypeSupport.ALL, F64),

    TRUNC(TypeSupport.FLOAT_ONLY);


    override fun toString() = name.titleCase()
}