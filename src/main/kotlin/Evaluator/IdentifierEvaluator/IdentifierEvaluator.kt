package me.user.Evaluator.IdentifierEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.builtinEnvironment
import me.user.Object.FoxObject
import me.user.Parser.Identifier
import me.user.Parser.Node
import me.user.Utils.ErrorUtils.throwError
import me.user.Utils.StringUtils

fun evalIdentifier(node: Node, env: Environment): FoxObject? {
    val ident = node as Identifier
    val name = StringUtils.Trim(ident.value)

    val envQueryResult = env.getValue(name)
    if (envQueryResult.exist) {
        return envQueryResult.foxObject
    }

    val builtinQueryResult = builtinEnvironment.getValue(name)
    if (builtinQueryResult.exist) {
        return builtinQueryResult.foxObject
    }

    return throwError("找不到标识符 $name")
}
