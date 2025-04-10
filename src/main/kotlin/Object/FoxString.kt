package me.user.Object

import me.user.Environment.ClassEnvironment
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import java.util.*

class FoxString(val value: String): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.STRING_OBJ))))
        env.addFunction("notEquals", FoxKotlinFunction(::notEquals, 1, arrayListOf(Type(ObjectType.STRING_OBJ))))
        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.STRING_OBJ))))
        env.addFunction("count", FoxKotlinFunction(::count, 0, arrayListOf()))
        env.addFunction("toArray", FoxKotlinFunction(::toArray, 0, arrayListOf()))
    }

    private fun toArray(args: List<FoxObject?>): FoxObject {
        val array = arrayListOf<FoxObject?>()

        value.forEach {
            array.add(FoxString("$it"))
        }

        return FoxArray(array)
    }

    private fun count(args: List<FoxObject?>): FoxObject {
        return FoxInteger(this.value.count().toBigInteger())
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxString?): FoxObject {
            return FoxString(value + right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.STRING_OBJ -> plus(args[0]!! as FoxString)
            else -> null
        }
    }


    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxString?): FoxObject {
            return nativeBooleanToBooleanObject(value == right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.STRING_OBJ -> equals(args[0]!! as FoxString)
            else -> null
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject? {
        fun notEquals(right: FoxString?): FoxObject {
            return nativeBooleanToBooleanObject(value != right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.STRING_OBJ -> notEquals(args[0]!! as FoxString)
            else -> null
        }
    }

    override fun inspect(): String {
        return value
    }

    override fun getValue(): Any {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.STRING_OBJ
    }
}

