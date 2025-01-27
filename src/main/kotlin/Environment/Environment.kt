package me.user.Environment

import me.user.Object.*
import me.user.Utils.ErrorUtils.throwError


class Data(var foxObject: FoxObject, var isReadonly: Boolean)
class QueryResults(val exist: Boolean, val foxObjects: ArrayList<FoxObject>?)
class QueryResult(val exist: Boolean, val foxObject: FoxObject?)

open class Environment(var outer: Environment? = null) {
    open val dataMap: MutableMap<String, ArrayList<Data>> = mutableMapOf()

    fun getValues(name: String): QueryResults {
        // 判断此名字在数据表中是否存在
        if (dataMap.containsKey(name)) {
            // 当存在时，返回结果
            val objects: ArrayList<FoxObject> = arrayListOf()
            for (data in dataMap[name]!!) {
                objects.add(data.foxObject)
            }

            return QueryResults(true, objects)
        }

        // 尝试在父类查找
        outer?.let {
            return outer!!.getValues(name)
        }

        return QueryResults(false, null)
    }

    fun getValue(name: String): QueryResult {
        // 判断此名字在数据表中是否存在
        if (dataMap.containsKey(name)) {
            // 当存在时，返回结果
            return QueryResult(true, dataMap[name]!![0].foxObject)
        }

        return QueryResult(false, null)
    }


    fun setValue(name: String, data: Data, inPlaceUpdate: Boolean = false): FoxObject? {
        // 当数据表没有这个名字
        if (!dataMap.containsKey(name)) {
            // 向数据表添加数据
            dataMap[name] = arrayListOf(data)
            return null
        }

        val dataArray: ArrayList<Data> = dataMap[name]!!
        if (dataArray.isNotEmpty()) {
            for (i in dataArray.indices) {
                val d = dataArray[i]
                if (d.foxObject == data.foxObject) {
                    if (d.isReadonly) {
                        return throwError("$name 为只读！")
                    }
                    return null
                }

                if (inPlaceUpdate) {
                    dataMap[name]!![i].foxObject = data.foxObject
                } else {
                    dataMap[name]!!.add(data)
                }
            }
        }

        return null
    }

    fun removeValue(name: String) {
        if (dataMap.containsKey(name)) {
            dataMap.remove(name)
        }
    }
}

class ClassEnvironment: Environment() {
    override val dataMap: MutableMap<String, ArrayList<Data>> = mutableMapOf()

    fun addFunction(name: String,kotlinFunctionObject: FoxKotlinFunction) {
        setValue(name, Data(foxObject = kotlinFunctionObject, true))
    }
}

class FunctionEnvironment: Environment()
