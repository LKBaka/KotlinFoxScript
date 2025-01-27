package me.user.Evaluator.LogicalExpressionEvaluator

import me.user.Environment.Environment
import me.user.Evaluator.FunctionEvaluator.FunctionCaller.callKotlinFunction
import me.user.Evaluator.builtinEnvironment
import me.user.Evaluator.eval
import me.user.Utils.BooleanUtils.*
import me.user.Object.FoxObject
import me.user.Parser.*
import me.user.Utils.ErrorUtils.isError

fun evalLogicalExpression(node: Node, env: Environment): FoxObject? {
    return when (node) {
        is AndExpression -> {
            val andExp = node // 直接类型检查

            // 计算左侧表达式
            val leftObject = eval(andExp.left, env)
            if (isError(leftObject)) return leftObject

            // 转换为布尔值
            val leftBoolObject = callKotlinFunction("CBool", listOf(leftObject), builtinEnvironment)
            if (isError(leftBoolObject)) return leftBoolObject

            // 短路逻辑：左侧为假直接返回假
            if (!isTruthy(leftBoolObject)) {
                return FoxFalse
            }

            // 计算右侧表达式
            val rightObject = eval(andExp.right, env)
            if (isError(rightObject)) return rightObject

            // 转换为布尔值
            val rightBoolObject = callKotlinFunction("CBool", listOf(rightObject), builtinEnvironment)
            if (isError(rightBoolObject)) return rightBoolObject

            // 最终结果判断
            if (isTruthy(rightBoolObject)) FoxTrue else FoxFalse
        }

        is OrExpression -> {
            val orExp = node // 直接类型检查

            // 计算左侧表达式
            val leftObject = eval(orExp.left, env)
            if (isError(leftObject)) return leftObject

            // 转换为布尔值
            val leftBoolObject = callKotlinFunction("CBool", listOf(leftObject), builtinEnvironment)
            if (isError(leftBoolObject)) return leftBoolObject

            // 短路逻辑：左侧为真直接返回真
            if (isTruthy(leftBoolObject)) {
                return FoxTrue
            }

            // 计算右侧表达式
            val rightObject = eval(orExp.right, env)
            if (isError(rightObject)) return rightObject

            // 转换为布尔值
            val rightBoolObject = callKotlinFunction("CBool", listOf(rightObject), builtinEnvironment)
            if (isError(rightBoolObject)) return rightBoolObject

            // 最终结果判断
            if (isTruthy(rightBoolObject)) FoxTrue else FoxFalse
        }
        is NotExpression -> {
            val notExp = node // 智能类型转换

            // 计算右侧表达式
            val rightObject = eval(notExp.right, env)
            if (isError(rightObject)) {
                return rightObject // 返回错误对象
            }

            // 取反逻辑：将右侧结果取反后转换为布尔对象
            nativeBooleanToBooleanObject(!isTruthy(rightObject))
        }

        else -> null
    }
}