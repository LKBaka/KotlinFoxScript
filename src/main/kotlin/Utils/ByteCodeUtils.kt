package me.user.Utils

import me.user.Compiler.Compiler.ByteCode
import me.user.Compiler.Instructions
import me.user.Compiler.OpCode

fun toByteArrayList(intArray: ArrayList<Int>): ArrayList<Byte> {
    val list = ArrayList<Byte>()
    intArray.forEach {
        list.add(it.toByte())
    }

    return list
}

fun checkByteCode(byteCode1: ByteCode, byteCode2: ByteCode): Boolean {
    return byteCode1.constants == byteCode2.constants && byteCode1.instructions == byteCode2.instructions
}

fun byteToOpCode(ins: Instructions): ArrayList<OpCode> {
    val opCodes = arrayListOf<OpCode>()

    ins.forEach {
        opCodes.add(OpCode.entries[it.toInt()])
    }

    return opCodes
}