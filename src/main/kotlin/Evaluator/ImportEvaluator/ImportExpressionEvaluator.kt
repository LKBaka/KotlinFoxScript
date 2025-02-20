package me.user.Evaluator.ImportEvaluator

import me.user.Environment.ClassEnvironment
import me.user.Environment.Data
import me.user.Environment.Environment
import me.user.Object.FoxArray
import me.user.Object.FoxClass
import me.user.Object.FoxObject
import me.user.Parser.Identifier
import me.user.Parser.ImportExpression
import me.user.Parser.Node
import me.user.Parser.ObjectMemberExpression
import me.user.Runner.FileRunner
import me.user.Runner.ModuleRunner
import me.user.Utils.findFile

fun evalImportExpression(node: Node, env: Environment): FoxObject? {
    val importExpression = node as ImportExpression

    val moduleName = when (importExpression.module) {
        is Identifier -> (importExpression.module as Identifier).value
        is ObjectMemberExpression -> ((importExpression.module as ObjectMemberExpression).left as Identifier).value
        else -> TODO("不支持的表达式: ${importExpression.module!!::class}")
    }

    val modulePath = ((env.getValue("ModulePath").foxObject as FoxArray).getValue() as ArrayList<FoxObject?>).map {
        it?.getValue() as String
    }

    val foxFilePath = findFile(modulePath, moduleName, "Fox")
    foxFilePath?.let { importFoxModule(moduleName ,foxFilePath, env) }
    
    return null
}

private fun importFoxModule(moduleName: String, filePath: String, env: Environment) {
    val moduleRunner = ModuleRunner(filePath)
    val environment = ClassEnvironment()

    val block = moduleRunner.run(environment)

    env.setValue(moduleName, Data(FoxClass().apply { this.body = block }, false))
}