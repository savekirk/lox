package com.savekirk.tool

import java.io.PrintWriter
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory")
        exitProcess(1)
    }
    val outputDir = args[0]
    val generateAst = GenerateAst()
    generateAst.defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   -> val left: Expr, private val operator: Token, private val right: Expr",
            "Grouping -> val expression: Expr",
            "Literal  -> val value: Any",
            "Unary    -> val operator: Token, private val right: Expr"
    ))
}

/**
 * Helper class for generating AST types
 */
class GenerateAst {
    fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")

        writer.println("package com.savekirk.lox")
        writer.println("")
        writer.println("abstract class $baseName {")

        defineVisitor(writer, baseName, types)

        // The AST classes
        for (type in types) {
            val className = type.split("->")[0].trim()
            val fields = type.split("->")[1].trim()
            defineType(writer, baseName, className, fields)
        }

        writer.println("")
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
        writer.println("}")
        writer.close()
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<out R> {")

        types
                .map { it.split("->")[0].trim() }
                .forEach { writer.println("        fun visit$it$baseName(${baseName.toLowerCase()}: $it): R") }
        writer.println("    }")
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        writer.println("")
        writer.println("    class $className($fieldList) : $baseName() {")
        writer.println("")

        // Visitor Pattern
        writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
        writer.println("          return visitor.visit$className$baseName(this)")
        writer.println("        }")
        writer.println("    }")
    }


}