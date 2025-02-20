package me.user.Runner

import me.user.Compiler.Compiler.Compiler
import me.user.Compiler.SymbolTable.SymbolTable
import me.user.Constant.lf
import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Lexer.Lexer
import me.user.Lexer.TokenType
import me.user.Lexer.TokenType.*
import me.user.Object.FoxArray
import me.user.Object.FoxObject
import me.user.Object.FoxString
import me.user.Parser.BlockStatement
import me.user.Parser.Parser
import me.user.Utils.ErrorUtils.isError
import me.user.Utils.getCurrentDirectory
import me.user.VM.VM.VM
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.util.*
import kotlin.io.path.Path

enum class RunMode {
    REPL,
    FILE
}

fun eval(code: String,env: Environment): FoxObject? {
    val lexer = Lexer(code)
    val parser = Parser(lexer)
    val program = parser.parseProgram()

    if (parser.checkParseError()) {return null}

    return me.user.Evaluator.eval(program, env)
}

fun compileEval(
    code: String,
    symbolTable: SymbolTable = SymbolTable(),
    globals: ArrayList<FoxObject> = arrayListOf(),
    constants: ArrayList<FoxObject> = arrayListOf()
): FoxObject? {
    val lexer = Lexer(code)
    val parser = Parser(lexer)
    val program = parser.parseProgram()

    if (parser.checkParseError()) {return null}

    val compiler = Compiler(symbolTable, constants)
    val compileResult = compiler.compile(program)
    compileResult?.onFailure { err -> println("compiler error: ${err.message}"); err.printStackTrace(); return null}

    val vm = VM(compiler.byteCode(), globals)
    val result = vm.run()
    result.onFailure { err -> println("runtime error: ${err.message}"); err.printStackTrace(); return null }

    return vm.lastPoppedStackElem()
}

class ModuleRunner(private val filePath: String) {
    private val appPath = getCurrentDirectory()

    fun run(env: Environment): BlockStatement? {
        if (!File(filePath).exists()) {
            println("找不到文件 $filePath")
            return null
        }

            val file = File(filePath)
            val code = file.readText(Charset.defaultCharset())

            env.setValue(
                "ModulePath",
                Data(
                    FoxArray(
                        arrayListOf(
                            FoxString("${appPath}//includes//"),
                            FoxString(File(filePath).parent)
                        )
                    ),
                    true
                ),
                true
            )

            val lexer = Lexer(code)
            val parser = Parser(lexer)
            val program = parser.parseProgram()

            if (parser.checkParseError()) {return null}

            val result: FoxObject? = eval(code, env)
            result?.let {
                if (isError(result)) println("${result.inspect()} 在 $filePath")
            }

        return BlockStatement().apply { statements.addAll(program.statements) }
    }
}

class FileRunner(private val filePath: String) {
    private val appPath = getCurrentDirectory()

    fun run() {
        try {
            val file = File(filePath)
            val code = file.readText(Charset.defaultCharset())

            val env = Environment()
            env.setValue(
                "ModulePath",
                Data(
                    FoxArray(
                        arrayListOf(
                            FoxString("${appPath}\\includes\\"),
                            FoxString(File(filePath).parent)
                        )
                    ),
                    true
                ),
                true
            )

            val result: FoxObject? = eval(code, env)
            result?.let {
                if (isError(result)) println(result.inspect())
            }
        } catch (e: FileNotFoundException) {
            println("找不到文件 $filePath")
            println(e.message)
        }
    }
}

class REPLRunner {
    private val appPath = getCurrentDirectory()

    private val multiLineDelimiters = mapOf(
        IF to END,
        WHILE to END,
        FOR to NEXT,
        FUNC to END,
        CLASS to END,
        TRY to END,
        LPAREN to RPAREN,
        LBRACE to RBRACE
    )

    fun run() {
        val env = Environment()
        env.setValue(
            "ModulePath",
            Data(
                FoxArray(
                    arrayListOf(FoxString("${appPath}\\includes\\"))
                ),
                true
            ),
            true
        )

        while (true) {
            print(">>> ")
            val initialInput = readln().trim()
            if (initialInput.isEmpty()) continue

            val fullCode = readMultiLineInput(initialInput)
            val result = eval(fullCode, env)
            result?.let { println(it.inspect()) }
        }
    }

    private fun readMultiLineInput(initialLine: String): String {
        val lines = mutableListOf(initialLine)
        var delimiterStack = Stack<TokenType>()

        var continueLoop = true
        while (continueLoop) {
            val currentCode = lines.joinToString("\n")
            analyzeTokens(currentCode).let { newStack ->
                continueLoop = newStack.isNotEmpty()
                delimiterStack = newStack
            }

            if (continueLoop) {
                print("... ")
                val line = readln().trim()

                // 检查是否输入了结束标志
                if (line.isNotEmpty() && !isLineWithEndDelimiter(line)) {
                    lines.add(line)
                } else {
                    analyzeTokens(line).let { newStack ->
                        continueLoop = newStack.isNotEmpty()
                        delimiterStack = newStack
                    }

                    lines.add(line)
                }
            }
        }

        return lines.joinToString("$lf")
    }

    private fun isLineWithEndDelimiter(line: String): Boolean {
        return line.uppercase().contains("END") || line.contains("}") || line.contains(")")
    }

    private fun analyzeTokens(code: String): Stack<TokenType> {
        val stack = Stack<TokenType>()
        val tokens = Lexer(code).getTokens()

        tokens.forEach { token ->
            when (token.tokenType) {
                // 检测函数定义的结束
                FUNC -> stack.push(FUNC)
                IF -> stack.push(IF)
                CLASS -> stack.push(CLASS)
                WHILE -> stack.push(WHILE)
                FOR -> stack.push(FOR)
                LPAREN -> stack.push(LPAREN)
                LBRACKET -> stack.push(LBRACKET)
                END -> {
                    if (stack.isNotEmpty() && multiLineDelimiters.containsKey(stack.peek())) {
                        stack.pop()
                    }
                }
                else -> {}
            }
        }
        return stack
    }
}

class Runner(private val runMode: RunMode, private val filePath: String = "") {
    fun run() {
        return when (runMode) {
            RunMode.REPL -> REPLRunner().run()
            RunMode.FILE -> FileRunner(filePath).run()
        }
    }
}