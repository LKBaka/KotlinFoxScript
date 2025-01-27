package me.user.Evaluator.DeclareStatementEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxKotlinFunction
import me.user.Object.FoxObject
import me.user.Object.ObjectType
import me.user.Object.Type
import me.user.Parser.DeclareStatement
import me.user.Parser.Node
import me.user.Parser.TypeExpression
import me.user.Utils.DLLUtils
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.ObjNothing

fun evalDeclareStatement(node: Node, env: Environment): FoxObject? {
    val declareStatement = node as DeclareStatement
    try {
        val nativeLibrary = declareStatement.dllPath?.let { DLLUtils.loadLibrary(it.value) }
        val func = nativeLibrary?.let { DLLUtils.findFunction(it, declareStatement.funcName?.value ?: "") }
        // 参数数量校验
        val expectedArgCount = declareStatement.parameters?.size ?: 0

        // 创建类型安全的函数包装
        val funcObject = FoxKotlinFunction(
            function = { args ->
                // 参数校验（显式返回 FoxObject）
                if (args.size != expectedArgCount) {
                    throwError("找不到合适的重载")
                }

                // 类型转换（处理可能的 null）
                val nativeArgs = args.map {
                    it?.getValue()
                }

                // 执行调用并保证返回类型
                when (val result = func?.let { nativeArgs.toTypedArray().let { it1 -> DLLUtils.callFunction(it, *arrayOf(
                    it1
                )) } }) {
                    null -> ObjNothing // 处理 null 返回值
                    else -> FoxObject.fromNative(result)
                        ?: throwError("暂不支持的返回值类型: ${result::class.java.simpleName}")
                }
            },
            argsCount = expectedArgCount,
            argTypes = declareStatement.parameters?.map {
                it?.let {
                    if (it::class == TypeExpression::class) {
                        val typeExpression = it as TypeExpression
                        Type(eval(typeExpression.type, env)?.type() ?: ObjectType.OBJ)
                    }
                }

                Type(ObjectType.OBJ)
            } ?: emptyList()
        )

        // 注册到环境
//        val bindingName = declareStatement.aliasFunctionName?.value ?: declareStatement.funcName?.value ?: ""
//        env.setValue(bindingName, Data(funcObject, isReadonly = true))

        return funcObject
    } catch (e: Exception) {
        return e.message?.let { throwError(it) }
    }
}