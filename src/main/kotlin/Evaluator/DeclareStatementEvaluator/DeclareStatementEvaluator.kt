package me.user.Evaluator.DeclareStatementEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxKotlinFunction
import me.user.Object.FoxObject
import me.user.Object.ObjectType
import me.user.Object.Type
import me.user.Parser.DeclareStatement
import me.user.Parser.Identifier
import me.user.Parser.Node
import me.user.Parser.TypeExpression
import me.user.Utils.DLLUtils
import me.user.Utils.ErrorUtils.throwError

fun evalDeclareStatement(node: Node, env: Environment): FoxObject? {
    val declareStatement = node as DeclareStatement
    try {
        val nativeLibrary = declareStatement.dllPath?.let { DLLUtils.loadLibrary(it.value) }
        val func = nativeLibrary?.let { DLLUtils.findFunction(it, declareStatement.funcName?.value ?: "") }

        // 预期实参数量
        val expectedArgCount = declareStatement.parameters?.size ?: 0

        // 获取所有类型的表达式
        val types: ArrayList<Type> = arrayListOf()
        declareStatement.parameters?.forEach {
            if (it!!::class == Identifier::class) {
                types.add(Type(ObjectType.OBJ))
            } else if (it::class == TypeExpression::class) {
                types.add(Type(eval((it as TypeExpression).type, env)?.type() ?: ObjectType.OBJ))
            }
        }

        // 创建类型安全的函数包装
        val funcObject = FoxKotlinFunction(
            function = fun(args: List<FoxObject?>): FoxObject {
                val nativeArgs = args.map {
                    it?.getValue()
                }


                val result = func?.let { DLLUtils.callFunction(it, nativeArgs.toTypedArray()) }
                return FoxObject.fromNative(result)
            },
            paramsCount = expectedArgCount,
            paramTypes = types
        )

        // 注册到环境
         val bindingName = declareStatement.aliasFunctionName?.value ?: declareStatement.funcName?.value ?: ""
         env.setValue(bindingName, Data(funcObject, isReadonly = true))

        return null
    } catch (e: Exception) {
        return e.message?.let { throwError(it) }
    }
}