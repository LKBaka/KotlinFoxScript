package me.user.Runner

import me.user.Constant.lf
import me.user.Environment.Environment
import me.user.Lexer.Lexer
import me.user.Lexer.Token
import me.user.Lexer.TokenType
import me.user.Object.FoxObject
import me.user.Parser.Parser
import me.user.Utils.ErrorUtils.isError
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.Charset

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

class FileRunner(private val filePath: String) {
    fun run() {
        try {
            val file = File(filePath)
            val code = file.readText(Charset.defaultCharset())

            val result: FoxObject? = eval(code, Environment())
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
    fun run() {
        val env = Environment()

        while (true) {
            print(">>> ")
            var code: String = readln() + lf
            val multilineCode = readMultiLineInput(code)

            if (multilineCode != "") code = multilineCode

            val result: FoxObject? = eval(code, env)

            if (result != null) {
                println(result.inspect())
            }
        }
    }

    private fun getTokenTypes(tokens: ArrayList<Token>): ArrayList<TokenType> {
        val tokenTypes = ArrayList<TokenType>()
        if (tokens.isEmpty()) {
            return tokenTypes
        }
        for (tkn in tokens) {
            tokenTypes.add(tkn.tokenType)
        }
        return tokenTypes
    }


    private fun readMultiLineInput(line: String): String {
        var code = ""
        val tokens = Lexer(line).getTokens()
        val tokenTypes = getTokenTypes(tokens)
        val tokenTypePos = 0

        var curTokenType = tokenTypes[tokenTypePos]

        val multiLineTokenTypeDictionary = HashMap<TokenType, TokenType>()
        multiLineTokenTypeDictionary[TokenType.IF] = TokenType.END
        multiLineTokenTypeDictionary[TokenType.WHILE] = TokenType.END
        multiLineTokenTypeDictionary[TokenType.FOR] = TokenType.NEXT
        multiLineTokenTypeDictionary[TokenType.FUNC] = TokenType.END
        multiLineTokenTypeDictionary[TokenType.CLASS] = TokenType.END
        multiLineTokenTypeDictionary[TokenType.TRY] = TokenType.END
        multiLineTokenTypeDictionary[TokenType.LPAREN] = TokenType.RPAREN
        multiLineTokenTypeDictionary[TokenType.LBRACE] = TokenType.RBRACE

        for ((startTokenType) in multiLineTokenTypeDictionary) {
            if (!tokenTypes.contains(startTokenType)) continue
            if (tokenTypes.contains(startTokenType) && tokenTypes.contains(multiLineTokenTypeDictionary[startTokenType])) continue
            val lines = ArrayList<String>()
            var input: String
            lines.add(line)
            while (curTokenType!= multiLineTokenTypeDictionary[startTokenType]) {
                print("... ")
                input = readln() + lf
                lines.add(input)

                tokens.clear()
                tokens.addAll(Lexer(input).getTokens())
                tokenTypes.clear()
                tokenTypes.addAll(getTokenTypes(tokens))
                curTokenType = tokenTypes[0]
            }
            code = lines.joinToString("\n")
        }
        return code
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