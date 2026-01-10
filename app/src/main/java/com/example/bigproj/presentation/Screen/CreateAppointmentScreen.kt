// presentation/Screen/CreateAppointmentScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.bigproj.presentation.Screen.state.CreateAppointmentEvent
import com.example.bigproj.presentation.Screen.state.FrequencyType
import com.example.bigproj.presentation.Screen.viewmodel.CreateAppointmentViewModel
import com.example.bigproj.presentation.components.DatePickerDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<CreateAppointmentViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(CreateAppointmentEvent.LoadPatients)
        viewModel.onEvent(CreateAppointmentEvent.LoadSurveys)

        // Set default start date to today
        viewModel.onEvent(CreateAppointmentEvent.StartDateChanged(getTodayDate()))
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
            snackbarHostState.showSnackbar(
                message = "Назначение создано успешно!",
                duration = SnackbarDuration.Short
            )
            delay(1000)
            navController?.popBackStack()
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Новое назначение",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Patient dropdown
            Column {
                Text(
                    text = "Пациент *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PatientDropdown(
                    patients = state.patients,
                    selectedPatient = state.selectedPatient,
                    onPatientSelected = { patient ->
                        viewModel.onEvent(CreateAppointmentEvent.PatientSelected(patient))
                    }
                )
            }

            // Survey dropdown
            Column {
                Text(
                    text = "Опрос *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SurveyDropdown(
                    surveys = state.surveys,
                    selectedSurvey = state.selectedSurvey,
                    onSurveySelected = { survey ->
                        viewModel.onEvent(CreateAppointmentEvent.SurveySelected(survey))
                    }
                )
            }

            // Frequency dropdown
            Column {
                Text(
                    text = "Периодичность *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FrequencyDropdown(
                    selectedFrequency = state.frequency,
                    onFrequencySelected = { frequency ->
                        viewModel.onEvent(CreateAppointmentEvent.FrequencyChanged(frequency))
                    }
                )
            }

            // Times per day (only for daily)
            if (state.frequency == FrequencyType.DAILY) {
                Column {
                    Text(
                        text = "Раз в день",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444444),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TimesPerDayDropdown(
                        selectedTimes = state.timesPerDay,
                        onTimesSelected = { times ->
                            viewModel.onEvent(CreateAppointmentEvent.TimesPerDayChanged(times))
                        }
                    )
                }
            }

            // Start date
            Column {
                Text(
                    text = "Дата начала *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DateField(
                    value = formatDateForDisplay(state.startDate),
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // End date (optional)
            Column {
                Text(
                    text = "Дата окончания (опционально)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DateField(
                    value = state.endDate?.let { formatDateForDisplay(it) } ?: "дд.мм.гггг",
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
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
                    onClick = { viewModel.onEvent(CreateAppointmentEvent.SaveAppointment) },
                    modifier = Modifier.weight(1f),
                    enabled = state.canSave && !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Создать назначение")
                    }
                }
            }
        }
    }

    // Date pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = state.startDate,
            onDateSelected = { date ->
                viewModel.onEvent(CreateAppointmentEvent.StartDateChanged(date))
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = state.endDate,
            onDateSelected = { date ->
                viewModel.onEvent(CreateAppointmentEvent.EndDateChanged(date))
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDropdown(
    patients: List<com.example.bigproj.data.model.PatientDto>,
    selectedPatient: com.example.bigproj.data.model.PatientDto?,
    onPatientSelected: (com.example.bigproj.data.model.PatientDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedPatient?.fullName ?: "Выберите пациента",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            if (patients.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Нет доступных пациентов") },
                    onClick = {}
                )
            } else {
                patients.forEach { patient ->
                    DropdownMenuItem(
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = patient.fullName ?: patient.email,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (patient.email != null) {
                                    Text(
                                        text = patient.email,
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onPatientSelected(patient)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyDropdown(
    surveys: List<com.example.bigproj.data.model.SurveyManagementResponseDto>,
    selectedSurvey: com.example.bigproj.data.model.SurveyManagementResponseDto?,
    onSurveySelected: (com.example.bigproj.data.model.SurveyManagementResponseDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSurvey?.title ?: "Выберите опрос",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            if (surveys.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Нет доступных опросов") },
                    onClick = {}
                )
            } else {
                surveys.forEach { survey ->
                    DropdownMenuItem(
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = survey.title,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (survey.description != null && survey.description.isNotBlank()) {
                                    Text(
                                        text = survey.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF666666),
                                        maxLines = 2
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSurveySelected(survey)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDropdown(
    selectedFrequency: FrequencyType,
    onFrequencySelected: (FrequencyType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedFrequency.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            FrequencyType.entries.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency.displayName) },
                    onClick = {
                        onFrequencySelected(frequency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimesPerDayDropdown(
    selectedTimes: Int,
    onTimesSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "$selectedTimes раз",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFFCCCCCC)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            (1..3).forEach { times ->
                DropdownMenuItem(
                    text = { Text("$times раз") },
                    onClick = {
                        onTimesSelected(times)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 17.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = if (value == "дд.мм.гггг")
                        Color(0xFF999999) else Color.Black
                )
                Text("📅", fontSize = 18.sp)
            }
        }
    }
}

private fun getTodayDate(): String {
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date())
}

private fun formatDateForDisplay(dateStr: String): String {
    if (dateStr.isBlank()) return "дд.мм.гггг"
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        date?.let { outputFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}