package me.user.Parser

import me.user.Constant.*
import me.user.Environment.Environment
import me.user.Environment.FunctionEnvironment
import me.user.Lexer.Token
import me.user.Lexer.TokenType
import me.user.Object.ObjectType
import me.user.Object.Type
import me.user.Utils.StringUtils
import java.math.BigDecimal
import java.math.BigInteger

interface Node {
    fun tokenLiteral(): String
}

interface Statement: Node
interface Expression: Node

class Program: Node {
    val statements = ArrayList<Statement>()

    override fun tokenLiteral(): String {
        return if (statements.isNotEmpty()) statements[0].tokenLiteral() else ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        for (stmt in statements) {
            stringBuilder.append(stmt.toString())
        }

        return stringBuilder.toString()
    }
}

class BlockStatement: Statement {
    val statements = ArrayList<Statement>()

    override fun tokenLiteral(): String {
        return if (statements.isNotEmpty()) statements[0].tokenLiteral() else ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        for (stmt in statements) {
            stringBuilder.append(stmt.toString())
        }

        return stringBuilder.toString()
    }
}

class VarDefineStatement: Statement {
    var token: Token? = null // 词法单元类型可能是TokenType.LET 或者 TokenType.DIM
    var identifier: Expression? = null // 欲定义标识符
    var value: Expression? = null // 欲定义内容
    var type: Expression? = null
    var isReadOnly: Boolean = false

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append(this.tokenLiteral())
        stringBuilder.append(" ${identifier?.toString() ?: ""} ")
        stringBuilder.append("= ${value?.toString() ?: ""}")

        return stringBuilder.toString()
    }
}

class ReturnStatement: Statement {
    var token: Token? = null // 词法单元类型可能是TokenType.RETURN
    var returnValue: Expression? = null // 欲定义内容

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append(this.tokenLiteral())
        stringBuilder.append(" ${returnValue.toString()} ")

        return stringBuilder.toString()
    }
}

class ExpressionStatement: Statement {
    var expression: Expression? = null

    override fun tokenLiteral(): String {
        return expression?.tokenLiteral() ?: ""
    }

    override fun toString(): String {
        return expression?.toString() ?: ""
    }
}

class Identifier(var value: String = ""): Expression {
    var token: Token? = null // 词法单元类型可能是TokenType.IDENT
     // 标识符名字

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return tokenLiteral()
    }
}

class BooleanLiteral(var value: Boolean): Expression {
    var token: Token? = null // 词法单元类型可能是TokenType.BOOL_TRUE或者TokenType.BOOL_FALSE

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return tokenLiteral()
    }
}

class IntegerLiteral(val value: BigInteger = 0.toBigInteger()): Expression {
    var token: Token? = null // 词法单元类型可能是TokenType.INTEGER

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return tokenLiteral()
    }
}

class DoubleLiteral(var value: BigDecimal = 0.toBigDecimal()): Expression {
    var token: Token? = null // 词法单元类型可能是TokenType.INTEGER

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return value.toString()
    }
}

class StringLiteral(val value: String =""): Expression {
    var token: Token? = null // 词法单元类型可能是TokenType.STRING

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return tokenLiteral()
    }
}

class InfixExpression: Expression {
    var token: Token? = null // 词法单元类型不确定
    var operator: String = "" // 运算符
    var left: Expression? = null // 左侧表达式
    var right: Expression? = null // 右侧表达式

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("(")
        stringBuilder.append(left.toString())
        stringBuilder.append(" $operator ")
        stringBuilder.append(right.toString())
        stringBuilder.append(")")

        return stringBuilder.toString()
    }
}

class PrefixExpression: Expression {
    var token: Token? = null // 词法单元类型不确定
    var operator: String = "" // 运算符
    var right: Expression? = null // 右侧表达式

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("(")
        stringBuilder.append(operator)
        stringBuilder.append(right.toString())
        stringBuilder.append(")")

        return stringBuilder.toString()
    }
}

//If 表达式
class IfExpression: Expression{
    var token: Token? = null    // IF 词法单元
    var condition: Expression? = null // 条件
    var consequence: BlockStatement? = null // If 表达式中默认要运行的块
    var alternative: BlockStatement? = null // If 表达式中默认条件不满足时运行的块
    var elseif_array: ArrayList<ElseIfExpression>? = null // 存放所有ElseIf 表达式 的列表

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("if")
        stringBuilder.append(" ")
        stringBuilder.append(condition.toString().replace("$cr", ""))
        stringBuilder.append(" ")
        stringBuilder.append("then")
        stringBuilder.append(" ")
        stringBuilder.append(consequence.toString().replace("$cr", ""))
        stringBuilder.append(" ")

        if (elseif_array != null && elseif_array!!.isNotEmpty()){
            for (expression: ElseIfExpression in elseif_array!!) {
                stringBuilder.append(expression.toString())
            }
            stringBuilder.append(" ")
        }

        if (alternative != null) {
            stringBuilder.append("else ")
            stringBuilder.append(alternative.toString().replace("$cr", ""))
        }

        return stringBuilder.toString()
    }
}


class ElseIfExpression: Expression {
    var token :Token? = null // ELSEIF 词法单元
    var condition :Expression? = null // 条件表达式
    var consequence :BlockStatement? = null // 欲执行的块

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("elseif")
        stringBuilder.append(" ")
        stringBuilder.append(condition.toString().replace("$cr", ""))
        stringBuilder.append(" ")
        stringBuilder.append("then")
        stringBuilder.append(" ")
        stringBuilder.append(consequence.toString().replace("$cr", ""))

        return stringBuilder.toString()
    }
}

class BlockEndStatement: Statement {
    var token: Token? = null

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return "End ${token?.value}"
    }
}

class FunctionExpression: Expression {
    var token: Token? = null // FUNC 词法单元
    var body: BlockStatement? = null // 函数中的代码
    var name: Identifier? = null // 函数名
    var parameters: ArrayList<Expression?>? = null // 函数形参列表

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    private fun parametersToString(): String {
        if (parameters!!.isNotEmpty()) {
            val parameterStringBuilder = StringBuilder()

            val stringList: ArrayList<String> = arrayListOf()
            for (parameter in parameters!!) {
                stringList.add(parameter.toString())
            }

            parameterStringBuilder.append(stringList.joinToString(", "))
            return parameterStringBuilder.toString()
        }

        return ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("func ${name?.toString()}(${parametersToString()})")
        stringBuilder.append(space)
        stringBuilder.append(body?.toString())
        stringBuilder.append(space)

        return stringBuilder.toString()
    }
}

class CallExpression: Expression {
    var token: Token? = null // 词法单元不确定
    var function: Expression? = null // 调用的函数
    var arguments: ArrayList<Expression?>? = null // 实参列表

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    private fun argumentsToString(): String {
        if (arguments!!.isNotEmpty()) {
            val argumentsStringBuilder = StringBuilder()

            val stringList: ArrayList<String> = arrayListOf()
            for (argument in arguments!!) {
                stringList.add(argument.toString())
            }

            argumentsStringBuilder.append(stringList.joinToString(", "))
            return argumentsStringBuilder.toString()
        }

        return ""
    }

    override fun toString(): String {
        return "${function.toString()}(${argumentsToString()})"
    }
}

class ArrayLiteral: Expression {
    var token: Token? = null // 词法单元可能是 LBRACKET
    var expressions: ArrayList<Expression?>? = null // 表达式列表

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val exprStrings: ArrayList<String> = arrayListOf()

        expressions?.let {
            for (expr in it) {
                exprStrings.add(expr?.toString() ?: "")
            }
        }

        return "[${exprStrings.joinToString(", ")}}]"
    }
}


class IndexExpression: Expression { // 索引表达式 比如 range(100)[0]
    var token: Token? = null // 词法单元可能是LBRACKET
    var left: Expression? = null
    var index: Expression? = null

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(")
        stringBuilder.append(left.toString())
        stringBuilder.append("[")
        stringBuilder.append(index.toString())
        stringBuilder.append("])")

        return stringBuilder.toString()
    }
}

class DictionaryLiteral : Expression {
    var token: Token? = null
    var pairs: HashMap<Expression?, Expression?>?  = null

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        val pairsList = mutableListOf<String>()

        pairs?.let {
            for ((key, value) in it) {
                pairsList.add("$key:$value")
            }
        }

        stringBuilder.append("{")
        stringBuilder.append(pairsList.joinToString(", "))
        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}

class AndExpression : Expression {
    var token: Token? = null  // 显式初始化为 null
    var left: Expression? = null
    var right: Expression? = null

    override fun tokenLiteral(): String {
        // 处理 token 为空的情况，返回空字符串
        return token?.value ?: ""
    }

    override fun toString(): String {
        // 处理 left/right 为空的情况，避免 NPE(NullPointerError)
        return "${left?.toString() ?: "?"} and ${right?.toString() ?: "?"}"
    }
}

class OrExpression : Expression {
    var token: Token? = null  // 显式初始化为 null
    var left: Expression? = null
    var right: Expression? = null

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return "${left?.toString() ?: "?"} or ${right?.toString() ?: "?"}"
    }
}

class NotExpression : Expression {
    var token: Token? = null // BOOL_NOT 词法单元
    var right: Expression? = null     // 右侧表达式

    override fun tokenLiteral(): String {
        return token?.value ?: "" // 处理空 token
    }

    override fun toString(): String {
        return "not ${right?.toString() ?: "<?>"}" // 处理空 right
    }
}

class ForStatement : Statement {
    var token: Token? = null          // FOR 词法单元
    var itemVar: Identifier? = null            // 迭代变量
    var items: Expression? = null              // 迭代集合表达式
    var loopBlock: BlockStatement? = null      // 循环体块

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return buildString {
            append("for ")
            append(StringUtils.Trim(itemVar?.toString()  ?: "<?>"))
            append(" in ")
            append(items?.toString() ?: "<?>")
            append(loopBlock?.toString()?.replace("\r", "") ?: "<?>")
            append("next")
        }
    }
}

class WhileStatement : Statement {
    var token: Token? = null          // WHILE 词法单元
    var condition: Expression? = null            // 条件表达式
    var loopBlock: BlockStatement? = null      // 循环体块

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return buildString {
            append("while ${condition ?: "<?>"}")
            append(loopBlock?.toString()?.replace("\r", "") ?: "<?>")
        }
    }
}

class ObjectMemberExpression : Expression {
    var token: Token? = null  // 显式初始化为 null
    var left: Expression? = null // 左侧对象
    var right: Expression? = null // 欲访问的成员

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return "${left?.toString() ?: "<?>"}.${right?.toString() ?: "<?>"}"
    }
}

class TypeExpression : Expression {
    var token: Token? = null  // 显式初始化为 null
    var identifier: Identifier? = null // 左侧对象
    var type: Expression? = null // 欲访问的成员

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        return "$identifier: $type"
    }
}

class DeclareStatement : Statement {
    var token: Token? = null          // DECLARE 词法单元
    var funcName: Identifier? = null // 函数名
    var aliasFunctionName: Identifier? = null // 函数别名
    var dllPath: StringLiteral? = null // dll路径
    var parameters: ArrayList<Expression?>? = null // 函数形参列表

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    private fun parametersToString(): String {
        if (parameters!!.isNotEmpty()) {
            val parameterStringBuilder = StringBuilder()

            val stringList: ArrayList<String> = arrayListOf()
            for (parameter in parameters!!) {
                stringList.add(parameter.toString())
            }

            parameterStringBuilder.append(stringList.joinToString(", "))
            return parameterStringBuilder.toString()
        }

        return ""
    }

    override fun toString(): String {
        return "" +
        """
        Declare Func $funcName Lib "${dllPath?.value}" Alias $aliasFunctionName (
            ${parametersToString()}
        )
        """.trimIndent()
    }
}

class ClassStatement: Statement {
    var token: Token? = null
    var body: BlockStatement? = null
    var name: Expression? = null
    var baseClass: Expression? = null

    override fun tokenLiteral(): String = token?.value ?: ""

    override fun toString(): String = buildString {
        append("class ")
        name?.let { append(it.toString().replace("\r", "")) }
        append(" ")
        baseClass?.let {
            append(": ")
            append(it.toString().replace("\r", ""))
        }
        body?.let {
            append(it.toString().replace("\r", ""))
            append(" ")
        }
    }
}

class ObjectCreateExpression : Expression {
    var token: Token? = null
    var objType: Expression? = null
    var arguments: ArrayList<Expression?> = arrayListOf()

    override fun tokenLiteral(): String {
        return token?.value ?: ""
    }

    override fun toString(): String {
        val typeName = objType?.toString().orEmpty()
        val args = arguments.joinToString(", ") { it.toString() }
        return "New $typeName($args)"
    }
}
