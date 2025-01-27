package me.user.Object

import java.util.*

class FoxReturnValue(val returnValue: FoxObject?): FoxObject() {
    init {
        uuid = UUID.randomUUID()
    }

    override fun type(): ObjectType {
        return ObjectType.RETURN_VALUE_OBJ
    }

    override fun inspect(): String {
        return returnValue?.inspect() ?: ""
    }

    override fun getValue(): Any? {
        return returnValue?.getValue()
    }
}