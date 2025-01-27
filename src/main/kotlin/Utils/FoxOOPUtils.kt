package me.user.Utils

import me.user.Object.*
import me.user.Utils.ErrorUtils.*

fun extendClassEnv(clsObj: FoxClass): FoxObject? {
    clsObj.baseClass?.let { base ->
        if (isError(base)) return base

        val baseClass = base as FoxClass
        val baseEnv = baseClass.env

        baseEnv.dataMap.forEach { (name, data) ->
            when (name) {
                "Me", "New" -> return@forEach
                else -> {
                    for (d in data) {
                        clsObj.env.setValue(name, d)
                    }
                }
            }
        }

        clsObj.env.dataMap.forEach {(name, data) ->
            if (name == "Me") return@forEach

            for (d in data) {
                if (d.foxObject is FoxFunction) {
                    val newFunction = (d.foxObject as FoxFunction)
                    newFunction.env = clsObj.env
                    d.foxObject = newFunction

                    clsObj.env.setValue(name, d)
                }
            }
        }
    }

    return clsObj
}