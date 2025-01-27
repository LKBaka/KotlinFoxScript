package me.user.Object

import me.user.Environment.*
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import me.user.Utils.ErrorUtils.throwError
import java.math.BigDecimal
import me.user.OperatorExtension.*
import java.math.BigInteger
import java.util.*

class FoxDouble(val value: BigDecimal = 0.toBigDecimal()): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("minus", FoxKotlinFunction(::minus, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("multiply", FoxKotlinFunction(::multiply, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("divide", FoxKotlinFunction(::divide, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("moreThan", FoxKotlinFunction(::moreThan, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("lessThan", FoxKotlinFunction(::lessThan, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
        env.addFunction("notEquals", FoxKotlinFunction(::notEquals, 1, arrayListOf(Type(ObjectType.DOUBLE_OBJ, ObjectType.INTEGER_OBJ))))
    }

    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxDouble?): FoxObject {
            return nativeBooleanToBooleanObject(value.compareTo(right!!.value) == 0)
        }
        fun equals(right: FoxInteger?): FoxObject {
            return nativeBooleanToBooleanObject(value.compareTo(right!!.value.toBigDecimal()) == 0)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> equals(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> equals(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject? {
        fun notEquals(right: FoxDouble?): FoxObject {
            return nativeBooleanToBooleanObject(value.compareTo(right!!.value) != 0)
        }
        fun notEquals(right: FoxInteger?): FoxObject {
            return nativeBooleanToBooleanObject(value.compareTo(right!!.value.toBigDecimal()) != 0)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> notEquals(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> notEquals(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxDouble?): FoxObject {
            return FoxDouble(value + right!!.value)
        }

        fun plus(right: FoxInteger?): FoxObject {
            return FoxDouble(value + right!!.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> plus(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> plus(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun minus(args: List<FoxObject?>): FoxObject? {
        fun minus(right: FoxDouble?): FoxObject {
            return FoxDouble(value - right!!.value)
        }
        fun minus(right: FoxInteger?): FoxObject {
            return FoxDouble(value - right!!.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> minus(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> minus(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun multiply(args: List<FoxObject?>): FoxObject? {
        fun multiply(right: FoxDouble?): FoxObject {
            return FoxDouble(value * right!!.value)
        }
        fun multiply(right: FoxInteger?): FoxObject {
            return FoxDouble(value * right!!.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> multiply(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> multiply(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun divide(args: List<FoxObject?>): FoxObject? {
        fun divide(right: FoxDouble?): FoxObject {
            if ((right!!.value) == 0.toBigDecimal()) return throwError("除数不能除以 0")
            return FoxDouble(value / right.value)
        }
        fun divide(right: FoxInteger?): FoxObject {
            if (right!!.value == 0.toBigDecimal()) return throwError("除数不能除以 0")
            return FoxDouble(value / right.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> divide(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> divide(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun moreThan(args: List<FoxObject?>): FoxObject? {
        fun moreThan(right: FoxDouble?): FoxObject {
            return nativeBooleanToBooleanObject(value > right!!.value)
        }
        fun moreThan(right: FoxInteger?): FoxObject {
            return nativeBooleanToBooleanObject(value > right!!.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> moreThan(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> moreThan(args[0] as? FoxDouble)
            else -> null
        }
    }

    private fun lessThan(args: List<FoxObject?>): FoxObject? {
        fun lessThan(right: FoxDouble?): FoxObject {
            return nativeBooleanToBooleanObject(value < right!!.value)
        }
        fun lessThan(right: FoxInteger?): FoxObject {
            return nativeBooleanToBooleanObject(value < right!!.value)
        }
        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> lessThan(args[0] as? FoxInteger)
            ObjectType.DOUBLE_OBJ -> lessThan(args[0] as? FoxDouble)
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other::class != FoxDouble::class) return false
            return (other as FoxDouble).value == value
        }

        return false
    }

    override fun inspect(): String {
        val strippedValue = value.stripTrailingZeros()
        val plainString = strippedValue.toPlainString()

        return plainString
    }

    override fun getValue(): Any {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.DOUBLE_OBJ
    }
}


