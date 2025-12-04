// data/interceptor/AuthInterceptor.kt
package com.example.bigproj.data.interceptor

import com.example.bigproj.domain.repository.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        println("🔧 AuthInterceptor: обрабатываем запрос к ${originalRequest.url}")

        // 🔥 ЛОГИРУЕМ ТЕЛО ЗАПРОСА ДЛЯ POST ЗАПРОСОВ
        if (originalRequest.method == "POST" && originalRequest.body != null) {
            try {
                val buffer = Buffer()
                originalRequest.body!!.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()
                println("📦 ТЕЛО POST ЗАПРОСА: $requestBodyString")
            } catch (e: Exception) {
                println("⚠️ Не удалось прочитать тело запроса: ${e.message}")
            }
        }

        val requestBuilder = originalRequest.newBuilder()
            .addHeader("apikey", "apikeyvalue22a28be9ad3f484aacf6f164a501f61d820a2e7a710b4adbb3852c9da6754326efa6d329918a4fe082d781e4c02b55b31764084620106912")
            .addHeader("api_key_dbm", "1")

        val userToken = tokenManager.getUserToken()
        if (userToken != null) {
            requestBuilder.addHeader("user-token", userToken)
            println("🔐 Добавляем user-token: $userToken")
        }

        val request = requestBuilder.build()

        println("📨 Заголовки запроса:")
        request.headers.forEach { header ->
            println(" ${header.first}: ${header.second}")
        }

        return chain.proceed(request)
    }
}