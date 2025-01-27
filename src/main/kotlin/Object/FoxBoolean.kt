package me.user.Object

import me.user.Environment.ClassEnvironment
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import java.util.*

class FoxBoolean(val value: Boolean): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("equals",FoxKotlinFunction(::equals,1, arrayListOf(Type(ObjectType.BOOLEAN_OBJ))))
        env.addFunction("notEquals",FoxKotlinFunction(::notEquals,1, arrayListOf(Type(ObjectType.BOOLEAN_OBJ))))
    }

    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxBoolean?): FoxObject {
            return nativeBooleanToBooleanObject(value == right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.BOOLEAN_OBJ -> equals(args[0]!! as FoxBoolean)
            else -> null
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject? {
        fun notEquals(right: FoxBoolean?): FoxObject {
            return nativeBooleanToBooleanObject(value != right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.BOOLEAN_OBJ -> notEquals(args[0]!! as FoxBoolean)
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun inspect(): String {
        return "$value"
    }

    override fun getValue(): Any {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.BOOLEAN_OBJ
    }
}

