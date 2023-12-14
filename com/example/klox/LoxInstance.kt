package com.example.klox

class LoxInstance(private val klass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()

    fun get(name: Token): Any {
        if (fields.containsKey(name.lexeme)) {
            return checkNotNull(fields[name.lexeme])
        }

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}