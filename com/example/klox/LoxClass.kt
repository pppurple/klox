package com.example.klox

class LoxClass(
    val name: String,
    private val methods: Map<String, LoxFunction>,
) : LoxCallable {
    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }

    override fun toString(): String {
        return name
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
    }

    override fun arity(): Int {
        return 0
    }
}