package me.user.VM.Handlers.FunctionOpHandler

import me.user.Error.RuntimeError
import me.user.Object.FoxCompiledFunction
import me.user.VM.*
import me.user.VM.VM.OpHandlerResult
import me.user.VM.VM.VM

fun VM.opCallHandler(): OpHandlerResult {
    return runCatching {
        // 获取编译后的函数
//        val function = stack[this.stackNextPos - 1] as? FoxCompiledFunction ?: throw RuntimeError("${stack[this.stackNextPos - 1]?.type()} 类型的对象不可被当成函数调用")
        val function = stack.last() as? FoxCompiledFunction ?: throw RuntimeError("${stack.last()?.type()} 类型的对象不可被当成函数调用")

        // 获取栈帧
        val frame = Frame(function, basePointer = stackNextPos)

        // 添加栈帧
        pushFrame(frame)

        stackNextPos = frame.basePointer + function.numLocals
    }
}