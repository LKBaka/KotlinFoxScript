package me.user.Object

import me.user.Environment.Environment
import me.user.Environment.FunctionEnvironment
import me.user.Parser.BlockStatement
import me.user.Parser.Expression
import java.util.*
import kotlin.collections.ArrayList

class FoxFunction: FoxObject() {
    var paramTypes: List<ObjectType> = listOf()
    var body: BlockStatement? = null // 函数中的代码
    var parameters: ArrayList<Expression?>? = null // 函数形参列表
    var paramsCount: Int = -1
    var env: Environment = FunctionEnvironment()

    init {
        this.uuid = UUID.randomUUID()
    }

    override fun type(): ObjectType {
        return ObjectType.FUNCTION_OBJ
    }

    override fun getValue(): Any {
        return uuid!!
    }

    fun parametersToString(): String {
        if (parameters?.isNotEmpty() == true) {
            val stringBuilder = StringBuilder()

            val stringList: ArrayList<String> = arrayListOf()

            for (param in parameters!!) {
                stringList.add(param.toString())
            }

            stringBuilder.append(stringList.joinToString(", "))
            return stringBuilder.toString()
        }

        return ""
    }

    override fun inspect(): String {
        return "<FoxFunction (${parametersToString()}) [${this.uuid}]>"
    }
}