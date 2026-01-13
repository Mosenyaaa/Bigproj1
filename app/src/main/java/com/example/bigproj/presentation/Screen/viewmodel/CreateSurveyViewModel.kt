// presentation/Screen/viewmodel/CreateSurveyViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.CreateSurveyRequestDto
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.presentation.Screen.state.CreateSurveyEvent
import com.example.bigproj.presentation.Screen.state.CreateSurveyState
import kotlinx.coroutines.launch

class CreateSurveyViewModel : ViewModel() {

    var state by mutableStateOf(CreateSurveyState())
        private set

    private lateinit var repository: SurveyManagementRepository

    fun setupDependencies(context: Context) {
        repository = SurveyManagementRepository(context)
    }

    fun onEvent(event: CreateSurveyEvent) {
        when (event) {
            is CreateSurveyEvent.TitleChanged -> {
                state = state.copy(title = event.title)
            }
            is CreateSurveyEvent.DescriptionChanged -> {
                state = state.copy(description = event.description)
            }
            CreateSurveyEvent.CreateSurvey -> createSurvey()
        }
    }

    private fun createSurvey() {
        if (state.title.isBlank()) {
            state = state.copy(errorMessage = "Название опроса обязательно")
            return
        }

        state = state.copy(isLoading = true, errorMessage = null, isSuccess = false)

        viewModelScope.launch {
            try {
                println("📦 Создаем новый опрос: title='${state.title}'")
                
                val request = CreateSurveyRequestDto(
                    title = state.title,
                    description = state.description.takeIf { it.isNotBlank() },
                    status = "draft",
                    isPublic = false
                )

                val created = repository.createSurvey(request)
                println("✅ Опрос создан: ID=${created.id}")

                state = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    createdSurveyId = created.id
                )
            } catch (e: Exception) {
                println("❌ Ошибка создания опроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка создания опроса: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }

    fun resetSuccess() {
        state = state.copy(isSuccess = false, createdSurveyId = null)
    }
}
