package me.user.VM.Handlers.FunctionOpHandler

import me.user.VM.VM.OpHandlerResult
import me.user.VM.VM.VM

fun VM.opReturnValueHandler(): OpHandlerResult {
    return runCatching {
        // 获取返回值
        val returnValue = pop()

        val frame = popFrame()
        stackNextPos = frame.basePointer - 1

        // 尝试添加返回值到栈中，添加时报错则抛出错误
        returnValue?.let { push(it) }?.getOrThrow()
    }
}