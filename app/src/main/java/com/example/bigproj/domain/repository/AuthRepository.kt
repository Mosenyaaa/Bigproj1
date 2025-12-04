// domain/repository/AuthRepository.kt
package com.example.bigproj.domain.repository

import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.VerifyCodeResponseDto
import com.example.bigproj.domain.utils.ErrorHandler

class AuthRepository {
    private val generalServiceApi = RetrofitClient.apiService

    suspend fun sendCodeOnEmail(email: String, fullName: String? = null) {
        try {
            println("🔍 AuthRepository: отправляем код на email='$email', имя='$fullName'")

            val response = if (fullName != null) {
                println("📤 Отправляем имя на сервер: '$fullName'")
                generalServiceApi.sendCodeOnEmail(email = email, fullName = fullName)
            } else {
                println("📤 Отправляем только email (без имени)")
                generalServiceApi.sendCodeOnEmail(email = email)
            }

            println("📡 Ответ sendCodeOnEmail:")
            println("   Код: ${response.code()}")
            println("   Успешно: ${response.isSuccessful}")
            println("   Тело: ${response.body()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                println("❌ Тело ошибки: $errorBody")
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }

            println("✅ Код отправлен успешно")
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorHandler.parseNetworkError(e)
            println("💥 Ошибка отправки кода: $userFriendlyMessage")
            throw Exception(userFriendlyMessage)
        }
    }

    suspend fun verifyCode(code: String): VerifyCodeResponseDto {
        try {
            println("🔐 Начинаем верификацию кода: $code")
            val response = generalServiceApi.verifyCode(code)

            println("📡 Сырой ответ верификации:")
            println("   Код: ${response.code()}")
            println("   Успешно: ${response.isSuccessful}")
            println("   Тело: ${response.body()}")

            if (response.body()?.hasError == true) {
                val errorDescription = response.body()?.errorDescription
                throw Exception(errorDescription ?: "Ошибка верификации")
            }

            if (!response.isSuccessful) {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }

            val responseBody = response.body() ?: throw Exception("Пустой ответ от сервера")

            if (responseBody.value.isNullOrBlank()) {
                throw Exception("Токен не получен от сервера")
            }

            println("✅ Верификация успешна, токен получен")
            return responseBody
        } catch (e: Exception) {
            val userFriendlyMessage = ErrorHandler.parseNetworkError(e)
            throw Exception(userFriendlyMessage)
        }
    }
}