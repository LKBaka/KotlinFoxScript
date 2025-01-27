package me.user.Object

import me.user.Environment.ClassEnvironment
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.OperatorExtension.*
import java.util.*
import kotlin.collections.ArrayList

class FoxArray(val elements: ArrayList<FoxObject?>): FoxObject() {
    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.ARRAY_OBJ))))
        env.addFunction("notEquals", FoxKotlinFunction(::notEquals, 1, arrayListOf(Type(ObjectType.ARRAY_OBJ))))
        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.ARRAY_OBJ))))
        env.addFunction("get", FoxKotlinFunction(::get, 1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
    }

    private fun get(args: List<FoxObject?>): FoxObject? {
        args[0]?.let {
            val index = (args[0] as FoxInteger).value
            if (index > elements.count() - 1) return throwError("索引 $index 超出范围")

            var newIndex = index
            if (index < 0) {
                newIndex = elements.count().toBigInteger() + index
            }

            if (newIndex < 0) return throwError("索引 $index 超出范围")

            return elements[newIndex] as FoxObject
        }

        return null
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxArray?): FoxObject {
            val elements = this.elements

            right?.let {
                for (element in right.elements) {
                    elements.add(element)
                }
            }

            return FoxArray(elements)
        }

        return when (args[0]!!.type()) {
            ObjectType.ARRAY_OBJ -> plus(args[0]!! as FoxArray)
            else -> null
        }
    }

    private fun equals(args: List<FoxObject?>): FoxObject? {
        fun equals(right: FoxArray?): FoxObject {
            right?.let {
                if (elements.count() != it.elements.count()) return nativeBooleanToBooleanObject(false)
                val leftVal = elements
                val rightVal = it.elements

                val results = ArrayList<Boolean>()
                for (i in 0 until elements.count()) {
                    val leftObj = leftVal[i]
                    val rightObj = rightVal[i]

                    val boolean = leftObj == rightObj
                    results.add(boolean)
                }
                return nativeBooleanToBooleanObject(!results.contains(false))
            }

            return nativeBooleanToBooleanObject(false)
        }

        return when (args[0]!!.type()) {
            ObjectType.ARRAY_OBJ -> equals(args[0]!! as FoxArray)
            else -> null
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject? {
        fun notEquals(right: FoxArray?): FoxObject {
            right?.let {
                if (elements.count() != it.elements.count()) return nativeBooleanToBooleanObject(false)
                if (elements.isEmpty() && it.elements.isEmpty()) return nativeBooleanToBooleanObject(true)


                val leftVal = elements
                val rightVal = it.elements

                val results = ArrayList<Boolean>()
                for (i in 0 until elements.count()) {
                    val leftObj = leftVal[i]
                    val rightObj = rightVal[i]

                    val boolean = leftObj != rightObj
                    results.add(boolean)
                }
                return nativeBooleanToBooleanObject(results.contains(true))
            }

            return nativeBooleanToBooleanObject(false)
        }

        return when (args[0]!!.type()) {
            ObjectType.ARRAY_OBJ -> notEquals(args[0]!! as FoxArray)
            else -> null
        }
    }

    override fun inspect(): String {
        val elementStrings: ArrayList<String> = arrayListOf()

        for (element in elements) {
            elementStrings.add(
                if (element != null) {
                    if (element.type() != ObjectType.STRING_OBJ) {
                        element.inspect()
                    } else "\"${(element as FoxString).inspect()}\""
                } else ""
            )
        }

        return "[${elementStrings.joinToString(", ")}]"
    }

    override fun getValue(): Any {
        return elements
    }

    override fun type(): ObjectType {
        return ObjectType.ARRAY_OBJ
    }
}

