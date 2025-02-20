package me.user.VM.InfixExpressionVM

import me.user.Compiler.OpCode
import me.user.Object.*
import me.user.Utils.ErrorUtils.throwError

private typealias infixExpressionOpHandler = (left: FoxObject?, right: FoxObject?, op: OpCode) -> FoxObject?
private val infixExpressionEvaluatorMap: Map<ObjectType, infixExpressionOpHandler> = mapOf(
    ObjectType.INTEGER_OBJ to ::evalIntegerInfixExpression,
)

private val opCodeStringMap: Map<OpCode, String> = mapOf(
    OpCode.OpAdd to "+"
)

fun infixExpressionOpHandler(left: FoxObject?, right: FoxObject?, op: OpCode): FoxObject? {
    if (infixExpressionEvaluatorMap.containsKey(left?.type())) {
        return infixExpressionEvaluatorMap[left?.type()]?.invoke(left, right, op)
    }

    val opString = opCodeStringMap[op]
    return throwError("不支持的操作: ${left?.inspect()} $opString ${right?.inspect()}")
}

fun evalIntegerInfixExpression(left: FoxObject?, right: FoxObject?, op: OpCode): FoxObject {
    return throwError("不支持的操作: ${left!!.inspect()} ${opCodeStringMap[op]} ${right!!.inspect()}")

//    return when (op) {
//        OpCode.OpAdd -> callKotlinFunction("plus", arrayListOf(right), left!!.env)
//        "-" -> callKotlinFunction("minus", arrayListOf(right), left!!.env)
//        "*" -> callKotlinFunction("multiply", arrayListOf(right), left!!.env)
//        "/" -> callKotlinFunction("divide", arrayListOf(right), left!!.env)
//        "<" -> callKotlinFunction("lessThan", arrayListOf(right), left!!.env)
//        ">" -> callKotlinFunction("moreThan", arrayListOf(right), left!!.env)
//        "==" -> callKotlinFunction("equals", arrayListOf(right), left!!.env)
//        "!=" -> callKotlinFunction("notEquals", arrayListOf(right), left!!.env)
//        else -> throwError("不支持的操作: ${left!!.inspect()} ${opCodeStringMap[op]} ${right!!.inspect()}")
//    }
}
