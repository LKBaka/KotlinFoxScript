package me.user.Compiler.CompileHandlers

import me.user.Compiler.Compiler.Compiler
import me.user.Compiler.OpCode
import me.user.Compiler.Compiler.handlerResult
import me.user.Object.FoxCompiledFunction
import me.user.Parser.FunctionExpression
import me.user.Parser.Node

fun Compiler.compileFunctionExpression(node: Node): handlerResult {
    return runCatching {
        // 转换节点
        val functionExpression = node as FunctionExpression

        // 进入作用域
        enterScope()

        // 编译函数中所有的代码
        val compileResult = compile(functionExpression.body)
        compileResult?.let { return@let compileResult }

        if (lastInstructionIs(OpCode.OpPop)) {
            replaceLastPopWithReturn()
        }

        if (!lastInstructionIs(OpCode.OpReturnValue)) {
            emit(OpCode.OpReturn)
        }

        // 获取所有局部绑定的数量
        val numLocals = symbolTable.numDefinitions

        // 退出作用域并保存所有指令
        val instructions = leaveScope()

        // 创建函数
        val compiledFunction = FoxCompiledFunction() .apply {
            this.instructions = instructions
            this.numLocals = numLocals
        }

        emit(OpCode.OpConstant, addConstant(compiledFunction))

        functionExpression.name?.let {
            symbolTable.defineFunc(it.value)
            emit(OpCode.OpSetGlobal)
        }
    }
}