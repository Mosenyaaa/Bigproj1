package com.example.bigproj.data.interceptor

import com.example.bigproj.domain.repository.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val userToken = tokenManager.getUserToken()
        val requestBuilder = originalRequest.newBuilder()

        if (userToken != null) {
            requestBuilder.addHeader("user-token", userToken)
            println("🔐 Добавляем user-token в заголовок: $userToken")
        }

        val request = requestBuilder.build()

        println("📨 Заголовки запроса: ${request.headers}")

        return chain.proceed(request)
    }
}