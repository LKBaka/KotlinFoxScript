package me.user.VM.VM

import me.user.Compiler.Compiler.ByteCode
import me.user.Compiler.OpCode
import me.user.Compiler.readUInt16
import me.user.Compiler.readUInt8
import me.user.Error.RuntimeError
import me.user.Object.FoxCompiledFunction
import me.user.Object.FoxObject
import me.user.VM.Frame
import me.user.VM.Handlers.FunctionOpHandler.opCallHandler
import me.user.VM.Handlers.FunctionOpHandler.opReturnValueHandler
import me.user.VM.Handlers.FunctionOpHandler.opReturnHandler
import me.user.VM.InfixExpressionVM.infixExpressionOpHandler
import java.util.EmptyStackException
import java.util.Stack

const val stackSize = 2048
const val globalsSize = 65536
const val maxFrames = 1024

private typealias OpHandler = () -> Result<Any?>?
typealias OpHandlerResult = Result<Any?>?

class VM(byteCode: ByteCode, private val globals: ArrayList<FoxObject>) {
    // 存放常量的列表
    private val constants: ArrayList<FoxObject> = byteCode.constants

    // 最后弹出的元素
    private var lastPopped: FoxObject? = null

    // 存放栈的列表
    val stack: Stack<FoxObject?> = Stack()
    var stackNextPos: Int = 0 // 始终指向栈中的下一个空闲槽。栈顶的值是stack[stackNextPos-1]

    // 主函数
    private val mainFunction = FoxCompiledFunction().apply { this.instructions = byteCode.instructions }

    // 存放栈帧的列表
    private val frames: ArrayList<Frame> = ArrayList<Frame>(maxFrames).apply {
        this.add(Frame(mainFunction, basePointer = 0).apply { this.ip = 0 })
    }
    private var frameIndex = 1 // 栈帧索引

    // 存放处理各种操作码的函数的Map
    private val opHandlers: Map<OpCode, OpHandler> = mapOf(
        OpCode.OpConstant to {
            runCatching {
                val ins = this.currentFrame().instructions()

                val constIndex = readUInt16(ArrayList(ins.slice(currentFrame().ip + 1..currentFrame().ip + 2)))
                currentFrame().ip += 2

                val pushResult = this.push(this.constants[constIndex.toInt()])
                if (pushResult?.isFailure == true) return@runCatching pushResult

                return@runCatching null
            }
        },
        OpCode.OpAdd to {
            runCatching {
                val left = pop()
                val right = pop()

                return@runCatching infixExpressionOpHandler(left, right, OpCode.OpAdd)
            }
        },
        OpCode.OpSetGlobal to {
            runCatching {
                val globalIndex = readUInt16(ArrayList(currentFrame().instructions().subList(currentFrame().ip + 1, currentFrame().instructions().count() - 1)))
                currentFrame().ip += 2

                pop()?.let { globals.add(globalIndex.toInt(), it) }
            }
        },
        OpCode.OpGetGlobal to {
            runCatching {
                val globalIndex = readUInt16(ArrayList(currentFrame().instructions().subList(currentFrame().ip + 1, currentFrame().instructions().count() - 1)))
                currentFrame().ip += 2

                val obj = push(globals[globalIndex.toInt()])
                obj?.let { return@runCatching obj }
            }
        },
        OpCode.OpSetLocal to {
            runCatching {
                val localIndex = readUInt8(ArrayList(currentFrame().instructions().subList(currentFrame().ip + 1, currentFrame().instructions().count() - 1)))
                currentFrame().ip += 1

                stack[currentFrame().basePointer + localIndex.toInt()] = pop()
            }
        },
        OpCode.OpGetLocal to {
            runCatching {
                val localIndex = readUInt8(ArrayList(currentFrame().instructions().subList(currentFrame().ip + 1, currentFrame().instructions().count() - 1)))
                currentFrame().ip += 1

                val obj = stack[currentFrame().basePointer + localIndex.toInt()]?.let { push(it) }
                obj?.let { return@runCatching obj }
            }
        },
        OpCode.OpPop to {runCatching { pop() }},
        OpCode.OpCall to ::opCallHandler,
        OpCode.OpReturnValue to ::opReturnValueHandler,
        OpCode.OpReturn to ::opReturnHandler
    )

    fun run(): Result<Any?> {
        fun execute(ip: Int): Result<Any?>? {
            val op = OpCode.entries.getOrNull(currentFrame().instructions()[ip].toInt())
                ?: throw RuntimeError("Unknown opcode: ${currentFrame().instructions()[ip]}")

            // 获取处理函数
            val handler = opHandlers[op] ?: throw RuntimeError("Unknown opcode: $op")
            val result = handler() // 执行对应的处理函数
            return result
        }

        return kotlin.runCatching {
            while (true) {
                if ((currentFrame().ip > currentFrame().instructions().count() - 1)) {
                    return@runCatching null
                }

                val ip = currentFrame().ip
                val executeResult = execute(ip)
                if (executeResult?.isFailure == true) return executeResult

                if (currentFrame().ip == currentFrame().instructions().count() - 1) {
                    val result = execute(ip)
                    if (result?.isFailure == true) return result
                }

                currentFrame().ip ++
            }
        }
    }

    private fun currentFrame(): Frame {
        return frames[frameIndex - 1]
    }

    fun pushFrame(f: Frame) {
        frames.add(frameIndex, f)
        frameIndex++
    }

    fun popFrame(): Frame {
        frameIndex--
        return frames[frameIndex]
    }

    fun pop(): FoxObject? {
        try {
            val obj = stack.pop()
            lastPopped = obj

            stackNextPos--

            return obj
        } catch (e: EmptyStackException) {
            return null
        }
    }

    fun push(obj: FoxObject): OpHandlerResult {
        return runCatching {
            if (stackNextPos >= stackSize) {
                return@runCatching StackOverflowError("StackOverflow!")
            }

            this.stack.push(obj)
            stackNextPos++
        }
    }

    fun stackTop(): FoxObject? {
        if (stackNextPos == 0) {
            return null
        }

        return stack[stackNextPos - 1]
    }

    fun lastPoppedStackElem(): FoxObject? {
        return lastPopped
    }
}