package me.user.Utils.EvaluatorUtils

import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxObject
import me.user.Parser.Expression
import me.user.Utils.ErrorUtils.isError

fun evalExpressions(expressions: ArrayList<Expression?>?, env: Environment): ArrayList<FoxObject?> {
    // 创建新列表
    val result = ArrayList<FoxObject?>()

    // 遍历表达式
    for (e in expressions!!) {

        // 对表达式求值
        val evaluated = eval(e, env)

        // 若为错误对象则返回
        if (isError(evaluated)) {
            val errorList = ArrayList<FoxObject?>()
            errorList.add(evaluated)
            return errorList
        }

        // 添加对象至列表中
        result.add(evaluated)
    }
    return result
}

