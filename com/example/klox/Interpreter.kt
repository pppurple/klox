package com.example.klox

import com.example.klox.TokenType.BANG
import com.example.klox.TokenType.BANG_EQUAL
import com.example.klox.TokenType.EQUAL_EQUAL
import com.example.klox.TokenType.GREATER
import com.example.klox.TokenType.GREATER_EQUAL
import com.example.klox.TokenType.LESS
import com.example.klox.TokenType.LESS_EQUAL
import com.example.klox.TokenType.MINUS
import com.example.klox.TokenType.OR
import com.example.klox.TokenType.PLUS
import com.example.klox.TokenType.SLASH
import com.example.klox.TokenType.STAR

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment = globals
    private val locals = mutableMapOf<Expr, Int>()

    init {
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int = 0
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                return System.currentTimeMillis() / 1000.0
            }

            override fun toString(): String = "<native fn>"
        })
    }

    fun interpret(statements: List<Stmt?>) {
        try {
            statements.forEach {
                execute(it)
            }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            BANG_EQUAL -> !isEqual(left, right)
            EQUAL_EQUAL -> isEqual(left, right)
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            PLUS -> {
                if (left is Double && right is Double) {
                    left + right
                } else if (left is String && right is String) {
                    left + right
                } else {
                    throw RuntimeError(
                        expr.operator,
                        "Operands must be two numbers or two strings."
                    )
                }
            }

            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }

            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            else -> {
                // unreachable
                null
            }
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = expr.arguments.map {
            evaluate(it)
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expected ${callee.arity()} arguments but got ${arguments.size}.")
        }

        return callee.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any {
        val obj = evaluate(expr.obj)
        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }

        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)

        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }
        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any {
        val distance = checkNotNull(locals[expr])
        val superclass = environment.getAt(distance, "super") as LoxClass
        val obj = environment.getAt(distance - 1, "this") as LoxInstance
        val method = superclass.findMethod(expr.method.lexeme)

        if (method == null) {
            throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")
        }

        return method.bind(obj)
    }

    override fun visitThisExpr(expr: Expr.This): Any {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }

            else -> {
                // unreachable
                null
            }
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any {
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr): Any {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return
        throw RuntimeError(operator, "Operands must be a numbers.")
    }

    private fun isTruthy(o: Any?): Boolean {
        return when (o) {
            null -> false
            is Boolean -> o
            else -> true
        }
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null) return false

        return a == b
    }

    private fun stringify(o: Any?): String {
        if (o == null) return "nil"

        if (o is Double) {
            val text = o.toString()
            return if (text.endsWith(".0")) {
                text.substring(0, text.length - 2)
            } else {
                text
            }
        }

        return o.toString()
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt?) {
        stmt?.accept(this)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    fun executeBlock(
        statements: List<Stmt?>,
        environment: Environment,
    ) {
        val previous = this.environment
        try {
            this.environment = environment
            statements.forEach {
                execute(it)
            }
        } finally {
            this.environment = previous
        }

    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        var superclass: Any? = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }

        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = mutableMapOf<String, LoxFunction>()
        stmt.methods.forEach {
            val function = LoxFunction(it, environment, it.name.lexeme == "init")
            methods[it.name.lexeme] = function
        }

        val klass = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)

        if (superclass != null) {
            environment = checkNotNull(environment.enclosing)
        }

        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = if (stmt.value == null) {
            null
        } else {
            evaluate(stmt.value)
        }

        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = if (stmt.initializer != null) {
            evaluate(stmt.initializer)
        } else {
            null
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }
}