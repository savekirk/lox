package com.savekirk.lox

/**
 * Parser for Lox
 *
 * Lox Expression Grammar
 * ======================
 * expression   -> literal | unary | binary | grouping ;
 * literal      -> NUMBER | STRING | "false" | "true" | "nil" ;
 * grouping     -> "(" expression ")" ;
 * unary        -> ( "-" | "!" ) expression ;
 * binary       -> expression operator expression ;
 * operator     -> "==" | "!=" | "<" | "<=" | ">" | ">=" | "+"
 *                 | "-" | "*" | "/" ;
 *
 * -------------------------------------
 *
 * expression     -> equality ;
 * equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
 * addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
 * multiplication -> unary ( ( "/" | "*" ) unary )* ;
 * unary          -> ( "!" | "-" ) unary ;
 *                   | primary ;
 * primary        -> NUMBER | STRING | "false" | "true" | "nil"
 *                   | "(" expression ")" ;
 */

class Parser(private val tokens: List<Token>) {
    private inner class ParseError : RuntimeException()

    private var current = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var expr: Expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            expr = Expr.Binary(expr, previous(), comparison())
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr: Expr = addition()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            expr = Expr.Binary(expr, previous(), addition())
        }

        return expr
    }

    private fun addition(): Expr {
        var expr: Expr = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            expr = Expr.Binary(expr, previous(), multiplication())
        }

        return expr
    }

    private fun multiplication(): Expr {
        var expr = unary()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            expr = Expr.Binary(expr, previous(), unary())
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            return Expr.Unary(operator, unary())
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")

            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(tokenType: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == tokenType
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR,
                TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
                else -> advance()
            }
        }
    }

}
