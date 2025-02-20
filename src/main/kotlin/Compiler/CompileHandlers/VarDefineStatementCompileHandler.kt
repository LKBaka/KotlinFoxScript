package me.user.Compiler.CompileHandlers

import me.user.Compiler.Compiler.Compiler
import me.user.Compiler.Compiler.handlerResult
import me.user.Compiler.OpCode.*
import me.user.Compiler.SymbolTable.GlobalScope
import me.user.Parser.Node
import me.user.Parser.VarDefineStatement

fun Compiler.compileVarDefineStatement(node: Node): handlerResult {
    return kotlin.runCatching {
        val varDefineStatement = node as VarDefineStatement

        val result = compile(varDefineStatement.value)?.getOrThrow()
        val symbol = this.symbolTable.define(varDefineStatement.identifier.toString())

        if (symbol.scope == GlobalScope) {
            this.emit(OpSetGlobal, symbol.index)
        } else {
            this.emit(OpSetLocal, symbol.index)
        }

        this.emit(OpPop)
        return null
    }
}