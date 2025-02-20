package me.user.VM

import me.user.Compiler.Instructions
import me.user.Object.FoxCompiledFunction

class Frame (
    private val func: FoxCompiledFunction,
    var ip: Int = -1,
    var basePointer: Int
) {
    fun instructions(): Instructions {
        return func.instructions
    }
}