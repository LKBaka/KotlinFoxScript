package me.user.Object

import java.util.*
import kotlin.collections.ArrayList

typealias functionDelegate = (args: List<FoxObject?>) -> FoxObject?
class FoxKotlinFunction(val function: functionDelegate, val argsCount: Int, val argTypes: List<Type> = listOf()): FoxObject() {
    init {
        this.uuid = UUID.randomUUID()
    }

    override fun type(): ObjectType {
        return ObjectType.KT_FUNCTION_OBJ
    }

    override fun getValue(): Any {
        return function
    }

    private fun argTypeToString(argType: Type): String {
        val argObjectType = argType.getTypes()
        return argObjectType.joinToString(" | ")
    }

    private fun argsToString(): String {
        if (argTypes.isNotEmpty()) {
            val stringBuilder = StringBuilder()

            val stringList: ArrayList<String> = arrayListOf()

            var argIndex = 1
            for (argumentType in argTypes) {
                stringList.add("arg$argIndex: ${argTypeToString(argumentType)}")
                argIndex ++
            }

            stringBuilder.append(stringList.joinToString(", "))
            return stringBuilder.toString()
        }

        return ""
    }

    override fun inspect(): String {
        return "<FoxKotlinFunction (${argsToString()}) [${this.uuid}]>"
    }
}