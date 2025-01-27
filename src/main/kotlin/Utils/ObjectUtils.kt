package me.user.Utils

import me.user.Object.FoxObject
import me.user.Object.ObjectType
import me.user.Object.Type

fun replaceObj(list1: List<Type>, list2: List<Type>): List<Type> {
    return if (list1.size == list2.size) {
        val newList = arrayListOf<Type>()
        for (i in list1.indices) {
            val list1Types = list1[i].getTypes()
            val list2Types = list2[i].getTypes()

            // 如果 list1 当前元素包含 OBJ，尝试用 list2 的非 OBJ 类型替换
            if (list1Types.contains(ObjectType.OBJ)) {
                // 寻找 list2 中的第一个非 OBJ 类型
                val replacementType = list2Types.firstOrNull { it != ObjectType.OBJ }
                if (replacementType != null) {
                    // 创建新 Type，替换所有 OBJ 为 replacementType
                    val newTypes = list1Types.map {
                        if (it == ObjectType.OBJ) replacementType else it
                    }
                    newList.add(Type(*newTypes.toTypedArray()))
                } else {
                    // list2 中也全是 OBJ，保留原值
                    newList.add(list1[i])
                }
            } else {
                // list1 当前元素无 OBJ，直接保留
                newList.add(list1[i])
            }
        }
        newList
    } else {
        list1
    }
}

fun isInstance(obj1: FoxObject, obj2: FoxObject): Boolean {
    if (obj1.uuid == obj2.uuid) return true
    if (obj1.type() == obj2.type()) return true
    return false
}