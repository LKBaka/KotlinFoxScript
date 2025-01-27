package me.user.Parser

import me.user.Constant.nullChar
import me.user.Error.Fail
import me.user.Lexer.Lexer
import me.user.Lexer.StringMap
import me.user.Lexer.Token
import me.user.Lexer.TokenType
import me.user.OperatorExtension.*
import java.awt.Color

private typealias prefixParseFunctionType = () -> Expression?
private typealias infixParseFunctionType = (left: Expression) -> Expression?
private typealias parseStatementFunctionType = () -> Statement?

enum class Precedence{
    LOWEST,
    ASSIGNMENT,
    AND_OR,      // Or | And
    EQUALS,      // ==
    LESSGREATER, // > | <
    SUM,         // +
    PRODUCT,    // *
    PREFIX,      // -X | !X
    CALL,      // myFunction(X)
    INDEX, // array[index]
    TYPE,
    IDENT,
    OBJ_MEMBER // person.Name
}

class Parser(lexer: Lexer) {
    private var curToken: Token
    private var peekToken: Token
    private var tokenPos = 0.toBigInteger()
    private val tokens: List<Token> = lexer.getTokens()

    private val prefixParseFunctions: MutableMap<TokenType, prefixParseFunctionType> = mutableMapOf()
    private val infixParseFunctions: MutableMap<TokenType, infixParseFunctionType> = mutableMapOf()

    private val statementParseFunction: Map<TokenType, parseStatementFunctionType> = mapOf(
        TokenType.DIM to ::parseVarDefineStatement,
        TokenType.END to ::parseBlockEndStatement,
        TokenType.DECLARE to ::parseDeclareStatement,
        TokenType.WHILE to ::parseWhileStatement,
        TokenType.CLASS to ::parseClassStatement,
        TokenType.LET to ::parseVarDefineStatement,
        TokenType.FOR to ::parseForStatement,
        TokenType.RETURN to ::parseReturnStatement
    )

    private val tokenPrecedences: Map<TokenType, Precedence> = mapOf(
        TokenType.EQ to Precedence.EQUALS,
        TokenType.COLON to Precedence.TYPE,
        TokenType.NOT_EQ to Precedence.EQUALS,
        TokenType.LT to Precedence.LESSGREATER,
        TokenType.GT to Precedence.LESSGREATER,
        TokenType.PLUS to Precedence.SUM,
        TokenType.MINUS to Precedence.SUM,
        TokenType.SLASH to Precedence.PRODUCT,
        TokenType.ASTERISK to Precedence.PRODUCT,
        TokenType.LPAREN to Precedence.CALL,
        TokenType.LBRACKET to Precedence.INDEX,
        TokenType.ASSIGN to Precedence.ASSIGNMENT,
        TokenType.DOT to Precedence.OBJ_MEMBER,
        TokenType.BOOL_OR to Precedence.AND_OR,
        TokenType.BOOL_AND to Precedence.AND_OR
    )

    private val countMap: HashMap<Class<*>, Int> = hashMapOf(
        IfExpression::class.java to 0,
        ElseIfExpression::class.java to 0,
//        ForStatement::class.java to 0,
//        WhileStatement::class.java to 0,
//        ClassStatement::class.java to 0,
//        TryCatchStatement::class.java to 0,
        FunctionExpression::class.java to 0
    )

    private val errors = ArrayList<String>()

    init {
        // 初始化当前词法单元和下一个词法单元
        this.curToken = Token(TokenType.NONSENSE, "$nullChar", (-1).toBigInteger())
        this.peekToken = Token(TokenType.NONSENSE, "$nullChar", (-1).toBigInteger())

        // 初始化前缀表达式解析函数表
        prefixParseFunctions[TokenType.IDENT] = ::parseIdentifier
        prefixParseFunctions[TokenType.NEW] = ::parseObjectCreateExpression
        prefixParseFunctions[TokenType.INTEGER] = ::parseNumberLiteral
        prefixParseFunctions[TokenType.BOOL_TRUE] = ::parseBoolean
        prefixParseFunctions[TokenType.BOOL_FALSE] = ::parseBoolean
        prefixParseFunctions[TokenType.MINUS] = ::parsePrefixExpression
        prefixParseFunctions[TokenType.BANG] = ::parsePrefixExpression
        prefixParseFunctions[TokenType.LPAREN] = ::parseGroupedExpression
        prefixParseFunctions[TokenType.IF] = ::parseIfExpression
        prefixParseFunctions[TokenType.FUNC] = ::parseFunctionExpression
        prefixParseFunctions[TokenType.STRING] = ::parseStringLiteral
        prefixParseFunctions[TokenType.LBRACKET] = ::parseArrayLiteral
        prefixParseFunctions[TokenType.BOOL_NOT] = ::parseNotExpression
        prefixParseFunctions[TokenType.LBRACE] = ::parseDictionaryLiteral

        // 初始化中缀表达式解析函数表
        infixParseFunctions[TokenType.PLUS] = ::parseInfixExpression
        infixParseFunctions[TokenType.COLON] = ::parseTypeExpression
        infixParseFunctions[TokenType.BOOL_AND] = ::parseLogicalExpression
        infixParseFunctions[TokenType.BOOL_OR] = ::parseLogicalExpression
        infixParseFunctions[TokenType.MINUS] = ::parseInfixExpression
        infixParseFunctions[TokenType.SLASH] = ::parseInfixExpression
        infixParseFunctions[TokenType.ASTERISK] = ::parseInfixExpression
        infixParseFunctions[TokenType.EQ] = ::parseInfixExpression
        infixParseFunctions[TokenType.NOT_EQ] = ::parseInfixExpression
        infixParseFunctions[TokenType.LT] = ::parseInfixExpression
        infixParseFunctions[TokenType.GT] = ::parseInfixExpression
        infixParseFunctions[TokenType.LPAREN] = ::parseCallExpression
        infixParseFunctions[TokenType.LBRACKET] = ::parseIndexExpression
        infixParseFunctions[TokenType.DOT] = ::parseObjectMemberExpression
    }

    fun parseObjectCreateExpression(): Expression? {
        val objectCreateExpr = ObjectCreateExpression().apply {
            token = curToken // 假设 currentToken 是解析器的成员变量
        }

        // 检查是否为 New() 结构
        if (peekTokenIs(TokenType.LPAREN)) {
            return Identifier(curToken.value)
        }

        nextToken()
        objectCreateExpr.objType = parseExpression(Precedence.IDENT)

        nextToken()
        val args = parseExpressionList(TokenType.RPAREN) ?: return null
        objectCreateExpr.arguments = args

        return objectCreateExpr
    }

    fun parseClassStatement(): Statement? {
        val stmt = ClassStatement().apply {
            token = curToken
        }

        // 移动到下一个 token
        nextToken()

        // 解析类名
        stmt.name = parseExpression(Precedence.LOWEST)
        if (stmt.name == null) {
            expectCur(TokenType.IDENT)
            return null
        }

        // 处理基类继承
        if (peekTokenIs(TokenType.COLON)) {
            // 跳过冒号
            nextToken()

            // 移动到基类名称 token
            nextToken()

            val baseClassName = parseExpression(Precedence.LOWEST)
            if (baseClassName == null) {
                errors.add("应为类型")
                return null
            }
            stmt.baseClass = baseClassName
        }

        // 检查 EOL
        if (!expectPeek(TokenType.EOL)) {
            return null
        }

        // 移动到代码块开始
        nextToken()

        // 解析类主体
        stmt.body = parseBlockStatement(arrayOf(TokenType.CLASS), arrayOf(true))

        return stmt
    }

    fun parseDeclareStatement(): Statement? {
        val declareStatement = DeclareStatement().apply {
            token = curToken
        }

        nextToken()
        if (!expectCur(TokenType.FUNC)) return null

        nextToken()
        if (!expectCur(TokenType.IDENT)) return null
        declareStatement.funcName = parseExpression(Precedence.LOWEST) as Identifier

        nextToken()
        if (!expectCur(TokenType.LIB)) return null

        nextToken()
        declareStatement.dllPath = parseExpression(Precedence.LOWEST) as? StringLiteral
        nextToken()

        if (curTokenIs(TokenType.ALIAS)) {
            nextToken()
            declareStatement.aliasFunctionName = parseExpression(Precedence.IDENT) as? Identifier
            nextToken()
        }

        declareStatement.parameters = parseExpressionList(TokenType.RPAREN)
        return declareStatement
    }

    fun parseTypeExpression(left: Expression): Expression {
        val typeExpression = TypeExpression().apply {
            token = curToken
            this.identifier = left as Identifier
        }

        nextToken()
        typeExpression.type = parseExpression(Precedence.LOWEST)

        return typeExpression
    }

    fun parseObjectMemberExpression(left: Expression): Expression {
        val objectMemberExpression = ObjectMemberExpression().apply {
            token = curToken
            this.left = left
        }

        nextToken()
        objectMemberExpression.right = parseExpression(Precedence.OBJ_MEMBER)

        return objectMemberExpression
    }

    fun parseWhileStatement(): Statement? {
        val stmt = WhileStatement().apply { token = curToken }

        nextToken()

        stmt.condition = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.EOL)) return null
        nextToken()
        nextToken()

        stmt.loopBlock = parseBlockStatement(TokenType.WHILE, true)

        return stmt
    }

    fun parseForStatement(): Statement? {
        val stmt = ForStatement().apply { token = curToken }

        nextToken()

        if (!expectCur(TokenType.IDENT)) return null
        stmt.itemVar = parseExpression(Precedence.LOWEST) as? Identifier

        if (!expectPeek(TokenType.IN)) return null
        nextToken()
        nextToken()

        stmt.items = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.EOL)) return null

        stmt.loopBlock = parseBlockStatement(TokenType.NEXT, false)

        if (!expectCur(TokenType.NEXT)) return null

        return stmt
    }

    fun parseNotExpression(): Expression? {
        val notExpr = NotExpression().apply {
            token = curToken
        }

        nextToken() // 前进到下一个 Token
        notExpr.right = parseExpression(Precedence.LOWEST)
        if (notExpr.right == null) {
            errors.add("应为表达式 在第${notExpr.token?.line}行")
            return null
        }

        return notExpr
    }

    fun parseLogicalExpression(leftExp: Expression): Expression? {
        if (curTokenIs(TokenType.BOOL_AND)) {
            val andExp = AndExpression().apply {
                token = curToken
                left = leftExp
            }
            nextToken() // 前进到下一个token
            val rightExp = parseExpression(Precedence.LOWEST)
            andExp.right = rightExp
            return andExp
        }

        if (curTokenIs(TokenType.BOOL_OR)) {
            val orExp = OrExpression().apply {
                token = curToken // 同样检查token是否正确
                left = leftExp
            }
            nextToken()
            val rightExp = parseExpression(Precedence.LOWEST)
            orExp.right = rightExp
            return orExp
        }
        return null
    }

    private fun parseIndexExpression(left: Expression): Expression? {
        val indexExpression = IndexExpression()
        with (indexExpression) {
            this.token = curToken
            this.left = left
        }

        nextToken()
        indexExpression.index = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.RBRACKET)) {
            return null
        }

        return indexExpression
    }

    private fun parseArrayLiteral(): Expression {
        val arrayLiteral = ArrayLiteral()
        with (arrayLiteral) {
            token = curToken
            expressions = parseExpressionList(TokenType.RBRACKET)
        }

        return arrayLiteral
    }

    private fun parseCallExpression(left: Expression): Expression {
        val callExpression = CallExpression()
        with (callExpression) {
            token = curToken
            function = left
            arguments = parseExpressionList(TokenType.RPAREN)
        }

        return callExpression
    }

    private fun parseExpressionList(endTokenType: TokenType): ArrayList<Expression?>? {
        val expressions: ArrayList<Expression?> = arrayListOf()

        if (curTokenIs(endTokenType)) {
            return expressions
        }

        if (peekTokenIs(endTokenType)) {
            nextToken()
            return expressions
        }

        // 前进Token 正常情况下执行前应该是左括号
        nextToken()
        expressions.add(parseExpression(Precedence.LOWEST))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            expressions.add(parseExpression(Precedence.LOWEST))
        }


        if (!curTokenIs(endTokenType)) {
            if (peekTokenIs(endTokenType)) {
                nextToken()
                return expressions
            }

            expectCur(endTokenType)
            return null
        }

        nextToken()
        return expressions
    }

    // 解析函数形参
    private fun parseFunctionParameters(): ArrayList<Expression?>? {
        // 初始化表达式列表
        val expressions: ArrayList<Expression?> = arrayListOf()

        // 移动Token
        nextToken()

        // 如果下一个词法单元是右括号
        if (peekTokenIs(TokenType.RPAREN)) {
            // 移动Token
            nextToken()

            // 返回列表
            return expressions
        }

        // 移动Token
        nextToken()

        var expression: Expression?

        // 重复执行直到下一个Token不是逗号
        while (peekTokenIs(TokenType.COMMA)){
            // 解析表达式并添加至列表
            expression = parseExpression(Precedence.LOWEST)
            expressions.add(expression)

            // 移动Token
            nextToken()
            nextToken()
        }

        expressions.add(parseExpression(Precedence.LOWEST))
        nextToken()

        // 如果是右括号
        if (expectCur(TokenType.RPAREN)) {
            return expressions
        }

        return null // 返回空
    }

    private fun parseFunctionExpression(): Expression? {
        val function = FunctionExpression()
        function.token = curToken

        if (peekTokenIs(TokenType.IDENT) || peekTokenIs(TokenType.NEW)) {
            nextToken()
            function.name = parseExpression(Precedence.IDENT) as Identifier
        }

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }

        function.parameters = parseFunctionParameters()
        function.body = parseBlockStatement(
            arrayOf(TokenType.FUNC, TokenType.RBRACE),
            arrayOf(true, false)
        )

        return function
    }


    private fun parseBlockEndStatement(): Statement {
        val blockEndStatement = BlockEndStatement()
        nextToken()

        blockEndStatement.token = curToken

        return blockEndStatement
    }

    fun parseDictionaryLiteral(): DictionaryLiteral? {
        val dict = DictionaryLiteral()
        with (dict) {
            token = curToken
            pairs = hashMapOf()
        }

        while (!peekTokenIs(TokenType.RBRACE)) {
            nextToken()
            val key = parseExpression(Precedence.LOWEST) ?: return null
            if (!expectPeek(TokenType.COLON)) {
                return null
            }

            nextToken()
            nextToken()
            val value = parseExpression(Precedence.LOWEST) ?: return null

            dict.pairs?.set(key, value)
            if (!peekTokenIs(TokenType.RBRACE) && !expectPeek(TokenType.COMMA)) {
                return null
            }
        }

        nextToken()
        return dict
    }

    private fun parseBlockStatement(endTokenType: TokenType, withEnd: Boolean = true): BlockStatement? {
        val blockStatement = BlockStatement()
        var stmt: Statement? = parseStatement()

        fun parseWithEnd() {
            while (!(stmt is BlockEndStatement && (stmt as BlockEndStatement).token?.tokenType == endTokenType)) {
                stmt = parseStatement()
                stmt?.let { blockStatement.statements.add(it) }
                nextToken()
            }
        }

        fun parse() {
            while (!curTokenIs(endTokenType)) {
                stmt = parseStatement()
                stmt?.let { blockStatement.statements.add(it) }
                nextToken()
            }
        }

        if (withEnd) {
            parseWithEnd()
        } else {
            parse()
        }

        return blockStatement
    }

    private fun parseBlockStatement(endTokenTypes: Array<TokenType>, tokenTypeWithEnds: Array<Boolean>): BlockStatement {
        // 创建一个 BlockStatement 实例，用于存储解析得到的块语句
        val blockStatement = BlockStatement()
        // 调用 parseStatement 函数解析一条语句，并将结果存储在 stmt 中，可能为 null
        var stmt: Statement? = parseStatement()

        // 解析带有特定结束标记的情况
        fun parseWithEnd(endTokenType: TokenType): Boolean {
            while (true) {
                // 如果当前解析的语句是 BlockEndStatement 且其 token 的 tokenType 等于指定的结束标记类型，则跳出循环
                if (stmt is BlockEndStatement && (stmt as BlockEndStatement).token?.tokenType == endTokenType) break

                // 如果已经到达 token 列表的末尾，返回 false
                if (tokenPos >= tokens.size - 1) return false

                // 继续解析下一条语句
                stmt = parseStatement()
                stmt?.let { blockStatement.statements.add(it) }

                // 移动到下一个 token
                nextToken()
            }
            return true
        }

        // 解析没有特定结束标记的情况
        fun parse(endTokenTypes: Array<TokenType>): Boolean {
            while (true) {
                var foundEndToken = false
                // 检查当前 token 是否为指定的结束标记类型之一，如果是则跳出循环
                for (endTokenType in endTokenTypes) {
                    if (curTokenIs(endTokenType)) {
                        foundEndToken = true
                        break
                    }
                }
                if (foundEndToken) break

                // 如果已经到达 token 列表的末尾，返回 false
                if (tokenPos >= tokens.size - 1) return false

                // 继续解析下一条语句
                stmt = parseStatement()
                stmt?.let { blockStatement.statements.add(it) }
                // 移动到下一个 token
                nextToken()
            }
            return true
        }

        var found = false
        for (i in 0 until endTokenTypes.size) {
            if (!tokenTypeWithEnds[i]) {
                if (parse(endTokenTypes)) {
                    found = true
                    break
                }
            } else {
                if (parseWithEnd(endTokenTypes[i])) {
                    found = true
                    break
                }
            }
        }

        return blockStatement
    }

    // 解析if表达式
    private fun parseIfExpression(): Expression? {
        // 初始化
        val expression = IfExpression()
        expression.token = curToken

        try {
            // 移动Token
            nextToken()

            // 解析条件
            expression.condition = parseExpression(Precedence.LOWEST)

            if (!expectPeek(TokenType.THEN)) {
                throw Fail()
            }

            nextToken()
            nextToken()

            // 解析if条件满足时的块
            expression.consequence = parseBlockStatement(arrayOf(TokenType.ELSE, TokenType.ELSEIF, TokenType.IF), arrayOf(false, false, true)) as BlockStatement

            expression.elseif_array = arrayListOf()
            // 如果当前词法单元是ElseIf
            while (curTokenIs(TokenType.ELSEIF)) {
                //解析代码块语句并设置为ElseIf条件的块
                val elseifExp = parseElseIfExpression()
                expression.elseif_array!!.add(elseifExp as ElseIfExpression)
            }

            // 如果当前词法单元是Else
            if (curTokenIs(TokenType.ELSE)) {
                //解析代码块语句并设置为Else条件的块
                expression.alternative = parseBlockStatement(TokenType.IF) as BlockStatement
            }

            countMap[expression::class.java] = countMap[expression::class.java]!! + 1
            return expression
        } catch (ex: Fail) {
            countMap[expression::class.java] = countMap[expression::class.java]!! + 1
            return null
        }
    }

    // 解析elseif表达式
    private fun parseElseIfExpression(): Expression? {
        // 初始化
        val expression = ElseIfExpression()
        with (expression) {this.token = curToken }

        // 移动词法单元
        nextToken()

        // 解析条件
        expression.condition = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.THEN)) {
            return null
        }

        nextToken()
        nextToken()

        // 解析条件满足时执行的块）
        expression.consequence = parseBlockStatement(
            arrayOf(TokenType.ELSEIF, TokenType.ELSE, TokenType.IF),
            arrayOf(false, false, true)
        )

        return expression
    }

    private fun parseGroupedExpression(): Expression? {
        nextToken()

        val expression: Expression? = parseExpression(Precedence.LOWEST)

        if (!expectCur(TokenType.RPAREN)) {
            return null
        }

        nextToken()
        return expression
    }

    private fun parseNumberLiteral(): Expression? {
        try {
            val integerLiteral = IntegerLiteral(curToken.value.toBigInteger())
            integerLiteral.token = curToken

            if (peekTokenIs(TokenType.DOT)) {
                val doubleLiteral = DoubleLiteral().apply {
                    this.token = curToken
                }

                nextToken()
                nextToken()

                if (!curTokenIs(TokenType.INTEGER)) {
                    backToken()
                    backToken()

                    return integerLiteral
                }

                doubleLiteral.value = "${doubleLiteral.token!!.value}.${curToken.value}".toBigDecimal()
                return doubleLiteral
            }

            return integerLiteral
        } catch (e: NumberFormatException) {
            errors.add("不是一个数字 ${curToken.value}")
        }

        return null
    }

    private fun parseStringLiteral(): Expression {
        val stringLiteral = StringLiteral(curToken.value)
        with (stringLiteral) {
            token = curToken
        }

        return stringLiteral
    }

    private fun parseIdentifier(): Expression {
        val identifier = Identifier(curToken.value)
        identifier.token = curToken

        return identifier
    }

    private fun parseBoolean(): Expression {
        val boolean = BooleanLiteral(curTokenIs(TokenType.BOOL_TRUE))
        boolean.token = curToken

        return boolean
    }

    private fun parseVarDefineStatement(): Statement? {
        val varDefineStatement = VarDefineStatement()
        varDefineStatement.token = curToken

        if (!expectPeek(TokenType.IDENT)) {
            return null
        }

        nextToken()
        varDefineStatement.identifier = parseExpression(Precedence.IDENT)

        if (peekTokenIs(TokenType.COLON)) {
            nextToken()
            nextToken()

            varDefineStatement.type = parseExpression(Precedence.LOWEST)
        }

        if (!expectPeek(TokenType.ASSIGN)) {
            return null
        }

        nextToken()
        nextToken()
        varDefineStatement.value = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.EOL)) {
            return null
        }

        return varDefineStatement
    }
    private fun parseReturnStatement(): Statement {
        val returnStatement = ReturnStatement()
        returnStatement.token = curToken

        nextToken()
        returnStatement.returnValue = parseExpression(Precedence.LOWEST)

        return returnStatement
    }

    private fun parsePrefixExpression(): Expression {
        val prefixExpression = PrefixExpression()
        with(prefixExpression) {
            this.token = curToken
            this.operator = curToken.value
        }

        val precedence = curPrecedence()

        nextToken()
        prefixExpression.right = parseExpression(precedence)
        return prefixExpression
    }

    private fun parseInfixExpression(left: Expression): Expression {
        val infixExpression = InfixExpression()
        with(infixExpression) {
            this.token = curToken
            this.left = left
            this.operator = curToken.value
        }

        val precedence: Precedence = curPrecedence()
        nextToken()

        infixExpression.right = parseExpression(
            Precedence.entries.getOrNull(
                if (infixExpression.operator == "+") {
                    precedence.ordinal.minus(1)
                } else {
                    precedence.ordinal
                }
            )!!
        )

        return infixExpression
    }

    private fun parseExpression(precedence: Precedence): Expression? {
        // 先解析左侧表达式
        var left: Expression? = null

        // 检查一遍前缀表达式解析函数表，如果存在，就返回解析函数解析完后的表达式
        if (prefixParseFunctions.containsKey(curToken.tokenType)) {
            left = prefixParseFunctions[curToken.tokenType]?.invoke()
        } else {
            return null
        }

        // 如果下一个词法单元的优先级大于给定的优先级则尝试组合中缀表达式
        while (!peekTokenIs(TokenType.EOL) && peekPrecedence() > precedence){
            val infixParseFunction = infixParseFunctions[peekToken.tokenType] ?: return left

            nextToken()

            val infix = left?.let { infixParseFunction.invoke(it) }
            left = infix
        }

        // 返回表达式
        return left
    }

    private fun peekPrecedence(): Precedence {
        if (tokenPrecedences.containsKey(peekToken.tokenType)) {
            return tokenPrecedences[peekToken.tokenType]!!
        }

        return Precedence.LOWEST
    }

    private fun curPrecedence(): Precedence {
        if (tokenPrecedences.containsKey(curToken.tokenType)) {
            return tokenPrecedences[curToken.tokenType]!!
        }

        return Precedence.LOWEST
    }

    private fun parseExpressionStatement(): Statement {
        val expressionStatement = ExpressionStatement()
        expressionStatement.expression = parseExpression(Precedence.LOWEST)

        return expressionStatement
    }

    private fun parseStatement(): Statement? {
        if (statementParseFunction.containsKey(curToken.tokenType)) {
            return statementParseFunction[curToken.tokenType]!!.invoke()
        }

        return parseExpressionStatement()
    }

    fun parseProgram(): Program {
        val program = Program()

        while (!curTokenIs(TokenType.EOF)) {
            val stmt = parseStatement()
            if (stmt != null) {
                program.statements.add(stmt)
            }

            nextToken()
        }

        return program
    }

    fun checkParseError(): Boolean {
        if (errors.isEmpty()) {return false}

        println("parser has ${errors.count()} ${if (errors.count() == 1) "error" else "errors"}")
        for (error in errors) {
            println("parser error: $error")
        }

        return true
    }

    private fun backToken() {
        // 索引 - 1
        tokenPos--

        curToken = tokens[(tokenPos - 1).toInt()]
        peekToken = tokens[tokenPos.toInt()]
    }


    private fun nextToken() {
        // 如果索引没有越界
        if (tokenPos >= (tokens.count() - 1)) {
            // 设置TokenType 为 EOF
            curToken = Token(TokenType.EOF, "$nullChar", (-1).toBigInteger())
            peekToken = Token(TokenType.EOF, "$nullChar", (-1).toBigInteger())
        } else { //否则
            // 转到下一个词法单元
            curToken = tokens[tokenPos.toInt()]
            peekToken = tokens[(tokenPos + 1).toInt()]

            // 索引 + 1
            tokenPos ++
        }
    }


    private fun curTokenIs(tokenType: TokenType): Boolean {
        return tokenType == curToken.tokenType
    }

    private fun peekTokenIs(tokenType: TokenType): Boolean {
        return tokenType == peekToken.tokenType
    }

    private fun expectPeek(tokenType: TokenType): Boolean {
        if (peekTokenIs(tokenType)) {
            return true
        }

        if (StringMap.containsKey(tokenType)) {
            errors.add("缺少 ${StringMap[tokenType]}")
            return false
        }

        errors.add("缺少 $tokenType")
        return false
    }

    private fun expectCur(tokenType: TokenType): Boolean {
        if (curTokenIs(tokenType)) {
            return true
        }

        if (StringMap.containsKey(tokenType)) {
            errors.add("缺少 ${StringMap[tokenType]}")
            return false
        }

        errors.add("缺少 $tokenType")
        return false
    }
}
