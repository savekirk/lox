package com.savekirk.lox

fun main(args: Array<String>) {
    val expression = Expr.Binary(
            Expr.Unary(Token(TokenType.MINUS, "-", null, 1), Expr.Literal(123)),
            Token(TokenType.STAR, "*", null, 1),
            Expr.Grouping(Expr.Literal(45.67))
    )
    println(AstPrinter().print(expression))
}
// Creates an unambiguous string representation of AST nodes
class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr) = expr.accept(this)

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
       if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()

        builder.append("($name")
        for (exp in exprs) {
            builder.append(" ${exp.accept(this)}")
        }
        builder.append(")")

        return builder.toString()
    }
}