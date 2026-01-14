// presentation/Screen/viewmodel/QuestionEditorViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.data.model.CreateQuestionRequestDto
import com.example.bigproj.data.model.QuestionTypes
import com.example.bigproj.data.model.UpdateQuestionRequestDto
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.presentation.Screen.state.QuestionDisplayType
import com.example.bigproj.presentation.Screen.state.QuestionEditorEvent
import com.example.bigproj.presentation.Screen.state.QuestionEditorState
import kotlinx.coroutines.launch

class QuestionEditorViewModel : ViewModel() {

    var state by mutableStateOf(QuestionEditorState())
        private set

    private lateinit var repository: SurveyManagementRepository

    fun setupDependencies(context: Context) {
        repository = SurveyManagementRepository(context)
    }

    fun loadQuestion(question: QuestionResponseDto) {
        println("🔍 Загружаем вопрос ID: ${question.id}")
        println("   text: ${question.text}")
        println("   type: ${question.type}")
        println("   answerOptions: ${question.answerOptions}")

        // Определяем тип вопроса
        val displayType = determineDisplayType(question)

        // Извлекаем текст и метаданные
        val fullText = question.text ?: ""

        // ⚠️ УДАЛЯЕМ МАРКЕРЫ ИЗ ТЕКСТА ДЛЯ РЕДАКТИРОВАНИЯ
        var cleanText = fullText
            .replace("\\[MULTIPLE_CHOICE\\]".toRegex(), "")
            .replace("\\[SCALE:\\d+-\\d+\\]".toRegex(), "")
            .trim()

        // Удаляем описание шкалы если есть
        if (displayType == QuestionDisplayType.SCALE) {
            cleanText = cleanText.replace("Оцените по шкале от \\d+ до \\d+".toRegex(), "").trim()
        }

        // Разделяем на вопрос и описание
        val (questionText, description) = if (cleanText.contains("\n\n")) {
            val parts = cleanText.split("\n\n", limit = 2)
            parts[0].trim() to (if (parts.size > 1) parts[1].trim() else "")
        } else {
            cleanText to ""
        }

        val isRequired = question.extraData?.get("is_required")?.toBoolean() ?: false

        // Извлекаем диапазон шкалы если это шкала
        var scaleMin = 1
        var scaleMax = 10
        if (displayType == QuestionDisplayType.SCALE) {
            // Извлекаем из маркера
            val text = question.text ?: ""
            val scaleRegex = "\\[SCALE:(\\d+)-(\\d+)\\]".toRegex()
            val match = scaleRegex.find(text)
            if (match != null) {
                scaleMin = match.groupValues[1].toIntOrNull() ?: 1
                scaleMax = match.groupValues[2].toIntOrNull() ?: 10
            }
        }

        state = state.copy(
            questionId = question.id,
            text = questionText,
            description = description,
            displayType = displayType,
            answerOptions = question.answerOptions ?: emptyList(),
            isRequired = isRequired,
            voiceFilename = question.voiceFilename,
            pictureFilename = question.pictureFilename,
            scaleMin = scaleMin,
            scaleMax = scaleMax
        )

        println("📝 Загруженные данные:")
        println("   displayType: $displayType")
        println("   text: '$questionText'")
        println("   description: '$description'")
        println("   answerOptions: ${question.answerOptions}")
        println("   scaleMin: $scaleMin, scaleMax: $scaleMax")
    }

    private fun extractTextAndDescription(question: QuestionResponseDto, displayType: QuestionDisplayType): Pair<String, String> {
        val fullText = question.text ?: ""

        // Удаляем все маркеры для получения чистого текста
        var cleanText = fullText
            .replace("\\[MULTIPLE_CHOICE\\]".toRegex(), "")
            .replace("\\[SCALE:\\d+-\\d+\\]".toRegex(), "")
            .trim()

        // Разделяем на вопрос и описание
        return if (cleanText.contains("\n\n")) {
            val parts = cleanText.split("\n\n", limit = 2)
            parts[0].trim() to (if (parts.size > 1) parts[1].trim() else "")
        } else {
            cleanText to ""
        }
    }

    private fun determineDisplayType(question: QuestionResponseDto): QuestionDisplayType {
        // Проверяем тип из API
        return when (question.type) {
            "voice" -> QuestionDisplayType.VOICE
            "picture" -> QuestionDisplayType.PHOTO
            "combined" -> QuestionDisplayType.PHOTO // Комбинированный показываем как фото
            else -> {
                // Для текстовых вопросов определяем подтип
                if (question.answerOptions != null && question.answerOptions.isNotEmpty()) {
                    // ⚠️ НОВАЯ ЛОГИКА: Определяем по is_public и характеру ответов
                    val isPublic = question.isPublic ?: false

                    // Проверяем если это шкала (все варианты - последовательные числа)
                    val numericOptions = question.answerOptions.mapNotNull { it.toIntOrNull() }
                    val isSequential = numericOptions.size > 1 &&
                            numericOptions.sorted() == numericOptions &&
                            numericOptions.zipWithNext().all { (a, b) -> b - a == 1 }

                    if (isSequential && numericOptions.size >= 3) {
                        // Если есть последовательные числа от 1 до N - это шкала
                        QuestionDisplayType.SCALE
                    } else if (isPublic && question.answerOptions.size > 1) {
                        // Если is_public = true и несколько вариантов - MULTIPLE_CHOICE
                        QuestionDisplayType.MULTIPLE_CHOICE
                    } else {
                        // Иначе - SINGLE_CHOICE
                        QuestionDisplayType.SINGLE_CHOICE
                    }
                } else {
                    QuestionDisplayType.TEXT
                }
            }
        }
    }

    fun onEvent(event: QuestionEditorEvent) {
        when (event) {
            is QuestionEditorEvent.TextChanged -> {
                state = state.copy(text = event.text)
            }
            is QuestionEditorEvent.DescriptionChanged -> {
                state = state.copy(description = event.description)
            }
            is QuestionEditorEvent.QuestionTypeChanged -> {
                state = state.copy(
                    displayType = event.type,
                    answerOptions = when (event.type) {
                        QuestionDisplayType.TEXT,
                        QuestionDisplayType.VOICE,
                        QuestionDisplayType.PHOTO -> emptyList()
                        QuestionDisplayType.SCALE -> {
                            // Generate scale options based on current range
                            (state.scaleMin..state.scaleMax).map { it.toString() }
                        }
                        QuestionDisplayType.SINGLE_CHOICE,
                        QuestionDisplayType.MULTIPLE_CHOICE -> {
                            // Keep existing options if any, otherwise empty
                            state.answerOptions
                        }
                    }
                )
            }
            is QuestionEditorEvent.AddAnswerOption -> {
                if (event.option.isNotBlank()) {
                    state = state.copy(
                        answerOptions = state.answerOptions + event.option,
                        newAnswerOption = ""
                    )
                }
            }
            is QuestionEditorEvent.RemoveAnswerOption -> {
                state = state.copy(
                    answerOptions = state.answerOptions.filterIndexed { index, _ -> index != event.index }
                )
            }
            is QuestionEditorEvent.AnswerOptionChanged -> {
                val updated = state.answerOptions.toMutableList()
                if (event.index < updated.size) {
                    updated[event.index] = event.value
                    state = state.copy(answerOptions = updated)
                }
            }
            is QuestionEditorEvent.SetRequired -> {
                state = state.copy(isRequired = event.required)
            }
            is QuestionEditorEvent.SetVoiceFilename -> {
                state = state.copy(voiceFilename = event.filename)
            }
            is QuestionEditorEvent.SetPictureFilename -> {
                state = state.copy(pictureFilename = event.filename)
            }
            is QuestionEditorEvent.RemoveVoiceFile -> {
                state = state.copy(voiceFilename = null)
            }
            is QuestionEditorEvent.RemovePictureFile -> {
                state = state.copy(pictureFilename = null)
            }
            is QuestionEditorEvent.ScaleRangeChanged -> {
                state = state.copy(
                    scaleMin = event.min,
                    scaleMax = event.max,
                    answerOptions = (event.min..event.max).map { it.toString() }
                )
            }
            QuestionEditorEvent.SaveQuestion -> saveQuestion()
        }
    }

    private fun saveQuestion() {
        // Проверка для разных типов вопросов
        when (state.displayType) {
            QuestionDisplayType.TEXT,
            QuestionDisplayType.SINGLE_CHOICE,
            QuestionDisplayType.MULTIPLE_CHOICE,
            QuestionDisplayType.SCALE -> {
                if (state.text.isBlank()) {
                    state = state.copy(errorMessage = "Текст вопроса обязателен")
                    return
                }
            }
            QuestionDisplayType.VOICE -> {
                if (state.voiceFilename.isNullOrEmpty() && state.text.isBlank()) {
                    state = state.copy(errorMessage = "Добавьте голосовую запись или текст")
                    return
                }
            }
            QuestionDisplayType.PHOTO -> {
                if (state.pictureFilename.isNullOrEmpty() && state.text.isBlank()) {
                    state = state.copy(errorMessage = "Добавьте изображение или текст")
                    return
                }
            }
        }

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Основной текст вопроса
                val mainText = state.text.trim()

                // Формируем окончательный текст (БЕЗ МАРКЕРОВ)
                var finalText = mainText

                // Добавляем описание, если есть
                if (state.description.isNotBlank()) {
                    val descriptionText = state.description.trim()
                    if (descriptionText.isNotBlank()) {
                        finalText = if (finalText.isNotBlank()) {
                            "$finalText\n\n$descriptionText"
                        } else {
                            descriptionText
                        }
                    }
                }

                // ⚠️ ВАЖНОЕ ИЗМЕНЕНИЕ: НЕ добавляем маркеры в текст!
                // Вместо этого будем использовать is_public или другой механизм
                // Для совместимости со старой логикой оставляем только для SCALE
                when (state.displayType) {
                    QuestionDisplayType.SCALE -> {
                        // Только для шкалы добавляем описание в текст
                        val scaleDesc = "Оцените по шкале от ${state.scaleMin} до ${state.scaleMax}"
                        finalText = "$finalText\n\n$scaleDesc"
                        // НЕ добавляем [SCALE:X-Y] маркер
                        println("📌 Добавлено описание шкалы")
                    }
                    else -> {
                        // Для всех остальных типов НЕ добавляем маркеры
                        println("📌 Без маркеров в тексте")
                    }
                }

                // Подготавливаем варианты ответов
                val answerOptions = when (state.displayType) {
                    QuestionDisplayType.SCALE -> {
                        (state.scaleMin..state.scaleMax).map { it.toString() }
                    }
                    QuestionDisplayType.SINGLE_CHOICE,
                    QuestionDisplayType.MULTIPLE_CHOICE -> {
                        // Фильтруем пустые варианты
                        state.answerOptions.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() }
                    }
                    else -> null
                }

                // ⚠️ Определяем is_public на основе типа вопроса
                // Для MULTIPLE_CHOICE используем is_public = true, чтобы отличать от SINGLE_CHOICE
                val isPublic = when (state.displayType) {
                    QuestionDisplayType.MULTIPLE_CHOICE -> true
                    QuestionDisplayType.SCALE -> true
                    else -> true // По умолчанию true
                }

                if (state.isCreateMode) {
                    // Create new question
                    val request = CreateQuestionRequestDto(
                        text = finalText,
                        isPublic = isPublic, // ⚠️ Используем is_public для маркировки типа
                        answerOptions = answerOptions,
                        voiceFilename = state.voiceFilename,
                        pictureFilename = state.pictureFilename
                    )

                    println("📦 Отправляем запрос на создание вопроса")
                    println("📦 Тип вопроса: ${state.displayType}")
                    println("📦 isPublic: $isPublic")
                    println("📦 Текст (без маркеров): '$finalText'")
                    println("📦 Варианты ответов: $answerOptions")

                    val created = repository.addQuestion(request)
                    println("✅ Вопрос создан успешно!")

                    state = state.copy(
                        isLoading = false,
                        isSuccess = true,
                        questionId = created.id
                    )
                } else {
                    // Update existing question
                    val request = UpdateQuestionRequestDto(
                        text = finalText,
                        isPublic = isPublic, // ⚠️ Обновляем is_public
                        answerOptions = answerOptions,
                        voiceFilename = state.voiceFilename,
                        pictureFilename = state.pictureFilename
                    )

                    println("📦 Отправляем запрос на обновление вопроса ID ${state.questionId}")
                    println("📦 Тип вопроса: ${state.displayType}")
                    println("📦 isPublic: $isPublic")
                    println("📦 Текст (без маркеров): '$finalText'")
                    println("📦 Варианты ответов: $answerOptions")

                    val updated = repository.updateQuestion(state.questionId!!, request)
                    println("✅ Вопрос обновлен успешно!")

                    state = state.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                println("❌ Ошибка сохранения вопроса: ${e.message}")
                println("❌ Stack trace: ${e.stackTraceToString()}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }

    fun resetSuccess() {
        state = state.copy(isSuccess = false)
    }
}