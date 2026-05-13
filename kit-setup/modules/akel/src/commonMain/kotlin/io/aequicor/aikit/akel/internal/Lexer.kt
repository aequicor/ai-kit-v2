package io.aequicor.aikit.akel.internal

import io.aequicor.aikit.akel.AkelError

private const val IN_KEYWORD = "in"
private const val TRUE_KEYWORD = "true"
private const val FALSE_KEYWORD = "false"
private const val TWO_CHAR_LEN = 2

internal class Lexer(private val source: String) {

    private var pos: Int = 0

    fun tokenize(): List<Token> {
        val tokens = ArrayList<Token>()
        while (pos < source.length) {
            if (source[pos].isWhitespace()) {
                pos++
                continue
            }
            tokens += nextToken()
        }
        tokens += Token(TokenKind.EOF, pos, "")
        return tokens
    }

    private fun nextToken(): Token {
        val c = source[pos]
        return readPunct(c)
            ?: readOperator(c)
            ?: readLiteralOrRef(c)
            ?: throw AkelError.Syntax(pos, "unexpected character '$c'")
    }

    private fun readPunct(c: Char): Token? = when (c) {
        '(' -> single(TokenKind.LPAREN, "(")
        ')' -> single(TokenKind.RPAREN, ")")
        '[' -> single(TokenKind.LBRACKET, "[")
        ']' -> single(TokenKind.RBRACKET, "]")
        ',' -> single(TokenKind.COMMA, ",")
        else -> null
    }

    private fun readOperator(c: Char): Token? = when (c) {
        '!' -> readTwoChar(second = '=', whenDouble = TokenKind.NEQ, whenSingle = TokenKind.BANG)
        '=' -> readTwoChar(second = '=', whenDouble = TokenKind.EQ, whenSingle = null)
        '<' -> readTwoChar(second = '=', whenDouble = TokenKind.LE, whenSingle = TokenKind.LT)
        '>' -> readTwoChar(second = '=', whenDouble = TokenKind.GE, whenSingle = TokenKind.GT)
        '&' -> readTwoChar(second = '&', whenDouble = TokenKind.AND, whenSingle = null)
        '|' -> readTwoChar(second = '|', whenDouble = TokenKind.OR, whenSingle = null)
        else -> null
    }

    private fun readLiteralOrRef(c: Char): Token? = when {
        c == '\'' -> readString()
        c == '$' -> readRef()
        isNumberStart(c) -> readNumber()
        c.isLetter() || c == '_' -> readWord()
        else -> null
    }

    private fun isNumberStart(c: Char): Boolean = c.isDigit() ||
        (c == '-' && pos + 1 < source.length && source[pos + 1].isDigit() && precedingAllowsUnaryMinus())

    private fun precedingAllowsUnaryMinus(): Boolean {
        var i = pos - 1
        while (i >= 0 && source[i].isWhitespace()) i--
        if (i < 0) return true
        val prev = source[i]
        return prev == '(' || prev == ',' || prev == '[' || prev == '=' || prev == '!' ||
            prev == '<' || prev == '>' || prev == '&' || prev == '|'
    }

    private fun single(kind: TokenKind, text: String): Token {
        val t = Token(kind, pos, text)
        pos += text.length
        return t
    }

    private fun readTwoChar(second: Char, whenDouble: TokenKind, whenSingle: TokenKind?): Token {
        val isDouble = pos + 1 < source.length && source[pos + 1] == second
        if (isDouble) {
            val text = "${source[pos]}$second"
            val t = Token(whenDouble, pos, text)
            pos += TWO_CHAR_LEN
            return t
        }
        if (whenSingle == null) {
            throw AkelError.Syntax(pos, "expected '${source[pos]}$second'")
        }
        return single(whenSingle, source[pos].toString())
    }

    private fun readString(): Token {
        val start = pos
        pos++
        val sb = StringBuilder()
        while (pos < source.length && source[pos] != '\'') {
            sb.append(source[pos])
            pos++
        }
        if (pos >= source.length) {
            throw AkelError.Syntax(start, "unterminated string literal")
        }
        pos++
        return Token(TokenKind.STRING, start, "'$sb'", stringValue = sb.toString())
    }

    private fun readRef(): Token {
        val start = pos
        if (pos + 1 >= source.length || source[pos + 1] != '{') {
            throw AkelError.Syntax(start, "expected '\${...}' reference")
        }
        val pathStart = start + TWO_CHAR_LEN
        var cursor = scanIdentifier(pathStart)
        while (cursor < source.length && source[cursor] == '.') {
            cursor = scanIdentifier(cursor + 1)
        }
        if (cursor >= source.length || source[cursor] != '}') {
            throw AkelError.Syntax(cursor, "expected '}' to close reference")
        }
        val path = source.substring(pathStart, cursor)
        pos = cursor + 1
        return Token(TokenKind.REF, start, source.substring(start, pos), stringValue = path)
    }

    private fun scanIdentifier(from: Int): Int {
        if (from >= source.length || !(source[from].isLetter() || source[from] == '_')) {
            throw AkelError.Syntax(from, "expected identifier in reference path")
        }
        var cursor = from + 1
        while (cursor < source.length) {
            val ch = source[cursor]
            if (!(ch.isLetterOrDigit() || ch == '_' || ch == '-')) break
            cursor++
        }
        return cursor
    }

    private fun readNumber(): Token {
        val start = pos
        if (source[pos] == '-') pos++
        while (pos < source.length && source[pos].isDigit()) pos++
        val isDouble = pos < source.length && source[pos] == '.' &&
            pos + 1 < source.length && source[pos + 1].isDigit()
        if (isDouble) {
            pos++
            while (pos < source.length && source[pos].isDigit()) pos++
            val text = source.substring(start, pos)
            val v = text.toDoubleOrNull() ?: throw AkelError.Syntax(start, "invalid double '$text'")
            return Token(TokenKind.DOUBLE, start, text, doubleValue = v)
        }
        val text = source.substring(start, pos)
        val v = text.toIntOrNull() ?: throw AkelError.Syntax(start, "invalid integer '$text'")
        return Token(TokenKind.INT, start, text, intValue = v)
    }

    private fun readWord(): Token {
        val start = pos
        while (pos < source.length && (source[pos].isLetterOrDigit() || source[pos] == '_')) pos++
        return when (val text = source.substring(start, pos)) {
            TRUE_KEYWORD -> Token(TokenKind.BOOL, start, text, boolValue = true)
            FALSE_KEYWORD -> Token(TokenKind.BOOL, start, text, boolValue = false)
            IN_KEYWORD -> Token(TokenKind.IDENT_IN, start, text)
            else -> throw AkelError.Syntax(start, "unknown identifier '$text'")
        }
    }
}
