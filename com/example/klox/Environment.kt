package com.example.klox

class Environment() {
    private var enclosing: Environment? = null

    constructor(enclosing: Environment) : this() {
        this.enclosing = enclosing
    }

    private val values = mutableMapOf<String, Any?>()

    fun get(name: Token): Any {
        if (values.containsKey(name.lexeme)) {
            return checkNotNull(values[name.lexeme])
        }

        if (enclosing != null) {
            // Smart cast to 'Environment' is impossible,
            // because 'enclosing' is a mutable property that could have been changed by this time
            return checkNotNull(enclosing).get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            // Smart cast to 'Environment' is impossible,
            // because 'enclosing' is a mutable property that could have been changed by this time
            checkNotNull(enclosing).assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun getAt(distance: Int, name: String): Any {
        return checkNotNull(ancestor(distance).values[name])
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0..distance) {
            // already checked if exists value in Resolver
            environment = checkNotNull(environment.enclosing)
        }
        return environment
    }
}