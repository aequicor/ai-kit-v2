package io.aequicor.aikit.akel.internal

import io.aequicor.aikit.akel.AkelContext
import io.aequicor.aikit.akel.AkelError
import io.aequicor.aikit.akel.AkelValue

internal object Evaluator {

    fun eval(node: Node, ctx: AkelContext): AkelValue = when (node) {
        is Node.BoolLit -> AkelValue.Bool(node.value)
        is Node.IntLit -> AkelValue.Int(node.value)
        is Node.DoubleLit -> AkelValue.Dbl(node.value)
        is Node.StringLit -> AkelValue.Str(node.value)
        is Node.ListLit -> AkelValue.Lst(node.elements.map { eval(it, ctx) })
        is Node.Ref -> ctx.lookup(node.path) ?: throw AkelError.UnknownRef(node.path)
        is Node.Not -> evalNot(node, ctx)
        is Node.And -> evalAnd(node, ctx)
        is Node.Or -> evalOr(node, ctx)
        is Node.Eq -> evalEq(node, ctx)
        is Node.Cmp -> evalCmp(node, ctx)
        is Node.In -> evalIn(node, ctx)
    }

    private fun evalNot(node: Node.Not, ctx: AkelContext): AkelValue {
        val v = eval(node.operand, ctx)
        val b = (v as? AkelValue.Bool) ?: throw AkelError.Type("operator '!' requires boolean, got ${v.type}")
        return AkelValue.Bool(!b.value)
    }

    private fun evalAnd(node: Node.And, ctx: AkelContext): AkelValue {
        val left = boolOf(eval(node.left, ctx), "&&")
        if (!left) return AkelValue.Bool(false)
        return AkelValue.Bool(boolOf(eval(node.right, ctx), "&&"))
    }

    private fun evalOr(node: Node.Or, ctx: AkelContext): AkelValue {
        val left = boolOf(eval(node.left, ctx), "||")
        if (left) return AkelValue.Bool(true)
        return AkelValue.Bool(boolOf(eval(node.right, ctx), "||"))
    }

    private fun evalEq(node: Node.Eq, ctx: AkelContext): AkelValue {
        val left = eval(node.left, ctx)
        val right = eval(node.right, ctx)
        if (left.type != right.type) {
            throw AkelError.Type("'${if (node.negated) "!=" else "=="}' requires same types, got ${left.type} and ${right.type}")
        }
        val equal = left == right
        return AkelValue.Bool(if (node.negated) !equal else equal)
    }

    private fun evalCmp(node: Node.Cmp, ctx: AkelContext): AkelValue {
        val left = eval(node.left, ctx)
        val right = eval(node.right, ctx)
        if (left.type != right.type) {
            throw AkelError.Type("comparison requires same types, got ${left.type} and ${right.type}")
        }
        val cmp = when (left) {
            is AkelValue.Int -> left.value.compareTo((right as AkelValue.Int).value)
            is AkelValue.Dbl -> left.value.compareTo((right as AkelValue.Dbl).value)
            is AkelValue.Str -> left.value.compareTo((right as AkelValue.Str).value)
            else -> throw AkelError.Type("comparison not supported for ${left.type}")
        }
        val result = when (node.op) {
            CmpOp.LT -> cmp < 0
            CmpOp.LE -> cmp <= 0
            CmpOp.GT -> cmp > 0
            CmpOp.GE -> cmp >= 0
        }
        return AkelValue.Bool(result)
    }

    private fun evalIn(node: Node.In, ctx: AkelContext): AkelValue {
        val element = eval(node.element, ctx)
        val list = eval(node.list, ctx) as? AkelValue.Lst
            ?: throw AkelError.Type("right side of 'in' must be a list")
        for (item in list.elements) {
            if (item.type != element.type) {
                throw AkelError.Type("'in' element type ${element.type} does not match list element type ${item.type}")
            }
            if (item == element) return AkelValue.Bool(true)
        }
        return AkelValue.Bool(false)
    }

    private fun boolOf(value: AkelValue, op: String): Boolean {
        return (value as? AkelValue.Bool)?.value
            ?: throw AkelError.Type("operator '$op' requires boolean, got ${value.type}")
    }
}
