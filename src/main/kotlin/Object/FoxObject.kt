package me.user.Object

import me.user.Utils.ObjNothing
import java.util.*

enum class ObjectType {
    OBJ,
    INTEGER_OBJ, // FoxInteger （使用BigInteger储存）
    INT, // FoxInt （使用Int储存）
    LONG,
    BOOLEAN_OBJ,
    NOTHING_OBJ,
    KT_FUNCTION_OBJ,
    ERROR_OBJ,
    RETURN_VALUE_OBJ,
    FUNCTION_OBJ,
    STRING_OBJ,
    ARRAY_OBJ,
    DICTIONARY_OBJ,
    DICTIONARY_KEY_OBJ,
    DICTIONARY_VALUE_OBJ,
    DOUBLE_OBJ,
    CLASS_OBJ,
    KT_CLASS_OBJ,
    COMPILED_FUNCTION_OBJ,
    KT_COMPILED_FUNCTION_OBJ
}

interface IFoxObject{
    fun type(): ObjectType
    fun inspect(): String
    fun getValue(): Any?
}

open class FoxObject: IFoxObject {
    var uuid: UUID? = null

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != this::class) return false

        val foxObject = other as FoxObject
        return this.uuid == foxObject.uuid
    }

    override fun type(): ObjectType {
        return ObjectType.OBJ
    }

    override fun inspect(): String {
        return ""
    }

    override fun getValue(): Any? {
        return null
    }

    companion object {
        fun fromNative(result: Any?) =
            result?.let {
                when (result) {
                    is Int -> FoxInteger(result.toBigInteger())
                    is Unit -> ObjNothing
                    else -> TODO("未知的类型: ${result::class}")
                }
            } ?: ObjNothing
    }
}
