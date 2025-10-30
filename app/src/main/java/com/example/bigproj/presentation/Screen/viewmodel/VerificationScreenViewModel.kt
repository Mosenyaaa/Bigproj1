package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.domain.repository.TokenManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VerificationScreenViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val authRepository = AuthRepository()
    private val _events = Channel<VerificationEvent>()
    val events = _events.receiveAsFlow()

    private var tokenManager: TokenManager? = null

    fun setupTokenManager(context: Context) {
        tokenManager = TokenManager(context)
    }

    fun verifyCode(code: String, context: Context) {
        isLoading = true
        errorMessage = null

        if (tokenManager == null) {
            tokenManager = TokenManager(context)
        }

        viewModelScope.launch {
            try {
                println("🔐 Начинаем верификацию кода: $code")
                val response = authRepository.verifyCode(code)
                println("🔐 Полный ответ от verifyCode: $response")

                val isSuccess = response.hasError != true && response.value != null

                if (isSuccess) {
                    val token = response.value!!
                    println("🎉 УСПЕХ! Получен токен: $token")

                    tokenManager!!.saveUserToken(token)
                    val savedToken = tokenManager!!.getUserToken()
                    println("💾 Токен сохранен в TokenManager: $savedToken")

                    _events.send(VerificationEvent.NavigateToMain)
                } else {
                    println("❌ Ошибка верификации: ${response.errorDescription}")
                    errorMessage = response.errorDescription ?: "Неверный код"
                }
            } catch (e: Exception) {
                println("💥 Исключение: ${e.message}")
                errorMessage = "Ошибка сети: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 🔥 ДОБАВЛЯЕМ КЛАСС VerificationEvent ВНУТРИ ViewModel
    sealed class VerificationEvent {
        object NavigateToMain : VerificationEvent()
    }
}