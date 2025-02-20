package me.user.Compiler.Test

import me.user.Compiler.Compiler.Compiler
import me.user.Compiler.SymbolTable.SymbolTable
import me.user.Lexer.Lexer
import me.user.Parser.*
import me.user.Utils.toByteArrayList

private fun testCompiler() {

    fun testCompilerCompiledByteCode() {
        println("testCompilerCompiledByteCode:")
        fun testIdent(): Unit? {
            println("testIdent (Local):")

            val code = "let a = 1; a"
            val parser = Parser(Lexer(code))
            val program = parser.parseProgram()
            if (parser.checkParseError()) {
                return null
            }

            val compiler = Compiler(SymbolTable(outer = SymbolTable()))
            val compileResult = compiler.compile(program)
            compileResult?.onFailure { err -> println("fail: ${err.message}"); err.printStackTrace(); return null}

            val byteCode = compiler.byteCode()
            if (byteCode.instructions != toByteArrayList(arrayListOf(0, 0, 0, 8, 0, 2, 9, 0, 2))) {
                println("fail: ${byteCode.instructions}")
                return null
            }

            println("OK!")
            return null
        }

        fun testVarDefineStatement(): Unit? {
            println("testVarDefineStatement (Local):")
            val program = Program().apply {
                this.statements.add(
                    VarDefineStatement().apply {
                        this.identifier = Identifier("a")
                        this.value = IntegerLiteral(0.toBigInteger())
                    }
                )
            }

            val compiler = Compiler(SymbolTable(outer = SymbolTable()))
            val compilerResult = compiler.compile(program)
            compilerResult?.onFailure { err -> err.printStackTrace() ;return null }

            val byteCode = compiler.byteCode()
            if (byteCode.instructions != toByteArrayList(arrayListOf(0, 0, 0, 8, 0, 2))) {
                println("fail: ${byteCode.instructions}")
                return null
            }

            println("OK!")

            return null
        }

        testIdent()
        testVarDefineStatement()
    }

    testCompilerCompiledByteCode()
}

fun main() {
    testCompiler()
}