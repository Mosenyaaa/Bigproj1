package com.example.bigproj.presentation.Screen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.AuthRepository
import com.example.bigproj.presentation.Screen.state.RegisterScreenEvent
import com.example.bigproj.presentation.Screen.state.RegisterScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterScreenViewModel : ViewModel() {
    var state by mutableStateOf(RegisterScreenState())
        private set

    private val authRepository = AuthRepository()

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun sendCodeOnEmail() {
        viewModelScope.launch {
            try {
                // Сначала отправляем имя на сервер, если оно указано
                if (state.name.isNotBlank()) {
                    // Здесь нужно добавить вызов для сохранения имени
                    // Пока просто логируем
                    println("📝 Сохраняем имя: ${state.name}")
                }

                authRepository.sendCodeOnEmail(email = state.email)
                _events.send(AuthEvent.NavigateToVerification)
            } catch (e: Exception) {
                println("❌ Ошибка отправки кода: ${e.message}")
            }
        }
    }

    fun onEvent(event: RegisterScreenEvent) {
        when (event) {
            is RegisterScreenEvent.EmailUpdated -> this.state = state.copy(email = event.newEmail)
            is RegisterScreenEvent.NameUpdated -> this.state = state.copy(name = event.newName)
        }
    }
}