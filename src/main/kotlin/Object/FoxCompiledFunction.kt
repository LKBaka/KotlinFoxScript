package me.user.Object

import me.user.Compiler.Instructions
import java.util.*

class FoxCompiledFunction: FoxObject() {
    var instructions: Instructions = arrayListOf()
    var numLocals = 0

    init {
        this.uuid = UUID.randomUUID()
    }

    override fun type(): ObjectType {
        return ObjectType.COMPILED_FUNCTION_OBJ
    }

    override fun getValue(): Any {
        return uuid!!
    }

    override fun inspect(): String {
        return "<FoxCompiledFunction [${this.uuid}]>"
    }
}