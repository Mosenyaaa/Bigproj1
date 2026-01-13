// domain/repository/SurveyManagementRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.data.api.SurveyWithQuestionsDto
import com.example.bigproj.data.model.*
import com.example.bigproj.domain.utils.ErrorHandler
import kotlinx.serialization.json.Json

class SurveyManagementRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val surveyManagementService by lazy {
        RetrofitClient.createSurveyManagementService(tokenManager)
    }

    // SharedPreferences для хранения удаленных вопросов
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("deleted_questions", Context.MODE_PRIVATE)
    }

    // Опросы
    suspend fun createSurvey(request: CreateSurveyRequestDto): SurveyManagementResponseDto {
        val response = surveyManagementService.createSurvey(
            title = request.title,
            description = request.description,
            status = request.status,
            isPublic = request.isPublic,
            body = request
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun updateSurvey(surveyId: Int, request: UpdateSurveyRequestDto): SurveyManagementResponseDto {
        val response = surveyManagementService.updateSurvey(surveyId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun deleteSurvey(surveyId: Int) {
        val response = surveyManagementService.deleteSurvey(surveyId)
        if (!response.isSuccessful) {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    // Вопросы
    suspend fun getAvailableQuestions(
        query: String? = null,
        start: Int? = null,
        finish: Int? = null,
        limit: Int? = null
    ): List<QuestionResponseDto> {
        println("🔄 Загружаем доступные вопросы...")
        val response = surveyManagementService.getAvailableQuestions(query, start, finish, limit)

        if (response.isSuccessful) {
            val allQuestions = response.body() ?: emptyList()

            // Получаем ID локально удаленных вопросов
            val deletedIds = getDeletedQuestionIds()

            // Фильтруем вопросы - убираем те, что удалены локально
            val activeQuestions = allQuestions.filter { question ->
                !deletedIds.contains(question.id)
            }

            println("📊 ФИЛЬТРАЦИЯ ВОПРОСОВ:")
            println("   Всего от сервера: ${allQuestions.size}")
            println("   Локально удалено: ${deletedIds.size}")
            println("   Показываем: ${activeQuestions.size}")

            if (deletedIds.isNotEmpty()) {
                println("   ID удаленных вопросов: $deletedIds")
            }

            return activeQuestions
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ Ошибка загрузки вопросов: $errorMessage")
            throw Exception(errorMessage)
        }
    }

    suspend fun getQuestion(questionId: Int): QuestionResponseDto {
        val response = surveyManagementService.getQuestion(questionId)
        if (response.isSuccessful) {
            val wrapper = response.body()
            val question = wrapper?.question
            return question ?: throw Exception("Вопрос не найден")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun addQuestion(request: CreateQuestionRequestDto): QuestionResponseDto {
        // Преобразуем список в JSON строку
        val answerOptionsJson = if (request.answerOptions != null && request.answerOptions.isNotEmpty()) {
            Json.encodeToString(request.answerOptions)
        } else {
            null
        }

        val response = surveyManagementService.addQuestion(
            text = request.text,
            isPublic = request.isPublic,
            answerOptions = answerOptionsJson,
            voiceFilename = request.voiceFilename,
            pictureFilename = request.pictureFilename
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun updateQuestion(questionId: Int, request: UpdateQuestionRequestDto): QuestionResponseDto {
        // Преобразуем список в JSON строку
        val answerOptionsJson = if (request.answerOptions != null && request.answerOptions.isNotEmpty()) {
            Json.encodeToString(request.answerOptions)
        } else {
            null
        }

        val response = surveyManagementService.updateQuestion(
            questionId = questionId,
            text = request.text,
            isPublic = request.isPublic,
            answerOptions = answerOptionsJson,
            voiceFilename = request.voiceFilename,
            pictureFilename = request.pictureFilename
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun deleteQuestion(questionId: Int) {
        println("🗑️ Удаляем вопрос ID: $questionId")
        val response = surveyManagementService.deleteQuestion(questionId)

        if (!response.isSuccessful) {
            val errorMessage = ErrorHandler.parseError(response)
            println("❌ Ошибка удаления вопроса: $errorMessage")
            throw Exception(errorMessage)
        } else {
            // Сохраняем ID удаленного вопроса локально
            saveDeletedQuestionId(questionId)
            println("✅ Вопрос $questionId удален на сервере и сохранен локально")
        }
    }

    // Привязка вопросов
    suspend fun addQuestionToSurvey(request: AddQuestionToSurveyRequestDto): SurveyWithQuestionsDto {
        val response = surveyManagementService.addQuestionToSurvey(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun removeQuestionFromSurvey(questionInSurveyId: Int): SurveyWithQuestionsDto {
        val response = surveyManagementService.removeQuestionFromSurvey(questionInSurveyId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun swapQuestionsInSurvey(
        surveyId: Int,
        firstOrderIndex: Int,
        secondOrderIndex: Int
    ): SurveyWithQuestionsDto {
        val response = surveyManagementService.swapQuestionsInSurvey(
            surveyId,
            firstOrderIndex,
            secondOrderIndex
        )
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    // Получение деталей
    suspend fun getSurveyWithQuestions(surveyId: Int): SurveyWithQuestionsDto {
        val response = surveyManagementService.getSurveyWithQuestions(surveyId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun changeSurveyStatus(surveyId: Int, newStatus: String): SurveyManagementResponseDto {
        val response = surveyManagementService.changeSurveyStatus(surveyId, newStatus)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun getSurveyStatuses(): List<String> {
        val response = surveyManagementService.getSurveyStatuses()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    // Вспомогательные методы для определения типа вопроса
    fun determineQuestionType(
        text: String?,
        voiceFilename: String?,
        pictureFilename: String?
    ): QuestionType {
        return when {
            voiceFilename != null && pictureFilename != null -> QuestionType.COMBINED
            voiceFilename != null -> QuestionType.VOICE
            pictureFilename != null -> QuestionType.PICTURE
            else -> QuestionType.TEXT
        }
    }

    fun validateQuestion(
        text: String?,
        voiceFilename: String?,
        pictureFilename: String?,
        answerOptions: List<String>?
    ): ValidationResult {
        // Для текстового вопроса должен быть текст
        if (text.isNullOrBlank() && voiceFilename == null && pictureFilename == null) {
            return ValidationResult.Error("Вопрос должен содержать текст, голос или изображение")
        }

        // Для голосового вопроса должен быть голосовой файл
        if (voiceFilename != null && pictureFilename == null && text == null) {
            // Голосовой вопрос без текста - допустимо
        }

        // Для вопроса с изображением должен быть файл изображения
        if (pictureFilename != null && voiceFilename == null && text == null) {
            // Вопрос с изображением без текста - допустимо
        }

        return ValidationResult.Success
    }

    // --- МЕТОДЫ ДЛЯ ЛОКАЛЬНОГО ХРАНЕНИЯ УДАЛЕННЫХ ВОПРОСОВ ---

    private fun saveDeletedQuestionId(questionId: Int) {
        val currentIds = prefs.getStringSet("deleted_ids", mutableSetOf()) ?: mutableSetOf()
        val updatedIds = currentIds.toMutableSet()
        updatedIds.add(questionId.toString())
        prefs.edit().putStringSet("deleted_ids", updatedIds).apply()
    }

    private fun getDeletedQuestionIds(): Set<Int> {
        val stringIds = prefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
        return stringIds.mapNotNull { it.toIntOrNull() }.toSet()
    }

    // Метод для очистки локального списка (если нужно)
    fun clearDeletedQuestionIds() {
        prefs.edit().remove("deleted_ids").apply()
        println("🧹 Очищен локальный список удаленных вопросов")
    }
}

// Простой класс для валидации
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}