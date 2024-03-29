package org.kobjects.greenspun.core.tree

abstract class AbstractBinaryOperation(
    val operator: BinaryOperator,
    val leftOperand: Node,
    val rightOperand: Node,
) : Node() {

    init {
        require(rightOperand.returnType == leftOperand.returnType) {
            "Second operand type ${rightOperand.returnType} does not match first operand type ${leftOperand.returnType}."
        }
    }

    final override fun toString(writer: CodeWriter) =
        when (operator) {
            BinaryOperator.MIN,
            BinaryOperator.MAX,
            BinaryOperator.COPYSIGN ->
                stringifyChildren(writer, "$operator(", ", ", ")")
            else ->
                stringifyChildren(writer, "(", " $operator ", ")")
        }

    final override fun children(): List<Node> = listOf(leftOperand, rightOperand)
}