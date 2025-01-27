package me.user.Evaluator.DictionaryLiteralEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.eval
import me.user.Object.*
import me.user.Parser.DictionaryLiteral
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.FoxDictionaryUtils

fun evalHashLiteral(
    node: Node,
    env: Environment
): FoxObject? {
    val pairs = mutableMapOf<FoxDictionaryKey, FoxDictionaryPair>()
    val dictionaryLiteral = node as DictionaryLiteral

    dictionaryLiteral.pairs?. let {
        for ((keyNode, valueNode) in it) {
            val key = eval(keyNode, env)
            val dictKeyType = key?.type()
            if (isError(key)) return key

            val dictValue = eval(valueNode, env)
            if (isError(dictValue)) return dictValue

            when (dictKeyType) {
                ObjectType.INTEGER_OBJ, ObjectType.BOOLEAN_OBJ -> {
                    pairs[FoxDictionaryUtils.createKey(key)] = FoxDictionaryPair().apply {
                        this.key = FoxDictionaryUtils.createKey(key)
                        this.value = dictValue
                    }
                }
                ObjectType.STRING_OBJ -> {
                    val valStr = (key as FoxString).inspect()
                    pairs[FoxDictionaryUtils.createKey(FoxString(valStr))] =
                        FoxDictionaryPair().apply {
                            this.key = FoxDictionaryUtils.createKey(FoxString(valStr))
                            value = dictValue
                        }
                }
                else -> return throwError("字典的键不支持${dictKeyType}类型")
            }
        }
    }

    return FoxDictionary().apply { this.pairs = pairs }
}