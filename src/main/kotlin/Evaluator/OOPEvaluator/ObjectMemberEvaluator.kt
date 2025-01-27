package me.user.Evaluator.OOPEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxObject
import me.user.Parser.Node
import me.user.Parser.ObjectMemberExpression
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.getProperty
import me.user.Utils.getPropertyValue
import me.user.Utils.hasMember

fun evalObjectMemberExpression(node: Node, env: Environment): FoxObject? {
    val objectMemberExpression = node as ObjectMemberExpression
    return objectMemberExpression.left?.let {
        val obj = eval(it, env)
        if (isError(obj)) return obj

        if (!obj!!.hasMember("env")) return throwError("成员访问不支持类型为 ${obj.type()} 的对象")

        val objEnv: Environment = obj.getPropertyValue("env") as Environment

        return eval(objectMemberExpression.right, objEnv)
    }
}