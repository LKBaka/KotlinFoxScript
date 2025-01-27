package me.user.Utils

import me.user.Object.*

object FoxDictionaryUtils {
    private fun createKey(key: FoxBoolean): FoxDictionaryKey {
        return FoxDictionaryKey().apply {
            keyType = key.type()
            this.key = key
        }
    }
    private fun createKey(key: FoxInteger): FoxDictionaryKey {
        return FoxDictionaryKey().apply {
            keyType = key.type()
            this.key = key
        }
    }
    private fun createKey(key: FoxString): FoxDictionaryKey {
        return FoxDictionaryKey().apply {
            keyType = key.type()
            this.key = key
        }
    }
    fun createKey(key: FoxObject): FoxDictionaryKey {
        return when (key.type()) {
            ObjectType.INTEGER_OBJ -> createKey(key as FoxInteger)
            ObjectType.BOOLEAN_OBJ -> createKey(key as FoxBoolean)
            ObjectType.STRING_OBJ -> createKey(key as FoxString)
            else -> null
        }!!
    }


    private fun createValue(value: FoxBoolean): FoxDictionaryValue {
        return FoxDictionaryValue().apply {
            valueType = value.type()
            this.dictionaryValue = value
        }
    }
    private fun createValue(value: FoxInteger): FoxDictionaryValue {
        return FoxDictionaryValue().apply {
            valueType = value.type()
            this.dictionaryValue = value
        }
    }
    private fun createValue(value: FoxString): FoxDictionaryValue {
        return FoxDictionaryValue().apply {
            valueType = value.type()
            this.dictionaryValue = value
        }
    }
    fun createValue(value: FoxObject): FoxDictionaryValue {
        return when (value.type()) {
            ObjectType.INTEGER_OBJ -> createValue(value as FoxInteger)
            ObjectType.BOOLEAN_OBJ -> createValue(value as FoxBoolean)
            ObjectType.STRING_OBJ -> createValue(value as FoxString)
            else -> null
        }!!
    }
}