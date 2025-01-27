package me.user.Object

import me.user.Environment.ClassEnvironment

class FoxNothing(val value: Nothing? = null): FoxObject() {
    val env = ClassEnvironment()

    override fun inspect(): String {
        return "Nothing"
    }

    override fun getValue(): Any? {
        return value
    }

    override fun type(): ObjectType {
        return ObjectType.NOTHING_OBJ
    }
}