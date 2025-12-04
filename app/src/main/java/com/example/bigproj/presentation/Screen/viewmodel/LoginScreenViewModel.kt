// presentation/Screen/viewmodel/LoginScreenViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.domain.validation.AuthValidations
import com.example.bigproj.domain.validation.ValidationResult
import com.example.bigproj.presentation.Screen.state.LoginScreenEvent
import com.example.bigproj.presentation.Screen.state.LoginScreenState
import com.example.bigproj.presentation.navigation.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {
    var state by mutableStateOf(LoginScreenState())
        private set

    // 🔥 ДОБАВЛЯЕМ СОСТОЯНИЕ ДЛЯ ОШИБОК ВАЛИДАЦИИ
    var emailValidation by mutableStateOf<ValidationResult>(ValidationResult.Success)
        private set

    private val authRepository = AuthRepository()
    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun goToVerification() {
        viewModelScope.launch {
            try {
                // 🔥 ПРОВЕРЯЕМ ВАЛИДАЦИЮ ПЕРЕД ОТПРАВКОЙ
                emailValidation = AuthValidations.validateEmail(state.email)
                if (emailValidation.isError) {
                    state = state.copy(errorMessage = (emailValidation as ValidationResult.Error).message)
                    return@launch
                }

                val currentEmail = state.email
                println("📧 Отправляем код на email: $currentEmail (логин)")

                authRepository.sendCodeOnEmail(email = currentEmail)
                _events.send(AuthEvent.NavigateToVerification)
            } catch (e: Exception) {
                println("❌ Ошибка отправки кода: ${e.message}")
                // 🔥 ОШИБКА УЖЕ В ПОНЯТНОМ ФОРМАТЕ ИЗ ErrorHandler
                state = state.copy(errorMessage = e.message)
            }
        }
    }

    fun goToRegistration() {
        viewModelScope.launch {
            _events.send(AuthEvent.NavigateToRegistration)
        }
    }

    fun onEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.EmailUpdated -> {
                println("📧 Email обновлен: '${state.email}' -> '${event.newEmail}'")
                this.state = state.copy(email = event.newEmail, errorMessage = null)
                // 🔥 ВАЛИДИРУЕМ В РЕАЛЬНОМ ВРЕМЕНИ (ОПЦИОНАЛЬНО)
                if (event.newEmail.isNotBlank()) {
                    emailValidation = AuthValidations.validateEmail(event.newEmail)
                }
            }
            is LoginScreenEvent.NavigateToScreen -> when (event.screen) {
                is Screen.Verification -> goToVerification()
                is Screen.Register -> goToRegistration()
                else -> Unit
            }
        }
    }

    // 🔥 ОЧИСТКА ОШИБОК
    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}