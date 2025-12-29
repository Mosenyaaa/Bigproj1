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
    @SerialName("is_public") val isPublic: Boolean = false, // Значение по умолчанию, если сервер не вернул
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
    @SerialName("survey_id") val surveyId: Int,
    @SerialName("question_id") val questionId: Int,
    @SerialName("order_index") val orderIndex: Int = 0
)

@Serializable
data class UpdateQuestionInSurveyRequestDto(
    @SerialName("order_index") val orderIndex: Int
)

// Планирование опросов (doctor)
@Serializable
data class ScheduleSurveyRequestDto(
    @SerialName("survey_id") val surveyId: Int,
    @SerialName("patient_id") val patientId: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("frequency_type") val frequencyType: String = "once",
    @SerialName("times_per_day") val timesPerDay: Int? = null,
    @SerialName("interval_days") val intervalDays: Int? = null,
    @SerialName("days_of_week") val daysOfWeek: List<Int>? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("scheduled_times") val scheduledTimes: List<String>? = null,
    @SerialName("timezone") val timezone: String = "UTC",
    @SerialName("max_reminders") val maxReminders: Int? = null,
    @SerialName("reminder_interval_minutes") val reminderIntervalMinutes: Int? = null
)

@Serializable
data class ScheduledSurveyDto(
    @SerialName("id") val id: Int,
    @SerialName("survey_id") val surveyId: Int,
    @SerialName("patient_id") val patientId: Int,
    @SerialName("doctor_id") val doctorId: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("frequency_type") val frequencyType: String? = null,
    @SerialName("times_per_day") val timesPerDay: Int? = null,
    @SerialName("interval_days") val intervalDays: Int? = null,
    @SerialName("days_of_week") val daysOfWeek: List<Int>? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("scheduled_times") val scheduledTimes: List<String>? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("max_reminders") val maxReminders: Int? = null,
    @SerialName("reminder_interval_minutes") val reminderIntervalMinutes: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("next_scheduled_date") val nextScheduledDate: String? = null
)

@Serializable
data class PatientScheduledSurveysResponse(
    @SerialName("scheduled_surveys") val scheduledSurveys: List<ScheduledSurveyDto> = emptyList()
)

// Напоминания пациента
@Serializable
data class PatientReminderDto(
    @SerialName("id") val id: Int,
    @SerialName("scheduled_survey_id") val scheduledSurveyId: Int,
    @SerialName("attempt_id") val attemptId: Int? = null,
    @SerialName("reminder_number") val reminderNumber: Int? = null,
    @SerialName("reminder_type") val reminderType: String? = null,
    @SerialName("scheduled_time") val scheduledTime: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("sent_at") val sentAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("delivery_method") val deliveryMethod: String? = null,
    @SerialName("delivery_status") val deliveryStatus: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("survey_id") val surveyId: Int? = null,
    @SerialName("survey_title") val surveyTitle: String? = null,
    @SerialName("survey_description") val surveyDescription: String? = null,
    @SerialName("scheduled_survey_title") val scheduledSurveyTitle: String? = null
)

@Serializable
data class PatientRemindersResponse(
    @SerialName("reminders") val reminders: List<PatientReminderDto> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("returned_count") val returnedCount: Int = 0
)