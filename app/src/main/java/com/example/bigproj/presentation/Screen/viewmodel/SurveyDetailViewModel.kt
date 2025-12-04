// presentation/Screen/viewmodel/SurveyDetailViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.SurveyRepository
import com.example.bigproj.presentation.Screen.state.SurveyScreenEvent
import com.example.bigproj.presentation.Screen.state.SurveyScreenState
import com.example.bigproj.presentation.Screen.state.SurveyUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SurveyDetailViewModel : ViewModel() {
    var state by mutableStateOf(SurveyScreenState())
        private set

    private lateinit var surveyRepository: SurveyRepository
    var currentSurveyData: com.example.bigproj.data.model.SurveyResponseDto? = null
        private set

    var currentQuestionIndex by mutableStateOf(0)
        private set

    var userAnswers by mutableStateOf<Map<Int, String>>(emptyMap())
        private set

    // 🔥 ДОБАВЛЯЕМ ФЛАГ УСПЕШНОЙ ОТПРАВКИ
    var isSurveySubmitted by mutableStateOf(false)
        private set

    private var _realQuestionsList by mutableStateOf<List<com.example.bigproj.data.model.QuestionDto>>(emptyList())

    fun getRealQuestions(): List<com.example.bigproj.data.model.QuestionDto> = _realQuestionsList

    fun getCurrentQuestion(): com.example.bigproj.data.model.QuestionDto? {
        return if (_realQuestionsList.isNotEmpty() && currentQuestionIndex < _realQuestionsList.size) {
            _realQuestionsList[currentQuestionIndex]
        } else {
            null
        }
    }

    fun getProgress(): Float {
        return if (_realQuestionsList.isNotEmpty()) {
            currentQuestionIndex.toFloat() / _realQuestionsList.size.toFloat()
        } else {
            0f
        }
    }

    fun setupDependencies(context: Context) {
        surveyRepository = SurveyRepository(context)
    }

    fun loadSurvey(surveyId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Начинаем загрузку опроса ID: $surveyId")

                // 🔥 ПАРАЛЛЕЛЬНАЯ ЗАГРУЗКА ОПРОСА И ПРОГРЕССА
                val surveyDeferred = async { surveyRepository.getSurvey(surveyId) }
                val progressDeferred = async {
                    try {
                        surveyRepository.getSurveyProgress(surveyId)
                    } catch (e: Exception) {
                        println("⚠️ Не удалось загрузить прогресс: ${e.message}")
                        null
                    }
                }

                val survey = surveyDeferred.await()
                val progress = progressDeferred.await()

                println("✅ Реальный опрос загружен: ${survey.title}")
                println("📊 Количество реальных вопросов: ${survey.questions.size}")

                currentSurveyData = survey
                _realQuestionsList = survey.questions

                // Восстанавливаем ответы из прогресса если он есть
                val restoredAnswers = mutableMapOf<Int, String>()
                progress?.answeredQuestions?.forEach { answeredQuestion ->
                    restoredAnswers[answeredQuestion.questionInSurveyId] = answeredQuestion.answerText
                }
                userAnswers = restoredAnswers

                val uiModel = SurveyUiModel(
                    id = survey.id,
                    title = survey.title,
                    description = survey.description,
                    status = progress?.let { surveyRepository.determineStatus(it) } ?: "new",
                    progress = progress?.let { surveyRepository.calculateProgress(it) } ?: 0f
                )

                state = state.copy(
                    isLoading = false,
                    currentSurvey = uiModel
                )

                println("🎯 Готово: реальный опрос '${survey.title}' с ${survey.questions.size} вопросами")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки реального опроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки опроса: ${e.message}"
                )
            }
        }
    }

    fun goToNextQuestion() {
        if (currentQuestionIndex < _realQuestionsList.size - 1) {
            currentQuestionIndex++
        }
    }

    fun goToPreviousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
        }
    }

    fun saveAnswer(answer: String) {
        val currentQuestion = getCurrentQuestion()
        currentQuestion?.let { question ->
            val newAnswers = userAnswers.toMutableMap()
            newAnswers[question.questionInSurveyId] = answer
            userAnswers = newAnswers
            println("💾 Сохранен ответ на вопрос ${question.questionInSurveyId}: $answer")
        }
    }

    fun isLastQuestion(): Boolean {
        return currentQuestionIndex == _realQuestionsList.size - 1
    }

    fun isFirstQuestion(): Boolean {
        return currentQuestionIndex == 0
    }

    // 🔥 ПУБЛИЧНЫЙ МЕТОД ДЛЯ ОТПРАВКИ
    fun submitSurvey() {
        println("📤 Начинаем отправку реального опроса...")
        state = state.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val surveyId = state.currentSurvey?.id ?: throw Exception("Опрос не загружен")
                val answers = prepareAnswersForRealSurvey()

                if (answers.isEmpty()) {
                    throw Exception("Нет ответов для отправки")
                }

                println("📝 Подготовлено ответов для реальных вопросов: ${answers.size}")

                // 🔥 СОЗДАЕМ ЗАПРОС С REMINDER_ID = 0
                val request = com.example.bigproj.data.model.SurveyAttemptRequest(
                    surveyId = surveyId,
                    reminderId = 0, // 🔥 ОТПРАВЛЯЕМ 0 ВМЕСТО NULL
                    answers = answers
                )

                println("📦 Отправляемый запрос:")
                println(" - survey_id: $surveyId")
                println(" - reminder_id: 0")
                println(" - answers: ${answers.size}")

                val result = surveyRepository.submitSurveyAttempt(request)
                state = state.copy(isLoading = false)

                println("🎉 Ответы отправлены! Attempt ID: ${result.attemptId}")

                // 🔥 УСТАНАВЛИВАЕМ ФЛАГ УСПЕШНОЙ ОТПРАВКИ
                isSurveySubmitted = true

                state = state.copy(
                    errorMessage = "✅ Опрос успешно отправлен врачу!"
                )

            } catch (e: Exception) {
                println("❌ Ошибка отправки реального опроса: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "❌ Ошибка отправки: ${e.message}"
                )
            }
        }
    }

    private fun prepareAnswersForRealSurvey(): List<com.example.bigproj.data.model.SurveyAnswerRequestDto> {
        val answers = mutableListOf<com.example.bigproj.data.model.SurveyAnswerRequestDto>()

        userAnswers.forEach { (questionId, answerText) ->
            if (answerText.isNotBlank()) {
                answers.add(
                    com.example.bigproj.data.model.SurveyAnswerRequestDto(
                        questionInSurveyId = questionId,
                        text = answerText
                    )
                )
            }
        }

        println("📝 Подготовлено ответов: ${answers.size}")
        answers.forEachIndexed { index, answer ->
            println(" ${index + 1}. question_in_survey_id: ${answer.questionInSurveyId}, text: '${answer.text}'")
        }

        return answers
    }

    fun onEvent(event: SurveyScreenEvent) {
        when (event) {
            is SurveyScreenEvent.AnswerQuestion -> {
                saveAnswer(event.answer)
                // 🔥 УБИРАЕМ АВТОМАТИЧЕСКИЙ ПЕРЕХОД НА СЛЕДУЮЩИЙ ВОПРОС
                // Теперь переход только по кнопке "Далее"
            }
            is SurveyScreenEvent.NavigateToNextStep -> {
                if (!isLastQuestion()) {
                    goToNextQuestion()
                }
            }
            is SurveyScreenEvent.NavigateToPreviousStep -> goToPreviousQuestion()
            is SurveyScreenEvent.SubmitSurvey -> submitSurvey()
            else -> {}
        }
    }

    // Остальные методы остаются без изменений
    private fun selectSymptom(symptom: String) {
        val currentSymptoms = state.selectedSymptoms.toMutableSet()
        if (currentSymptoms.contains(symptom)) {
            currentSymptoms.remove(symptom)
        } else {
            currentSymptoms.add(symptom)
        }
        state = state.copy(selectedSymptoms = currentSymptoms)
    }

    private fun setWellBeingRating(rating: Int) {
        state = state.copy(wellBeingRating = rating)
    }
}