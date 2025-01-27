package me.user.Evaluator.OOPEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxClass
import me.user.Object.FoxFunction
import me.user.Object.FoxObject
import me.user.Object.ObjectType
import me.user.Parser.ClassStatement
import me.user.Parser.Identifier
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.StatementUtils.findFunctionLiteral
import me.user.Utils.extendClassEnv

fun evalClassStatement(node: Node, env: Environment): FoxObject? {
    val classStatement = node as ClassStatement
    val baseClassObject = eval(classStatement.baseClass, env)
    if (isError(baseClassObject)) return baseClassObject

    baseClassObject?.let {
        if (it.type() != ObjectType.CLASS_OBJ) return throwError("不是一个类型: ${it.type()}")
    }

    val classObject = FoxClass().apply {
        baseClass = baseClassObject as FoxClass?
        name = classStatement.name as Identifier?
        body = classStatement.body
        createFunc = body?.statements?.toList()?.let {
            eval(findFunctionLiteral("New", it) as Node?, this.env) as FoxFunction?
        }
    }

    val result = extendClassEnv(classObject)
    if (isError(result)) return result

    eval(classObject.body, classObject.env)

    classObject.env.setValue("Me", Data(classObject, false), true)
    classObject.baseClass?.let { Data(it, false) }?.let { classObject.env.setValue("MyBase", it, true) }
    classObject.name?.value?.let { env.setValue(it, Data(classObject, false), true) }

    return classObject
}