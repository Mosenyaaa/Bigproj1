// presentation/Screen/viewmodel/ConstructorViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.presentation.Screen.state.ConstructorEvent
import com.example.bigproj.presentation.Screen.state.ConstructorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConstructorViewModel : ViewModel() {

    var state by mutableStateOf(ConstructorState())
        private set

    private lateinit var repository: SurveyManagementRepository

    fun setupDependencies(context: Context) {
        repository = SurveyManagementRepository(context)
    }

    fun onEvent(event: ConstructorEvent) {
        when (event) {
            is ConstructorEvent.LoadQuestions -> loadQuestions()
            is ConstructorEvent.SearchQuestions -> searchQuestions(event.query)
            is ConstructorEvent.DeleteQuestion -> deleteQuestion(event.questionId)
        }
    }

    private fun loadQuestions() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем доступные вопросы...")
                val questions = repository.getAvailableQuestions()
                
                state = state.copy(
                    isLoading = false,
                    questions = questions,
                    filteredQuestions = if (state.searchQuery.isBlank()) {
                        questions
                    } else {
                        filterQuestions(questions, state.searchQuery)
                    }
                )
                println("✅ Успешно загружено вопросов: ${questions.size}")
            } catch (e: Exception) {
                println("❌ Ошибка загрузки вопросов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки вопросов: ${e.message}"
                )
            }
        }
    }

    private fun searchQuestions(query: String) {
        state = state.copy(searchQuery = query)
        
        val filtered = if (query.isBlank()) {
            state.questions
        } else {
            filterQuestions(state.questions, query)
        }
        
        state = state.copy(filteredQuestions = filtered)
    }

    private fun filterQuestions(questions: List<com.example.bigproj.data.api.QuestionResponseDto>, query: String): List<com.example.bigproj.data.api.QuestionResponseDto> {
        val lowerQuery = query.lowercase()
        return questions.filter { question ->
            question.text?.lowercase()?.contains(lowerQuery) == true ||
                    (question.extraData?.values?.any { value ->
                        value.lowercase().contains(lowerQuery)
                    } == true)
        }
    }

    private fun deleteQuestion(questionId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🗑️ Удаляем вопрос ID: $questionId")
                repository.deleteQuestion(questionId)

                // Даем время серверу обработать
                delay(300)

                // ПРЯМОЕ ОБНОВЛЕНИЕ СПИСКА без повторной загрузки
                val updatedQuestions = state.questions.filter { it.id != questionId }
                val updatedFiltered = state.filteredQuestions.filter { it.id != questionId }

                state = state.copy(
                    isLoading = false,
                    questions = updatedQuestions,
                    filteredQuestions = updatedFiltered
                )

                println("✅ Вопрос ID:$questionId удален из UI")

            } catch (e: Exception) {
                println("❌ Ошибка удаления вопроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка удаления вопроса: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
