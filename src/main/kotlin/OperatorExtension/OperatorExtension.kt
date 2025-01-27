package me.user.OperatorExtension

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

operator fun BigInteger.compareTo(i: Int): Int {
    return this.compareTo(i.toBigInteger())
}

operator fun BigInteger.plus(i: Int): BigInteger {
    return this.add(i.toBigInteger())
}

operator fun BigInteger.minus(i: Int): BigInteger {
    return this.minus(i.toBigInteger())
}

operator fun Boolean.unaryMinus(): Boolean {
    return !this
}

operator fun <E> ArrayList<E>.get(index: BigInteger): Any? {
    for ((i, element) in this.withIndex()) {
        if (i.toBigInteger() == index) {
            return element
        }
    }

    throw IndexOutOfBoundsException()
}

operator fun BigDecimal.plus(value: BigInteger): BigDecimal {
    return this.plus(value.toBigDecimal())
}

operator fun BigDecimal.minus(value: BigInteger): BigDecimal {
    return this.minus(value.toBigDecimal())
}

operator fun BigDecimal.compareTo(value: BigInteger): Int {
    return this.compareTo(value.toBigDecimal())
}

operator fun BigDecimal.div(value: BigInteger): BigDecimal {
    return this.divide(value.toBigDecimal(),10 , RoundingMode.UP)
}

operator fun BigDecimal.times(value: BigInteger): BigDecimal {
    return this.multiply(value.toBigDecimal())
}