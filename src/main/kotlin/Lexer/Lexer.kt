package me.user.Lexer

import java.math.BigInteger

import me.user.Constant.*
import me.user.Utils.StringUtils
import me.user.OperatorExtension.*

class Lexer(private val code: String) {
    private var pos: BigInteger = 0.toBigInteger()
    private var nextPos: BigInteger = 0.toBigInteger()
    private var currentChar: Char = nullChar
    private var line: BigInteger = 1.toBigInteger()

    init {
        readChar()
    }

    private fun isEmoji(char: Char): Boolean {
        val codePoint = char.code
        return codePoint in 0x1F600..0x1F64F ||  // 表情符号 & 人 & 手势
                codePoint in 0x1F900..0x1F9FF ||  // 补充符号 & 图案
                codePoint in 0x2600..0x26FF ||    // 杂项符号
                codePoint in 0x2700..0x27BF       // 杂项符号 & 图案
    }

    private fun isValidChar(char: Char): Boolean {
        return char.isLetterOrDigit() || char == '_' || isEmoji(char)
    }

    private fun peekChar(): Char {
        if (nextPos <= code.count() - 1) {
            return code[nextPos.toInt()]
        }

        return nullChar
    }

    private fun readChar() {
        if (pos <= code.count() - 1 && nextPos <= code.count() - 1) {
            pos = nextPos
            nextPos += 1

            currentChar = code[pos.toInt()]
        } else {
            this.currentChar = nullChar
        }

        if ("$currentChar" == newLine) {line ++}
    }

    private fun skipWhiteSpace() {
        while (
            "$currentChar" != newLine &&
            currentChar != cr &&
            currentChar != lf &&
            currentChar.isWhitespace()
        ) {
            readChar()
        }
    }

    private fun getNextToken(): Token {
        skipWhiteSpace()
        val token = Token(TokenType.ILLEGAL,currentChar.toString(), line)

        when ("$currentChar") {
            "=" -> {
                if (peekChar() == '=') {
                    token.value = "=="
                    token.tokenType = TokenType.EQ
                    readChar()
                } else {
                    token.tokenType = TokenType.ASSIGN
                }
            }

            "!" -> {
                if (peekChar() == '=') {
                    token.value = "!="
                    token.tokenType = TokenType.NOT_EQ
                    readChar()
                } else {
                    token.tokenType = TokenType.BANG
                }
            }

            "<" -> {
                if (peekChar() == '>') {
                    token.value = "<>"
                    token.tokenType = TokenType.NOT_EQ
                    readChar()
                } else {
                    token.tokenType = TokenType.LT
                }
            }

            "$cr", "$lf", newLine, ";" -> {
                token.tokenType = TokenType.EOL
            }

            "\"" -> {
                token.tokenType = TokenType.STRING
                if (code.count() - 1 >= (pos + 2).toInt() && peekChar() == '"' && code[(nextPos + 1).toInt()] == '"') {
                    token.value = readMultiLineString()
                    return token
                }
                token.value = readString()
            }

            else -> {
                if (currentChar.isDigit()) {
                    token.value = readIntNumber()
                    token.tokenType = TokenType.INTEGER
                    readChar()
                    return token
                }

                if (isValidChar(currentChar)) {
                    token.value = StringUtils.Trim(readIdentifier())
                    token.tokenType = getIdentTokenType(token.value.uppercase())
                    return token
                }

                if (TokenTypeMap.containsKey(currentChar.toString())) {
                    token.tokenType = TokenTypeMap[currentChar.toString()]!!
                    readChar()
                    return token
                }
            }
        }

        readChar()
        return token
    }

    private fun readMultiLineString(): String {
        val result = StringBuilder()
        // 跳过开始的三个引号
        readChar()
        readChar()
        readChar()

        // 读取字符串内容，直到遇到结束的三个引号
        while (true) {
            if (currentChar == '"' && peekChar() == '"' && code.getOrElse((nextPos + 1).toInt()) { '\u0000' } == '"') {
                // 跳过结束的三个引号
                readChar()
                readChar()
                readChar()
                return result.toString()
            } else {
                result.append(currentChar)
                readChar()
                if (currentChar == '\u0000') {
                    // 如果遇到字符串结束，则返回空字符串
                    return ""
                }
            }
        }
    }

    private fun readString(): String {
        val p = pos
        
        var exitWhile = false
        while (!exitWhile) {
            readChar()
            if (currentChar == '"' || currentChar == lf) {
                exitWhile = true
            }
        }
        return code.substring((p + 1).toInt(), pos.toInt())
    }

    private fun getIdentTokenType(ident: String): TokenType {
        if (TokenTypeMap.containsKey(ident)) {return TokenTypeMap[ident]!!}

        return TokenType.IDENT
    }

    private fun readIdentifier(): String {
        val startPos = pos

        readChar()
        while (pos <= code.count() - 1 && isValidChar(currentChar)) {
            readChar()
        }

        return code.substring(startPos.toInt(),pos.toInt())
    }
    private fun readIntNumber(): String {
        val startPos = pos

        while (currentChar.isDigit() && peekChar().isDigit()) {
            readChar()
        }

        return code.substring(startPos.toInt(),(pos + 1).toInt())
    }

    fun getTokens(): ArrayList<Token> {
        val tokens = arrayListOf<Token>()

        while (currentChar != nullChar) {
            val token = getNextToken()
            tokens.add(token)
        }

        return tokens
    }
}