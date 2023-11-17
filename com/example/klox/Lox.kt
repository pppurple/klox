package com.example.klox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: klox [script]")
        exitProcess(64)
    } else if (args.size == 1) {
        Lox.runFile(args[0])
    } else {
        Lox.runPrompt()
    }
}

class Lox {
    companion object {
        private var hadError = false
        fun runFile(path: String) {
            val bytes = Files.readAllBytes(Paths.get(path))
            run(String(bytes, Charset.defaultCharset()))

            if (hadError) exitProcess(65)
        }

        fun runPrompt() {
            val input = InputStreamReader(System.`in`)
            val reader = BufferedReader(input)

            while (true) {
                print("> ")
                val line = reader.readLine() ?: break
                run(line)
                hadError = false // reset error
            }
        }

        fun run(source: String) {
            val scanner = Scanner(source)
            val tokens = scanner.scanTokens()

            tokens.forEach {
                println(it)
            }
        }

        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        fun report(
            line: Int,
            where: String,
            message: String,
        ) {
            println("[line: $line ] Error $where : $message")
            hadError = true
        }
    }


}