// domain/repository/SurveyManagementRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.data.api.SurveyWithQuestionsDto
import com.example.bigproj.data.model.*
import com.example.bigproj.domain.utils.ErrorHandler

class SurveyManagementRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val surveyManagementService by lazy {
        RetrofitClient.createSurveyManagementService(tokenManager)
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
        val response = surveyManagementService.getAvailableQuestions(query, start, finish, limit)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun addQuestion(request: CreateQuestionRequestDto): QuestionResponseDto {
        val response = surveyManagementService.addQuestion(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun updateQuestion(questionId: Int, request: UpdateQuestionRequestDto): QuestionResponseDto {
        val response = surveyManagementService.updateQuestion(questionId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun deleteQuestion(questionId: Int) {
        val response = surveyManagementService.deleteQuestion(questionId)
        if (!response.isSuccessful) {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
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

    suspend fun changeSurveyStatus(surveyId: Int, status: String): SurveyManagementResponseDto {
        val response = surveyManagementService.changeSurveyStatus(surveyId, status)
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
}

// Простой класс для валидации
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}