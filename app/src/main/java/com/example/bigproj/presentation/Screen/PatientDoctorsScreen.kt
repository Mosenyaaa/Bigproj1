// presentation/Screen/PatientDoctorsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.state.PatientDoctorsEvent
import com.example.bigproj.presentation.Screen.state.PatientDoctorsTab
import com.example.bigproj.presentation.Screen.viewmodel.PatientDoctorsViewModel
import kotlinx.coroutines.delay
import com.example.bigproj.data.model.User1ClientSO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDoctorsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<PatientDoctorsViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(PatientDoctorsEvent.LoadMyDoctors)
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
                            text = "Врачи",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.selectedTab == PatientDoctorsTab.MY_DOCTORS) {
                            Text(
                                text = "${state.myDoctors.size} моих врачей",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PatientDoctorsContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(paddingValues)
        )

        // Диалог привязки
        if (state.showAssociateDialog && state.selectedDoctor != null) {
            AssociateDoctorDialog(
                doctor = state.selectedDoctor!!,
                onConfirm = {
                    viewModel.onEvent(PatientDoctorsEvent.AssociateDoctor(state.selectedDoctor!!.id))
                },
                onDismiss = {
                    viewModel.onEvent(PatientDoctorsEvent.HideAssociateDialog)
                }
            )
        }

        // Диалог отвязки
        if (state.showDisassociateDialog && state.selectedDoctor != null) {
            DisassociateDoctorDialog(
                doctor = state.selectedDoctor!!,
                onConfirm = {
                    viewModel.onEvent(PatientDoctorsEvent.DisassociateDoctor(state.selectedDoctor!!.id))
                },
                onDismiss = {
                    viewModel.onEvent(PatientDoctorsEvent.HideDisassociateDialog)
                }
            )
        }
    }
}

@Composable
fun PatientDoctorsContent(
    state: com.example.bigproj.presentation.Screen.state.PatientDoctorsState,
    onEvent: (PatientDoctorsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        // Вкладки
        TabRow(
            selectedTabIndex = when (state.selectedTab) {
                PatientDoctorsTab.MY_DOCTORS -> 0
                PatientDoctorsTab.ALL_DOCTORS -> 1
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[when (state.selectedTab) {
                        PatientDoctorsTab.MY_DOCTORS -> 0
                        PatientDoctorsTab.ALL_DOCTORS -> 1
                    }])
                )
            }
        ) {
            Tab(
                selected = state.selectedTab == PatientDoctorsTab.MY_DOCTORS,
                onClick = { onEvent(PatientDoctorsEvent.SelectTab(PatientDoctorsTab.MY_DOCTORS)) },
                text = { Text("Мои врачи") }
            )
            Tab(
                selected = state.selectedTab == PatientDoctorsTab.ALL_DOCTORS,
                onClick = { onEvent(PatientDoctorsEvent.SelectTab(PatientDoctorsTab.ALL_DOCTORS)) },
                text = { Text("Все врачи") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.selectedTab) {
            PatientDoctorsTab.MY_DOCTORS -> {
                MyDoctorsTabContent(
                    isLoading = state.isLoading,
                    myDoctors = state.myDoctors,
                    onFindDoctorClick = { onEvent(PatientDoctorsEvent.SelectTab(PatientDoctorsTab.ALL_DOCTORS)) },
                    onDisassociateClick = { doctor ->
                        onEvent(PatientDoctorsEvent.SetSelectedDoctor(doctor))
                        onEvent(PatientDoctorsEvent.ShowDisassociateDialog)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            PatientDoctorsTab.ALL_DOCTORS -> {
                AllDoctorsTabContent(
                    isLoading = state.isLoading,
                    filteredDoctors = state.filteredDoctors,
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = { query ->
                        onEvent(PatientDoctorsEvent.SearchDoctors(query))
                    },
                    onAssociateClick = { doctor ->
                        onEvent(PatientDoctorsEvent.SetSelectedDoctor(doctor))
                        onEvent(PatientDoctorsEvent.ShowAssociateDialog)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MyDoctorsTabContent(
    isLoading: Boolean,
    myDoctors: List<com.example.bigproj.data.model.User1ClientSO>,
    onFindDoctorClick: () -> Unit,
    onDisassociateClick: (com.example.bigproj.data.model.User1ClientSO) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Кнопка "Найти врача"
        Button(
            onClick = onFindDoctorClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Найти врача", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (myDoctors.isEmpty()) {
            EmptyMyDoctorsState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myDoctors) { doctor ->
                    MyDoctorCard(
                        doctor = doctor,
                        onDisassociateClick = { onDisassociateClick(doctor) }
                    )
                }
            }
        }
    }
}

@Composable
fun AllDoctorsTabContent(
    isLoading: Boolean,
    filteredDoctors: List<com.example.bigproj.data.model.User1ClientSO>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onAssociateClick: (com.example.bigproj.data.model.User1ClientSO) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Поле поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Поиск по имени или email...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredDoctors.isEmpty()) {
            if (searchQuery.isNotBlank()) {
                EmptySearchState()
            } else {
                EmptyAllDoctorsState()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDoctors) { doctor ->
                    AllDoctorCard(
                        doctor = doctor,
                        onAssociateClick = { onAssociateClick(doctor) }
                    )
                }
            }
        }
    }
}

@Composable
fun MyDoctorCard(
    doctor: com.example.bigproj.data.model.User1ClientSO,
    onDisassociateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = doctor.fullName?.take(2)?.uppercase() ?: "DR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = doctor.fullName ?: "Без имени",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_send),
                        contentDescription = "Email",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = doctor.email ?: "Нет email",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_today),
                        contentDescription = "Дата",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "С ${doctor.creationDate?.take(10) ?: "неизвестно"}",
                        fontSize = 10.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // Кнопка отвязки
            IconButton(
                onClick = onDisassociateClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = "Отвязаться",
                    tint = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
fun AllDoctorCard(
    doctor: com.example.bigproj.data.model.User1ClientSO,
    onAssociateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = doctor.fullName?.take(2)?.uppercase() ?: "DR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = doctor.fullName ?: "Без имени",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = doctor.email ?: "Нет email",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Кнопка привязки
            IconButton(
                onClick = onAssociateClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_add),
                    contentDescription = "Привязаться",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyMyDoctorsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_myplaces),
                contentDescription = "Нет врачей",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Нет привязанных врачей",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Найдите и привяжитесь к врачу, чтобы получать опросы",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyAllDoctorsState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_myplaces),
                contentDescription = "Все врачи",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Все врачи добавлены",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Вы уже привязаны ко всем доступным врачам",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptySearchState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_search),
                contentDescription = "Не найдено",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Врачи не найдены",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Попробуйте изменить поисковый запрос",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AssociateDoctorDialog(
    doctor: com.example.bigproj.data.model.User1ClientSO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Привязаться к врачу?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Вы уверены, что хотите привязаться к врачу ${doctor.fullName ?: doctor.email}? После привязки врач сможет назначать вам опросы.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm
                    ) {
                        Text("Привязаться")
                    }
                }
            }
        }
    }
}

@Composable
fun DisassociateDoctorDialog(
    doctor: com.example.bigproj.data.model.User1ClientSO,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Отвязаться от врача?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Вы уверены, что хотите отвязаться от врача ${doctor.fullName ?: doctor.email}? Вы больше не будете получать от него опросы.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828)
                        )
                    ) {
                        Text("Отвязаться")
                    }
                }
            }
        }
    }
}