package me.user.VM.Handlers.FunctionOpHandler

import me.user.Utils.ObjNothing
import me.user.VM.VM.OpHandlerResult
import me.user.VM.VM.VM

fun VM.opReturnHandler(): OpHandlerResult {
    return runCatching {
        val frame = popFrame()
        stackNextPos = frame.basePointer - 1

        // 尝试添加默认返回值（Nothing）到栈中，添加时报错则抛出错误
        push(ObjNothing)?.getOrThrow()
    }
}