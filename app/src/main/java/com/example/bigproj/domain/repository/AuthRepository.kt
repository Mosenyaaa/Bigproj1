package com.example.bigproj.domain.repository

import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.VerifyCodeResponseDto

class AuthRepository {
    private val generalServiceApi = RetrofitClient.apiService

    suspend fun sendCodeOnEmail(email: String) {
        generalServiceApi.sendCodeOnEmail(email)
    }

    suspend fun verifyCode(code: String): VerifyCodeResponseDto {
        val response = generalServiceApi.verifyCode(code)
        return response.body() ?: throw Exception("Ошибка верификации")
    }
}