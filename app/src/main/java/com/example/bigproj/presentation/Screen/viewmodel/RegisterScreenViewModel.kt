// presentation/Screen/viewmodel/RegisterScreenViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.domain.validation.AuthValidations
import com.example.bigproj.domain.validation.ValidationResult
import com.example.bigproj.presentation.Screen.RegistrationHolder
import com.example.bigproj.presentation.Screen.state.RegisterScreenEvent
import com.example.bigproj.presentation.Screen.state.RegisterScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterScreenViewModel : ViewModel() {
    var state by mutableStateOf(RegisterScreenState())
        private set

    // 🔥 ДОБАВЛЯЕМ ОТДЕЛЬНЫЕ СОСТОЯНИЯ ДЛЯ КАЖДОЙ ОШИБКИ
    var emailError by mutableStateOf<String?>(null)
        private set
    var nameError by mutableStateOf<String?>(null)
        private set
    var termsError by mutableStateOf<String?>(null)
        private set

    private val authRepository = AuthRepository()
    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun sendCodeOnEmail(agreedToTerms: Boolean) {
        viewModelScope.launch {
            try {
                // 🔥 ОЧИЩАЕМ ВСЕ ОШИБКИ ПЕРЕД ПРОВЕРКОЙ
                clearAllErrors()

                // 🔥 ПРОВЕРЯЕМ ВСЕ ВАЛИДАЦИИ
                val emailValidation = AuthValidations.validateEmail(state.email)
                val nameValidation = AuthValidations.validateName(state.name)
                val termsValidation = AuthValidations.validateTermsAgreement(agreedToTerms)

                // 🔥 СОБИРАЕМ ВСЕ ОШИБКИ
                var hasErrors = false

                if (emailValidation.isError) {
                    emailError = (emailValidation as ValidationResult.Error).message
                    hasErrors = true
                }

                if (nameValidation.isError) {
                    nameError = (nameValidation as ValidationResult.Error).message
                    hasErrors = true
                }

                if (termsValidation.isError) {
                    termsError = (termsValidation as ValidationResult.Error).message
                    hasErrors = true
                }

                if (hasErrors) {
                    return@launch
                }

                println("📝 Регистрация с именем: '${state.name}', email: '${state.email}'")

                // 🔥 ПЕРЕДАЕМ ИМЯ И EMAIL
                authRepository.sendCodeOnEmail(email = state.email, fullName = state.name)

                // Сохраняем имя в RegistrationHolder для верификации
                RegistrationHolder.tempName = state.name
                RegistrationHolder.tempEmail = state.email

                _events.send(AuthEvent.NavigateToVerification)
            } catch (e: Exception) {
                println("❌ Ошибка отправки кода: ${e.message}")
                state = state.copy(errorMessage = "Ошибка регистрации: ${e.message}")
            }
        }
    }

    fun onEvent(event: RegisterScreenEvent) {
        when (event) {
            is RegisterScreenEvent.EmailUpdated -> {
                this.state = state.copy(email = event.newEmail)
                // 🔥 ОЧИЩАЕМ ОШИБКУ EMAIL ПРИ ИЗМЕНЕНИИ
                emailError = null
                // 🔥 ВАЛИДАЦИЯ В РЕАЛЬНОМ ВРЕМЕНИ (ОПЦИОНАЛЬНО)
                if (event.newEmail.isNotBlank()) {
                    val validation = AuthValidations.validateEmail(event.newEmail)
                    if (validation.isError) {
                        emailError = (validation as ValidationResult.Error).message
                    }
                }
            }
            is RegisterScreenEvent.NameUpdated -> {
                this.state = state.copy(name = event.newName)
                // 🔥 ОЧИЩАЕМ ОШИБКУ ИМЕНИ ПРИ ИЗМЕНЕНИИ
                nameError = null
                // 🔥 ВАЛИДАЦИЯ В РЕАЛЬНОМ ВРЕМЕНИ (ОПЦИОНАЛЬНО)
                if (event.newName.isNotBlank()) {
                    val validation = AuthValidations.validateName(event.newName)
                    if (validation.isError) {
                        nameError = (validation as ValidationResult.Error).message
                    }
                }
            }
        }
    }

    // 🔥 МЕТОД ДЛЯ ОЧИСТКИ ВСЕХ ОШИБОК
    fun clearAllErrors() {
        emailError = null
        nameError = null
        termsError = null
        state = state.copy(errorMessage = null)
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}