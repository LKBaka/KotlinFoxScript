package me.user.PrefixExpressionEvaluator

import me.user.Environment.Environment
import me.user.Object.FoxObject
import me.user.Parser.Node
import me.user.Parser.PrefixExpression
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.OperatorExtension.not
import me.user.Utils.BooleanUtils.FoxFalse
import me.user.Utils.BooleanUtils.FoxTrue
import me.user.Utils.ErrorUtils.throwError

fun evalPrefixExpression(node: Node?, env: Environment): FoxObject? {
    val prefixExpression = node as PrefixExpression

    return when (prefixExpression.operator) {
        "-" -> evalMinusPrefixOperatorExpression(node, env)
        "!" -> evalBangOperatorExpression(node, env)
        else -> throwError("不支持的操作: ${prefixExpression.operator} ${prefixExpression.right}")
    }
}

private fun evalMinusPrefixOperatorExpression(node: Node?, env: Environment): FoxObject? {
    val prefixExpression = node as PrefixExpression
    val right: FoxObject = eval(prefixExpression.right, env) ?: return null

    return when (right.type()) {
        ObjectType.INTEGER_OBJ -> FoxInteger(-(right as FoxInteger).value)
        ObjectType.BOOLEAN_OBJ -> FoxInteger(-(if ((right as FoxBoolean).value) 1 else 0).toBigInteger())
        else -> null
    }
}

private fun evalBangOperatorExpression(node: Node?, env: Environment): FoxObject? {
    val prefixExpression = node as PrefixExpression
    val right: FoxObject = eval(prefixExpression.right, env) ?: return null

    return when (right.type()) {
        ObjectType.INTEGER_OBJ -> {
            val integerObject = right as FoxInteger
            if (integerObject.value != 0.toBigInteger()) {
                FoxFalse
            } else {
                FoxTrue
            }
        }
        ObjectType.BOOLEAN_OBJ -> FoxBoolean(!(right as FoxBoolean).value)
        ObjectType.STRING_OBJ -> FoxBoolean(!(right as FoxString).value)
        else -> null
    }
}