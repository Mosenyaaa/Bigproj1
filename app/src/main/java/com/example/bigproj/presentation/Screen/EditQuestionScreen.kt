// presentation/Screen/EditQuestionScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.domain.repository.SurveyManagementRepository
import com.example.bigproj.presentation.Screen.state.QuestionDisplayType
import com.example.bigproj.presentation.Screen.state.QuestionEditorEvent
import com.example.bigproj.presentation.Screen.viewmodel.QuestionEditorViewModel
import com.example.bigproj.presentation.components.ImagePickerDialog
import com.example.bigproj.presentation.components.VoiceRecorderDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(
    questionId: Int,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<QuestionEditorViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        // Load question data
        try {
            val repository = SurveyManagementRepository(context)
            val question = repository.getQuestion(questionId)
            viewModel.loadQuestion(question)
        } catch (e: Exception) {
            println("❌ Ошибка загрузки вопроса ID $questionId: ${e.message}")
            snackbarHostState.showSnackbar(
                message = "Ошибка загрузки вопроса: ${e.message}",
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = state.errorMessage,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController?.popBackStack()
        }
    }

    var showVoiceRecorder by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать вопрос") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Text("←", fontSize = 20.sp)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        QuestionEditorContent(
            state = state,
            navController = navController,
            onEvent = { event ->
                when (event) {
                    is QuestionEditorEvent.SetVoiceFilename -> {
                        viewModel.onEvent(event)
                        showVoiceRecorder = false
                    }
                    is QuestionEditorEvent.SetPictureFilename -> {
                        viewModel.onEvent(event)
                        showImagePicker = false
                    }
                    QuestionEditorEvent.RemoveVoiceFile -> {
                        viewModel.onEvent(event)
                    }
                    QuestionEditorEvent.RemovePictureFile -> {
                        viewModel.onEvent(event)
                    }
                    else -> viewModel.onEvent(event)
                }
            },
            onStartVoiceRecording = { showVoiceRecorder = true },
            onStartImageSelection = { showImagePicker = true },
            modifier = Modifier.padding(paddingValues)
        )

        if (showVoiceRecorder) {
            VoiceRecorderDialog(
                onRecordingComplete = { filename ->
                    viewModel.onEvent(QuestionEditorEvent.SetVoiceFilename(filename))
                },
                onDismiss = { showVoiceRecorder = false }
            )
        }

        if (showImagePicker) {
            ImagePickerDialog(
                onImageSelected = { filename ->
                    viewModel.onEvent(QuestionEditorEvent.SetPictureFilename(filename))
                },
                onDismiss = { showImagePicker = false }
            )
        }
    }
}
