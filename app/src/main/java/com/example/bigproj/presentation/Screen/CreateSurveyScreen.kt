// presentation/Screen/CreateSurveyScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.state.CreateSurveyEvent
import com.example.bigproj.presentation.Screen.viewmodel.CreateSurveyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(
    navController: NavHostController? = null,
    onSurveyCreated: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<CreateSurveyViewModel>()
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

    LaunchedEffect(state.isSuccess, state.createdSurveyId) {
        if (state.isSuccess && state.createdSurveyId != null) {
            println("🚀 НЕМЕДЛЕННЫЙ переход к редактированию опроса ID: ${state.createdSurveyId}")

            // СРАЗУ переходим без задержки
            navController?.navigate("edit_survey/${state.createdSurveyId}") {
                popUpTo("create_survey") { inclusive = true }
            }
            onSurveyCreated?.invoke(state.createdSurveyId)

            // Сбрасываем состояние после перехода
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание нового опроса") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title field
            Column {
                Text(
                    text = "Название *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onEvent(CreateSurveyEvent.TitleChanged(it)) },
                    placeholder = { Text("Введите название опроса") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    ),
                    isError = state.title.isBlank() && state.errorMessage != null
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
                    onValueChange = { viewModel.onEvent(CreateSurveyEvent.DescriptionChanged(it)) },
                    placeholder = { Text("Введите описание опроса (необязательно)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    ),
                    minLines = 4,
                    maxLines = 6
                )
            }

            // Error message
            if (state.errorMessage != null && state.title.isBlank()) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    onClick = { viewModel.onEvent(CreateSurveyEvent.CreateSurvey) },
                    modifier = Modifier.weight(1f),
                    enabled = state.canCreate && !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Создать")
                    }
                }
            }
        }
    }
}