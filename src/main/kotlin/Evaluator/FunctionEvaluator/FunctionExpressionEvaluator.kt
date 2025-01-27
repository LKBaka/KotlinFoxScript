package me.user.Evaluator.FunctionEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Object.FoxFunction
import me.user.Object.FoxObject
import me.user.Parser.FunctionExpression
import me.user.Parser.Node

fun evalFunctionExpression(node: Node, env: Environment): FoxObject {
    val funcExpr = node as FunctionExpression
    val funcObject = FoxFunction()

    with (funcObject) {
        this.parameters = funcExpr.parameters
        this.paramsCount = funcExpr.parameters!!.count()
        this.body = funcExpr.body
    }

    funcObject.env.outer = env

    funcExpr.name?.let {
        env.setValue(funcExpr.name!!.value, Data(funcObject,true))
    }

    return funcObject
}