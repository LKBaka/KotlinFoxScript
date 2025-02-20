package me.user.Compiler.SymbolTable

private typealias SymbolScope = String

const val GlobalScope: SymbolScope = "GLOBAL"
const val LocalScope: SymbolScope = "LOCAL"

class QueryResult(val exist: Boolean, val symbol: Symbol?)
class QueryResults(val exist: Boolean, val symbols: ArrayList<Symbol>?)

class SymbolTable(val outer: SymbolTable? = null) {
    private val dataMap: MutableMap<String, Symbol> = mutableMapOf()
    private val funcDataMap: MutableMap<String, ArrayList<Symbol>> = mutableMapOf()
    var numDefinitions: Int = 0
    private var functionNumDefinitions: Int = 0

    fun define(name: String): Symbol {
        val scope = if (outer != null) LocalScope else GlobalScope

        val symbol = Symbol(name, scope, numDefinitions)
        dataMap[name] = symbol

        numDefinitions++
        return symbol
    }

    fun defineFunc(name: String): Symbol {
        val scope = if (outer != null) LocalScope else GlobalScope

        val symbol = Symbol(name, scope, numDefinitions)
        if (funcDataMap.containsKey(name)) {
            funcDataMap[name]?.add(symbol)
        } else {
            funcDataMap[name] = arrayListOf(symbol)
        }

        functionNumDefinitions++
        return symbol
    }

    fun resolve(name: String): QueryResult {
        dataMap[name]?.let { return QueryResult(true, it) }
        funcDataMap[name]?. let { return QueryResult(true, it[0]) }

        outer?.let {
            return it.resolve(name)
        }

        return QueryResult(false, null)
    }

    fun getValues(name: String): QueryResults {
        dataMap[name]?.let { return QueryResults(true, arrayListOf(it)) }
        funcDataMap[name]?. let { return QueryResults(true, it) }

        return QueryResults(false, null)
    }
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)
