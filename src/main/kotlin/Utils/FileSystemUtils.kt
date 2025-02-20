package me.user.Utils

import com.sun.tools.javac.Main
import java.io.File
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

fun findFile(folderPaths: List<String>, fileName: String, fileExtension: String): String? {
    val lowerCaseExtension = fileExtension.lowercase() // 将扩展名转换为小写

    for (folderPath in folderPaths) {
        val folder = Paths.get(folderPath)
        if (!folder.toFile().exists() || !folder.toFile().isDirectory) continue

        // 使用 Files.walk 遍历文件夹
        var foundPath: Path? = null // 用于存储找到的路径
        Files.walk(folder)
            .filter { it.toFile().isFile } // 筛选出文件
            .forEach { path ->
                val name = path.fileName.toString()
                val nameWithoutExtension = name.substringBeforeLast('.')
                val extension = name.substringAfterLast('.').lowercase()

                if (nameWithoutExtension == fileName && extension == lowerCaseExtension) {
                    foundPath = path // 找到匹配文件，存储路径
                }
            }

        if (foundPath != null) {
            return foundPath.toString() // 如果找到路径，返回
        }
    }
    return null // 如果未找到文件，返回 null
}


fun findFolder(folderPaths: List<String>, folderName: String): String? {
    for (folderPath in folderPaths) {
        val dirPath = Paths.get(folderPath, folderName).toString()
        if (File(dirPath).isDirectory) {
            return dirPath // 返回文件夹路径
        }
    }
    return null // 如果未找到文件夹，返回 null
}

fun getCurrentDirectory(): String {
    // 获取系统属性中的当前工作目录
    val currentDir = System.getProperty("user.dir")
    return Paths.get(currentDir).toString()
}