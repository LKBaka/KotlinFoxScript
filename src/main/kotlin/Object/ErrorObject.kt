package me.user.Object

class FoxError(private val message: String): FoxObject() {
    override fun inspect(): String {
        return "Error: $message"
    }

    override fun getValue(): Any {
        return Exception(message)
    }

    override fun type(): ObjectType {
        return ObjectType.ERROR_OBJ
    }
}