// domain/utils/ErrorHandler.kt
package com.example.bigproj.domain.utils

import com.example.bigproj.data.model.ErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.Response

object ErrorHandler {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseError(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            println("🔍 ErrorHandler: код=${response.code()}, тело=$errorBody")

            if (!errorBody.isNullOrBlank()) {
                parseErrorMessage(errorBody)
            } else {
                getDefaultErrorMessage(response.code())
            }
        } catch (e: Exception) {
            println("🔍 ErrorHandler: исключение=${e.message}")
            getDefaultErrorMessage(response.code())
        }
    }

    private fun parseErrorMessage(errorBody: String): String {
        return try {
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)

            // 🔥 БИЗНЕС-ОШИБКИ: преобразуем технические коды в понятные сообщения
            val businessMessage = when (errorResponse.errorCode) {
                "CANNOT_AUTHORIZE" -> "Ошибка авторизации. Проверьте данные и попробуйте снова"
                "USER_ALREADY_EXISTS" -> "Пользователь с такой почтой уже зарегистрирован"
                "INVALID_VERIFICATION_CODE" -> "Неверный код подтверждения"
                "VERIFICATION_CODE_EXPIRED" -> "Код подтверждения устарел"
                "USER_NOT_FOUND" -> "Пользователь не найден"
                "ACCESS_DENIED" -> "Доступ запрещен"
                "INVALID_ACCESS_KEY" -> "Неверный access key"
                else -> null
            }

            when {
                !businessMessage.isNullOrBlank() -> businessMessage
                !errorResponse.errorDescription.isNullOrBlank() -> errorResponse.errorDescription
                !errorResponse.errorCode.isNullOrBlank() -> "Ошибка: ${errorResponse.errorCode}"
                else -> getDefaultErrorMessage(0)
            }
        } catch (e: Exception) {
            "Ошибка сервера"
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Неверный запрос"
            401 -> "Ошибка авторизации"
            403 -> "Доступ запрещен"
            404 -> "Ресурс не найден"
            409 -> "Конфликт данных"
            422 -> "Ошибка валидации"
            500 -> "Внутренняя ошибка сервера"
            else -> "Ошибка сервера: $statusCode"
        }
    }

    // 🔥 ОБРАБОТКА СЕТЕВЫХ ОШИБОК
    fun parseNetworkError(exception: Exception): String {
        return when {
            exception.message?.contains("timeout", true) == true -> "Превышено время ожидания"
            exception.message?.contains("unable to resolve", true) == true -> "Проблемы с интернет-соединением"
            exception.message?.contains("connection", true) == true -> "Нет соединения с интернетом"
            else -> exception.message ?: "Неизвестная ошибка"
        }
    }
}