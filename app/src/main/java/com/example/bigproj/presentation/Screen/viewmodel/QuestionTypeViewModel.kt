package com.example.bigproj.presentation.Screen.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bigproj.data.model.QuestionTypes
import com.example.bigproj.domain.repository.ValidationResult

class QuestionTypeViewModel : ViewModel() {

    fun validateQuestionForType(
        type: String,
        text: String?,
        voiceFilename: String?,
        pictureFilename: String?
    ): ValidationResult {
        val config = QuestionTypes.getByType(type) ?:
        return ValidationResult.Error("Неизвестный тип вопроса")

        // Проверяем обязательные поля
        val errors = mutableListOf<String>()

        if ("text" in config.requiredFields && text.isNullOrBlank()) {
            errors.add("Текст обязателен для текстового вопроса")
        }

        if ("voice_filename" in config.requiredFields && voiceFilename.isNullOrBlank()) {
            errors.add("Голосовой файл обязателен")
        }

        if ("picture_filename" in config.requiredFields && pictureFilename.isNullOrBlank()) {
            errors.add("Изображение обязательно")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors.joinToString(", "))
        }
    }

    fun getQuestionTypeDescription(type: String): String {
        return QuestionTypes.getByType(type)?.description ?: "Неизвестный тип"
    }
}