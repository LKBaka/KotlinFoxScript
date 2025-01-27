package me.user.Utils.ConvertUtils

import java.math.BigInteger

fun CInt(boolean: Boolean): Int {
    return if (boolean) 1 else 0
}

fun CBool(int: Int): Boolean {
    return int != 0
}

fun CBool(bigInteger: BigInteger): Boolean {
    return bigInteger != 0.toBigInteger()
}