// domain/repository/FileRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.domain.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID

class FileRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val fileService by lazy {
        RetrofitClient.createFileService(tokenManager)
    }

    suspend fun uploadFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, null)
    }

    suspend fun uploadVoiceFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, "audio")
    }

    suspend fun uploadImageFile(fileUri: Uri): String {
        return uploadFileInternal(fileUri, "image")
    }

    private suspend fun uploadFileInternal(fileUri: Uri, fileType: String? = null): String {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(fileUri.path ?: throw Exception("Неверный URI файла"))

                // Валидация файла
                when (fileType) {
                    "audio" -> {
                        val validation = validateAudioFile(fileUri)
                        if (validation is ValidationResult.Error) {
                            throw Exception(validation.message)
                        }
                    }
                    "image" -> {
                        val validation = validateImageFile(fileUri)
                        if (validation is ValidationResult.Error) {
                            throw Exception(validation.message)
                        }
                    }
                }

                val originalName = file.name
                val extension = originalName.substringAfterLast(".", "")
                val uniqueName = "${UUID.randomUUID()}.$extension"

                val mimeType = when (fileType) {
                    "audio" -> "audio/*"
                    "image" -> "image/*"
                    else -> getMimeType(file) ?: "application/octet-stream"
                }

                val requestFile = file.asRequestBody(mimeType.toMediaType())

                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    uniqueName,
                    requestFile
                )

                val response = fileService.uploadFile(filePart)

                if (response.isSuccessful) {
                    val result = response.body()
                    result?.filename ?: throw Exception("Пустой ответ от сервера")
                } else {
                    val errorMessage = ErrorHandler.parseError(response)
                    throw Exception(errorMessage)
                }
            } catch (e: Exception) {
                throw Exception("Ошибка загрузки файла: ${e.message}")
            }
        }
    }

    // ВАЛИДАЦИЯ АУДИО ФАЙЛОВ (возвращает ValidationResult)
    fun validateAudioFile(uri: Uri): ValidationResult {
        return try {
            val file = File(uri.path ?: return ValidationResult.Error("Неверный URI файла"))

            val extension = file.extension.lowercase()
            val validExtensions = listOf("mp3", "wav", "ogg", "m4a", "aac", "flac")

            if (extension !in validExtensions) {
                return ValidationResult.Error("Недопустимый формат аудио. Разрешены: ${validExtensions.joinToString(", ")}")
            }

            val maxSizeBytes = 50 * 1024 * 1024
            if (file.length() > maxSizeBytes) {
                return ValidationResult.Error("Файл слишком большой. Максимальный размер: 50 МБ")
            }

            ValidationResult.Success
        } catch (e: Exception) {
            ValidationResult.Error("Ошибка проверки аудиофайла: ${e.message}")
        }
    }

    // ВАЛИДАЦИЯ ИЗОБРАЖЕНИЙ (возвращает ValidationResult)
    fun validateImageFile(uri: Uri): ValidationResult {
        return try {
            val file = File(uri.path ?: return ValidationResult.Error("Неверный URI файла"))

            val extension = file.extension.lowercase()
            val validExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")

            if (extension !in validExtensions) {
                return ValidationResult.Error("Недопустимый формат изображения. Разрешены: ${validExtensions.joinToString(", ")}")
            }

            val maxSizeBytes = 10 * 1024 * 1024
            if (file.length() > maxSizeBytes) {
                return ValidationResult.Error("Изображение слишком большое. Максимальный размер: 10 МБ")
            }

            ValidationResult.Success
        } catch (e: Exception) {
            ValidationResult.Error("Ошибка проверки изображения: ${e.message}")
        }
    }

    // ПРОВЕРКА ДОПУСТИМЫХ ТИПОВ ФАЙЛОВ (возвращает Boolean) - переименовываем
    fun isVoiceFileValid(uri: Uri): Boolean {
        val file = File(uri.path ?: return false)
        val extension = file.extension.lowercase()
        return extension in listOf("mp3", "wav", "ogg", "m4a", "aac")
    }

    fun isImageFileValid(uri: Uri): Boolean {
        val file = File(uri.path ?: return false)
        val extension = file.extension.lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }

    suspend fun uploadFileWithValidation(
        fileUri: Uri,
        fileType: String
    ): String {
        return when (fileType) {
            "audio" -> {
                val validation = validateAudioFile(fileUri)
                if (validation is ValidationResult.Error) {
                    throw Exception(validation.message)
                }
                uploadVoiceFile(fileUri)
            }
            "image" -> {
                val validation = validateImageFile(fileUri)
                if (validation is ValidationResult.Error) {
                    throw Exception(validation.message)
                }
                uploadImageFile(fileUri)
            }
            else -> uploadFile(fileUri)
        }
    }

    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    fun getFileSizeMb(uri: Uri): Double {
        val file = File(uri.path ?: return 0.0)
        return file.length().toDouble() / (1024 * 1024)
    }

    fun getFileSizeReadable(uri: Uri): String {
        val file = File(uri.path ?: return "0 Б")
        val bytes = file.length()

        return when {
            bytes < 1024 -> "$bytes Б"
            bytes < 1024 * 1024 -> "${bytes / 1024} КБ"
            else -> "${String.format("%.1f", bytes.toDouble() / (1024 * 1024))} МБ"
        }
    }
}