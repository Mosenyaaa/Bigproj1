// domain/repository/UserRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.EnableDoctorFeaturesResponseDto
import com.example.bigproj.data.model.UserResponseDto
import com.example.bigproj.domain.utils.ErrorHandler

class UserRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val clientService by lazy {
        RetrofitClient.createClientService(tokenManager)
    }

    suspend fun getCurrentUser(): UserResponseDto {
        val response = clientService.getCurrentUser()

        // 🔥 ДОБАВИМ ДЕТАЛЬНОЕ ЛОГИРОВАНИЕ
        println("📡 Ответ getCurrentUser:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")
        println("   Тело: ${response.body()}")

        if (response.isSuccessful) {
            val user = response.body() ?: throw Exception("Пустой ответ от сервера")

            // 🔥 СОХРАНЯЕМ ДАННЫЕ В ТОКЕН МЕНЕДЖЕР
            tokenManager.saveUserEmail(user.email ?: "")
            if (!user.fullName.isNullOrBlank()) {
                tokenManager.saveUserName(user.fullName)
            }

            println("💾 Данные пользователя сохранены:")
            println("   Email: ${user.email}")
            println("   Имя: ${user.fullName}")
            println("   Роли: ${user.roles}")
            println("   isDoctor: ${user.isDoctor}")

            return user
        } else {
            // 🔥 ИЗВЛЕКАЕМ error_description ИЗ ОШИБКИ
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun enableDoctorFeatures(accessKey: String): EnableDoctorFeaturesResponseDto {
        println("🔧 enableDoctorFeatures вызывается с accessKey: '$accessKey'")

        val response = clientService.enableDoctorFeatures(accessKey)

        println("📡 Ответ enableDoctorFeatures:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")
        println("   Тело: ${response.body()}")

        if (response.isSuccessful) {
            val user = response.body()
            println("✅ УСПЕХ! enableDoctorFeatures:")
            println("   ID: ${user?.id}")
            println("   Email: ${user?.email}")
            println("   FullName: ${user?.fullName}")
            println("   isDoctor: ${user?.isDoctor}")
            println("   isActive: ${user?.isActive}")

            return user ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ ОШИБКА enableDoctorFeatures: $errorMessage")
            throw Exception(errorMessage)
        }
    }

    suspend fun updateFullName(newName: String): UserResponseDto {
        println("🔍 ДИАГНОСТИКА updateFullName:")
        println("📤 Отправляем имя: '$newName'")

        val response = clientService.updateFullName(newName)

        println("📥 Ответ сервера:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")
        println("   Тело: ${response.body()}")

        if (response.isSuccessful) {
            val user = response.body()
            println("✅ УСПЕХ! Обновленное имя: ${user?.fullName}")

            // 🔥 ПРОВЕРЯЕМ СОХРАНИЛОСЬ ЛИ ИМЯ НА СЕРВЕРЕ
            if (user?.fullName.isNullOrBlank()) {
                println("⚠️ ВНИМАНИЕ: сервер вернул пустое имя!")
            } else {
                println("✅ Имя успешно сохранено на сервере: ${user?.fullName}")
            }

            return user ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ ОШИБКА updateFullName: $errorMessage")
            throw Exception(errorMessage)
        }
    }

    suspend fun sendResetEmailCode(newEmail: String) {
        println("📧 sendResetEmailCode вызывается с email: '$newEmail'")

        val response = clientService.sendResetEmailCode(newEmail)

        println("📡 Ответ sendResetEmailCode:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")

        if (response.isSuccessful) {
            println("✅ Код отправлен успешно")
        } else {
            // 🔥 ИЗВЛЕКАЕМ error_description ИЗ ОШИБКИ
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ Ошибка отправки кода: $errorMessage")
            throw Exception(errorMessage)
        }
    }

    suspend fun resetEmail(verificationCode: String): UserResponseDto {
        println("🔐 resetEmail вызывается с кодом: '$verificationCode'")

        val response = clientService.resetEmail(verificationCode)

        println("📡 Ответ resetEmail:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")

        if (response.isSuccessful) {
            val user = response.body()
            println("✅ УСПЕХ! resetEmail:")
            println("   Новый email: ${user?.email}")
            println("   FullName: ${user?.fullName}")
            return user ?: throw Exception("Пустой ответ от сервера")
        } else {
            // 🔥 ИЗВЛЕКАЕМ error_description ИЗ ОШИБКИ
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ ОШИБКА resetEmail: $errorMessage")
            throw Exception(errorMessage)
        }
    }
}