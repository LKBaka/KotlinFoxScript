package me.user.Evaluator.LoopEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.builtinEnvironment
import me.user.Evaluator.eval
import me.user.Object.FoxBoolean
import me.user.Object.FoxError
import me.user.Object.FoxObject
import me.user.Parser.Node
import me.user.Parser.WhileStatement
import me.user.Utils.ErrorUtils.isError

// While 循环求值逻辑
fun evalWhileStatement(node: Node, env: Environment): FoxObject? {
    val whileStmt = node as? WhileStatement

    // 计算初始条件
    var condObj = eval(whileStmt?.condition, env)
    if (isError(condObj)) return condObj

    // 使用CBool函数对对象进行转换
    condObj = callKotlinFunction("CBool", listOf(condObj), builtinEnvironment)
    if (isError(condObj)) return condObj

    // 循环执行逻辑
    while ((condObj as FoxBoolean).value) {
        // 重新计算条件
        condObj = eval(whileStmt?.condition, env)
        condObj = callKotlinFunction("CBool", listOf(condObj), builtinEnvironment)
        if (isError(condObj)) return condObj

        // 检查新条件是否终止循环，防止条件为False后继续执行
        if (!(condObj as FoxBoolean).value) break

        // 3. 执行循环体
        val result = eval(whileStmt?.loopBlock, env)

        when (result) {
            null -> continue          // 允许空结果继续循环
            is FoxError -> return result // 错误传播
            else -> result.inspect()    // 正常执行
        }
    }

    return null
}