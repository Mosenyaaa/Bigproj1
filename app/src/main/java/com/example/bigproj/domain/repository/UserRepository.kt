package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.EnableDoctorFeaturesResponseDto
import com.example.bigproj.data.model.UserResponseDto

class UserRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val clientService by lazy {
        RetrofitClient.createClientService(tokenManager)
    }

    suspend fun getCurrentUser(): UserResponseDto {
        val response = clientService.getCurrentUser()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            throw Exception("Ошибка сервера: ${response.code()}")
        }
    }

    suspend fun enableDoctorFeatures(accessKey: String): EnableDoctorFeaturesResponseDto {
        println("🔧 enableDoctorFeatures вызывается с accessKey: '$accessKey'")

        val response = clientService.enableDoctorFeatures(accessKey)

        println("📡 Ответ enableDoctorFeatures:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")

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
            val errorBody = response.errorBody()?.string()
            println("❌ ОШИБКА enableDoctorFeatures:")
            println("   Тело ошибки: $errorBody")
            println("   Заголовки: ${response.headers()}")
            throw Exception("Ошибка сервера: ${response.code()} - $errorBody")
        }
    }

    suspend fun updateFullName(newName: String): UserResponseDto {
        println("🔍 ДИАГНОСТИКА updateFullName:")
        println("📤 Отправляем имя: '$newName'")
        println("📏 Длина: ${newName.length}")
        println("🔤 Кириллица: ${newName.any { it in 'А'..'я' }}")

        val response = clientService.updateFullName(newName)

        println("📥 Ответ сервера:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")

        if (response.isSuccessful) {
            val user = response.body()
            println("✅ УСПЕХ! Обновленное имя: ${user?.fullName}")
            return user ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorBody = response.errorBody()?.string()
            println("❌ ОШИБКА 400:")
            println("   Тело ошибки: $errorBody")
            println("   Заголовки: ${response.headers()}")
            throw Exception("Ошибка сервера: ${response.code()} - $errorBody")
        }
    }

    suspend fun sendResetEmailCode(newEmail: String) {
        println("📧 sendResetEmailCode вызывается с email: '$newEmail'")

        val response = clientService.sendResetEmailCode(newEmail)

        println("📡 Ответ sendResetEmailCode:")
        println("   Код: ${response.code()}")
        println("   Успешно: ${response.isSuccessful}")
        println("   Заголовки: ${response.headers()}")

        if (response.isSuccessful) {
            println("✅ Код отправлен успешно")
        } else {
            val errorBody = response.errorBody()?.string()
            println("❌ Ошибка отправки кода:")
            println("   Тело ошибки: $errorBody")
            println("   Код ошибки: ${response.code()}")
            throw Exception("Ошибка отправки кода: ${response.code()} - $errorBody")
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
            val errorBody = response.errorBody()?.string()
            println("❌ ОШИБКА resetEmail:")
            println("   Тело ошибки: $errorBody")
            throw Exception("Ошибка смены email: ${response.code()} - $errorBody")
        }
    }
}