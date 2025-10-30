package com.example.bigproj.data

import com.example.bigproj.data.api.ClientService
import com.example.bigproj.data.api.GeneralService
import com.example.bigproj.data.interceptor.AuthInterceptor
import com.example.bigproj.domain.repository.TokenManager
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val contentType = "application/json".toMediaType()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://87.251.73.164/")
        .addConverterFactory(json.asConverterFactory(contentType))
        .client(client)
        .build()
    val apiService: GeneralService = retrofit.create(GeneralService::class.java)

    fun createClientService(tokenManager: TokenManager): ClientService {
        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://87.251.73.164/")
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(authenticatedClient)
            .build()
            .create(ClientService::class.java)
    }
}