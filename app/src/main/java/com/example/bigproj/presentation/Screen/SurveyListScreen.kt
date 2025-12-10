// presentation/Screen/SurveyListScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.bigproj.presentation.Screen.state.SurveyScreenEvent
import com.example.bigproj.presentation.Screen.state.SurveyTab
import com.example.bigproj.presentation.Screen.viewmodel.SurveyListViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun SurveyListScreen(
    onNavigateToSurvey: (Int) -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<SurveyListViewModel>()

    // Snackbar для ошибок
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(SurveyScreenEvent.LoadSurveys)
    }

    // Обработка ошибок
    val errorMessage = viewModel.state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SurveyListContent(
            state = viewModel.state,
            onEvent = viewModel::onEvent,
            onNavigateToSurvey = onNavigateToSurvey,
            modifier = Modifier.padding(paddingValues) // Используем modifier
        )
    }
}

@Composable
fun SurveyListContent(
    state: com.example.bigproj.presentation.Screen.state.SurveyScreenState,
    onEvent: (SurveyScreenEvent) -> Unit,
    onNavigateToSurvey: (Int) -> Unit,
    modifier: Modifier = Modifier // Принимаем modifier
) {
    Column(
        modifier = modifier // Используем переданный modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        // Заголовок
        Text(
            text = "Опросы",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(16.dp)
        )

        // Поиск
        SearchBox(
            query = state.searchQuery,
            onQueryChange = { onEvent(SurveyScreenEvent.SearchQueryChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Вкладки
        SurveyTabs(
            selectedTab = state.selectedTab,
            onTabSelected = { onEvent(SurveyScreenEvent.ChangeTab(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Список опросов
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SurveyList(
                surveys = state.surveys,
                onSurveyClick = { surveyId -> onNavigateToSurvey(surveyId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localQuery by remember { mutableStateOf(query) }

    TextField(
        value = localQuery,
        onValueChange = {
            localQuery = it
            onQueryChange(it)
        },
        placeholder = { Text("Поиск") },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SurveyTabs(
    selectedTab: SurveyTab,
    onTabSelected: (SurveyTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Новые", "Начатые", "Завершенные")
    val surveyTabs = listOf(SurveyTab.NEW, SurveyTab.STARTED, SurveyTab.COMPLETED)

    TabRow(
        selectedTabIndex = surveyTabs.indexOf(selectedTab),
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = Color(0xFF006FFD)
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = surveyTabs[index] == selectedTab,
                onClick = { onTabSelected(surveyTabs[index]) }
            )
        }
    }
}

@Composable
fun SurveyList(
    surveys: List<com.example.bigproj.presentation.Screen.state.SurveyUiModel>,
    onSurveyClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (surveys.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Опросы не найдены",
                color = Color(0xFF666666),
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(surveys) { survey ->
                SurveyCard(
                    survey = survey,
                    onClick = { onSurveyClick(survey.id) }
                )
            }
        }
    }
}

@Composable
fun SurveyCard(
    survey: com.example.bigproj.presentation.Screen.state.SurveyUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = survey.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            // Исправление: проверка на null и пустую строку
            val description = survey.description ?: ""
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Прогресс бар
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(3.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(survey.progress)
                            .height(6.dp)
                            .background(
                                color = Color(0xFF006FFD),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${(survey.progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}