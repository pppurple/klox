package com.example.klox

import com.example.klox.Lox.ParserError
import com.example.klox.TokenType.AND
import com.example.klox.TokenType.BANG
import com.example.klox.TokenType.BANG_EQUAL
import com.example.klox.TokenType.CLASS
import com.example.klox.TokenType.COMMA
import com.example.klox.TokenType.DOT
import com.example.klox.TokenType.ELSE
import com.example.klox.TokenType.EOF
import com.example.klox.TokenType.EQUAL
import com.example.klox.TokenType.EQUAL_EQUAL
import com.example.klox.TokenType.FALSE
import com.example.klox.TokenType.FOR
import com.example.klox.TokenType.FUN
import com.example.klox.TokenType.GREATER
import com.example.klox.TokenType.GREATER_EQUAL
import com.example.klox.TokenType.IDENTIFIER
import com.example.klox.TokenType.IF
import com.example.klox.TokenType.LEFT_BRACE
import com.example.klox.TokenType.LEFT_PAREN
import com.example.klox.TokenType.LESS
import com.example.klox.TokenType.LESS_EQUAL
import com.example.klox.TokenType.MINUS
import com.example.klox.TokenType.NIL
import com.example.klox.TokenType.NUMBER
import com.example.klox.TokenType.OR
import com.example.klox.TokenType.PLUS
import com.example.klox.TokenType.PRINT
import com.example.klox.TokenType.RETURN
import com.example.klox.TokenType.RIGHT_BRACE
import com.example.klox.TokenType.RIGHT_PAREN
import com.example.klox.TokenType.SEMICOLON
import com.example.klox.TokenType.SLASH
import com.example.klox.TokenType.STAR
import com.example.klox.TokenType.STRING
import com.example.klox.TokenType.SUPER
import com.example.klox.TokenType.THIS
import com.example.klox.TokenType.TRUE
import com.example.klox.TokenType.VAR
import com.example.klox.TokenType.WHILE

class Parser(
    private val tokens: List<Token>
) {
    private var current: Int = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (e: ParserError) {
            null
        }
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)
        if (match(NUMBER, STRING)) return Expr.Literal(previous().literal)
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        types.forEach {
            if (check(it)) {
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

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    /**
     * Consume current token and return it
     */
    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    /**
     * Return current token without consuming
     */
    private fun peek(): Token {
        return tokens[current]
    }

    /**
     * Return the latest consumed token
     */
    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): ParserError {
        Lox.error(token, message)
        return ParserError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return

            when (peek().type) {
                CLASS,
                FOR,
                FUN,
                IF,
                PRINT,
                RETURN,
                VAR,
                WHILE -> return

                LEFT_PAREN,
                RIGHT_PAREN,
                LEFT_BRACE,
                RIGHT_BRACE,
                COMMA,
                DOT,
                MINUS,
                PLUS,
                SEMICOLON,
                SLASH,
                STAR,
                BANG,
                BANG_EQUAL,
                EQUAL,
                EQUAL_EQUAL,
                GREATER,
                GREATER_EQUAL,
                LESS,
                LESS_EQUAL,
                IDENTIFIER,
                STRING,
                NUMBER,
                AND,
                ELSE,
                FALSE,
                NIL,
                OR,
                SUPER,
                THIS,
                TRUE,
                EOF -> advance()
            }
        }
    }
}