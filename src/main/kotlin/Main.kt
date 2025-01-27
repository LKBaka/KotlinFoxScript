package me.user

import kotlinx.cli.*

import me.user.Runner.RunMode
import me.user.Runner.Runner

fun main(args: Array<String>) {
    val parser = ArgParser("KotlinFoxScript")
    val runMode by parser.option(ArgType.String, shortName = "m", description = "The run mode (File or REPL)").required()
    val filePath by parser.option(ArgType.String, shortName = "f", description = "The file path if run mode is File")

    try {
        parser.parse(args)

        if (runMode == "File") {
            if (filePath == null) {
                throw IllegalArgumentException("File path is required when run mode is File.")
            }

            val runner = Runner(RunMode.FILE, filePath!!)
            runner.run()
        } else {
            // REPL模式

            val runner = Runner(RunMode.REPL)
            runner.run()
        }
    } catch (e: IllegalArgumentException ) {
        println(e.message)
    }

}