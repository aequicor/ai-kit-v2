package io.aequicor.aikit.akel.internal

internal enum class TokenKind {
    BOOL, INT, DOUBLE, STRING, REF, IDENT_IN,
    LPAREN, RPAREN, LBRACKET, RBRACKET, COMMA,
    BANG, AND, OR, EQ, NEQ, LT, LE, GT, GE,
    EOF,
}

internal data class Token(
    val kind: TokenKind,
    val position: Int,
    val text: String,
    val boolValue: Boolean = false,
    val intValue: Int = 0,
    val doubleValue: Double = 0.0,
    val stringValue: String = "",
)
