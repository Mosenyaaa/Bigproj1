// data/model/SurveyModels.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyAttemptsResponse(
    @SerialName("attempts") val attempts: List<SurveyAttemptResponse>,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("returned_count") val returnedCount: Int
)

@Serializable
data class SurveyListResponseDto(
    @SerialName("surveys") val surveys: List<SurveySimpleDto>,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("returned_count") val returnedCount: Int
)

@Serializable
data class SurveyResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("status") val status: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("creation_dt") val creationDate: String,
    @SerialName("questions") val questions: List<QuestionDto> = emptyList()
)

@Serializable
data class QuestionDto(
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_id") val questionId: Int,
    @SerialName("question_text") val questionText: String,
    @SerialName("question_type") val questionType: String,
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String?,
    @SerialName("picture_filename") val pictureFilename: String?
)

@Serializable
data class SurveyProgressResponseDto(
    @SerialName("completed") val completed: Boolean,
    @SerialName("has_attempt") val hasAttempt: Boolean,
    @SerialName("total_count_questions") val totalCountQuestions: Int,
    @SerialName("count_answered_questions") val countAnsweredQuestions: Int,
    @SerialName("answered_questions") val answeredQuestions: List<AnsweredQuestionDto>,
    @SerialName("unanswered_questions") val unansweredQuestions: List<UnansweredQuestionDto>
)

@Serializable
data class AnsweredQuestionDto(
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_text") val questionText: String,
    @SerialName("answer_text") val answerText: String
)

@Serializable
data class UnansweredQuestionDto(
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("question_text") val questionText: String
)

@Serializable
data class SurveyAttemptRequest(
    @SerialName("survey_id") val surveyId: Int,
    @SerialName("reminder_id") val reminderId: Int = 0, // 🔥 ВСЕГДА ОТПРАВЛЯЕМ ЧИСЛО (0)
    @SerialName("answers") val answers: List<SurveyAnswerRequestDto>
)

@Serializable
data class SurveyAnswerRequestDto(
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("text") val text: String? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null
)

@Serializable
data class SurveyAttemptResponse(
    @SerialName("attempt_id") val attemptId: Int? = null,
    @SerialName("survey_id") val surveyId: Int? = null,
    @SerialName("answers_count") val answersCount: Int? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("is_ok") val isOk: Boolean? = null,
    @SerialName("success") val success: Boolean? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class SurveySimpleDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("status") val status: String,
    @SerialName("user_id") val userId: Int,
    @SerialName("creation_dt") val creationDate: String
)
