// data/model/QuestionTypeConfig.kt
package com.example.bigproj.data.model

data class QuestionTypeConfig(
    val type: String,
    val displayName: String,
    val requiredFields: List<String>,
    val optionalFields: List<String>,
    val description: String,
    val icon: String,
    val color: Long
)

object QuestionTypes {
    val TEXT = QuestionTypeConfig(
        type = "text",
        displayName = "Текстовый",
        requiredFields = listOf("text"),
        optionalFields = listOf("answer_options"),
        description = "Стандартный текстовый вопрос",
        icon = "📝",
        color = 0xFF4CAF50
    )

    val VOICE = QuestionTypeConfig(
        type = "voice",
        displayName = "Голосовой",
        requiredFields = listOf("voice_filename"),
        optionalFields = listOf("text", "answer_options"),
        description = "Вопрос с аудиозаписью",
        icon = "🎤",
        color = 0xFF2196F3
    )

    val PICTURE = QuestionTypeConfig(
        type = "picture",
        displayName = "С изображением",
        requiredFields = listOf("picture_filename"),
        optionalFields = listOf("text", "answer_options"),
        description = "Вопрос с изображением",
        icon = "🖼️",
        color = 0xFF9C27B0
    )

    val COMBINED = QuestionTypeConfig(
        type = "combined",
        displayName = "Комбинированный",
        requiredFields = listOf("voice_filename", "picture_filename"),
        optionalFields = listOf("text", "answer_options"),
        description = "Вопрос с аудио и изображением",
        icon = "🔗",
        color = 0xFFFF9800
    )

    fun getByType(type: String): QuestionTypeConfig? {
        return listOf(TEXT, VOICE, PICTURE, COMBINED).find { it.type == type }
    }

    fun determineType(
        text: String?,
        voiceFilename: String?,
        pictureFilename: String?
    ): String {
        val hasVoice = !voiceFilename.isNullOrBlank()
        val hasPicture = !pictureFilename.isNullOrBlank()

        return when {
            hasVoice && hasPicture -> COMBINED.type
            hasVoice && !hasPicture -> VOICE.type
            hasPicture && !hasVoice -> PICTURE.type
            else -> TEXT.type
        }
    }

    fun getTypeDescription(type: String): String {
        return when (type) {
            "text" -> "📝 Текстовый вопрос должен содержать текст"
            "voice" -> "🎤 Голосовой вопрос должен содержать аудиофайл"
            "picture" -> "🖼️ Вопрос с изображением должен содержать изображение"
            "combined" -> "🔗 Комбинированный вопрос должен содержать и аудио, и изображение"
            else -> "Неизвестный тип вопроса"
        }
    }
}