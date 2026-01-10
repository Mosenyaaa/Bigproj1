// presentation/Screen/viewmodel/SurveyManagementViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions.add(newQuestion)

                state = state.copy(
                    questions = updatedQuestions,
                    currentQuestionIndex = updatedQuestions.size - 1
                )
                // Автоматически вызываем навигацию к редактированию этого вопроса
                // (это должно обрабатываться в UI)
                validateSurvey()
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
                val updatedOptions = currentQuestion.answerOptions + event.option
                val updatedQuestions = state.questions.toMutableList()
                updatedQuestions[state.currentQuestionIndex] =
                    currentQuestion.copy(answerOptions = updatedOptions)
                state = state.copy(questions = updatedQuestions)
            }
            is SurveyManagementEvent.RemoveAnswerOption -> {
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val updatedOptions = currentQuestion.answerOptions.toMutableList()
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
                validateSurvey()
            }
            SurveyManagementEvent.RecordVoice -> {
                // Обработка записи голоса будет в UI
            }
            SurveyManagementEvent.PickImage -> {
                // Обработка выбора изображения будет в UI
            }
            SurveyManagementEvent.RemoveQuestionVoice -> {
                if (state.currentQuestionIndex in state.questions.indices) {
                    val updatedQuestions = state.questions.toMutableList()
                    updatedQuestions[state.currentQuestionIndex] =
                        updatedQuestions[state.currentQuestionIndex].copy(voiceFilename = null)
                    state = state.copy(questions = updatedQuestions)
                    determineQuestionType(state.currentQuestionIndex)
                }
            }
            SurveyManagementEvent.RemoveQuestionImage -> {
                if (state.currentQuestionIndex in state.questions.indices) {
                    val updatedQuestions = state.questions.toMutableList()
                    updatedQuestions[state.currentQuestionIndex] =
                        updatedQuestions[state.currentQuestionIndex].copy(pictureFilename = null)
                    state = state.copy(questions = updatedQuestions)
                    determineQuestionType(state.currentQuestionIndex)
                }
            }
            SurveyManagementEvent.SaveSurvey -> {
                saveSurvey()
            }
            SurveyManagementEvent.ResetState -> {
                state = SurveyManagementState()
            }
        }
    }

    private fun determineQuestionType(questionIndex: Int) {
        if (questionIndex !in state.questions.indices) return

        val question = state.questions[questionIndex]
        val hasVoice = !question.voiceFilename.isNullOrBlank()
        val hasPicture = !question.pictureFilename.isNullOrBlank()
        val hasText = question.text.isNotBlank()

        val determinedType = when {
            hasVoice && hasPicture -> "combined"
            hasVoice && !hasPicture -> "voice"
            hasPicture && !hasVoice -> "picture"
            else -> "text"
        }

        val updatedQuestions = state.questions.toMutableList()
        updatedQuestions[questionIndex] = question.copy(type = determinedType)
        state = state.copy(questions = updatedQuestions)
    }

    private fun validateQuestion(questionIndex: Int) {
        if (questionIndex !in state.questions.indices) return

        val question = state.questions[questionIndex]
        val result = validateQuestionStructure(question)

        val updatedValidation = state.questionValidation.toMutableMap()
        updatedValidation[questionIndex] = result
        state = state.copy(questionValidation = updatedValidation)
    }

    private fun validateQuestionStructure(question: QuestionUiModel): ValidationResult {
        val hasVoice = !question.voiceFilename.isNullOrBlank()
        val hasPicture = !question.pictureFilename.isNullOrBlank()
        val hasText = question.text.isNotBlank()

        return when {
            // Текстовый вопрос: должен быть текст
            !hasVoice && !hasPicture && !hasText ->
                ValidationResult.Error("Текстовый вопрос должен содержать текст")

            // Голосовой вопрос: должен быть голос
            hasVoice && !hasPicture && !hasText ->
                ValidationResult.Success // Допустимо только голос

            // Вопрос с изображением: должно быть изображение
            !hasVoice && hasPicture && !hasText ->
                ValidationResult.Success // Допустимо только изображение

            // Комбинированный: должен быть голос И изображение
            hasVoice && hasPicture ->
                ValidationResult.Success

            // Все остальные случаи - успех
            else -> ValidationResult.Success
        }
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
                        answerOptions = emptyList(),
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
                val updateRequest =
                    com.example.bigproj.data.model.UpdateSurveyRequestDto(
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
                val survey = repository.createSurvey(
                    com.example.bigproj.data.model.CreateSurveyRequestDto(
                        title = state.surveyTitle,
                        description = state.surveyDescription,
                        status = state.surveyStatus,
                        isPublic = state.isPublic
                    )
                )

                println("✅ Опрос создан: ID=${survey.id}, Title='${survey.title}'")

                // 2. Создаем вопросы и привязываем их к опросу
                for ((index, question) in state.questions.withIndex()) {
                    try {
                        println("📝 Создаем вопрос ${index + 1}/${state.questions.size}")

                        // Определяем тип вопроса на основе его свойств
                        val hasVoice = !question.voiceFilename.isNullOrBlank()
                        val hasPicture = !question.pictureFilename.isNullOrBlank()

                        println("   Текст: '${question.text}'")
                        println("   Голос: ${question.voiceFilename}")
                        println("   Изображение: ${question.pictureFilename}")
                        println("   Варианты ответов: ${question.answerOptions?.size ?: 0}")

                        // Создаем запрос на создание вопроса
                        val questionRequest =
                            com.example.bigproj.data.model.CreateQuestionRequestDto(
                                text = question.text.takeIf { it.isNotBlank() },
                                answerOptions = question.answerOptions,
                                voiceFilename = question.voiceFilename,
                                pictureFilename = question.pictureFilename,
                                isPublic = true
                            )

                        println("   Отправляем запрос на создание вопроса...")
                        val createdQuestion = repository.addQuestion(questionRequest)
                        println("   ✅ Вопрос создан: ID=${createdQuestion.id}")

                        // Проверяем, что ID вопроса получен
                        if (createdQuestion.id > 0) {
                            // Привязываем вопрос к опросу
                            val addToSurveyRequest =
                                com.example.bigproj.data.model.AddQuestionToSurveyRequestDto(
                                    surveyId = survey.id,
                                    questionId = createdQuestion.id,
                                    orderIndex = index
                                )

                            println("   Привязываем вопрос ${createdQuestion.id} к опросу ${survey.id}...")
                            val updatedSurvey = repository.addQuestionToSurvey(addToSurveyRequest)
                            println("   ✅ Вопрос привязан. Вопросов в опросе: ${updatedSurvey.questions.size}")
                        } else {
                            println("❌ Ошибка: не получен ID созданного вопроса")
                            throw Exception("Не удалось создать вопрос: не получен ID")
                        }
                    } catch (e: Exception) {
                        println("❌ Ошибка при создании вопроса ${index + 1}: ${e.message}")
                        throw Exception("Ошибка при создании вопроса ${index + 1}: ${e.message}", e)
                    }
                }

                println("🎉 Опрос успешно создан со всеми вопросами")
                state = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    savedSurveyId = survey.id
                )

            } catch (e: Exception) {
                println("❌ Ошибка сохранения опроса: ${e.message}")
                println("❌ Stack trace: ${e.stackTraceToString()}")
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