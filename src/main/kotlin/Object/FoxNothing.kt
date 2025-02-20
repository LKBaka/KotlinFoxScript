package me.user.Object

class FoxNothing(val value: Nothing? = null): FoxObject() {
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