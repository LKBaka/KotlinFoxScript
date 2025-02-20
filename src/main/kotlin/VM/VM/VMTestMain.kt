package me.user.VM.VM

import me.user.Compiler.Compiler.Compiler
import me.user.Compiler.SymbolTable.SymbolTable
import me.user.Lexer.Lexer
import me.user.Object.FoxInteger
import me.user.Object.FoxObject
import me.user.Parser.Parser
import me.user.Runner.compileEval

private typealias testFunction = () -> Unit?

private val tests: Array<testFunction> = arrayOf(
    ::testVMVar
)

private fun testVMVar() {
    println("testVMVar")

    fun localGetGlobal(symbolTable: SymbolTable, globals: ArrayList<FoxObject>, constants: ArrayList<FoxObject>, expectedValue: FoxObject): Unit? {
        val result = compileEval("a", symbolTable, globals, constants)
        if (result != expectedValue) {
            println("fail: result = ${result?.inspect()}")
            return null
        }

        println("OK!")
        return null
    }

    fun localTest(): Nothing? {
        println("localTest")

        val table = SymbolTable(outer = SymbolTable())
        val globals = arrayListOf<FoxObject>()
        val constants = arrayListOf<FoxObject>()

        val parser = Parser(Lexer("let a = 1"))
        val program = parser.parseProgram()

        if (parser.checkParseError()) {return null}

        val compiler = Compiler(table.outer!!, constants)
        val compileResult = compiler.compile(program)
        compileResult?.onFailure { err -> println("compiler error: ${err.message}"); err.printStackTrace(); return null}

        val vm = VM(compiler.byteCode(), globals)
        val r = vm.run()
        r.onFailure { err -> println("runtime error: ${err.message}"); err.printStackTrace(); return null }

        val expectedValue = FoxInteger(1.toBigInteger())
        localGetGlobal(symbolTable = table, globals, constants, expectedValue)

        return null
    }

    localTest()
}

fun main() {
    for (test in tests) {
        test()
    }
}