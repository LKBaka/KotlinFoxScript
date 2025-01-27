package me.user.Utils.BooleanUtils

import me.user.Object.*

val FoxTrue = FoxBoolean(true)
val FoxFalse = FoxBoolean(false)

fun nativeBooleanToBooleanObject(boolean: Boolean): FoxBoolean{
    return if (boolean) FoxTrue else FoxFalse
}

fun isTruthy(obj: FoxObject?): Boolean {
    // 判空
    if (obj == null) return false

    return when (obj) {
        is FoxNothing -> false
        is FoxBoolean -> obj.value
        is FoxInteger -> obj.value != 0.toBigInteger()
//        is FoxDouble -> (obj as Fox_Double).value!= 0.0
        is FoxString -> (obj as FoxString).value.isNotEmpty()
        else -> false
    }
}