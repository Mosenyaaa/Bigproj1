// data/model/DoctorModels.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientsListResponse(
    @SerialName("patients") val patients: List<PatientDto>,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("returned_count") val returnedCount: Int
)

@Serializable
data class PatientDto(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("creation_dt") val creationDate: String
)

@Serializable
data class PatientAttemptsResponse(
    @SerialName("attempts") val attempts: List<PatientAttemptDto>,
    @SerialName("patient_info") val patientInfo: PatientInfoDto? = null,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("returned_count") val returnedCount: Int
)

@Serializable
data class PatientAttemptDto(
    @SerialName("attempt_id") val attemptId: Int,
    @SerialName("survey_id") val surveyId: Int,
    @SerialName("survey_title") val surveyTitle: String,
    @SerialName("survey_description") val surveyDescription: String? = null,
    @SerialName("status") val status: String,
    @SerialName("creation_dt") val creationDate: String,
    @SerialName("answers") val answers: List<PatientAnswerDto>
)

@Serializable
data class PatientAnswerDto(
    @SerialName("answer_id") val answerId: Int,
    @SerialName("question_in_survey_id") val questionInSurveyId: Int,
    @SerialName("question_text") val questionText: String,
    @SerialName("question_type") val questionType: String,
    @SerialName("order_index") val orderIndex: Int,
    @SerialName("text") val text: String? = null,
    @SerialName("voice_filename") val voiceFilename: String? = null,
    @SerialName("picture_filename") val pictureFilename: String? = null,
    @SerialName("creation_dt") val creationDate: String
)

@Serializable
data class PatientInfoDto(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("creation_dt") val creationDate: String
)
