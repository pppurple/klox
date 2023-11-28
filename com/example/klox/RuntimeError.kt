package com.example.klox

class RuntimeError(
    val token: Token,
    message: String,
) : RuntimeException(message)