package me.user.Utils

import me.user.Parser.*

object StatementUtils {
    fun findAllStatement(originStatements: List<Statement>, type: Class<*>): List<Statement> {
        val Statements = mutableListOf<Statement>()
        for (stmt in originStatements) {
            if (stmt.javaClass == type) {
                Statements.add(stmt)
            }
        }
        return Statements
    }

    fun findAllExpression(originExpressions: List<ExpressionStatement>, type: Class<*>): List<Expression> {
        val expressions = mutableListOf<Expression>()
        for (exp in originExpressions) {
            if (exp.expression == null) continue

            if (exp.expression!!.javaClass == type) {
                expressions.add(exp.expression!!)
            }
        }
        return expressions
    }

    fun findFunctionLiteral(name: String, statements: List<Statement>): FunctionExpression? {
        val stmts = findAllStatement(statements, ExpressionStatement::class.java)
        val expStmts = mutableListOf<ExpressionStatement>()

        for (stmt in stmts) {
            expStmts.add(stmt as ExpressionStatement)
        }

        val funcLiterals = findAllExpression(expStmts, FunctionExpression::class.java)
        for (expr in funcLiterals) {
            val funcLiteral = expr as? FunctionExpression
            if (funcLiteral?.name?.value?.replace(" ", "") == name) {
                return funcLiteral
            }
        }
        return null
    }
}
