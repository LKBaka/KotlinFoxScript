package me.user.Evaluator.LoopEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.FoxArray
import me.user.Object.FoxObject
import me.user.Object.FoxString
import me.user.Object.ObjectType
import me.user.Parser.ForStatement
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError

fun evalForStatement(node: Node, env: Environment): FoxObject? {
    val forStatement = node as ForStatement
    val itemsObject = eval(forStatement.items, env)
    val itemName = forStatement.itemVar?.value ?: ""

    fun arrayForLoop(itemsObject: FoxArray, itemName: String): FoxObject? {
        for (item in itemsObject.elements) {
            item?.let {
                env.setValue(itemName, Data(it, true), true)

                val result = eval(forStatement.loopBlock, env)
                if (isError(result)) return result
            }
        }

        env.removeValue(itemName)
        return null
    }

    fun stringForLoop(itemsObject: FoxString, itemName: String): FoxObject? {
        for (char in itemsObject.value) {
            env.setValue(itemName, Data(FoxString("$char"), true), true)

            val result = eval(forStatement.loopBlock, env)
            if (isError(result)) return result
        }

        env.removeValue(itemName)
        return null
    }

    return itemsObject?. let {
        when (itemsObject.type()) {
            ObjectType.ARRAY_OBJ -> arrayForLoop(itemsObject as FoxArray, itemName)
            ObjectType.STRING_OBJ -> stringForLoop(itemsObject as FoxString, itemName)
            else -> null
        }
    }
}

