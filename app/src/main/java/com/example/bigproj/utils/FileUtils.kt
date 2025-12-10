// utils/FileUtils.kt
package com.example.bigproj.utils

import android.content.Context
import android.net.Uri
import java.io.File

object FileUtils {

    // Получение URI по имени файла (для предпросмотра)
    fun getFileUri(context: Context, filename: String, type: String): Uri? {
        return try {
            // В реальном приложении нужно получать файл с сервера или из кэша
            // Пока возвращаем null - это заглушка
            null
        } catch (e: Exception) {
            null
        }
    }

    // Создание временного файла
    fun createTempFile(context: Context, prefix: String, extension: String): File {
        val cacheDir = context.cacheDir
        return File.createTempFile(prefix, ".$extension", cacheDir)
    }

    // Копирование файла
    fun copyFile(sourceUri: Uri, destination: File, context: Context): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}