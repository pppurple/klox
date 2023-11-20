package com.example.tool

import java.io.PrintWriter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    GenerateAst.generate(args)
}

class GenerateAst {
    companion object {
        fun generate(args: Array<String>) {
            if (args.size != 1) {
                System.err.println("Usage: generate_ast <output directory>")
                exitProcess(64)
            }

            val outputDir = args[0]
            defineAst(
                outputDir = outputDir,
                baseName = "Expr",
                types = listOf(
                    "Binary : left Expr, operator Token, right Expr ",
                    "Grouping : expression Expr",
                    "Literal : value Any",
                    "Unary : operator Token, right Expr",
                )
            )
        }

        private fun defineAst(
            outputDir: String,
            baseName: String,
            types: List<String>,
        ) {
            val path = "$outputDir/$baseName.kt"
            val writer = PrintWriter(path, "UTF-8")

            writer.println("package com.example.klox")
            writer.println()
            writer.println("interface $baseName {")

            // AST classes
            types.forEach { type ->
                val className = type.split(":")[0].trim()
                val param = type.split(":")[1].trim()

                defineType(writer, baseName, className, param)
            }

            writer.println("}")
            writer.close()
        }

        private fun defineType(
            writer: PrintWriter,
            baseName: String,
            className: String,
            param: String,
        ) {
            writer.println("    data class $className(")
            // params
            val params = param.split(", ")
            params.forEach {
                val name = it.split(" ")[0]
                val type = it.split(" ")[1]
                writer.println("        val $name: $type,")
            }
            writer.println("    ) : $baseName")
            writer.println()
        }
    }
}