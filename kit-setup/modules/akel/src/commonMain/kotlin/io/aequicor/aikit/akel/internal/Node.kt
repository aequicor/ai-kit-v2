package io.aequicor.aikit.akel.internal

internal sealed interface Node {
    data class BoolLit(val value: Boolean) : Node
    data class IntLit(val value: Int) : Node
    data class DoubleLit(val value: Double) : Node
    data class StringLit(val value: String) : Node
    data class ListLit(val elements: List<Node>) : Node
    data class Ref(val path: String) : Node
    data class Not(val operand: Node) : Node
    data class And(val left: Node, val right: Node) : Node
    data class Or(val left: Node, val right: Node) : Node
    data class Eq(val left: Node, val right: Node, val negated: Boolean) : Node
    data class Cmp(val left: Node, val right: Node, val op: CmpOp) : Node
    data class In(val element: Node, val list: Node) : Node
}

internal enum class CmpOp { LT, LE, GT, GE }
