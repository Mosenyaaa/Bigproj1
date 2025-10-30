package com.example.bigproj.domain.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_token_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveUserToken(token: String) {
        println("💾 Сохраняем токен: $token")
        sharedPreferences.edit ().putString(USER_TOKEN_KEY, token).apply()

        val savedToken = sharedPreferences.getString(USER_TOKEN_KEY, null)
        println("✅ Токен сохранен: ${savedToken != null}")
    }

    fun getUserToken(): String? {
        val token = sharedPreferences.getString(USER_TOKEN_KEY, null)
        println("🔍 Получаем токен: ${if (token != null) "ЕСТЬ" else "НЕТ"}")
        return token
    }

    fun saveUserName(name: String) {
        println("💾 Сохраняем имя: $name")
        sharedPreferences.edit().putString(USER_NAME_KEY, name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }

    fun clearUserToken() {
        println("🗑️ Очищаем токен")
        sharedPreferences.edit().remove(USER_TOKEN_KEY).apply()


        val tokenAfterClear = sharedPreferences.getString(USER_TOKEN_KEY, null)
        println("✅ Токен очищен: ${tokenAfterClear == null}")
    }

    companion object {
        private const val USER_TOKEN_KEY = "user_token"
        private const val USER_NAME_KEY = "user_name"
    }
}