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
                    "Assign : name Token, value Expr",
                    "Binary : left Expr, operator Token, right Expr",
                    "Call : callee Expr, paren Token, arguments List<Expr>",
                    "Get : obj Expr, name Token",
                    "Grouping : expression Expr",
                    "Literal : value Any?",
                    "Logical : left Expr, operator Token, right Expr",
                    "Set : obj Expr, name Token, value Expr",
                    "Unary : operator Token, right Expr",
                    "Variable : name Token",
                )
            )

            defineAst(
                outputDir = outputDir,
                baseName = "Stmt",
                types = listOf(
                    "Block : statements List<Stmt?>",
                    "Class : name Token, methods List<Stmt.Function>",
                    "Expression : expression Expr",
                    "Function : name Token, params List<Token>, body List<Stmt?>",
                    "If : condition Expr, thenBranch Stmt, elseBranch Stmt?",
                    "Print : expression Expr",
                    "Return : keyword Token, value Expr?",
                    "Var : name Token, initializer Expr?",
                    "While : condition Expr, body Stmt",
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

            defineVisitor(writer, baseName, types)

            // fun accept() in base class
            writer.println()
            writer.println("    fun <R> accept(visitor: Visitor<R>): R")
            writer.println()

            // AST classes
            types.forEach { type ->
                val className = type.split(":")[0].trim()
                val param = type.split(":")[1].trim()

                defineType(writer, baseName, className, param)
            }

            writer.println("}")
            writer.close()
        }

        private fun defineVisitor(
            writer: PrintWriter,
            baseName: String,
            types: List<String>,
        ) {
            writer.println("    interface Visitor<R> {")
            types.forEach {
                val typeName = it.split(":")[0].trim()
                writer.println("        fun visit$typeName$baseName(${baseName.lowercase()}: $typeName): R")
            }
            writer.println("    }")
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
            writer.println("    ) : $baseName {")
            // implement accept()
            writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
            writer.println("            return visitor.visit$className$baseName(this)")
            writer.println("        }")
            writer.println("    }")
            writer.println()
        }
    }
}