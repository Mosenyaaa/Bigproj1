package com.example.bigproj.data.api

import com.example.bigproj.data.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface SurveyManagementService {

    // Опросы
    @POST("/api/doctor/create_survey")
    suspend fun createSurvey(
        @Query("title") title: String,
        @Query("description") description: String? = null,
        @Query("status") status: String = "draft",
        @Query("is_public") isPublic: Boolean = false,
        @Body body: CreateSurveyRequestDto? = null // дублируем для совместимости с бэком
    ): Response<SurveyManagementResponseDto>

    @PUT("/api/doctor/update_survey")
    suspend fun updateSurvey(
        @Query("survey_id") surveyId: Int,
        @Body request: UpdateSurveyRequestDto
    ): Response<SurveyManagementResponseDto>

    @DELETE("/api/doctor/delete_survey")
    suspend fun deleteSurvey(@Query("survey_id") surveyId: Int): Response<Unit>

    // Вопросы
    @GET("/api/doctor/get_available_questions")
    suspend fun getAvailableQuestions(
        @Query("query") query: String? = null,
        @Query("st") start: Int? = null,
        @Query("fn") finish: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("is_public") isPublic: Boolean? = true // Добавить этот параметр
    ): Response<List<QuestionResponseDto>>

    @Serializable
    data class GetQuestionResponse(
        @SerialName("question") val question: QuestionResponseDto?
    )

    @GET("/api/doctor/get_question")
    suspend fun getQuestion(
        @Query("question_id") questionId: Int
    ): Response<GetQuestionResponse>

    @FormUrlEncoded
    @POST("/api/doctor/add_question")
    suspend fun addQuestion(
        @Field("text") text: String? = null,
        @Field("is_public") isPublic: Boolean = true,
        @Field("answer_options") answerOptions: List<String>? = null,
        @Field("voice_filename") voiceFilename: String? = null,
        @Field("picture_filename") pictureFilename: String? = null
    ): Response<QuestionResponseDto>

    @PUT("/api/doctor/update_question")
    suspend fun updateQuestion(
        @Query("question_id") questionId: Int,
        @Query("text") text: String? = null,
        @Query("is_public") isPublic: Boolean? = null,
        @Query("answer_options") answerOptions: List<String>? = null, // ⚠️ Пробуем List<String>
        @Query("voice_filename") voiceFilename: String? = null,
        @Query("picture_filename") pictureFilename: String? = null
    ): Response<QuestionResponseDto>


    @DELETE("/api/doctor/delete_question")
    suspend fun deleteQuestion(@Query("question_id") questionId: Int): Response<Unit>

    // Привязка вопросов к опросу
    @POST("/api/doctor/add_question_to_survey")
    suspend fun addQuestionToSurvey(@Body request: AddQuestionToSurveyRequestDto): Response<SurveyWithQuestionsDto>

    @DELETE("/api/doctor/remove_question_from_survey")
    suspend fun removeQuestionFromSurvey(@Query("question_in_survey_id") questionInSurveyId: Int): Response<SurveyWithQuestionsDto>

    @PUT("/api/doctor/swap_questions_in_survey")
    suspend fun swapQuestionsInSurvey(
        @Query("survey_id") surveyId: Int,
        @Query("order_index_1") firstOrderIndex: Int,
        @Query("order_index_2") secondOrderIndex: Int
    ): Response<SurveyWithQuestionsDto>

    // Получение детальной информации
    @GET("/api/doctor/get_survey")
    suspend fun getSurveyWithQuestions(@Query("survey_id") surveyId: Int): Response<SurveyWithQuestionsDto>

    @GET("/api/doctor/get_survey_statuses")
    suspend fun getSurveyStatuses(): Response<List<String>>

    @PUT("/api/doctor/change_survey_status")
    suspend fun changeSurveyStatus(
        @Query("survey_id") surveyId: Int,
        @Query("new_status") newStatus: String
    ): Response<SurveyManagementResponseDto>
}

@Serializable
data class QuestionResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("long_id") val longId: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("creation_dt") val creationDate: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("is_public") val isPublic: Boolean? = null,
    @SerialName("user_id") val userId: Int? = null,
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
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("extra_data") val extraData: Map<String, String>? = null
)