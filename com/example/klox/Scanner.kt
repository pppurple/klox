package com.example.klox

import com.example.klox.TokenType.*

class Scanner(
    private val source: String,
) {
    private val tokens = mutableListOf<Token>()
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

    fun scanToken() {
        val c = advance()
        when (c) {
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

            else -> Lox.error(line, "Unexpected character.")
        }
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
}