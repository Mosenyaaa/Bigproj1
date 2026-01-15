// presentation/Screen/state/SurveyManagementState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.domain.repository.ValidationResult

sealed class SurveyManagementEvent {
    data class UpdateSurveyTitle(val title: String) : SurveyManagementEvent()
    data class UpdateSurveyDescription(val description: String) : SurveyManagementEvent()
    data class UpdateSurveyStatus(val status: String) : SurveyManagementEvent()

    object AddNewQuestion : SurveyManagementEvent()
    data class SelectQuestion(val index: Int) : SurveyManagementEvent()

    data class UpdateQuestionText(val text: String) : SurveyManagementEvent()
    data class UpdateQuestionVoiceFile(val filename: String?) : SurveyManagementEvent()
    data class UpdateQuestionImageFile(val filename: String?) : SurveyManagementEvent()

    data class AddAnswerOption(val option: String) : SurveyManagementEvent()
    data class RemoveAnswerOption(val index: Int) : SurveyManagementEvent()

    data class MoveQuestionUp(val index: Int) : SurveyManagementEvent()
    data class MoveQuestionDown(val index: Int) : SurveyManagementEvent()
    data class DeleteQuestion(val index: Int) : SurveyManagementEvent()

    object SaveSurvey : SurveyManagementEvent()
    object ResetState : SurveyManagementEvent()

    object RecordVoice : SurveyManagementEvent()

    object PickImage : SurveyManagementEvent()

    object RemoveQuestionVoice : SurveyManagementEvent()

    object RemoveQuestionImage : SurveyManagementEvent()
}

data class SurveyManagementState(
    // Данные опроса
    val surveyTitle: String = "",
    val surveyDescription: String = "",
    val surveyStatus: String = "draft", // draft, active
    val isPublic: Boolean = false,

    // Вопросы
    val questions: List<QuestionUiModel> = emptyList(),
    val currentQuestionIndex: Int = -1,

    // Валидация
    val surveyValidationErrors: List<String> = emptyList(),
    val questionValidation: Map<Int, ValidationResult> = emptyMap(),
    val isSurveyValid: Boolean = false,

    // Состояние UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val savedSurveyId: Int? = null,

    // Для редактирования существующего опроса
    val isEditingExisting: Boolean = false,
    val currentSurveyId: Int? = null
)

data class QuestionUiModel(
    val id: Int = 0,
    var text: String = "",
    var type: String? = "text", // Делаем nullable с дефолтным значением
    var voiceFilename: String? = null,
    var pictureFilename: String? = null,
    var answerOptions: List<String> = emptyList(),
    var isRequired: Boolean = true
) {
    val displayType: String
        get() = when (type) {
            "text" -> "📝 Текстовый"
            "voice" -> "🎤 Голосовой"
            "picture" -> "🖼️ С изображением"
            "combined" -> "🔗 Комбинированный"
            else -> "❓ Неизвестный"
        }

    // Добавляем вспомогательное свойство для безопасного доступа
    val safeType: String
        get() = type ?: "text"
}