// presentation/Screen/DoctorSurveysScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.data.model.SurveySimpleDto
import com.example.bigproj.presentation.Screen.state.DoctorSurveysEvent
import com.example.bigproj.presentation.Screen.state.DoctorSurveyTab
import com.example.bigproj.presentation.Screen.viewmodel.DoctorSurveysViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSurveysScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<DoctorSurveysViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(DoctorSurveysEvent.LoadSurveys)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Мои опросы",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isLoading) {
                            Text(
                                text = "${state.totalCount} опросов",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController?.navigate("create_survey")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFF))
        ) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(DoctorSurveysEvent.SearchQueryChanged(it)) },
                placeholder = { Text("Поиск вопросов...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Tabs
            TabRow(
                selectedTabIndex = when (state.selectedTab) {
                    DoctorSurveyTab.ALL -> 0
                    DoctorSurveyTab.DRAFTS -> 1
                    DoctorSurveyTab.ACTIVE -> 2
                    DoctorSurveyTab.ARCHIVE -> 3
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Tab(
                    selected = state.selectedTab == DoctorSurveyTab.ALL,
                    onClick = { viewModel.onEvent(DoctorSurveysEvent.ChangeTab(DoctorSurveyTab.ALL)) },
                    text = { Text("Все") }
                )
                Tab(
                    selected = state.selectedTab == DoctorSurveyTab.DRAFTS,
                    onClick = { viewModel.onEvent(DoctorSurveysEvent.ChangeTab(DoctorSurveyTab.DRAFTS)) },
                    text = { Text("Черновики") }
                )
                Tab(
                    selected = state.selectedTab == DoctorSurveyTab.ACTIVE,
                    onClick = { viewModel.onEvent(DoctorSurveysEvent.ChangeTab(DoctorSurveyTab.ACTIVE)) },
                    text = { Text("Активные") }
                )
                Tab(
                    selected = state.selectedTab == DoctorSurveyTab.ARCHIVE,
                    onClick = { viewModel.onEvent(DoctorSurveysEvent.ChangeTab(DoctorSurveyTab.ARCHIVE)) },
                    text = { Text("Архив") }
                )
            }

            // Surveys list
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.surveys.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "Опросы не найдены" else "Нет опросов",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        if (state.searchQuery.isBlank() && state.selectedTab == DoctorSurveyTab.ALL) {
                            Text(
                                text = "Создайте первый опрос",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.surveys) { survey ->
                        DoctorSurveyCard(
                            survey = survey,
                            onClick = {
                                navController?.navigate("edit_survey/${survey.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorSurveyCard(
    survey: SurveySimpleDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = survey.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )
                
                // Status badge
                StatusBadge(status = survey.status)
            }

            // Description
            survey.description?.let { description ->
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Creation date
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatCreationDate(survey.creationDate),
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status.lowercase()) {
        "draft" -> Color(0xFF757575) to "Черновик"
        "active" -> Color(0xFF4CAF50) to "Активен"
        "archived" -> Color(0xFFD32F2F) to "Архив"
        else -> Color(0xFF757575) to status
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun formatCreationDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let {
            val outputFormat = SimpleDateFormat("d MMM", Locale("ru", "RU"))
            outputFormat.format(it)
        } ?: dateString.take(10)
    } catch (e: Exception) {
        // Try alternative format
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let {
                val outputFormat = SimpleDateFormat("d MMM", Locale("ru", "RU"))
                outputFormat.format(it)
            } ?: dateString.take(10)
        } catch (e2: Exception) {
            dateString.take(10)
        }
    }
}
