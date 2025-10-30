package com.example.bigproj.presentation.Screen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.presentation.Screen.state.LoginScreenEvent
import com.example.bigproj.presentation.Screen.state.LoginScreenState
import com.example.bigproj.presentation.navigation.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {
    var state by mutableStateOf(LoginScreenState())
        private set

    private val authRepository = AuthRepository()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun goToVerification() {
        viewModelScope.launch {
            try {
                // 🔥 СОХРАНЯЕМ EMAIL ПЕРЕД ОТПРАВКОЙ КОДА
                val currentEmail = state.email
                println("📧 Отправляем код на email: $currentEmail")

                authRepository.sendCodeOnEmail(email = currentEmail)
                _events.send(AuthEvent.NavigateToVerification)
            } catch (e: Exception) {
                println("❌ Ошибка отправки кода: ${e.message}")
            }
        }
    }

    fun goToRegistration() {
        viewModelScope.launch {
            _events.send(AuthEvent.NavigateToRegistration)
        }
    }

    fun sendCodeOnEmail() {
        viewModelScope.launch {
            // 🔥 ЭТОТ МЕТОД МОЖЕТ БЫТЬ ПРОБЛЕМОЙ - ПЕРЕДАЕМ ТЕКУЩИЙ EMAIL
            val currentEmail = state.email
            println("📧 sendCodeOnEmail вызывается с email: $currentEmail")
            authRepository.sendCodeOnEmail(email = currentEmail)
        }
    }

    fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.EmailUpdated -> {
                // 🔥 ДОБАВИМ ЛОГ ДЛЯ ОТСЛЕЖИВАНИЯ ИЗМЕНЕНИЙ
                println("📧 Email обновлен: '${state.email}' -> '${event.newEmail}'")
                this.state = state.copy(email = event.newEmail)
            }
            is LoginScreenEvent.NavigateToScreen -> when (event.screen) {
                is Screen.Verification -> goToVerification()
                is Screen.Register -> goToRegistration()
                else -> Unit
            }
        }
    }
}

sealed class AuthEvent {
    object NavigateToVerification : AuthEvent()
    object NavigateToRegistration : AuthEvent()
}