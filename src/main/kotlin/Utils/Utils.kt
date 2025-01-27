package me.user.Utils

import me.user.Object.FoxNothing
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

val ObjNothing = FoxNothing()

fun println(list: List<Any>) {
    for (item in list) {
        println(item)
    }
}

fun Any.hasMember(memberName: String): Boolean {
    val kClass: KClass<out Any> = this::class

    // 检查是否存在属性
    val hasProperty = kClass.members.any { it.name == memberName }
    if (hasProperty) return true

    // 检查是否存在方法
    val hasFunction = kClass.members.any { it.name == memberName }
    return hasFunction
}

fun Any.getProperty(propertyName: String) =
    this::class.memberProperties.firstOrNull { it.name == propertyName }

fun Any.getPropertyValue(propertyName: String): Any? {
    return getProperty(propertyName)?.let { prop ->
        prop.isAccessible = true
        prop.getter.call(this)
    }
}