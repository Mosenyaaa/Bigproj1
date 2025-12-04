// data/model/SurveyManagementModels.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Модели для создания/редактирования опросов
@Serializable
data class CreateSurveyRequestDto(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("status") val status: String = "draft", // draft, active
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("extra_data") val extraData: Map<String, String>? = null
)

@Serializable
data class UpdateSurveyRequestDto(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("is_public") val isPublic: Boolean? = null,
    @SerialName("extra_data") val extraData: Map<String, String>? = null
)

@Serializable
data class SurveyManagementResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("long_id") val longId: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("creation_dt") val creationDate: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String?,
    @SerialName("status") val status: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("user_id") val userId: Int,
    @SerialName("extra_data") val extraData: Map<String, String>? = null
)

// Модели для работы с вопросами
@Serializable
data class CreateQuestionRequestDto(
    @SerialName("text") val text: String? = null,
    @SerialName("type") val type: String? = null, // Система определяет автоматически
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("is_public") val isPublic: Boolean = true
)

@Serializable
data class UpdateQuestionRequestDto(
    @SerialName("text") val text: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("answer_options") val answerOptions: List<String>? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("is_public") val isPublic: Boolean? = null
)

@Serializable
data class AddQuestionToSurveyRequestDto(
    @SerialName("question_id") val questionId: Int,
    @SerialName("order_index") val orderIndex: Int = 0
)

@Serializable
data class UpdateQuestionInSurveyRequestDto(
    @SerialName("order_index") val orderIndex: Int
)