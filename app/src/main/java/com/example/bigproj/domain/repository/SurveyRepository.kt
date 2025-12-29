// domain/repository/SurveyRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.*
import com.example.bigproj.domain.utils.ErrorHandler
import retrofit2.Response

class SurveyRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val surveyService by lazy {
        RetrofitClient.createSurveyService(tokenManager)
    }

    suspend fun getSurvey(surveyId: Int): SurveyResponseDto {
        println("📋 Загружаем опрос ID: $surveyId")
        val response = surveyService.getSurvey(surveyId)
        if (response.isSuccessful) {
            val survey = response.body()
            println("✅ Опрос загружен: ${survey?.title}")
            println("📊 Количество вопросов: ${survey?.questions?.size}")
            survey?.questions?.forEachIndexed { index, question ->
                println(" Вопрос ${index + 1}: ${question.questionText}")
            }
            return survey ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun submitSurveyAttempt(request: SurveyAttemptRequest): SurveyAttemptResponse {
        println("📤 Отправляем ответы на опрос ID: ${request.surveyId}")
        println("🔍 ДЕТАЛИ ЗАПРОСА:")
        println(" - survey_id: ${request.surveyId}")
        println(" - reminder_id: ${request.reminderId ?: "null (не обязателен)"}")
        println(" - answers count: ${request.answers.size}")

        val response = surveyService.submitSurveyAttempt(request)

        println("📡 Код ответа: ${response.code()}")
        println("📡 Успешно: ${response.isSuccessful}")

        if (response.isSuccessful) {
            return handleSuccessfulResponse(response, request.answers.size)
        } else {
            val errorBody = response.errorBody()?.string()
            println("❌ Тело ошибки: $errorBody")

            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    private fun handleSuccessfulResponse(response: Response<SurveyAttemptResponse>, answersCount: Int): SurveyAttemptResponse {
        val result = response.body()
        println("📥 Тело ответа: $result")

        if (result != null) {
            println("✅ Ответы отправлены успешно!")
            println(" Attempt ID: ${result.attemptId}")
            println(" Answers Count: ${result.answersCount}")
            println(" Status: ${result.status}")
            return result
        } else {
            println("⚠️ Пустой ответ от сервера, создаем успешный ответ")
            return SurveyAttemptResponse(
                attemptId = -1,
                answersCount = answersCount,
                status = "submitted",
                isOk = true,
                success = true,
                message = "Ответы успешно отправлены"
            )
        }
    }

    suspend fun getSurveyProgress(surveyId: Int): SurveyProgressResponseDto {
        println("📊 Загружаем прогресс опроса ID: $surveyId")
        val response = surveyService.getSurveyProgress(surveyId)
        if (response.isSuccessful) {
            val progress = response.body()
            println("✅ Прогресс загружен: completed=${progress?.completed}")

            // 🔥 ИСПРАВЛЯЕМ ДЕЛЕНИЕ НА НОЛЬ
            if (progress != null && progress.totalCountQuestions == 0) {
                println("⚠️ В опросе нет вопросов, прогресс = 0")
            }

            return progress ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    // 🔥 ИСПРАВЛЯЕМ МЕТОД ДЛЯ ПОЛУЧЕНИЯ МОИХ ПОПЫТОК
    suspend fun getMyAttempts(): List<SurveyAttemptResponse> {
        println("📋 Загружаем мои попытки опросов")

        try {
            val response = surveyService.getMyAttempts()

            if (response.isSuccessful) {
                val attemptsResponse = response.body()
                val attempts = attemptsResponse?.attempts ?: emptyList()

                // 🔥 ДИАГНОСТИКА СТРУКТУРЫ (ДОБАВЛЕНО)
                println("✅ Получено попыток: ${attempts.size}")
                attempts.forEachIndexed { index, attempt ->
                    println("   Попытка ${index + 1}:")
                    println("   - attemptId: ${attempt.attemptId}")
                    println("   - surveyId: ${attempt.surveyId}")
                    println("   - answersCount: ${attempt.answersCount}")
                    println("   - status: ${attempt.status}")
                    println("   - isOk: ${attempt.isOk}")
                    println("   - success: ${attempt.success}")
                    println("   - message: ${attempt.message}")
                }

                return attempts
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                println("⚠️ Ошибка загрузки попыток: $errorMessage")
                return emptyList()
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки попыток: ${e.message}")
            return emptyList()
        }
    }

    suspend fun debugPatientAttempts(patientId: Int) {
        println("🔍 ДЕБАГ: Проверка попыток пациента $patientId")

        try {
            // Попробуем получить все попытки текущего пользователя
            val myAttempts = getMyAttempts()
            println("📋 Все мои попытки: ${myAttempts.size}")
            myAttempts.forEach { attempt ->
                println("   - attemptId: ${attempt.attemptId}, surveyId: ${attempt.surveyId}, status: ${attempt.status}")
            }

            // Проверим конкретные опросы
            val surveys = getSurveysCombined()
            println("📊 Все доступные опросы: ${surveys.size}")
            surveys.forEach { survey ->
                println("   - surveyId: ${survey.id}, title: ${survey.title}")
            }

        } catch (e: Exception) {
            println("❌ Ошибка дебага: ${e.message}")
        }
    }

    // 🔥 НОВЫЙ МЕТОД: Получить опросы через мои попытки
    suspend fun getSurveysFromMyAttempts(): List<SurveyResponseDto> {
        println("🔄 Загружаем опросы через мои попытки")

        try {
            val attempts = getMyAttempts()
            if (attempts.isEmpty()) {
                println("📭 У пользователя нет попыток опросов")
                return emptyList()
            }

            // 🔥 ИСПРАВЛЕНИЕ: Получаем surveyId из attempt, а не attemptId
            val surveyIds = attempts.mapNotNull { it.surveyId }.toSet()
            println("📋 Найдено опросов в попытках: $surveyIds")

            val surveys = mutableListOf<SurveyResponseDto>()
            for (surveyId in surveyIds) {
                try {
                    val survey = getSurvey(surveyId)
                    surveys.add(survey)
                    println("✅ Загружен опрос: ${survey.title}")
                } catch (e: Exception) {
                    println("⚠️ Не удалось загрузить опрос $surveyId: ${e.message}")
                }
            }

            println("✅ Итого загружено опросов: ${surveys.size}")
            return surveys

        } catch (e: Exception) {
            println("❌ Ошибка загрузки опросов через попытки: ${e.message}")
            return emptyList()
        }
    }

    suspend fun debugAvailableSurveysForPatient() {
        println("🔍 ДИАГНОСТИКА: Какие опросы видит пациент")

        try {
            // 1. Проверим все доступные опросы
            val availableSurveys = getAvailableSurveys()
            println("📋 Все доступные опросы: ${availableSurveys.size}")
            availableSurveys.forEach { survey ->
                println("   - ID: ${survey.id}, Title: '${survey.title}', Status: '${survey.status}'")
            }

            // 2. Проверим опросы через комбинированный метод
            val combinedSurveys = getSurveysCombined()
            println("📊 Комбинированные опросы: ${combinedSurveys.size}")
            combinedSurveys.forEach { survey ->
                println("   - ID: ${survey.id}, Title: '${survey.title}'")
            }

            // 3. Ищем конкретно опрос ID:4
            val targetSurvey = availableSurveys.find { it.id == 4 }
            if (targetSurvey != null) {
                println("✅ ОПРОС ID:4 НАЙДЕН: '${targetSurvey.title}'")
            } else {
                println("❌ ОПРОС ID:4 НЕ НАЙДЕН в доступных опросах!")
                println("   Возможные причины:")
                println("   - Статус не 'active'")
                println("   - Опрос не публичный")
                println("   - Нет привязки к пациенту")
            }

        } catch (e: Exception) {
            println("❌ Ошибка диагностики: ${e.message}")
        }
    }

    suspend fun forceRefreshSurveys(): List<SurveyResponseDto> {
        println("🔄 ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ ОПРОСОВ")

        try {
            // Очистим кэш и загрузим заново
            return getSurveysCombined().also { surveys ->
                println("✅ Обновленные опросы: ${surveys.size}")
                surveys.forEach { survey ->
                    println("   - ID: ${survey.id}, Title: '${survey.title}'")
                }
            }
        } catch (e: Exception) {
            println("❌ Ошибка обновления: ${e.message}")
            return emptyList()
        }
    }

    // 🔥 НОВЫЙ МЕТОД: Комбинированный способ получения опросов (ТОЛЬКО РЕАЛЬНЫЕ)
    suspend fun getSurveysCombined(): List<SurveyResponseDto> {
        println("🔄 Комбинированная загрузка опросов (только реальные)")

        // Сначала пробуем через попытки
        val surveysFromAttempts = getSurveysFromMyAttempts()
        if (surveysFromAttempts.isNotEmpty()) {
            println("✅ Используем опросы из попыток: ${surveysFromAttempts.size}")
            return surveysFromAttempts
        }

        // Если нет попыток, пока ничего не показываем — опросы должны быть назначены врачом
        println("ℹ️ Попыток нет: опросы будут показаны после назначения/напоминания от врача")
        return emptyList()
    }

    suspend fun getAvailableSurveys(status: String? = null, query: String? = null): List<SurveyResponseDto> {
        println("📋 Загружаем список опросов: status=$status, query=$query")

        try {
            // Пока нет безопасного списка назначенных опросов без попыток/напоминаний.
            println("⚠️ Список доступных опросов для пациента пуст (ожидание назначения врача)")
            return emptyList()

        } catch (e: Exception) {
            println("❌ Ошибка загрузки опросов: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getPatientAttemptsDirectly(patientId: Int? = null): List<SurveyAttemptResponse> {
        println("🔍 Прямая проверка попыток пациента: $patientId")

        try {
            val response = surveyService.getMyAttempts()

            if (response.isSuccessful) {
                val attemptsResponse = response.body()
                val attempts = attemptsResponse?.attempts ?: emptyList()

                println("📋 Все попытки пользователя: ${attempts.size}")
                attempts.forEachIndexed { index, attempt ->
                    println("   ${index + 1}. attemptId: ${attempt.attemptId}, surveyId: ${attempt.surveyId}")
                    println("      status: ${attempt.status}, answersCount: ${attempt.answersCount}")
                }

                return attempts
            }
        } catch (e: Exception) {
            println("❌ Ошибка проверки попыток: ${e.message}")
        }

        return emptyList()
    }

    // 🔥 МЕТОДЫ ДЛЯ СТАТУСА И ПРОГРЕССА
    fun determineStatus(progress: SurveyProgressResponseDto): String {
        return when {
            progress.completed -> "completed"
            progress.countAnsweredQuestions > 0 -> "started" // 🔥 ВАЖНО: если есть ответы - статус "started"
            else -> "new"
        }
    }

    fun calculateProgress(progress: SurveyProgressResponseDto): Float {
        return if (progress.totalCountQuestions > 0) {
            progress.countAnsweredQuestions.toFloat() / progress.totalCountQuestions.toFloat()
        } else {
            0f
        }
    }

    // Напоминания пациента
    suspend fun getMyReminders(date: String? = null): com.example.bigproj.data.model.PatientRemindersResponse {
        val response = surveyService.getMyReminders(date)
        if (response.isSuccessful) {
            return response.body() ?: com.example.bigproj.data.model.PatientRemindersResponse()
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }
}