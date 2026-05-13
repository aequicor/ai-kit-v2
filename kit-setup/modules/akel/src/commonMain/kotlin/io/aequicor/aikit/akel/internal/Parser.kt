package io.aequicor.aikit.akel.internal

import io.aequicor.aikit.akel.AkelError

internal class Parser(@Suppress("UnusedPrivateProperty") private val source: String, private val tokens: List<Token>) {

    private var index: Int = 0

    fun parseExpression(): Node {
        val node = parseOr()
        val rest = peek()
        if (rest.kind != TokenKind.EOF) {
            throw AkelError.Syntax(rest.position, "unexpected '${rest.text}' after expression")
        }
        return node
    }

    private fun parseOr(): Node {
        var left = parseAnd()
        while (peek().kind == TokenKind.OR) {
            consume()
            left = Node.Or(left, parseAnd())
        }
        return left
    }

    private fun parseAnd(): Node {
        var left = parseIn()
        while (peek().kind == TokenKind.AND) {
            consume()
            left = Node.And(left, parseIn())
        }
        return left
    }

    private fun parseIn(): Node {
        val left = parseEq()
        if (peek().kind == TokenKind.IDENT_IN) {
            consume()
            return Node.In(left, parseEq())
        }
        return left
    }

    private fun parseEq(): Node {
        val left = parseCmp()
        val tk = peek().kind
        if (tk == TokenKind.EQ || tk == TokenKind.NEQ) {
            consume()
            return Node.Eq(left, parseCmp(), negated = tk == TokenKind.NEQ)
        }
        return left
    }

    private fun parseCmp(): Node {
        val left = parseUnary()
        val op = when (peek().kind) {
            TokenKind.LT -> CmpOp.LT
            TokenKind.LE -> CmpOp.LE
            TokenKind.GT -> CmpOp.GT
            TokenKind.GE -> CmpOp.GE
            else -> return left
        }
        consume()
        return Node.Cmp(left, parseUnary(), op)
    }

    private fun parseUnary(): Node {
        if (peek().kind == TokenKind.BANG) {
            consume()
            return Node.Not(parseUnary())
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Node {
        val t = peek()
        return when (t.kind) {
            TokenKind.BOOL -> consume().let { Node.BoolLit(it.boolValue) }
            TokenKind.INT -> consume().let { Node.IntLit(it.intValue) }
            TokenKind.DOUBLE -> consume().let { Node.DoubleLit(it.doubleValue) }
            TokenKind.STRING -> consume().let { Node.StringLit(it.stringValue) }
            TokenKind.REF -> consume().let { Node.Ref(path = it.stringValue) }
            TokenKind.LBRACKET -> parseListLiteral()
            TokenKind.LPAREN -> parseGrouped()
            else -> throw AkelError.Syntax(t.position, "unexpected '${t.text}'")
        }
    }

    private fun parseListLiteral(): Node {
        val open = consume()
        val elements = ArrayList<Node>()
        if (peek().kind != TokenKind.RBRACKET) {
            elements += parseOr()
            while (peek().kind == TokenKind.COMMA) {
                consume()
                elements += parseOr()
            }
        }
        if (peek().kind != TokenKind.RBRACKET) {
            throw AkelError.Syntax(open.position, "expected ']' to close list literal")
        }
        consume()
        return Node.ListLit(elements)
    }

    private fun parseGrouped(): Node {
        val open = consume()
        val inner = parseOr()
        if (peek().kind != TokenKind.RPAREN) {
            throw AkelError.Syntax(open.position, "expected ')' to close group")
        }
        consume()
        return inner
    }

    private fun peek(): Token = tokens[index]
    private fun consume(): Token = tokens[index++]
}
