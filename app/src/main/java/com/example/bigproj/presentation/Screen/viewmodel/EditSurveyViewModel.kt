// presentation/Screen/viewmodel/EditSurveyViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.AddQuestionToSurveyRequestDto
import com.example.bigproj.data.model.SurveyManagementResponseDto
import com.example.bigproj.data.model.UpdateSurveyRequestDto
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.presentation.Screen.state.EditSurveyEvent
import com.example.bigproj.presentation.Screen.state.EditSurveyState
import kotlinx.coroutines.launch

class EditSurveyViewModel : ViewModel() {

    var state by mutableStateOf(EditSurveyState())
        private set

    private lateinit var repository: SurveyManagementRepository
    private var currentSurveyId: Int? = null

    fun setupDependencies(context: Context) {
        repository = SurveyManagementRepository(context)
    }

    fun setSurveyId(surveyId: Int) {
        currentSurveyId = surveyId
    }

    fun onEvent(event: EditSurveyEvent) {
        when (event) {
            EditSurveyEvent.LoadSurvey -> loadSurvey()
            is EditSurveyEvent.UpdateTitle -> {
                state = state.copy(title = event.title)
            }
            is EditSurveyEvent.UpdateDescription -> {
                state = state.copy(description = event.description)
            }
            is EditSurveyEvent.ChangeStatus -> {
                state = state.copy(status = event.status)
            }
            EditSurveyEvent.SaveSurvey -> saveSurvey()
            is EditSurveyEvent.RemoveQuestion -> removeQuestion(event.questionInSurveyId)
            is EditSurveyEvent.SwapQuestions -> swapQuestions(event.index1, event.index2)
            EditSurveyEvent.ShowAddQuestionDialog -> {
                state = state.copy(showAddQuestionDialog = true)
            }
            EditSurveyEvent.HideAddQuestionDialog -> {
                state = state.copy(showAddQuestionDialog = false)
            }
            is EditSurveyEvent.AddQuestionToSurvey -> addQuestionToSurvey(event.questionId)
        }
    }

    private fun loadSurvey() {
        val surveyId = currentSurveyId ?: return
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем опрос ID: $surveyId")
                val surveyWithQuestions = repository.getSurveyWithQuestions(surveyId)

                // ⚠️ ПРОВЕРЯЕМ, ЧТО survey НЕ NULL
                if (surveyWithQuestions.survey == null) {
                    // Если опрос не загружен, создаем временный
                    println("⚠️ Сервер вернул null survey, создаем временный объект")
                    val tempSurvey = SurveyManagementResponseDto(
                        id = surveyId,
                        creationDate = "",
                        title = state.title.ifEmpty { "Новый опрос" },
                        description = state.description,
                        status = state.status,
                        userId = 0 // временное значение
                    )

                    state = state.copy(
                        isLoading = false,
                        survey = tempSurvey,
                        questions = surveyWithQuestions.questions,
                        title = state.title.ifEmpty { "Новый опрос" },
                        description = state.description
                    )
                } else {
                    // Sort questions by order_index
                    val sortedQuestions = surveyWithQuestions.questions.sortedBy { it.orderIndex }

                    state = state.copy(
                        isLoading = false,
                        survey = surveyWithQuestions.survey,
                        questions = sortedQuestions,
                        title = surveyWithQuestions.survey.title,
                        description = surveyWithQuestions.survey.description ?: "",
                        status = surveyWithQuestions.survey.status
                    )
                    println("✅ Опрос загружен: ${surveyWithQuestions.survey.title}, вопросов: ${sortedQuestions.size}")
                }
            } catch (e: Exception) {
                println("❌ Ошибка загрузки опроса: ${e.message}")

                // ⚠️ ЕСЛИ ОПРОС СОЗДАН, НО ЕСТЬ ОШИБКА - ВСЕ РАВНО ПОКАЗЫВАЕМ ФОРМУ
                if (surveyId > 0) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Опрос загружен, но детали временно недоступны. Вы можете редактировать основные данные.",
                        title = state.title.ifEmpty { "Новый опрос" },
                        description = state.description
                    )
                    println("⚠️ Переходим в режим 'частичной загрузки' для опроса ID: $surveyId")
                } else {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки опроса: ${e.message}"
                    )
                }
            }
        }
    }

    private fun saveSurvey() {
        val surveyId = currentSurveyId ?: return
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = UpdateSurveyRequestDto(
                    title = state.title.takeIf { it != state.survey?.title },
                    description = state.description.takeIf { it != (state.survey?.description ?: "") },
                    status = state.status.takeIf { it != state.survey?.status }
                )

                val updated = repository.updateSurvey(surveyId, request)
                println("✅ Опрос обновлен: ${updated.id}")

                // Reload survey to get updated data
                loadSurvey()
                state = state.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                println("❌ Ошибка обновления опроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка обновления опроса: ${e.message}"
                )
            }
        }
    }

    private fun removeQuestion(questionInSurveyId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🗑️ Удаляем вопрос из опроса: questionInSurveyId=$questionInSurveyId")
                val updated = repository.removeQuestionFromSurvey(questionInSurveyId)

                // Reload survey
                loadSurvey()
                state = state.copy(isLoading = false)
                println("✅ Вопрос удален из опроса")
            } catch (e: Exception) {
                println("❌ Ошибка удаления вопроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка удаления вопроса: ${e.message}"
                )
            }
        }
    }

    private fun swapQuestions(index1: Int, index2: Int) {
        val surveyId = currentSurveyId ?: return
        if (index1 == index2 || index1 < 0 || index2 < 0 ||
            index1 >= state.questions.size || index2 >= state.questions.size) {
            return
        }

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val question1 = state.questions[index1]
                val question2 = state.questions[index2]

                println("🔄 Меняем местами вопросы: ${question1.orderIndex} <-> ${question2.orderIndex}")
                val updated = repository.swapQuestionsInSurvey(
                    surveyId = surveyId,
                    firstOrderIndex = question1.orderIndex,
                    secondOrderIndex = question2.orderIndex
                )

                // Reload survey
                loadSurvey()
                state = state.copy(isLoading = false)
                println("✅ Порядок вопросов изменен")
            } catch (e: Exception) {
                println("❌ Ошибка изменения порядка: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка изменения порядка: ${e.message}"
                )
            }
        }
    }

    private fun addQuestionToSurvey(questionId: Int) {
        val surveyId = currentSurveyId ?: return
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Add question to the end (order_index = questions.size)
                val orderIndex = state.questions.size

                println("➕ Добавляем вопрос $questionId в опрос $surveyId на позицию $orderIndex")

                // ИСПРАВЛЕНИЕ: Используем метод с отдельными параметрами, а не DTO
                val updated = repository.addQuestionToSurvey(
                    surveyId = surveyId,
                    questionId = questionId,
                    orderIndex = orderIndex
                )

                // Reload survey
                loadSurvey()
                state = state.copy(
                    isLoading = false,
                    showAddQuestionDialog = false,
                    isSuccess = true
                )
                println("✅ Вопрос добавлен в опрос")
            } catch (e: Exception) {
                println("❌ Ошибка добавления вопроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка добавления вопроса: ${e.message}"
                )
            }
        }
    }

    fun changeSurveyStatus(newStatus: String) {
        val surveyId = currentSurveyId ?: return
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Изменяем статус опроса $surveyId на $newStatus")
                val updated = repository.changeSurveyStatus(surveyId, newStatus)

                // Reload survey
                loadSurvey()
                state = state.copy(isLoading = false, status = newStatus)
                println("✅ Статус опроса изменен на $newStatus")
            } catch (e: Exception) {
                println("❌ Ошибка изменения статуса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка изменения статуса: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }

    fun resetSuccess() {
        state = state.copy(isSuccess = false)
    }
}