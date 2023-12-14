package com.example.klox

class LoxInstance(val klass: LoxClass) {

    override fun toString(): String {
        return "${klass.name} instance"
    }
}