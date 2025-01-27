package me.user.Evaluator.IfExpression

import me.user.Environment.Environment
import me.user.Object.FoxObject
import me.user.Parser.IfExpression
import me.user.Utils.BooleanUtils.isTruthy
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ObjNothing
import me.user.Evaluator.eval
import me.user.Parser.Node

fun evalIfExpression(node: Node, env: Environment): FoxObject? {
    val expression = node as IfExpression

    // 获取条件表达式
    val condition = eval(expression.condition, env)

    // 是否为错误对象
    if (isError(condition)) return condition

    if (isTruthy(condition)) {
        // 为真则返回默认条件代码块
        return eval(expression.consequence, env)
    } else if (expression.elseif_array!= null) {
        // 遍历分支列表
        for (elseifExp in expression.elseif_array!!) {
            // 求条件的值
            val cond = eval(elseifExp.condition, env)

            // 条件为真
            if (isTruthy(cond)) {
                // 返回分支代码块
                return eval(elseifExp.consequence, env)
            }
        }

        if (expression.alternative!= null) {
            // 条件不为真，但是有 Else 分支代码块
            // 返回 Else 分支代码块
            return eval(expression.alternative, env)
        }
    } else if (expression.alternative!= null) {
        // 条件不为真，但是有 Else 分支代码块
        // 返回 Else 分支代码块
        return eval(expression.alternative, env)
    }
    // 都不是
    return ObjNothing
}