package me.user.Evaluator.InfixExpressionEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.Parser.InfixExpression
import me.user.Parser.Node
import me.user.Utils.BooleanUtils.isTruthy
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import me.user.Utils.ErrorUtils.throwError
import java.math.BigInteger

private typealias infixExpressionEvaluator = (node: Node, env: Environment) -> FoxObject?
private val infixExpressionEvaluatorMap: Map<ObjectType, infixExpressionEvaluator> = mapOf(
    ObjectType.INTEGER_OBJ to ::evalIntegerInfixExpression,
    ObjectType.STRING_OBJ to ::evalStringInfixExpression,
    ObjectType.DOUBLE_OBJ to ::evalDoubleInfixExpression,
    ObjectType.ARRAY_OBJ to ::evalArrayInfixExpression,
    ObjectType.DICTIONARY_OBJ to ::evalDictionaryInfixExpression,
    ObjectType.BOOLEAN_OBJ to ::evalBooleanInfixExpression
)

fun evalInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left = eval(infixExpression.left, env)!!
    val right = eval(infixExpression.right, env)!!

    if (infixExpressionEvaluatorMap.containsKey(left.type())) {
        return infixExpressionEvaluatorMap[left.type()]?.invoke(infixExpression, env)
    }

    return throwError("不支持的操作: ${left.inspect()} ${infixExpression.operator} ${right.inspect()}")
}

fun evalIntegerInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxInteger? = eval(infixExpression.left ,env) as FoxInteger?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "+" -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
        "-" -> callKotlinFunction("minus", arrayListOf(right), left!!.env)
        "*" -> callKotlinFunction("multiply", arrayListOf(right), left!!.env)
        "/" -> callKotlinFunction("divide", arrayListOf(right), left!!.env)
        "<" -> (callKotlinFunction("compareTo", arrayListOf(right), left!!.env)?.getValue() as BigInteger).let {
            return nativeBooleanToBooleanObject(it == (-1).toBigInteger())
        }
        ">" -> (callKotlinFunction("compareTo", arrayListOf(right), left!!.env)?.getValue() as BigInteger).let {
            return nativeBooleanToBooleanObject(it == 1.toBigInteger())
        }
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("equals", arrayListOf(right), left!!.env) ?.let {
            return nativeBooleanToBooleanObject(!isTruthy(it))
        }
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}

fun evalDoubleInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxDouble? = eval(infixExpression.left ,env) as FoxDouble?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "+" -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
        "-" -> callKotlinFunction("minus", arrayListOf(right), left!!.env)
        "*" -> callKotlinFunction("multiply", arrayListOf(right), left!!.env)
        "/" -> callKotlinFunction("divide", arrayListOf(right), left!!.env)
        "<" -> (callKotlinFunction("compareTo", arrayListOf(right), left!!.env)?.getValue() as BigInteger).let {
            return nativeBooleanToBooleanObject(it == (-1).toBigInteger())
        }
        ">" -> (callKotlinFunction("compareTo", arrayListOf(right), left!!.env)?.getValue() as BigInteger).let {
            return nativeBooleanToBooleanObject(it == 1.toBigInteger())
        }
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("equals", arrayListOf(right), left!!.env) ?.let {
            return nativeBooleanToBooleanObject(!isTruthy(it))
        }
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}

fun evalBooleanInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxBoolean? = eval(infixExpression.left ,env) as FoxBoolean?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("equals", arrayListOf(right), left!!.env) ?.let {
            return nativeBooleanToBooleanObject(!isTruthy(it))
        }
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}

fun evalStringInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxString? = eval(infixExpression.left ,env) as FoxString?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("notEquals", arrayListOf(right), left!!.env)
        "+" -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}

fun evalArrayInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxArray? = eval(infixExpression.left ,env) as FoxArray?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("equals", arrayListOf(right), left!!.env) ?.let {
            return nativeBooleanToBooleanObject(!isTruthy(it))
        }
        "+" -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}

fun evalDictionaryInfixExpression(node: Node, env: Environment): FoxObject? {
    val infixExpression = node as InfixExpression
    val left: FoxDictionary? = eval(infixExpression.left ,env) as FoxDictionary?
    val right = eval(infixExpression.right ,env)
    val op = infixExpression.operator

    return when (op) {
        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
        "!=" -> callKotlinFunction("equals", arrayListOf(right), left!!.env) ?.let {
            return nativeBooleanToBooleanObject(!isTruthy(it))
        }
        "+" -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
        else -> throwError("不支持的操作: ${left!!.inspect()} ${infixExpression.operator} ${right!!.inspect()}")
    }
}




