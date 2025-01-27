package me.user.Evaluator.FunctionEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Error.Fail
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callFunction
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.Parser.CallExpression
import me.user.Parser.Identifier
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.EvaluatorUtils.*

fun evalCallExpression(node: Node, env: Environment): FoxObject? {
    val callExpression = node as CallExpression

    // 当调用函数是非匿名函数，使用callFunction调用函数
    if (callExpression.function!!::class == Identifier::class) {
        val name = (callExpression.function as Identifier).value

        val args = evalExpressions(node.arguments, env)
        if (args.size == 1 && isError(args[0])) {
            return args[0]
        }

        return callFunction(name, args, env)
    }

    // 若调用函数是匿名函数，则开始执行下面的操作
    val func = eval(callExpression.function, env)

    if (isError(func)) {
        return func
    }

    val args = evalExpressions(node.arguments, env)
    if (args.size == 1 && isError(args[0])) {
        return args[0]
    }

    return applyFunction(func, args)
}

fun applyFunction(func: FoxObject?, args: List<FoxObject?>): FoxObject? {
    // 检查 func 是否为 null，如果是则抛出异常
    if (func == null) return throwError("未知函数")

    return when (func.type()) {
        ObjectType.FUNCTION_OBJ -> {
            // 如果是 FoxFunction 将对象转为 FoxFunction 对象
            val f = func as FoxFunction

            var extendedEnv: Environment? = null
            try {
                // 扩展函数环境
                extendedEnv = extendFunctionEnv(f, args)
            } catch (e: Fail) {
                return throwError(e.message)
            }

            // 对欲运行的代码求值
            val evaluated = eval(f.body, extendedEnv)
            if (isError(evaluated)) return evaluated

            // 解包返回函数
            unwrapReturnValue(evaluated)
        }

        ObjectType.KT_FUNCTION_OBJ -> {
            // 如果是 FoxKotlinFunction 将对象转为 FoxKotlinFunction 对象
            val f = func as FoxKotlinFunction

            return callKotlinFunction(f, args)
        }
        else -> throwError("不是一个函数: ${func.type()}")
    }
}

fun extendFunctionEnv(func: FoxFunction, args: List<FoxObject?>): Environment {
    val env = Environment().apply { outer = func.env }
    val classFunctionCall = env.outer?.getValue("Me")
    val baseClassFunctionCall = env.outer?.getValue("MyBase")

    if (func.parameters == null) return env

//    if (args.size > func.parameters!!.size) {
//        env.setValue("", Data(throwError("提供的参数数量过多"), true))
//        return env
//    } else if (args.size < func.parameters!!.size) {
//        env.setValue("", Data(throwError("提供的参数数量过少"), true))
//        return env
//    }

    if (args.size > func.parameters!!.size) {
        throw Fail("提供的参数数量过多")
    } else if (args.size < func.parameters!!.size) {
        throw Fail("提供的参数数量过少")
    }

    for (parmaIndex in func.parameters!!.indices) {
        env.setValue((func.parameters!![parmaIndex] as Identifier).value,Data(args[parmaIndex]!!, false))
    }

    if (classFunctionCall?.exist == true && baseClassFunctionCall?.exist == true) {
        TODO("哦我拿着笔，想写点什么东西")
    }

    return env
}

fun unwrapReturnValue(obj: FoxObject?): FoxObject? {
    val returnValue = obj as? FoxReturnValue
    return returnValue?.returnValue ?: obj
}