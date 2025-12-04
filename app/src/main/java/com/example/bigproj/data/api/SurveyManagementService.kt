package com.example.bigproj.data.api

import com.example.bigproj.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface SurveyManagementService {

    // Опросы
    @POST("/api/doctor/create_survey")
    suspend fun createSurvey(@Body request: CreateSurveyRequestDto): Response<SurveyManagementResponseDto>

    @PUT("/api/doctor/update_survey")
    suspend fun updateSurvey(
        @Query("survey_id") surveyId: Int,
        @Body request: UpdateSurveyRequestDto
    ): Response<SurveyManagementResponseDto>

    @DELETE("/api/doctor/delete_survey")
    suspend fun deleteSurvey(@Query("survey_id") surveyId: Int): Response<Unit>

    // Вопросы
    @POST("/api/doctor/create_question")
    suspend fun createQuestion(@Body request: CreateQuestionRequestDto): Response<QuestionResponseDto>

    @PUT("/api/doctor/update_question")
    suspend fun updateQuestion(
        @Query("question_id") questionId: Int,
        @Body request: UpdateQuestionRequestDto
    ): Response<QuestionResponseDto>

    @DELETE("/api/doctor/delete_question")
    suspend fun deleteQuestion(@Query("question_id") questionId: Int): Response<Unit>

    // Привязка вопросов к опросу
    @POST("/api/doctor/add_question_to_survey")
    suspend fun addQuestionToSurvey(@Body request: AddQuestionToSurveyRequestDto): Response<Unit>

    @PUT("/api/doctor/update_question_in_survey")
    suspend fun updateQuestionInSurvey(
        @Query("question_in_survey_id") questionInSurveyId: Int,
        @Body request: UpdateQuestionInSurveyRequestDto
    ): Response<Unit>

    @DELETE("/api/doctor/delete_question_from_survey")
    suspend fun deleteQuestionFromSurvey(@Query("question_in_survey_id") questionInSurveyId: Int): Response<Unit>

    // Получение детальной информации
    @GET("/api/doctor/survey_with_questions")
    suspend fun getSurveyWithQuestions(@Query("survey_id") surveyId: Int): Response<SurveyWithQuestionsDto>
}

@Serializable
data class QuestionResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("long_id") val longId: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("creation_dt") val creationDate: String,
    @SerialName("type") val type: String,
    @SerialName("text") val text: String?,
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("user_id") val userId: Int,
    @SerialName("extra_data") val extraData: Map<String, String>? = null
)

@Serializable
data class SurveyWithQuestionsDto(
    @SerialName("survey") val survey: SurveyManagementResponseDto,
    @SerialName("questions") val questions: List<QuestionInSurveyDto>
)

@Serializable
data class QuestionInSurveyDto(
    @SerialName("id") val id: Int,
    @SerialName("question_id") val questionId: Int,
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_text") val questionText: String?,
    @SerialName("question_type") val questionType: String,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null
)