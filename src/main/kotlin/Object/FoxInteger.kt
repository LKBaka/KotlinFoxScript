package me.user.Object

import me.user.Environment.*
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import me.user.Utils.ErrorUtils.throwError
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

class FoxInt(val value: Int = 0): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("minus", FoxKotlinFunction(::minus, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("multiply", FoxKotlinFunction(::multiply, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("divide", FoxKotlinFunction(::divide, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("moreThan", FoxKotlinFunction(::moreThan, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("lessThan", FoxKotlinFunction(::lessThan, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.INT))))
        env.addFunction("notEquals", FoxKotlinFunction(::notEquals, 1, arrayListOf(Type(ObjectType.INT))))
    }

    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxInt?): FoxObject {
            return nativeBooleanToBooleanObject(value == right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> equals(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject? {
        fun notEquals(right: FoxInt?): FoxObject {
            return nativeBooleanToBooleanObject(value != right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> notEquals(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxInt?): FoxObject {
            return FoxInt(value + right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> plus(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun minus(args: List<FoxObject?>): FoxObject? {
        fun minus(right: FoxInt?): FoxObject {
            return FoxInt(value - right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> minus(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun multiply(args: List<FoxObject?>): FoxObject? {
        fun multiply(right: FoxInt?): FoxObject {
            return FoxInt(value * right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> multiply(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun divide(args: List<FoxObject?>): FoxObject? {
        fun divide(right: FoxInt?): FoxObject {
            if ((right!!.value) == 0) return throwError("除数不能除以0")

            val result = value.toBigDecimal().divide(right.value.toBigDecimal(), 16, RoundingMode.HALF_UP)
            return FoxDouble(result)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> divide(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun moreThan(args: List<FoxObject?>): FoxObject? {
        fun moreThan(right: FoxInt?): FoxObject {
            return nativeBooleanToBooleanObject(value > right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> moreThan(args[0]!! as FoxInt)
            else -> null
        }
    }

    private fun lessThan(args: List<FoxObject?>): FoxObject? {
        fun lessThan(right: FoxInt?): FoxObject {
            return nativeBooleanToBooleanObject(value < right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INT -> lessThan(args[0]!! as FoxInt)
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other::class != FoxInt::class) return false
            return (other as FoxInt).value == value
        }

        return false
    }

    override fun inspect(): String {
        return "$value"
    }

    override fun getValue(): Any {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.INT
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}

class FoxInteger(val value: BigInteger = 0.toBigInteger()): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("minus", FoxKotlinFunction(::minus, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("multiply", FoxKotlinFunction(::multiply, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("divide", FoxKotlinFunction(::divide, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("compareTo", FoxKotlinFunction(::compareTo, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        env.addFunction("toInt", FoxKotlinFunction(::toInt, 0, arrayListOf()))
    }

    private fun toInt(args: List<FoxObject?>): FoxObject {
        if (value <= Int.MAX_VALUE.toBigInteger() && value >= Int.MIN_VALUE.toBigInteger()) {
           return FoxInt(value.toInt())
        }

        return throwError("数值 $value 超过了Int类型的极限")
    }


    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxInteger?): FoxObject {
            return nativeBooleanToBooleanObject(value == right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> equals(args[0]!! as FoxInteger)
            else -> null
        }
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxInteger?): FoxObject {
            return FoxInteger(value + right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> plus(args[0]!! as FoxInteger)
            else -> null
        }
    }

    private fun minus(args: List<FoxObject?>): FoxObject? {
        fun minus(right: FoxInteger?): FoxObject {
            return FoxInteger(value - right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> minus(args[0]!! as FoxInteger)
            else -> null
        }
    }

    private fun multiply(args: List<FoxObject?>): FoxObject? {
        fun multiply(right: FoxInteger?): FoxObject {
            return FoxInteger(value * right!!.value)
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> multiply(args[0]!! as FoxInteger)
            else -> null
        }
    }

    private fun divide(args: List<FoxObject?>): FoxObject? {
        fun divide(right: FoxInteger?): FoxObject {
            if ((right!!.value) == 0.toBigInteger()) return throwError("除数不能除以0")

            val result = value.toBigDecimal().divide(right.value.toBigDecimal(), 16, RoundingMode.HALF_UP)
            return FoxDouble(result)
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> divide(args[0]!! as FoxInteger)
            else -> null
        }
    }

    private fun compareTo(args: List<FoxObject?>): FoxObject? {
        fun compareTo(right: FoxInteger?): FoxObject {
            return FoxInteger(value.compareTo(right?.value).toBigInteger())
        }

        return when (args[0]!!.type()) {
            ObjectType.INTEGER_OBJ -> compareTo(args[0]!! as FoxInteger)
            else -> null
        }
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (other::class != FoxInteger::class) return false
            return (other as FoxInteger).value == value
        }

        return false
    }

    override fun inspect(): String {
        return "$value"
    }

    override fun getValue(): Any {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.INTEGER_OBJ
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}