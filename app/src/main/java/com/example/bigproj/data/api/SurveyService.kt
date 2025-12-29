// data/api/SurveyService.kt
package com.example.bigproj.data.api

import com.example.bigproj.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SurveyService {

    // 🔥 УБИРАЕМ ВСЕ ЗАГОЛОВКИ APKEY - они будут добавлены в AuthInterceptor
    @GET("/api/client/get_survey")
    suspend fun getSurvey(@Query("survey_id") surveyId: Int): Response<SurveyResponseDto>

    // 🔥 ТЕПЕРЬ ТОЛЬКО ОДИН МЕТОД С ОБЯЗАТЕЛЬНЫМ reminder_id
    @POST("/api/client/survey_attempt")
    suspend fun submitSurveyAttempt(@Body request: SurveyAttemptRequest): Response<SurveyAttemptResponse>

    @GET("/api/client/survey_completion_detailed")
    suspend fun getSurveyProgress(@Query("survey_id") surveyId: Int): Response<SurveyProgressResponseDto>

    @GET("/api/client/my_attempts")
    suspend fun getMyAttempts(): Response<MyAttemptsResponse>

    // Напоминания пациента
    @GET("/api/client/my_reminders")
    suspend fun getMyReminders(
        @Query("date") date: String? = null
    ): Response<com.example.bigproj.data.model.PatientRemindersResponse>
}