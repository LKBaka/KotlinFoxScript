package me.user.Compiler.SymbolTable

private fun testSymbolTable() {
    println("testSymbolTable:")

    fun testLocalSymbolTable(): Unit? {
        println("testLocalSymbolTable:")

        val globalSymbolTable = SymbolTable()

        val symbolTable = SymbolTable(globalSymbolTable)
        symbolTable.define("a")
        symbolTable.define("b")

        val expectASymbol = Symbol("a", LocalScope, 0)
        val expectBSymbol = Symbol("b", LocalScope, 1)

        val aResult: QueryResult = symbolTable.resolve("a")
        val bResult: QueryResult = symbolTable.resolve("b")

        if (!aResult.exist) {
            println("var a is not defined!")
            return null
        }

        if (!bResult.exist) {
            println("var b is not defined!")
            return null
        }

        require(aResult.symbol == expectASymbol) { println("Unexpected value a: ${aResult.symbol}"); return null }
        require(bResult.symbol == expectBSymbol) { println("Unexpected value b: ${bResult.symbol}"); return null }

        println("OK!")
        return null
    }

    fun testLocalSymbolTableOuter(): Unit? {
        println("testLocalSymbolTableOuter:")

        val globalSymbolTable = SymbolTable()
        globalSymbolTable.define("a")
        globalSymbolTable.define("b")

        val symbolTable = SymbolTable(globalSymbolTable)

        val expectASymbol = Symbol("a", GlobalScope, 0)
        val expectBSymbol = Symbol("b", GlobalScope, 1)

        val aResult: QueryResult = symbolTable.resolve("a")
        val bResult: QueryResult = symbolTable.resolve("b")

        if (!aResult.exist) {
            println("var a is not defined!")
            return null
        }

        if (!bResult.exist) {
            println("var b is not defined!")
            return null
        }

        require(aResult.symbol == expectASymbol) { println("Unexpected value a: ${aResult.symbol}"); return null }
        require(bResult.symbol == expectBSymbol) { println("Unexpected value b: ${bResult.symbol}"); return null }

        println("OK!")
        return null
    }

    testLocalSymbolTable()
    testLocalSymbolTableOuter()
}

fun main(){
    testSymbolTable()
}