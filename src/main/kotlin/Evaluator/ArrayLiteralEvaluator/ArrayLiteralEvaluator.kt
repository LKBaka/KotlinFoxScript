package me.user.Evaluator.ArrayLiteralEvaluator

import me.user.Environment.Environment
import me.user.Object.FoxArray
import me.user.Object.FoxObject
import me.user.Parser.ArrayLiteral
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.EvaluatorUtils.evalExpressions

fun evalArrayLiteral(node: Node, env: Environment): FoxObject?{
    val arrayLiteral = node as ArrayLiteral
    val objects = evalExpressions(arrayLiteral.expressions, env)

    for (obj in objects) {
        if (isError(obj)) return obj
    }

    val arrayObject = FoxArray(objects)
    return arrayObject
}