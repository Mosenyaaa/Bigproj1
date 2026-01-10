// presentation/Screen/CreateQuestionScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.bigproj.presentation.Screen.state.QuestionDisplayType
import com.example.bigproj.presentation.Screen.state.QuestionEditorEvent
import com.example.bigproj.presentation.Screen.viewmodel.QuestionEditorViewModel
import com.example.bigproj.presentation.components.ImagePickerDialog
import com.example.bigproj.presentation.components.VoiceRecorderDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<QuestionEditorViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
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
                title = { Text("Новый вопрос") },
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

@Composable
fun QuestionEditorContent(
    state: com.example.bigproj.presentation.Screen.state.QuestionEditorState,
    navController: NavHostController? = null,
    onEvent: (com.example.bigproj.presentation.Screen.state.QuestionEditorEvent) -> Unit,
    onStartVoiceRecording: () -> Unit,
    onStartImageSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text field
        Column {
            Text(
                text = "Текст вопроса *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.text,
                onValueChange = { onEvent(QuestionEditorEvent.TextChanged(it)) },
                placeholder = { Text("Введите текст вопроса") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )
        }

        // Description field
        Column {
            Text(
                text = "Описание (опционально)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(QuestionEditorEvent.DescriptionChanged(it)) },
                placeholder = { Text("Дополнительное пояснение") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 3
            )
        }

        // Question type selector
        Column {
            Text(
                text = "Тип вопроса",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            QuestionTypeSelector(
                selectedType = state.displayType,
                onTypeSelected = { type ->
                    onEvent(QuestionEditorEvent.QuestionTypeChanged(type))
                }
            )
        }

        // Required checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isRequired,
                onCheckedChange = { onEvent(QuestionEditorEvent.SetRequired(it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Обязательный вопрос",
                fontSize = 14.sp,
                color = Color(0xFF444444)
            )
        }

        // Answer options (for Single/Multiple choice)
        if (state.displayType == QuestionDisplayType.SINGLE_CHOICE || 
            state.displayType == QuestionDisplayType.MULTIPLE_CHOICE) {
            var localNewOption by remember(state.answerOptions.size) { mutableStateOf("") }
            AnswerOptionsSection(
                options = state.answerOptions,
                newOption = localNewOption,
                onNewOptionChange = { localNewOption = it },
                onAddOption = { 
                    if (localNewOption.isNotBlank()) {
                        onEvent(QuestionEditorEvent.AddAnswerOption(localNewOption))
                        localNewOption = ""
                    }
                },
                onRemoveOption = { onEvent(QuestionEditorEvent.RemoveAnswerOption(it)) },
                onOptionChange = { index, value ->
                    onEvent(QuestionEditorEvent.AnswerOptionChanged(index, value))
                }
            )
        }

        // Scale range selector (for Scale type)
        if (state.displayType == QuestionDisplayType.SCALE) {
            ScaleRangeSelector(
                minValue = state.scaleMin,
                maxValue = state.scaleMax,
                onRangeChanged = { min, max ->
                    onEvent(QuestionEditorEvent.ScaleRangeChanged(min, max))
                }
            )
        }

        // Media files section (optional for all question types)
        if (state.displayType == QuestionDisplayType.VOICE || 
            state.displayType == QuestionDisplayType.PHOTO) {
            MediaSection(
                voiceFilename = state.voiceFilename,
                pictureFilename = state.pictureFilename,
                onStartVoiceRecording = if (state.displayType == QuestionDisplayType.VOICE) onStartVoiceRecording else null,
                onStartImageSelection = if (state.displayType == QuestionDisplayType.PHOTO) onStartImageSelection else null,
                onRemoveVoice = { onEvent(QuestionEditorEvent.RemoveVoiceFile) },
                onRemoveImage = { onEvent(QuestionEditorEvent.RemovePictureFile) }
            )
        } else if (state.voiceFilename != null || state.pictureFilename != null) {
            // Show media section if media files exist even for other types (allows combined)
            MediaSection(
                voiceFilename = state.voiceFilename,
                pictureFilename = state.pictureFilename,
                onStartVoiceRecording = onStartVoiceRecording,
                onStartImageSelection = onStartImageSelection,
                onRemoveVoice = { onEvent(QuestionEditorEvent.RemoveVoiceFile) },
                onRemoveImage = { onEvent(QuestionEditorEvent.RemovePictureFile) }
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController?.popBackStack() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = { onEvent(QuestionEditorEvent.SaveQuestion) },
                modifier = Modifier.weight(1f),
                enabled = !state.isLoading && (state.text.isNotBlank() || 
                    state.displayType == QuestionDisplayType.VOICE || 
                    state.displayType == QuestionDisplayType.PHOTO),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (state.isCreateMode) "Создать" else "Сохранить")
                }
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = Color.Red,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTypeSelector(
    selectedType: QuestionDisplayType,
    onTypeSelected: (QuestionDisplayType) -> Unit
) {
    val types = listOf(
        QuestionDisplayType.TEXT,
        QuestionDisplayType.SINGLE_CHOICE,
        QuestionDisplayType.MULTIPLE_CHOICE,
        QuestionDisplayType.SCALE,
        QuestionDisplayType.VOICE,
        QuestionDisplayType.PHOTO
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "${selectedType.icon} ${selectedType.displayName}",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = type.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = type.displayName, fontSize = 14.sp)
                        }
                    },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AnswerOptionsSection(
    options: List<String>,
    newOption: String,
    onNewOptionChange: (String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onOptionChange: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Варианты ответа",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF444444),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = option,
                    onValueChange = { onOptionChange(index, it) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                IconButton(onClick = { onRemoveOption(index) }) {
                    Text("×", fontSize = 20.sp, color = Color.Red)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newOption,
                onValueChange = onNewOptionChange,
                placeholder = { Text("Новый вариант") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            )
            Button(
                onClick = onAddOption,
                enabled = newOption.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+ Добавить", fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleRangeSelector(
    minValue: Int,
    maxValue: Int,
    onRangeChanged: (Int, Int) -> Unit
) {
    val ranges = listOf(
        "1-3" to (1 to 3),
        "1-5" to (1 to 5),
        "1-10" to (1 to 10)
    )

    val currentRangeLabel = ranges.find { 
        it.second.first == minValue && it.second.second == maxValue 
    }?.first ?: "$minValue-$maxValue"

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Диапазон шкалы",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF444444),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentRangeLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                ranges.forEach { (label, range) ->
                    DropdownMenuItem(
                        text = { Text(text = label) },
                        onClick = {
                            onRangeChanged(range.first, range.second)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaSection(
    voiceFilename: String?,
    pictureFilename: String?,
    onStartVoiceRecording: (() -> Unit)? = null,
    onStartImageSelection: (() -> Unit)? = null,
    onRemoveVoice: (() -> Unit)? = null,
    onRemoveImage: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (onStartVoiceRecording != null) {
            Button(
                onClick = onStartVoiceRecording,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (voiceFilename != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (voiceFilename != null) "✓ Голос записан" else "🔊 Записать голос",
                    color = Color.White
                )
            }
            if (voiceFilename != null && onRemoveVoice != null) {
                TextButton(onClick = onRemoveVoice) {
                    Text("Удалить голос", color = Color.Red)
                }
            }
        }

        if (onStartImageSelection != null) {
            Button(
                onClick = onStartImageSelection,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pictureFilename != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (pictureFilename != null) "✓ Изображение загружено" else "🖼️ Загрузить изображение",
                    color = Color.White
                )
            }
            if (pictureFilename != null && onRemoveImage != null) {
                TextButton(onClick = onRemoveImage) {
                    Text("Удалить изображение", color = Color.Red)
                }
            }
        }
    }
}
