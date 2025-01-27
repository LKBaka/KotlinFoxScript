package me.user.Evaluator.IndexExpressionEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.Parser.IndexExpression
import me.user.Parser.InfixExpression
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError

private typealias indexExpressionEvaluator = (node: Node, env: Environment) -> FoxObject?
private val indexExpressionEvaluatorMap: Map<ObjectType, indexExpressionEvaluator> = mapOf(
    ObjectType.ARRAY_OBJ to ::evalArrayIndexExpression,
    ObjectType.DICTIONARY_OBJ to ::evalDictionaryIndexExpression
)

fun evalIndexExpression(node: Node, env: Environment): FoxObject? {
    val indexExpression = node as IndexExpression
    val left = eval(indexExpression.left, env)!!
    val index = eval(indexExpression.index, env)!!

    if (indexExpressionEvaluatorMap.containsKey(left.type())) {
        return indexExpressionEvaluatorMap[left.type()]?.invoke(indexExpression, env)
    }

    return throwError("不支持的操作: ${left.inspect()}[${index.inspect()}]")
}

fun evalArrayIndexExpression(node: Node, env: Environment): FoxObject? {
    val indexExpression = node as IndexExpression
    val left: FoxArray? = eval(indexExpression.left ,env) as FoxArray?
    val index = eval(indexExpression.index ,env)

    index?.let {
        return when (index.type()) {
            ObjectType.INTEGER_OBJ -> callKotlinFunction("get", arrayListOf(index), left!!.env)
            else -> throwError("不支持的操作: ${left!!.inspect()}[${index.inspect()}]")
        }
    }

    return null
}

fun evalDictionaryIndexExpression(node: Node, env: Environment): FoxObject? {
    val indexExpression = node as IndexExpression
    val left: FoxDictionary? = eval(indexExpression.left ,env) as FoxDictionary?
    if (isError(left)) return left

    val index = eval(indexExpression.index ,env)

    index?.let {
        return when (index.type()) {
            ObjectType.INTEGER_OBJ -> callKotlinFunction("get", arrayListOf(index), left!!.env)
            ObjectType.STRING_OBJ -> callKotlinFunction("get", arrayListOf(index), left!!.env)
            ObjectType.BOOLEAN_OBJ -> callKotlinFunction("get", arrayListOf(index), left!!.env)

            else -> throwError("不支持的操作: ${left!!.inspect()}[${index.inspect()}]")
        }
    }

    return null
}





