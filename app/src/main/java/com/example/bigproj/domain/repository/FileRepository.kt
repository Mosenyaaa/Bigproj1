package com.example.bigproj.domain.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.domain.utils.ErrorHandler
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream
import java.util.UUID

class FileRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val fileService by lazy {
        RetrofitClient.createFileService(tokenManager)
    }

    // Существующий метод
    suspend fun uploadFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, null)
    }

    // Новый метод для загрузки голосовых файлов
    suspend fun uploadVoiceFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, "audio")
    }

    // Новый метод для загрузки изображений
    suspend fun uploadImageFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, "image")
    }

    // Универсальный метод загрузки
    private suspend fun uploadFileInternal(fileUri: Uri, fileType: String? = null): String {
        try {
            val file = File(fileUri.path ?: throw Exception("Неверный URI файла"))

            // Генерируем уникальное имя файла
            val originalName = file.name
            val extension = originalName.substringAfterLast(".", "")
            val uniqueName = "${UUID.randomUUID()}.$extension"

            // Определяем MIME тип
            val mimeType = when (fileType) {
                "audio" -> "audio/*"
                "image" -> "image/*"
                else -> getMimeType(file) ?: "application/octet-stream"
            }

            val requestFile = RequestBody.create(
                mimeType.toMediaType(),
                file
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",
                uniqueName,
                requestFile
            )

            val response = fileService.uploadFile(filePart)

            if (response.isSuccessful) {
                val result = response.body()
                return result?.filename ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Ошибка загрузки файла: ${e.message}")
        }
    }

    // Получение MIME типа файла
    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    // Проверка допустимых типов файлов
    fun isValidVoiceFile(uri: Uri): Boolean {
        val file = File(uri.path ?: return false)
        val extension = file.extension.lowercase()
        return extension in listOf("mp3", "wav", "ogg", "m4a", "aac")
    }

    fun isValidImageFile(uri: Uri): Boolean {
        val file = File(uri.path ?: return false)
        val extension = file.extension.lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }

    // Получение размера файла в МБ
    fun getFileSizeMb(uri: Uri): Double {
        val file = File(uri.path ?: return 0.0)
        return file.length().toDouble() / (1024 * 1024)
    }
}