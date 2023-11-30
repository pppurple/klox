package com.example.klox

class Environment {
    private val values = mutableMapOf<String, Any?>()

    fun get(name: Token): Any {
        if (values.containsKey(name.lexeme)) {
            return checkNotNull(values[name.lexeme])
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }
}