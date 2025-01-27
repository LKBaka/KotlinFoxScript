package me.user.Object

import me.user.Environment.ClassEnvironment
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import me.user.Utils.ErrorUtils.throwError
import java.util.*
import kotlin.collections.ArrayList

interface IFoxDictionaryItem : IFoxObject {
    fun getKey(): FoxDictionaryKey
}

class FoxDictionaryPair : IFoxDictionaryItem {
    var key: FoxObject? = null
    var value: FoxObject? = null

    override fun getKey(): FoxDictionaryKey {
        return key as FoxDictionaryKey
    }

    override fun type(): ObjectType {
        throw NotImplementedError()
    }

    override fun inspect(): String {
        return "${key?.inspect()}:${value?.inspect()}"
    }

    override fun getValue(): Any? {
        return value?.getValue()
    }

    override fun equals(other: Any?): Boolean {
        other?. let {
            if (it::class != FoxDictionaryPair::class) {
                return false
            }

            val pairObj = it as FoxDictionaryPair
            return pairObj.key == key && pairObj.value == value
        }

        return false
    }

    override fun hashCode(): Int {
        return key.hashCode() + value.hashCode()
    }
}

class FoxDictionary : FoxObject() {
    val env = ClassEnvironment()
    var pairs = mutableMapOf<FoxDictionaryKey, FoxDictionaryPair>()

    init {
        this.uuid = UUID.randomUUID()

        env.addFunction("equals", FoxKotlinFunction(::equals, 1, arrayListOf(Type(ObjectType.DICTIONARY_OBJ))))
        env.addFunction("notEquals", FoxKotlinFunction(::notEquals, 1, arrayListOf(Type(ObjectType.DICTIONARY_OBJ))))
        env.addFunction("plus", FoxKotlinFunction(::plus, 1, arrayListOf(Type(ObjectType.DICTIONARY_OBJ))))
        env.addFunction("get", FoxKotlinFunction(::get, 1, arrayListOf(Type(ObjectType.STRING_OBJ, ObjectType.INTEGER_OBJ, ObjectType.BOOLEAN_OBJ))))
    }

    private fun get(args: List<FoxObject?>): FoxObject? {
        args[0]?.let {
            for (pair in pairs) {
                if (it == pair.key.key){
                    return pair.value.value
                }
            }

            return throwError("找不到键 ${it.inspect()}")
        }

        return null
    }

    private fun plus(args: List<FoxObject?>): FoxObject? {
        fun plus(right: FoxDictionary?): FoxObject {
            val pairs = this.pairs

            right?.let {
                for (pair in right.pairs) {
                    pairs[pair.key] = pair.value
                }
            }

            return FoxDictionary().apply {
                this.pairs = pairs
            }
        }

        return when (args[0]!!.type()) {
            ObjectType.DICTIONARY_OBJ -> plus(args[0]!! as FoxDictionary)
            else -> null
        }
    }

    private fun equals(args: List<FoxObject?>): FoxObject {
        args[0].let {
            val dict = it as FoxDictionary
            if (dict.pairs.count() != pairs.count()) return nativeBooleanToBooleanObject(false)

            val leftVal = pairs
            val rightVal = dict.pairs

            val results = ArrayList<Boolean>()
            for (i in 0 until pairs.count()) {
                val leftPair = leftVal.values.toList()[i]
                val rightPair = rightVal.values.toList()[i]

                val boolean = leftPair == rightPair
                results.add(boolean)
            }
            return nativeBooleanToBooleanObject(!results.contains(false))
        }
    }

    private fun notEquals(args: List<FoxObject?>): FoxObject {
        args[0].let {
            val dict = it as FoxDictionary
            if (dict.pairs.count() != pairs.count()) return nativeBooleanToBooleanObject(true)

            val leftVal = pairs
            val rightVal = dict.pairs

            val results = ArrayList<Boolean>()
            for (i in 0 until pairs.count()) {
                val leftPair = leftVal.values.toList()[i]
                val rightPair = rightVal.values.toList()[i]

                val boolean = leftPair != rightPair
                results.add(boolean)
            }
            return nativeBooleanToBooleanObject(results.contains(true))
        }
    }


    private fun inspectKey(key: FoxObject?): String? {
        return key?.let {
            if (it.type() == ObjectType.STRING_OBJ) {
                "\"${it.inspect()}\""
            } else it.inspect()
        }
    }

    private fun inspectValue(value: FoxObject?): String? {
        return value?.let {
            if (it.type() == ObjectType.STRING_OBJ) {
                "\"${it.inspect()}\""
            } else it.inspect()
        }
    }

    override fun inspect(): String {
        val paris = mutableListOf<String>()
        for (pair in this.pairs.values) {
            pair.getKey()
            paris.add("${inspectKey(pair.key as FoxDictionaryKey)}:${inspectValue((pair.value))}")
        }
        return "{${paris.joinToString(", ")}}"
    }

    override fun type(): ObjectType {
        return ObjectType.DICTIONARY_OBJ
    }

    override fun getValue(): Any? {
        return pairs
    }
}


class FoxDictionaryKey : FoxObject() {
    var keyType: ObjectType? = null
    var key: FoxObject? = null

    override fun inspect(): String {
        return key.toString()
    }

    override fun type(): ObjectType {
        return ObjectType.DICTIONARY_KEY_OBJ
    }

    override fun getValue(): Any? {
        return key
    }

    override fun equals(other: Any?): Boolean {
        other?. let {
            if (it::class != FoxDictionaryKey::class) {
                return false
            }

            val keyObj = it as FoxDictionaryKey
            return keyObj.key == key
        }

        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}


class FoxDictionaryValue : FoxObject() {
    var valueType: ObjectType? = null
    var dictionaryValue: FoxObject? = null

    override fun inspect(): String {
        return this.dictionaryValue.toString()
    }

    override fun type(): ObjectType {
        return ObjectType.DICTIONARY_VALUE_OBJ
    }

    override fun getValue(): Any? {
        return dictionaryValue?.getValue()
    }

    override fun equals(other: Any?): Boolean {
        other?. let {
            if (it::class != FoxObject::class) {
                return false
            }

            val valueObj = it as FoxDictionaryValue
            return valueObj.dictionaryValue == dictionaryValue
        }

        return false
    }

    override fun hashCode(): Int {
        return dictionaryValue.hashCode()
    }
}


