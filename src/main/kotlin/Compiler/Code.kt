package me.user.Compiler

import java.nio.ByteBuffer
import java.nio.ByteOrder

typealias Instructions = ArrayList<Byte>

// 定义 Opcode 枚举
enum class OpCode {
    OpConstant,
    OpAdd,
    OpPop,
    OpSetGlobal,
    OpGetGlobal,
    OpCall,
    OpReturnValue,
    OpReturn,
    OpSetLocal,
    OpGetLocal
}

class Definition(val name: String, val operandWidths: IntArray)

val definitions :Map<OpCode, Definition> = mapOf(
    OpCode.OpConstant to Definition(OpCode.OpConstant.toString(), intArrayOf(2)),
    OpCode.OpAdd to Definition(OpCode.OpAdd.toString(), intArrayOf()),
    OpCode.OpPop to Definition(OpCode.OpPop.toString(), intArrayOf()),
    OpCode.OpSetGlobal to Definition(OpCode.OpSetGlobal.toString(), intArrayOf(2)),
    OpCode.OpGetGlobal to Definition(OpCode.OpGetGlobal.toString(), intArrayOf(2)),
    OpCode.OpSetLocal to Definition(OpCode.OpSetGlobal.toString(), intArrayOf(1)),
    OpCode.OpGetLocal to Definition(OpCode.OpGetGlobal.toString(), intArrayOf(1)),
    OpCode.OpCall to Definition(OpCode.OpCall.toString(), intArrayOf()),
    OpCode.OpReturnValue to Definition(OpCode.OpReturnValue.toString(), intArrayOf()),
    OpCode.OpReturn to Definition(OpCode.OpReturn.toString(), intArrayOf()),
)


fun lookup(op: Byte): Result<Definition> {
    val opcode = OpCode.entries.getOrNull(op.toInt())
    return if (opcode != null) {
        val def = definitions[opcode]
        if (def != null) {
            Result.success(def)
        } else {
            Result.failure(IllegalStateException("Definition not found for opcode $op"))
        }
    } else {
        Result.failure(IllegalArgumentException("Opcode $op undefined"))
    }
}

// Make 函数
fun make(op: OpCode, vararg operands: Int): ByteArray {
    val def = definitions[op] ?: return byteArrayOf() // 如果未找到定义，返回空字节数组

    // 计算指令长度
    val instructionLen = 1 + def.operandWidths.sum()
    val instruction = ByteArray(instructionLen)

    // 设置操作码
    instruction[0] = op.ordinal.toByte()

    // 设置操作数
    var offset = 1

    for (i in operands.indices) {
        val width = def.operandWidths[i]
        when (width) {
            2 -> {
                ByteBuffer.wrap(instruction, offset, 2)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putShort(operands[i].toShort())
            }
            1 -> instruction[offset] = operands[i].toByte()
        }
        offset += width
    }

    return instruction
}

fun readOperands(def: Definition, ins: Instructions): Pair<IntArray, Int> {
    val operands = IntArray(def.operandWidths.count())
    var offset = 0
    for ((i, width) in def.operandWidths.withIndex()) {
        when (width) {
            2 -> operands[i] = readUInt16(ArrayList(ins.subList(0, offset))).toInt()
            1 -> operands[i] = readUInt8(ArrayList(ins.subList(0, offset))).toInt()
        }
        offset += width
    }
    return operands to offset
}

fun readUInt16(ins: Instructions): UShort {
    require(ins.toByteArray().size >= 2) { return 0.toUShort()}

    val highByte = ins[0].toUByte().toInt() // 高8位
    val lowByte = ins[1].toUByte().toInt()  // 低8位
    return ((highByte shl 8) or lowByte).toUShort()
}

fun readUInt8(ins: Instructions): UByte {
    require(ins.toByteArray().isNotEmpty()) { return 0.toUByte()}

    return ins[0].toUByte()
}

fun Instructions.toString(): String {
    val out = StringBuilder()
    var i = 0
    while (i < this.size) {
        val opcode = OpCode.entries.getOrNull(this[i].toInt())
        if (opcode == null) {
            out.append("ERROR: Invalid opcode at index $i\n")
            i++
            continue
        }

        val def = definitions[opcode]
        if (def == null) {
            out.append("ERROR: Definition not found for opcode $opcode\n")
            i++
            continue
        }

        val (operands, read) = readOperands(def, ArrayList(this.toByteArray().copyOfRange(i + 1, this.size).toList()))
        out.append("%04d %s\n".format(i, fmtInstruction(def, operands)))

        i += 1 + read
    }
    return out.toString()
}

fun fmtInstruction(def: Definition, operands: IntArray): String {
    val operandCount = def.operandWidths.size
    if (operands.size != operandCount) {
        return "ERROR: operand len ${operands.size} does not match defined $operandCount"
    }

    return when (operandCount) {
        0 -> def.name
        1 -> "${def.name} ${operands[0]}"
        else -> "ERROR: unhandled operandCount for ${def.name}"
    }
}