package me.user.Evaluator.VarDefineStatementEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxObject
import me.user.Parser.Identifier
import me.user.Parser.Node
import me.user.Parser.VarDefineStatement
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.StringUtils
import me.user.Utils.isInstance

fun evalVarDefineStatement(node: Node, env: Environment): FoxObject? {
    val expression = node as VarDefineStatement

    // 对变量的值进行求值操作
    val value = eval(expression.value, env) ?: return null
    if (isError(value)) return value // 判断是否为错误对象，如果是则返回

    expression.type?.let {
        val type = eval(it, env) ?: return null
        if (isError(value)) return value // 判断是否为错误对象，如果是则返回

        if (!isInstance(value, type)) return throwError("类型为 \"${value.type()}\" 的值无法转换为 \"${expression.type}\"")
    }

    // 设置变量
    env.setValue(StringUtils.Trim((expression.identifier as Identifier).value), Data(value, expression.isReadOnly), true)

    return null
}
