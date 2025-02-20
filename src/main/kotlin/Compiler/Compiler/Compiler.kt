package me.user.Compiler.Compiler

import me.user.Object.*
import me.user.Parser.*
import me.user.Utils.BooleanUtils.*
import me.user.Compiler.OpCode.*
import me.user.Error.CompilerError
import kotlin.reflect.KClass
import me.user.Compiler.CompileHandlers.*
import me.user.Compiler.Instructions
import me.user.Compiler.OpCode
import me.user.Compiler.SymbolTable.GlobalScope
import me.user.Compiler.SymbolTable.SymbolTable
import me.user.Compiler.make

typealias compileHandler = (node: Node) -> Result<Any?>?
typealias handlerResult = Result<Any?>?

class ByteCode(val instructions: Instructions, val constants: ArrayList<FoxObject> )
class EmittedInstruction(var opCode: OpCode? = null, var position: Int = 0)

class CompilationScope (
    var instructions: Instructions = arrayListOf(),
    var lastInstruction: EmittedInstruction = EmittedInstruction(),
    var previousInstruction: EmittedInstruction = EmittedInstruction(),
)

class Compiler(var symbolTable: SymbolTable, private val constants: ArrayList<FoxObject> = arrayListOf()) {
    private var scopes = arrayListOf(CompilationScope())
    private var scopeIndex = 0

    private val operatorOpCodeMap: Map<String, OpCode> = mapOf(
        "+" to OpAdd
    )

    private val nodeHandlerMap: Map<KClass<out Node>, compileHandler> = mapOf(
        Program::class to ::compileProgram,
        IntegerLiteral::class to fun(node: Node): handlerResult = compileLiteral { FoxInteger((it as IntegerLiteral).value) }(node),
        DoubleLiteral::class to fun(node: Node): handlerResult = compileLiteral { FoxDouble((it as DoubleLiteral).value) }(node),
        StringLiteral::class to fun(node: Node): handlerResult = compileLiteral { FoxString((it as StringLiteral).value) }(node),
        BooleanLiteral::class to fun(node: Node): handlerResult = compileLiteral { nativeBooleanToBooleanObject((it as BooleanLiteral).value) }(node),
        InfixExpression::class to fun (node: Node): handlerResult {
            return runCatching {
                val infixExpression = node as InfixExpression

                val left = compile(node.left)?.getOrThrow()
                val right = compile(node.right)?.getOrThrow()

                require(operatorOpCodeMap.containsKey(infixExpression.operator)) {throw CompilerError("Unsupported operator ${infixExpression.operator}") }
                operatorOpCodeMap[infixExpression.operator]?.let { emit(it) }
            }
        },
        Identifier::class to fun (node: Node): handlerResult {
            return runCatching {
                val identifier = node as Identifier
                val queryResult = symbolTable.resolve(identifier.toString())

                if (!queryResult.exist) {
                    throw CompilerError("undefined variable $identifier")
                }

                queryResult.symbol?.index?.let {
                    emit(if (queryResult.symbol.scope == GlobalScope) OpGetGlobal else OpGetLocal, it)
                }
            }
        },
        VarDefineStatement::class to ::compileVarDefineStatement,
//    PrefixExpression::class to ::evalPrefixExpression,
//    AndExpression::class to ::evalLogicalExpression,
//    NotExpression::class to ::evalLogicalExpression,
//    OrExpression::class to ::evalLogicalExpression,
//    ForStatement::class to ::evalForStatement,
//    WhileStatement::class to ::evalWhileStatement,
//    ObjectMemberExpression::class to ::evalObjectMemberExpression,
//    IfExpression::class to ::evalIfExpression,
        BlockStatement::class to ::compileBlockStatement,
//    Identifier::class to ::evalIdentifier,
        FunctionExpression::class to ::compileFunctionExpression,
    CallExpression::class to fun (node: Node): handlerResult {
        return runCatching {
            compile((node as CallExpression).function)?.getOrThrow()
            emit(OpCall)
        }
    },
//    ClassStatement::class to ::evalClassStatement,
//    ObjectCreateExpression::class to ::evalObjectCreateExpression,
//    ArrayLiteral::class to ::evalArrayLiteral,
//    IndexExpression::class to ::evalIndexExpression,
//    DictionaryLiteral::class to ::evalHashLiteral,
        ReturnStatement::class to fun (node: Node): handlerResult {return runCatching{compile((node as ReturnStatement).returnValue)?.getOrThrow() ?: return@runCatching ;emit(OpReturnValue)} },
        ExpressionStatement::class to fun (node: Node): handlerResult {
            return runCatching{
                val expr = (node as ExpressionStatement).expression
                expr?.let {
                    compile(it)
                    emit(OpPop)
                }

                return@runCatching
            }
        }
    )


    private fun currentInstructions(): Instructions {
        return scopes[scopeIndex].instructions
    }

    private fun compileLiteral(factory: (Node) -> FoxObject): (Node) -> handlerResult = { node ->
        runCatching { emit(OpConstant, addConstant(factory(node))) }
    }

    fun emit(op: OpCode, vararg operands: Int): Int {
        val ins = make(op, *operands)
        val pos = addInstruction(ins)

        setLastInstruction(op, pos)
        return pos
    }


    fun replaceLastPopWithReturn() {
        val lastPos = scopes[scopeIndex].lastInstruction.position
        replaceInstruction(lastPos, make(OpReturnValue))

        scopes[scopeIndex].lastInstruction.opCode = OpReturnValue
    }

    fun enterScope() {
        val scope = CompilationScope(
            instructions = Instructions(),
            lastInstruction = EmittedInstruction(),
            previousInstruction = EmittedInstruction()
        )

        scopes.add(scope)
        scopeIndex++

        symbolTable = SymbolTable(symbolTable)
    }

    fun leaveScope(): Instructions {
        val instructions = currentInstructions()

        scopes = ArrayList(scopes.subList(0, scopes.count() - 1))
        scopeIndex--

        symbolTable = symbolTable.outer!!

        return instructions
    }

    // 设置最后一条指令
    private fun setLastInstruction(op: OpCode, pos: Int) {
        val scope = scopes[scopeIndex]
        val previous = scope.lastInstruction
        val last = EmittedInstruction(op, pos)
        scope.previousInstruction = previous
        scope.lastInstruction = last
    }

    // 检查最后一条指令是否为 OpPop
    fun lastInstructionIs(op: OpCode): Boolean {
        if (currentInstructions().isEmpty()) return false
        return scopes[scopeIndex].lastInstruction.opCode == op
    }

    // 移除最后一条 Pop 指令
    fun removeLastPop() {
        val scope = scopes[scopeIndex]
        val last = scope.lastInstruction ?: return
        val previous = scope.previousInstruction
        val oldInstructions = currentInstructions()
        val newInstructions = oldInstructions.slice(0 until last.position)
        scope.instructions = newInstructions as Instructions
        scope.lastInstruction = previous
    }

    // 替换指令
    private fun replaceInstruction(pos: Int, newInstruction: ByteArray) {
        val instructions = currentInstructions()
        for (i in newInstruction.indices) {
            instructions[pos + i] = newInstruction[i]
        }
    }

    // 修改操作数
    fun changeOperand(opPos: Int, operand: Int) {
        val op = OpCode.entries[currentInstructions()[opPos].toInt()]
        val newInstruction = make(op, operand)
        replaceInstruction(opPos, newInstruction)
    }

    private fun addInstruction(ins: ByteArray): Int {
        val posNewInstruction = currentInstructions().size
        val updatedInstructions = ArrayList(currentInstructions()).apply { addAll(ins.toMutableList()) }
        scopes[scopeIndex].instructions = updatedInstructions
        return posNewInstruction
    }

    fun addConstant(obj: FoxObject): Int {
        constants.add(obj)
        return constants.count() - 1
    }

    fun byteCode(): ByteCode {
        return ByteCode(currentInstructions(), constants)
    }

    fun compile(node: Node?): Result<Any?>? {
        if (node == null) {
            return null
        }

        if (nodeHandlerMap.containsKey(node::class)) {
            return nodeHandlerMap[node::class]?.invoke(node)
        }

        return null
    }

    fun compileProgram(node: Node): Result<Any?>? {
        val program: Program = node as Program

        for (stmt in program.statements) {
            val r = compile(stmt)
            r?.let {
                if (it.isFailure) return it
            }
        }

        return null
    }

    fun compileBlockStatement(node: Node): Result<Any?>? {
        val blockStatement = node as BlockStatement

        for (stmt in blockStatement.statements) {
            val r = compile(stmt)
            r?.let {
                if (r.isFailure) return r
            }
        }

        return null
    }
}