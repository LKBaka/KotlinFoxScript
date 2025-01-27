package me.user.Environment

import me.user.Object.*
import me.user.Utils.BooleanUtils.nativeBooleanToBooleanObject
import java.math.BigInteger
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.*

class BuiltinEnvironment: Environment() {
    init {
        addFunction("print", FoxKotlinFunction(::builtinPrint,1, arrayListOf(Type(ObjectType.OBJ))))
        addFunction("len", FoxKotlinFunction(::builtinLen,1, arrayListOf(Type(ObjectType.STRING_OBJ))))
        addFunction("input", FoxKotlinFunction(::builtinInput,1, arrayListOf(Type(ObjectType.STRING_OBJ))))
        addFunction("input", FoxKotlinFunction(::builtinInput,0, arrayListOf()))
        addFunction("range", FoxKotlinFunction(::builtinRange,1, arrayListOf(Type(ObjectType.INTEGER_OBJ))))
        addFunction("CBool",
            FoxKotlinFunction(::builtinCBool,1, arrayListOf(Type(ObjectType.INTEGER_OBJ, ObjectType.BOOLEAN_OBJ, ObjectType.STRING_OBJ)))
        )

        setValue("String",Data(FoxString(""), true))
        setValue("Int",Data(FoxInt(), true))
        setValue("Integer",Data(FoxInteger(), true))
        setValue("Boolean",Data(FoxBoolean(false), true))
        setValue("Double",Data(FoxDouble(), true))
    }

    private fun builtinCBool(args: List<FoxObject?>): FoxObject? {
        args[0]?.let {
            return when (it.type()) {
                ObjectType.INTEGER_OBJ -> nativeBooleanToBooleanObject((it as FoxInteger).value != 0.toBigInteger())
                ObjectType.DOUBLE_OBJ -> nativeBooleanToBooleanObject((it as FoxDouble).value != 0.toBigDecimal())
                ObjectType.STRING_OBJ -> nativeBooleanToBooleanObject((it as FoxString).value.isNotEmpty())
                ObjectType.BOOLEAN_OBJ -> it
                else -> null
            }
        }

        return null
    }

    private fun builtinRange(args: List<FoxObject?>): FoxObject? {
        args[0]?.let {
            val intObj = args[0] as FoxInteger
            val max = intObj.value.toLong()

            if (max < 100_000) {
                val array =
                    (0 until max.toInt()).asSequence()
                        .map { FoxInteger(BigInteger.valueOf(it.toLong())) }
                        .toList()

                return FoxArray(ArrayList(array))
            }

            val numThreads = Runtime.getRuntime().availableProcessors() - 1
            val array = ArrayList<FoxObject>()
            val executorService = Executors.newFixedThreadPool(numThreads)
            val futures = mutableListOf<Future<*>>()
            val chunkSize = (max / numThreads).toInt()
            val remainder = (max % numThreads).toInt()

            for (i in 0 until numThreads) {
                val start = i * chunkSize + if (i < remainder) i else remainder
                val end = start + chunkSize + if (i < remainder) 1 else 0
                val task = executorService.submit(Callable {
                    val subArray = (start until end).map { FoxInteger(BigInteger.valueOf(it.toLong())) }
                    subArray
                })
                futures.add(task)
            }

            futures.forEach { future ->
                val subArray = future.get() as List<FoxObject>
                synchronized(array) {
                    array.addAll(subArray)
                }
            }
            executorService.shutdown()

            return FoxArray(ArrayList(array))
        }
        return null
    }

    private fun builtinPrint(args: List<FoxObject?>): FoxObject? {
        args[0]?.let {
            println(it.inspect())
        }
        return null
    }

    private fun builtinLen(args: List<FoxObject?>): FoxObject {
        return FoxInteger((args[0] as FoxString).value.count().toBigInteger())
    }

    private fun builtinInput(args: List<FoxObject?>): FoxObject {
        if (args.isEmpty()) {
            return FoxString(readln())
        }

        print(args[0]!!.inspect())
        return FoxString(readln())
    }

    private fun addFunction(name: String,kotlinFunctionObject: FoxKotlinFunction) {
        setValue(name, Data(foxObject = kotlinFunctionObject, true))
    }
}


