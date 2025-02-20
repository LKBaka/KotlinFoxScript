package me.user.Object

import me.user.Environment.ClassEnvironment
import me.user.Parser.BlockStatement
import me.user.Parser.Identifier
import java.util.*

class FoxClass: FoxObject() {
    var body: BlockStatement? = null
    var baseClass: FoxClass? = null
    var name: Identifier? = null
    var createFunc: FoxFunction? = null

    val env = ClassEnvironment()

    init {
        this.uuid = UUID.randomUUID()
    }

    override fun type(): ObjectType {
        return ObjectType.CLASS_OBJ
    }

    override fun getValue(): Any {
        return uuid!!
    }
    override fun inspect(): String {
        return "<FoxClass $name(${createFunc?.parametersToString() ?: ""}) [${this.uuid}]>"
    }
}