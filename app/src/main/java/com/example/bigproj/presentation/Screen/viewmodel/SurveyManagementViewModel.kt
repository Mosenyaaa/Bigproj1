package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.QuestionType
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.domain.repository.TokenManager
import com.example.bigproj.domain.repository.ValidationResult
import com.example.bigproj.presentation.Screen.state.QuestionUiModel
import com.example.bigproj.presentation.Screen.state.SurveyManagementEvent
import com.example.bigproj.presentation.Screen.state.SurveyManagementState
import kotlinx.coroutines.launch

class SurveyManagementViewModel : ViewModel() {

    var state by mutableStateOf(SurveyManagementState())
        private set

    private lateinit var repository: SurveyManagementRepository
    private lateinit var tokenManager: TokenManager

    fun setupDependencies(context: Context) {
        repository = SurveyManagementRepository(context)
        tokenManager = TokenManager(context)
    }

    fun onEvent(event: SurveyManagementEvent) {
        when (event) {
            is SurveyManagementEvent.UpdateSurveyTitle -> {
                state = state.copy(surveyTitle = event.title)
                validateSurvey()
            }

            is SurveyManagementEvent.UpdateSurveyDescription -> {
                state = state.copy(surveyDescription = event.description)
            }

            is SurveyManagementEvent.UpdateSurveyStatus -> {
                state = state.copy(surveyStatus = event.status)
            }

            is SurveyManagementEvent.AddNewQuestion -> {
                val newQuestion = QuestionUiModel(
                    id = state.questions.size,
                    text = "",
                    type = "text",
                    isRequired = true
                )
                state = state.copy(
                    questions = state.questions + newQuestion,
                    currentQuestionIndex = state.questions.size
                )
            }

            is SurveyManagementEvent.SelectQuestion -> {
                state = state.copy(currentQuestionIndex = event.index)
            }

            is SurveyManagementEvent.UpdateQuestionText -> {
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions[state.currentQuestionIndex] =
                    updatedQuestions[state.currentQuestionIndex].copy(text = event.text)
                state = state.copy(questions = updatedQuestions)
                validateQuestion(state.currentQuestionIndex)
            }

            is SurveyManagementEvent.UpdateQuestionVoiceFile -> {
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions[state.currentQuestionIndex] =
                    updatedQuestions[state.currentQuestionIndex].copy(voiceFilename = event.filename)
                state = state.copy(questions = updatedQuestions)
                validateQuestion(state.currentQuestionIndex)
                determineQuestionType(state.currentQuestionIndex)
            }

            is SurveyManagementEvent.UpdateQuestionImageFile -> {
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions[state.currentQuestionIndex] =
                    updatedQuestions[state.currentQuestionIndex].copy(pictureFilename = event.filename)
                state = state.copy(questions = updatedQuestions)
                validateQuestion(state.currentQuestionIndex)
                determineQuestionType(state.currentQuestionIndex)
            }

            is SurveyManagementEvent.AddAnswerOption -> {
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val updatedOptions = (currentQuestion.answerOptions ?: emptyList()) + event.option
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions[state.currentQuestionIndex] =
                    currentQuestion.copy(answerOptions = updatedOptions)
                state = state.copy(questions = updatedQuestions)
            }

            is SurveyManagementEvent.RemoveAnswerOption -> {
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val updatedOptions = currentQuestion.answerOptions?.toMutableList() ?: mutableListOf()
                if (event.index < updatedOptions.size) {
                    updatedOptions.removeAt(event.index)
                    val updatedQuestions = state.questions.toMutableList()
                    updatedQuestions[state.currentQuestionIndex] =
                        currentQuestion.copy(answerOptions = updatedOptions)
                    state = state.copy(questions = updatedQuestions)
                }
            }

            is SurveyManagementEvent.MoveQuestionUp -> {
                if (event.index > 0) {
                    val updatedQuestions = state.questions.toMutableList()
                    val temp = updatedQuestions[event.index]
                    updatedQuestions[event.index] = updatedQuestions[event.index - 1]
                    updatedQuestions[event.index - 1] = temp
                    state = state.copy(questions = updatedQuestions)
                }
            }

            is SurveyManagementEvent.MoveQuestionDown -> {
                if (event.index < state.questions.size - 1) {
                    val updatedQuestions = state.questions.toMutableList()
                    val temp = updatedQuestions[event.index]
                    updatedQuestions[event.index] = updatedQuestions[event.index + 1]
                    updatedQuestions[event.index + 1] = temp
                    state = state.copy(questions = updatedQuestions)
                }
            }

            is SurveyManagementEvent.DeleteQuestion -> {
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions.removeAt(event.index)
                state = state.copy(
                    questions = updatedQuestions,
                    currentQuestionIndex = if (updatedQuestions.isNotEmpty()) 0 else -1
                )
            }

            // Удаление голоса
            is SurveyManagementEvent.RemoveQuestionVoice -> {
                val updated = state.questions.toMutableList()
                updated[state.currentQuestionIndex] = updated[state.currentQuestionIndex].copy(
                    voiceFilename = null
                )
                updated[state.currentQuestionIndex] = updated[state.currentQuestionIndex].copy(
                    type = determineQuestionType(updated[state.currentQuestionIndex])
                )
                state = state.copy(questions = updated)
            }

            // Удаление фото
            is SurveyManagementEvent.RemoveQuestionImage -> {
                val updated = state.questions.toMutableList()
                updated[state.currentQuestionIndex] = updated[state.currentQuestionIndex].copy(
                    pictureFilename = null
                )
                updated[state.currentQuestionIndex] = updated[state.currentQuestionIndex].copy(
                    type = determineQuestionType(updated[state.currentQuestionIndex])
                )
                state = state.copy(questions = updated)
            }

            SurveyManagementEvent.SaveSurvey -> {
                saveSurvey()
            }

            SurveyManagementEvent.ResetState -> {
                state = SurveyManagementState()
            }
        }
    }

    private fun determineQuestionType(question: QuestionUiModel): String {
        val hasVoice = !question.voiceFilename.isNullOrBlank()
        val hasPicture = !question.pictureFilename.isNullOrBlank()

        return when {
            hasVoice && hasPicture -> "combined"
            hasVoice -> "voice"
            hasPicture -> "picture"
            else -> "text"
        }
    }

    private fun validateQuestion(questionIndex: Int) {
        val question = state.questions[questionIndex]
        val result = repository.validateQuestion(
            text = question.text,
            voiceFilename = question.voiceFilename,
            pictureFilename = question.pictureFilename,
            answerOptions = question.answerOptions
        )

        val updatedValidation = state.questionValidation.toMutableMap()
        updatedValidation[questionIndex] = result
        state = state.copy(questionValidation = updatedValidation)
    }

    private fun validateSurvey() {
        val errors = mutableListOf<String>()

        if (state.surveyTitle.isBlank()) {
            errors.add("Название опроса не может быть пустым")
        }

        if (state.questions.isEmpty()) {
            errors.add("Добавьте хотя бы один вопрос")
        }

        // Проверяем каждый вопрос
        state.questions.forEachIndexed { index, question ->
            val validation = state.questionValidation[index]
            if (validation is ValidationResult.Error) {
                errors.add("Вопрос ${index + 1}: ${validation.message}")
            }
        }

        state = state.copy(
            surveyValidationErrors = errors,
            isSurveyValid = errors.isEmpty()
        )
    }

    fun loadExistingSurvey(surveyId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Загружаем опрос с вопросами
                val surveyWithQuestions = repository.getSurveyWithQuestions(surveyId)

                // Преобразуем в UI модели
                val questions = surveyWithQuestions.questions.map { question ->
                    QuestionUiModel(
                        id = question.questionId,
                        text = question.questionText ?: "",
                        type = question.questionType,
                        voiceFilename = question.voiceFilename,
                        pictureFilename = question.pictureFilename,
                        answerOptions = emptyList(), // TODO: Загрузить варианты ответов если нужно
                        isRequired = true
                    )
                }

                state = state.copy(
                    isLoading = false,
                    surveyTitle = surveyWithQuestions.survey.title,
                    surveyDescription = surveyWithQuestions.survey.description ?: "",
                    surveyStatus = surveyWithQuestions.survey.status,
                    isPublic = surveyWithQuestions.survey.isPublic,
                    questions = questions,
                    currentQuestionIndex = if (questions.isNotEmpty()) 0 else -1,
                    isEditingExisting = true,
                    currentSurveyId = surveyId
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки опроса: ${e.message}"
                )
            }
        }
    }

    fun updateSurvey() {
        if (state.currentSurveyId == null) return

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Обновляем опрос
                val updateRequest = com.example.bigproj.data.model.UpdateSurveyRequestDto(
                    title = state.surveyTitle,
                    description = state.surveyDescription,
                    status = state.surveyStatus,
                    isPublic = state.isPublic
                )

                val updatedSurvey = repository.updateSurvey(state.currentSurveyId!!, updateRequest)

                // TODO: Обновить вопросы

                state = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    savedSurveyId = updatedSurvey.id
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка обновления: ${e.message}"
                )
            }
        }
    }

    private fun saveSurvey() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // 1. Создаем опрос
                val surveyRequest = com.example.bigproj.data.model.CreateSurveyRequestDto(
                    title = state.surveyTitle,
                    description = state.surveyDescription,
                    status = state.surveyStatus,
                    isPublic = state.isPublic
                )

                val survey = repository.createSurvey(surveyRequest)

                // 2. Создаем вопросы и привязываем их к опросу
                for ((index, question) in state.questions.withIndex()) {
                    val questionRequest = com.example.bigproj.data.model.CreateQuestionRequestDto(
                        text = question.text,
                        answerOptions = question.answerOptions,
                        voiceFilename = question.voiceFilename,
                        pictureFilename = question.pictureFilename,
                        isPublic = true
                    )

                    val createdQuestion = repository.createQuestion(questionRequest)

                    // Привязываем вопрос к опросу
                    val addToSurveyRequest = com.example.bigproj.data.model.AddQuestionToSurveyRequestDto(
                        questionId = createdQuestion.id,
                        orderIndex = index
                    )

                    repository.addQuestionToSurvey(addToSurveyRequest)
                }

                state = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    savedSurveyId = survey.id
                )

            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }



    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}