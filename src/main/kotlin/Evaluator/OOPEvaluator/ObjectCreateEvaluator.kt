package me.user.Evaluator.OOPEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Parser.*
import me.user.Evaluator.*
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callFunction
import me.user.Object.FoxClass
import me.user.Object.FoxObject
import me.user.Object.ObjectType
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.EvaluatorUtils.evalExpressions
import me.user.Utils.extendClassEnv

fun evalObjectCreateExpression(node: Node, env: Environment): FoxObject? {
    val objCreateExp = node as? ObjectCreateExpression ?: return null

    val r = eval(objCreateExp.objType, env)
    if (isError(r)) return r

    var newObj: Any? = null

    when (r?.type()) {
        ObjectType.CLASS_OBJ -> {
            val classObj = r as FoxClass
            newObj = FoxClass().apply {
                body = classObj.body
                name = classObj.name
                createFunc = classObj.createFunc
                baseClass = classObj.baseClass
                uuid = classObj.uuid
            }

            extendClassEnv(newObj)
            val result = eval(newObj.body, newObj.env)
            if (isError(result)) return result

            newObj.env.setValue("Me", Data(newObj, false), false)
            newObj.baseClass?.let { Data(it, false) }?.let { newObj.env.setValue("MyBase", it, true) }

            newObj.createFunc?.let {
                val createResult = callFunction("New",evalExpressions(objCreateExp.arguments, env), newObj.env)
                if (isError(createResult)) return createResult
            }
        }
//        ObjectType.KT_CLASS_OBJ -> {
//            val classObj = r as VBClass
//            newObj = VBClass().apply {
//                Env = Environment()
//                Name = classObj.Name
//                Members = classObj.Members.toMutableList() // 保持可修改性
//                Instance = classObj.Instance
//                CreateFunc = classObj.CreateFunc
//                CreateArgs = objCreateExp.Arguments
//                OnPropertyChangeFunction = classObj.OnPropertyChangeFunction
//                UUIDString = classObj.UUIDString
//            }
//
//            for (item in newObj.Members) {
//                newObj.Env.setValue(item.Name.Value, item, false)
//            }
//
//            if (newObj.CreateFunc != null) {
//                val createFunction = newObj.CreateFunc as VBFunction
//                val argsResult = evalExpressions(newObj.CreateArgs, env)
//                if (argsResult.any { isError(it) }) {
//                    return argsResult.firstOrNull { isError(it) }
//                }
//                val result = createFunction.Func.invoke(argsResult)
//                if (isError(result)) return result
//            }
//        }
        else -> return throwError("")
    }

    return newObj
}