package me.user.Evaluator.LoopEvaluator

import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.builtinEnvironment
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.Parser.ForStatement
import me.user.Parser.Node
import me.user.Parser.WhileStatement
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError

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

    return itemsObject?. let {
        when (itemsObject.type()) {
            ObjectType.ARRAY_OBJ -> arrayForLoop(itemsObject as FoxArray, itemName)
            else -> null
        }
    }
}

