package com.savekirk.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val lox = Lox()
    when {
        args.size > 1 -> println("Usage: klox [stript]")
        args.size == 1 -> lox.runFile(args[0])
        else -> lox.runPrompt()
    }
}

class Lox {
    private var hadError = false


    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val expression = Parser(tokens).parse()

        // Indicate an error in the exit code
        if (hadError) System.exit(65)

            println(AstPrinter().print(expression))
    }

    companion object {
        private val lox = Lox()
        fun error(line: Int, message: String) {
            lox.report(line, "", message)
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                lox.report(token.line, " at end", message)
            } else {
                lox.report(token.line, " at ${token.lexeme}", message)
            }
        }
    }

    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }
}