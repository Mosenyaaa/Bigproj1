// presentation/Screen/viewmodel/VerificationScreenViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.domain.repository.TokenManager
import com.example.bigproj.domain.repository.UserRepository
import com.example.bigproj.domain.validation.AuthValidations
import com.example.bigproj.domain.validation.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class VerificationScreenViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var codeValidation by mutableStateOf<ValidationResult>(ValidationResult.Success)
        private set

    private val authRepository = AuthRepository()
    private val _events = Channel<VerificationEvent>()
    val events = _events.receiveAsFlow()

    private var tokenManager: TokenManager? = null
    private var currentEmail: String = ""
    private var registrationName: String = ""

    fun setupTokenManager(context: Context, email: String, name: String = "") {
        tokenManager = TokenManager(context)
        currentEmail = email
        registrationName = name
        println("📧 VerificationScreenViewModel: установлен email='$email', имя='$name'")
    }

    fun verifyCode(code: String, context: Context) {
        // 🔥 ПРОВЕРЯЕМ ВАЛИДАЦИЮ КОДА
        codeValidation = AuthValidations.validateVerificationCode(code)
        if (codeValidation.isError) {
            errorMessage = (codeValidation as ValidationResult.Error).message
            return
        }

        isLoading = true
        errorMessage = null

        if (tokenManager == null) {
            tokenManager = TokenManager(context)
        }

        viewModelScope.launch {
            try {
                println("🔐 Начинаем верификацию кода: $code")
                val response = authRepository.verifyCode(code)

                val token = response.value ?: throw Exception("Токен не получен")
                println("🎉 УСПЕХ! Получен токен: $token")

                tokenManager!!.saveUserToken(token)

                // 🔥 АНАЛИЗ ОТВЕТА
                println("📊 Анализ ответа верификации:")
                println("   User ID: ${response.userId}")
                println("   User в ответе: ${response.user != null}")
                println("   Email в ответе: ${response.user?.email}")
                println("   FullName в ответе: ${response.user?.fullName}")

                // 🔥 СОХРАНЯЕМ EMAIL ИЗ ОТВЕТА
                val userEmail = response.user?.email ?: currentEmail
                tokenManager!!.saveUserEmail(userEmail)
                println("💾 Email сохранен: $userEmail")

                // 🔥🔴🔴🔴 ГЛАВНОЕ: ОБНОВЛЯЕМ ИМЯ НА СЕРВЕРЕ ЕСЛИ ОНО ПУСТОЕ 🔴🔴🔴
                if (registrationName.isNotBlank() && response.user?.fullName.isNullOrBlank()) {
                    try {
                        println("🔄 Обновляем имя на сервере: '$registrationName'")
                        val userRepository = UserRepository(context)
                        val updatedUser = userRepository.updateFullName(registrationName)

                        // 🔥 СОХРАНЯЕМ ОБНОВЛЕННОЕ ИМЯ
                        tokenManager!!.saveUserName(updatedUser.fullName ?: registrationName)
                        println("✅ Имя успешно обновлено на сервере: ${updatedUser.fullName}")

                    } catch (e: Exception) {
                        println("⚠️ Не удалось обновить имя на сервере: ${e.message}")
                        // 🔥 СОХРАНЯЕМ ИМЯ ЛОКАЛЬНО В ЛЮБОМ СЛУЧАЕ
                        tokenManager!!.saveUserName(registrationName)
                        println("💾 Имя сохранено локально: $registrationName")
                    }
                } else if (response.user?.fullName != null) {
                    // 🔥 ЕСЛИ ИМЯ УЖЕ ЕСТЬ В ОТВЕТЕ - СОХРАНЯЕМ ЕГО
                    tokenManager!!.saveUserName(response.user.fullName!!)
                    println("💾 Имя сохранено из ответа: ${response.user.fullName}")
                } else {
                    // 🔥 ЕСЛИ ИМЕНИ НЕТ ВООБЩЕ - СОХРАНЯЕМ ЛОКАЛЬНОЕ
                    tokenManager!!.saveUserName(registrationName)
                    println("💾 Имя сохранено из регистрации: $registrationName")
                }

                _events.send(VerificationEvent.NavigateToMain)

            } catch (e: Exception) {
                println("💥 Ошибка верификации: ${e.message}")
                errorMessage = e.message ?: "Неизвестная ошибка верификации"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    sealed class VerificationEvent {
        object NavigateToMain : VerificationEvent()
    }
}