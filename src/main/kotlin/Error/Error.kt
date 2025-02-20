package me.user.Error

class Fail(override val message: String = ""): Exception()
class CompilerError(override val message: String = ""): Error()
class RuntimeError(override val message: String = ""): Error()
