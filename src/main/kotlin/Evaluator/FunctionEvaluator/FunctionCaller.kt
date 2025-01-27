package me.user.Evaluator.FunctionEvaluator.FunctionCaller

import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.applyFunction
import me.user.Evaluator.builtinEnvironment
import me.user.Object.*
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.replaceObj

fun callKotlinFunction(function: FoxKotlinFunction, args: List<FoxObject?>): FoxObject? {
    return function.function.invoke(args)
}

fun callKotlinFunction(name: String, args: List<FoxObject?>, env: Environment): FoxObject? {
    val functions = (
        env.getValues(name).foxObjects?.filter { it.type() == ObjectType.KT_FUNCTION_OBJ }
        ?: return throwError("找不到函数 $name")
    )

    val callArgTypes: ArrayList<Type> = arrayListOf()
    for (arg in args) {
        arg?.let { callArgTypes.add(Type(it.type())) }
    }

    var function: FoxKotlinFunction? = null
    for (func in functions) {
        val f = func as FoxKotlinFunction
        val argTypes = replaceObj(f.argTypes, callArgTypes)

        if (args.count() == f.argsCount && callArgTypes == argTypes) {
            function = f
        }
    }

    if (function == null) return throwError("找不到合适的函数重载")

    return function.function.invoke(args)
}

fun callFunction(name: String, args: List<FoxObject?>, env: Environment): FoxObject? {
    val builtinFunctions = builtinEnvironment.getValues(name).foxObjects?.filter { it.type() == ObjectType.KT_FUNCTION_OBJ }
    builtinFunctions?.let {
        return callKotlinFunction(name, args, builtinEnvironment)
    }

    val functions = (
        env.getValues(name).foxObjects?.filter { it.type() == ObjectType.FUNCTION_OBJ || it.type() == ObjectType.KT_FUNCTION_OBJ }
        ?: return throwError("找不到函数 $name")
    )

    val callArgTypes: ArrayList<ObjectType> = arrayListOf()
    for (arg in args) {
        arg?.let { callArgTypes.add(it.type()) }
    }

    var function: FoxFunction? = null
    for (func in functions) {
        if (func.type() == ObjectType.KT_FUNCTION_OBJ) {
            return callKotlinFunction(name, args, env)
        }

        val f = func as FoxFunction

        // && callArgTypes == f.paramTypes
        if (args.count() == f.paramsCount) {
            function = f
        }
    }

    if (function == null) return throwError("找不到合适的函数重载")

    if (args.size == 1 && isError(args[0])) {
        return args[0]
    }

    return applyFunction(function, args)
}

