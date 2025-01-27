package me.user.Evaluator

import me.user.Environment.BuiltinEnvironment
import me.user.Environment.Environment
import me.user.Evaluator.ArrayLiteralEvaluator.evalArrayLiteral
import me.user.Evaluator.DeclareStatementEvaluator.evalDeclareStatement
import me.user.Evaluator.DictionaryLiteralEvaluator.evalHashLiteral
import me.user.Evaluator.FunctionEvaluator.evalCallExpression
import me.user.Evaluator.FunctionEvaluator.evalFunctionExpression
import me.user.Evaluator.IdentifierEvaluator.evalIdentifier
import me.user.Evaluator.IfExpression.evalIfExpression
import me.user.Evaluator.IndexExpressionEvaluator.evalIndexExpression
import me.user.Evaluator.InfixExpressionEvaluator.evalInfixExpression
import me.user.Evaluator.LogicalExpressionEvaluator.evalLogicalExpression
import me.user.Evaluator.LoopEvaluator.evalForStatement
import me.user.Evaluator.LoopEvaluator.evalWhileStatement
import me.user.Evaluator.OOPEvaluator.evalClassStatement
import me.user.Evaluator.OOPEvaluator.evalObjectCreateExpression
import me.user.Evaluator.OOPEvaluator.evalObjectMemberExpression
import me.user.Evaluator.VarDefineStatementEvaluator.evalVarDefineStatement
import me.user.Object.*
import me.user.Parser.*
import me.user.PrefixExpressionEvaluator.evalPrefixExpression
import me.user.Utils.BooleanUtils.*
import me.user.Utils.ErrorUtils.*

import kotlin.reflect.KClass

typealias nodeHandler = (node: Node, env: Environment) -> FoxObject?
private val nodeHandlerMap: Map<KClass<out Node>, nodeHandler> = mapOf(
    Program::class to ::evalProgram,
    IntegerLiteral::class to fun (node: Node, _: Environment): FoxObject {return FoxInteger((node as IntegerLiteral).value)},
    DoubleLiteral::class to fun (node: Node, _: Environment): FoxObject {return FoxDouble((node as DoubleLiteral).value)},
    StringLiteral::class to fun (node: Node, _: Environment): FoxObject {return FoxString((node as StringLiteral).value)},
    BooleanLiteral::class to fun (node: Node, _: Environment): FoxObject {return nativeBooleanToBooleanObject((node as BooleanLiteral).value)},
    PrefixExpression::class to ::evalPrefixExpression,
    AndExpression::class to ::evalLogicalExpression,
    NotExpression::class to ::evalLogicalExpression,
    OrExpression::class to ::evalLogicalExpression,
    ForStatement::class to ::evalForStatement,
    WhileStatement::class to ::evalWhileStatement,
    ObjectMemberExpression::class to ::evalObjectMemberExpression,
    InfixExpression::class to ::evalInfixExpression,
    IfExpression::class to ::evalIfExpression,
    BlockStatement::class to ::evalBlockStatement,
    Identifier::class to ::evalIdentifier,
    VarDefineStatement::class to ::evalVarDefineStatement,
    FunctionExpression::class to ::evalFunctionExpression,
    CallExpression::class to ::evalCallExpression,
    ClassStatement::class to ::evalClassStatement,
    ObjectCreateExpression::class to ::evalObjectCreateExpression,
//    DeclareStatement::class to ::evalDeclareStatement,
    ArrayLiteral::class to ::evalArrayLiteral,
    IndexExpression::class to ::evalIndexExpression,
    DictionaryLiteral::class to ::evalHashLiteral,
    ReturnStatement::class to fun (node: Node, env: Environment): FoxObject {return FoxReturnValue(eval((node as ReturnStatement).returnValue, env))},
    ExpressionStatement::class to fun (node: Node, env: Environment): FoxObject? {return eval((node as ExpressionStatement).expression, env)}
)

val builtinEnvironment = BuiltinEnvironment()

fun eval(node: Node?, env: Environment): FoxObject? {
    if (node == null) {
        return null
    }

    if (nodeHandlerMap.containsKey(node::class)) {
        return nodeHandlerMap[node::class]?.invoke(node, env)
    }

    return null
}

fun evalProgram(node: Node, env: Environment): FoxObject? {
    val program: Program = node as Program
    var result: FoxObject? = null

    for (stmt in program.statements) {
        val r: FoxObject? = eval(stmt, env)
        result = r ?: result

        if (isError(result) || result?.type() == ObjectType.RETURN_VALUE_OBJ) return result
    }

    return result
}


fun evalBlockStatement(node: Node, env: Environment): FoxObject? {
    val blockStatement = node as BlockStatement
    var result: FoxObject? = null

    for (stmt in blockStatement.statements) {
        val r: FoxObject? = eval(stmt, env)
        result = r ?: result

        if (isError(result) || result?.type() == ObjectType.RETURN_VALUE_OBJ) return result
    }

    return result
}

