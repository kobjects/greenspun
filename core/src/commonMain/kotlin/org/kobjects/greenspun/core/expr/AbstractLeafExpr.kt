package org.kobjects.greenspun.core.expr

abstract class AbstractLeafExpr : Expr() {
    final override fun children() = emptyList<Expr>()

    override fun reconstruct(newChildren: List<Expr>) = this
}