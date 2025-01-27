package me.user.Utils

import com.sun.jna.*
import com.sun.jna.Function
import java.io.File

/**
 * DLL 工具类，封装动态链接库的加载和函数调用。
*/
object DLLUtils {

    // 缓存已加载的库
    private val loadedLibraries = mutableMapOf<String, NativeLibrary>()

    /**
     * 加载动态链接库。
     *
     * @param dllPath DLL 文件路径（支持绝对路径和相对路径）。
     * @return 加载的 NativeLibrary 对象。
     * @throws IllegalStateException 如果加载失败。
     */
    fun loadLibrary(dllPath: String): NativeLibrary {
        return loadedLibraries.getOrPut(dllPath) {
            try {
                val absolutePath = File(dllPath).absolutePath
                NativeLibrary.getInstance(absolutePath)
            } catch (e: Exception) {
                throw IllegalStateException("无法加载动态链接库: $dllPath", e)
            }
        }
    }

    /**
     * 查找函数并指定返回类型
     * @param R 返回值类型（如 Int、Long、Pointer 等）
     */
    fun findFunction(library: NativeLibrary, name: String): Function {
        return library.getFunction(name) as Function
    }

    /**
     * 安全调用函数（带泛型支持）
     */
    fun callFunction(function: Function, vararg args: Any): Any {
        return function.invoke(args)
    }

    /**
     * 卸载已加载的 DLL。
     *
     * @param dllPath DLL 文件路径。
     */
    fun unloadLibrary(dllPath: String) {
        loadedLibraries.remove(dllPath)?.dispose()
    }

    /**
     * 获取当前平台的动态链接库扩展名。
     *
     * @return 扩展名（如 ".dll"、".so"、".dylib"）。
     */
    fun getPlatformLibraryExtension(): String {
        return when {
            Platform.isWindows() -> ".dll"
            Platform.isLinux() -> ".so"
            Platform.isMac() -> ".dylib"
            else -> throw UnsupportedOperationException("Unsupported platform: ${Platform.getOSType()}")
        }
    }
}