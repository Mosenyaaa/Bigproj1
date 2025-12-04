// presentation/Screen/main/MainScreenViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.TokenManager
import com.example.bigproj.domain.repository.UserRepository
import com.example.bigproj.presentation.Screen.state.MainScreenEvent
import com.example.bigproj.presentation.Screen.state.MainScreenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {
    var state by mutableStateOf(MainScreenState())
        private set

    private lateinit var userRepository: UserRepository
    private lateinit var tokenManager: TokenManager

    fun setupDependencies(context: Context) {
        tokenManager = TokenManager(context)
        userRepository = UserRepository(context)

        // 🔥 ЗАГРУЖАЕМ ЛОКАЛЬНЫЕ ДАННЫЕ ПРИ ИНИЦИАЛИЗАЦИИ
        val localName = tokenManager.getUserName()
        val localEmail = tokenManager.getUserEmail()

        if (!localName.isNullOrBlank()) {
            state = state.copy(userName = localName)
            println("📝 Локальное имя загружено: $localName")
        }

        if (!localEmail.isNullOrBlank()) {
            state = state.copy(userEmail = localEmail)
            println("📧 Локальный email загружен: $localEmail")
        }
    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.LoadUserData -> loadUserData()
            is MainScreenEvent.UpdateFullName -> updateFullName(event.newName)
            is MainScreenEvent.EnableDoctorFeatures -> enableDoctorFeatures(event.accessKey)
            is MainScreenEvent.Logout -> logout()
            is MainScreenEvent.ShowEditDialog -> showEditDialog()
            is MainScreenEvent.HideEditDialog -> hideEditDialog()
            is MainScreenEvent.ShowDoctorDialog -> showDoctorDialog()
            is MainScreenEvent.HideDoctorDialog -> hideDoctorDialog()
            is MainScreenEvent.ShowEmailDialog -> showEmailDialog()
            is MainScreenEvent.HideEmailDialog -> hideEmailDialog()
            is MainScreenEvent.UpdateEmail -> updateEmail(event.newEmail)
            is MainScreenEvent.VerifyEmailCode -> verifyEmailCode(event.code)
            is MainScreenEvent.ShowLogoutDialog -> showLogoutDialog()
            is MainScreenEvent.HideLogoutDialog -> hideLogoutDialog()
            is MainScreenEvent.ConfirmLogout -> confirmLogout()
        }
    }

    private fun loadUserData() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                println("👤 Данные пользователя: ${user.fullName}, врач: ${user.isDoctor}")

                state = state.copy(
                    isLoading = false,
                    userEmail = user.email ?: "Не указан",
                    userName = user.fullName ?: state.userName,
                    isDoctor = user.isDoctor
                )

                // 🔥 ДИАГНОСТИКА ДЛЯ ВРАЧА
                if (user.isDoctor) {
                    println("🎯 ПОЛЬЗОВАТЕЛЬ - ВРАЧ, должен видеть пациентов")
                    println("📧 Email врача: ${user.email}")
                    println("👤 Имя врача: ${user.fullName}")
                } else {
                    println("🎯 ПОЛЬЗОВАТЕЛЬ - ПАЦИЕНТ, не должен видеть пациентов")
                    println("📧 Email пациента: ${user.email}")
                    println("👤 Имя пациента: ${user.fullName}")
                }

            } catch (e: Exception) {
                println("❌ Ошибка загрузки пользователя: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки: ${e.message}"
                )
            }
        }
    }

    private fun updateFullName(newName: String) {
        if (newName.isBlank()) return

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Обновляем имя на: $newName")
                val user = userRepository.updateFullName(newName)

                // Сохраняем имя локально
                tokenManager.saveUserName(newName)
                println("💾 Имя сохранено локально: $newName")

                state = state.copy(
                    isLoading = false,
                    userName = user.fullName ?: newName,
                    showEditDialog = false
                )
                println("✅ Имя успешно обновлено: ${user.fullName}")

                // 🔥 ПРОВЕРЯЕМ ЧЕРЕЗ 2 СЕКУНДЫ
                launch {
                    delay(2000)
                    val currentName = tokenManager.getUserName()
                    println("🔍 Проверка через 2 секунды - текущее имя: $currentName")
                }

            } catch (e: Exception) {
                println("❌ Ошибка обновления имени: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("405") == true -> "Ошибка метода запроса - используйте PUT вместо GET"
                    e.message?.contains("400") == true -> "Неверный формат данных"
                    e.message?.contains("500") == true -> "Ошибка сервера"
                    else -> "Ошибка обновления: ${e.message}"
                }
                state = state.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }

    private fun enableDoctorFeatures(accessKey: String) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🎯 enableDoctorFeatures вызван с accessKey: '$accessKey'")

                if (accessKey.isNotBlank()) {
                    val user = userRepository.enableDoctorFeatures(accessKey)

                    // Если is_doctor есть в ответе - используем его, если нет - считаем что пользователь стал врачом
                    val becameDoctor = user.isDoctor ?: true

                    state = state.copy(
                        isLoading = false,
                        isDoctor = becameDoctor,
                        showDoctorDialog = false
                    )
                    println("🎯 Пользователь стал врачом: $becameDoctor")

                    // 🔥 ПРОВЕРЯЕМ ЧЕРЕЗ 2 СЕКУНДЫ
                    launch {
                        delay(2000)
                        println("🔍 Проверка роли через 2 секунды - текущая роль: ${state.isDoctor}")
                    }
                } else {
                    // Логика переключения на пациента
                    state = state.copy(
                        isLoading = false,
                        isDoctor = false,
                        showDoctorDialog = false
                    )
                    println("🎯 Пользователь стал пациентом")
                }
            } catch (e: Exception) {
                println("❌ Ошибка смены роли: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка смены роли: ${e.message}"
                )
            }
        }
    }

    private fun updateEmail(newEmail: String) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("📧 Отправляем код смены email на: $newEmail")
                userRepository.sendResetEmailCode(newEmail)
                state = state.copy(
                    isLoading = false,
                    tempNewEmail = newEmail,
                    showEmailDialog = false,
                    showEmailVerificationDialog = true
                )
                println("✅ Код отправлен на новый email")
            } catch (e: Exception) {
                println("❌ Ошибка отправки кода: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка отправки кода: ${e.message}"
                )
            }
        }
    }

    private fun verifyEmailCode(code: String) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔐 Подтверждаем смену email с кодом: $code")
                val user = userRepository.resetEmail(code)

                // 🔥 СОХРАНЯЕМ НОВЫЙ EMAIL ЛОКАЛЬНО
                tokenManager.saveUserEmail(user.email ?: "") // 🔥 ИСПРАВЛЕНИЕ

                state = state.copy(
                    isLoading = false,
                    userEmail = user.email ?: state.userEmail, // 🔥 ИСПРАВЛЕНИЕ
                    showEmailVerificationDialog = false,
                    tempNewEmail = ""
                )
                println("✅ Email успешно изменен на: ${user.email}")
            } catch (e: Exception) {
                println("❌ Ошибка смены email: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка смены email: ${e.message}"
                )
            }
        }
    }

    private fun logout() {
        tokenManager.clearUserToken()
    }

    private fun showEditDialog() {
        state = state.copy(showEditDialog = true)
    }

    private fun hideEditDialog() {
        state = state.copy(showEditDialog = false)
    }

    private fun showDoctorDialog() {
        state = state.copy(showDoctorDialog = true)
    }

    private fun hideDoctorDialog() {
        state = state.copy(showDoctorDialog = false)
    }

    private fun showEmailDialog() {
        state = state.copy(showEmailDialog = true)
    }

    private fun hideEmailDialog() {
        state = state.copy(showEmailDialog = false, tempNewEmail = "")
    }

    // 🔥 МЕТОДЫ ДЛЯ ДИАЛОГА ВЫХОДА
    private fun showLogoutDialog() {
        state = state.copy(showLogoutDialog = true)
    }

    private fun hideLogoutDialog() {
        state = state.copy(showLogoutDialog = false)
    }

    private fun confirmLogout() {
        logout()
        state = state.copy(showLogoutDialog = false)
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}