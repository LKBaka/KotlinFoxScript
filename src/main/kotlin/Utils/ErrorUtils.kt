package me.user.Utils.ErrorUtils

import me.user.Object.FoxError
import me.user.Object.FoxObject
import me.user.Object.ObjectType

fun throwError(message: String): FoxError {
    return FoxError(message)
}

fun isError(obj: FoxObject?): Boolean {
    if (obj == null) return false

    return when (obj.type()) {
        ObjectType.ERROR_OBJ -> true
        else -> false
    }
}