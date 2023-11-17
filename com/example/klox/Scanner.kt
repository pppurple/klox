package com.example.klox

import com.example.klox.TokenType.*

class Scanner(
    private val source: String,
) {
    private val tokens = mutableListOf<Token>()
    private val keywords = mapOf(
        "and" to AND,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE,
    )
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) advance()
            } else {
                addToken(SLASH)
            }

            ' ',
            '\r',
            '\t' -> {
                // do nothing
            }

            '\n' -> line++
            '"' -> string()

            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isAlpha()) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        while (peek().isAlphaNumeric()) advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (peek().isDigit()) advance()

        // seek decimal part
        if (peek() == '.' && peekNext().isDigit()) {
            // consume decimal point
            advance()

            while (peek().isDigit()) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char {
        if (isAtEnd()) return Char.MIN_VALUE
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return Char.MIN_VALUE
        return source[current + 1]
    }

    private fun Char.isAlpha(): Boolean {
        return (this in 'a'..'z') ||
                (this in 'A'..'Z') ||
                this == '_'
    }

    private fun Char.isAlphaNumeric(): Boolean {
        return this.isAlpha() || this.isDigit()
    }

    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char = source[current++]

    private fun addToken(type: TokenType) = addToken(type, null)

    private fun addToken(
        type: TokenType,
        literal: Any?,
    ) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        advance() // consume right "

        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }
}