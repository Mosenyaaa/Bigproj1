// presentation/Screen/state/QuestionEditorState.kt
package com.example.bigproj.presentation.Screen.state

sealed class QuestionEditorEvent {
    data class TextChanged(val text: String) : QuestionEditorEvent()
    data class DescriptionChanged(val description: String) : QuestionEditorEvent()
    data class QuestionTypeChanged(val type: QuestionDisplayType) : QuestionEditorEvent()
    data class AddAnswerOption(val option: String) : QuestionEditorEvent()
    data class RemoveAnswerOption(val index: Int) : QuestionEditorEvent()
    data class AnswerOptionChanged(val index: Int, val value: String) : QuestionEditorEvent()
    data class SetRequired(val required: Boolean) : QuestionEditorEvent()
    data class SetVoiceFilename(val filename: String) : QuestionEditorEvent()
    data class SetPictureFilename(val filename: String) : QuestionEditorEvent()
    object RemoveVoiceFile : QuestionEditorEvent()

    object RemovePictureFile : QuestionEditorEvent()
    data class ScaleRangeChanged(val min: Int, val max: Int) : QuestionEditorEvent()
    object SaveQuestion : QuestionEditorEvent()
}

enum class QuestionDisplayType(val displayName: String, val icon: String, val apiType: String) {
    TEXT("Текстовый", "📝", "text"),
    SINGLE_CHOICE("Один выбор", "☑️", "text"), // API type will be "text" with answer_options
    MULTIPLE_CHOICE("Несколько вариантов", "☑️☑️", "text"), // API type will be "text" with answer_options
    SCALE("Шкала", "📊", "text"), // API type will be "text" with scale description
    VOICE("Голосовой", "🎤", "voice"),
    PHOTO("Фото", "🖼️", "picture")
}

data class QuestionEditorState(
    val questionId: Int? = null, // null for create, set for edit
    val text: String = "",
    val description: String = "",
    val displayType: QuestionDisplayType = QuestionDisplayType.TEXT,
    val answerOptions: List<String> = emptyList(),
    val newAnswerOption: String = "",
    val isRequired: Boolean = false,
    val voiceFilename: String? = null,
    val pictureFilename: String? = null,
    val scaleMin: Int = 1,
    val scaleMax: Int = 10,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val isCreateMode: Boolean
        get() = questionId == null
}
