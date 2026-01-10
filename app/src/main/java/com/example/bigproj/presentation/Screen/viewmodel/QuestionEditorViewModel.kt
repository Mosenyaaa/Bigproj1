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
        println("   voiceFilename: ${question.voiceFilename}")
        println("   pictureFilename: ${question.pictureFilename}")
        println("   extraData: ${question.extraData}")
        // Извлекаем текст без маркера MULTIPLE_CHOICE
        val fullText = question.text ?: ""
        val (questionText, description) = if (fullText.contains("[MULTIPLE_CHOICE]")) {
            val parts = fullText.split("[MULTIPLE_CHOICE]")
            (parts.first().trim()) to (if (parts.size > 1) parts[1].trim() else "")
        } else if (fullText.contains("\n\n")) {
            val parts = fullText.split("\n\n", limit = 2)
            parts[0] to (if (parts.size > 1) parts[1] else "")
        } else {
            fullText to ""
        }

        val displayType = determineDisplayType(question)
        val isRequired = question.extraData?.get("is_required")?.toBoolean() ?: false

        // Извлекаем диапазон шкалы если это шкала
        var scaleMin = 1
        var scaleMax = 10
        if (displayType == QuestionDisplayType.SCALE && question.answerOptions != null) {
            val numericOptions = question.answerOptions.mapNotNull { it.toIntOrNull() }
            if (numericOptions.isNotEmpty()) {
                scaleMin = numericOptions.minOrNull() ?: 1
                scaleMax = numericOptions.maxOrNull() ?: 10
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
                    // Проверяем если это шкала (все варианты - числа)
                    val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
                    if (isNumeric) {
                        QuestionDisplayType.SCALE
                    } else {
                        // Проверяем multiple_choice в extra_data или в тексте
                        val hasMultipleMarker = question.extraData?.get("multiple_choice") == "true" ||
                                question.text?.contains("[MULTIPLE_CHOICE]") == true
                        if (hasMultipleMarker) {
                            QuestionDisplayType.MULTIPLE_CHOICE
                        } else {
                            QuestionDisplayType.SINGLE_CHOICE
                        }
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
                // Готовим текст с описанием
                var finalText = state.text.trim()
                if (state.description.isNotBlank()) {
                    finalText = if (finalText.isNotBlank()) {
                        "$finalText\n\n${state.description.trim()}"
                    } else {
                        state.description.trim()
                    }
                }

                // Проверяем, что текст не пустой для типов, требующих текста
                if ((state.displayType == QuestionDisplayType.TEXT ||
                            state.displayType == QuestionDisplayType.SINGLE_CHOICE ||
                            state.displayType == QuestionDisplayType.MULTIPLE_CHOICE ||
                            state.displayType == QuestionDisplayType.SCALE) && finalText.isBlank()) {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Текст вопроса обязателен для этого типа вопроса"
                    )
                    return@launch
                }

                // Для шкалы добавляем описание диапазона
                if (state.displayType == QuestionDisplayType.SCALE && finalText.isNotBlank()) {
                    val scaleDesc = "Оцените по шкале от ${state.scaleMin} до ${state.scaleMax}"
                    finalText = "$finalText\n\n$scaleDesc"
                }

                // Для множественного выбора добавляем маркер
                if (state.displayType == QuestionDisplayType.MULTIPLE_CHOICE) {
                    val multipleMarker = "\n[MULTIPLE_CHOICE]"
                    finalText = if (finalText.isNotBlank()) {
                        "$finalText$multipleMarker"
                    } else {
                        multipleMarker.trim()
                    }
                }

                // Определяем API тип
                val apiType = when (state.displayType) {
                    QuestionDisplayType.VOICE -> "voice"
                    QuestionDisplayType.PHOTO -> "picture"
                    else -> "text" // TEXT, SINGLE_CHOICE, MULTIPLE_CHOICE, SCALE
                }

                // Подготавливаем варианты ответов
                val answerOptions = when (state.displayType) {
                    QuestionDisplayType.SCALE -> {
                        (state.scaleMin..state.scaleMax).map { it.toString() }
                    }
                    QuestionDisplayType.SINGLE_CHOICE,
                    QuestionDisplayType.MULTIPLE_CHOICE -> {
                        state.answerOptions.takeIf { it.isNotEmpty() }
                    }
                    else -> null
                }

                if (state.isCreateMode) {
                    // Create new question
                    val request = CreateQuestionRequestDto(
                        text = finalText.takeIf { it.isNotBlank() },
                        isPublic = true,
                        answerOptions = answerOptions,
                        voiceFilename = state.voiceFilename,
                        pictureFilename = state.pictureFilename
                    )

                    println("📦 Отправляем запрос на создание вопроса: $request")
                    println("📦 Текст вопроса (trimmed): '${finalText.takeIf { it.isNotBlank() } ?: "null"}'")

                    val created = repository.addQuestion(request)
                    println("✅ Вопрос создан: ${created.id}")
                    state = state.copy(isLoading = false, isSuccess = true)
                } else {
                    // Update existing question
                    val request = UpdateQuestionRequestDto(
                        text = finalText,
                        isPublic = null, // Don't change public status on update
                        answerOptions = answerOptions,
                        voiceFilename = state.voiceFilename,
                        pictureFilename = state.pictureFilename
                    )

                    println("📦 Отправляем запрос на обновление вопроса: $request")
                    println("📦 Текст вопроса (trimmed): '$finalText'")

                    val updated = repository.updateQuestion(state.questionId!!, request)
                    println("✅ Вопрос обновлен: ${updated.id}")
                    state = state.copy(isLoading = false, isSuccess = true)
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
