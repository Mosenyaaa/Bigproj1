// presentation/Screen/main/MainScreen.kt
package com.example.bigproj.presentation.Screen.main

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.state.MainScreenEvent
import com.example.bigproj.presentation.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import com.example.bigproj.presentation.Screen.main.LogoutConfirmationDialog
import com.example.bigproj.presentation.Screen.state.MainScreenState
import com.example.bigproj.presentation.Screen.viewmodel.MainScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    onNavigateTo: (Screen) -> Unit = {},
    navController: NavHostController? = null,
) {
    val context = LocalContext.current
    val viewModel = viewModel<MainScreenViewModel>()

    // Временное решение: сохраняем имя локально при первой загрузке
    var localUserName by remember { mutableStateOf("Не указано") }

    // Snackbar для отображения ошибок
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(MainScreenEvent.LoadUserData)
    }

    // Следим за ошибками и показываем Snackbar
    val errorMessage = viewModel.state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            // Автоматически очищаем ошибку после показа
            delay(3000)
            viewModel.clearError()
        }
    }

    // Следим за изменением состояния и обновляем локальное имя
    LaunchedEffect(viewModel.state.userName) {
        if (viewModel.state.userName != "Не указано") {
            localUserName = viewModel.state.userName
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        MainScreenContent(
            state = viewModel.state.copy(userName = localUserName),
            onEvent = { event ->
                when (event) {
                    is MainScreenEvent.UpdateFullName -> {
                        // Обновляем локально сразу
                        localUserName = event.newName
                        // И отправляем на сервер
                        viewModel.onEvent(event)
                    }
                    else -> viewModel.onEvent(event)
                }
            },
            onNavigateTo = onNavigateTo,
            onClearError = { viewModel.clearError() },
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun MainScreenContent(
    state: MainScreenState,
    onEvent: (MainScreenEvent) -> Unit,
    onNavigateTo: (Screen) -> Unit,
    onClearError: () -> Unit,
    navController: NavHostController? = null,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(0xFF006FFD)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Профиль",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Карточка профиля
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Аватар и имя
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.userName.take(2).uppercase(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = state.userName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (state.isDoctor) "Врач" else "Пациент",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFF0F0F0))
                    )

                    // Информация
                    Column {
                        Text(
                            text = "Полное имя:",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = state.userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Эл. почта:",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = state.userEmail,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Роль:",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = if (state.isDoctor) "Врач" else "Пациент",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }

            // Кнопка редактирования имени
            Button(
                onClick = { onEvent(MainScreenEvent.ShowEditDialog) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text("✏️ Редактировать имя")
            }

            // Кнопка изменения email
            Button(
                onClick = { onEvent(MainScreenEvent.ShowEmailDialog) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text("📧 Изменить email")
            }

            // Кнопка переключения врач/пациент
            Button(
                onClick = { onEvent(MainScreenEvent.ShowDoctorDialog) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text(if (state.isDoctor) "👨‍⚕️ Переключить на пациента" else "👨‍⚕️ Стать врачом")
            }

            // Кнопка выхода (теперь показывает диалог подтверждения)
            Button(
                onClick = { onEvent(MainScreenEvent.ShowLogoutDialog) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text("🚪 Выйти")
            }

            // Кнопка перехода к опросам
            Button(
                onClick = { onNavigateTo(Screen.SurveyList) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text("📊 Мои опросы")
            }

            // Индикатор загрузки
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Диалог редактирования имени
    if (state.showEditDialog) {
        EditNameDialog(
            currentName = state.userName,
            onDismiss = { onEvent(MainScreenEvent.HideEditDialog) },
            onSave = { newName ->
                onEvent(MainScreenEvent.UpdateFullName(newName))
            }
        )
    }

    // Диалог для врача
    if (state.showDoctorDialog) {
        DoctorRoleDialog(
            isDoctor = state.isDoctor,
            onDismiss = { onEvent(MainScreenEvent.HideDoctorDialog) },
            onSwitchRole = { accessKey ->
                onEvent(MainScreenEvent.EnableDoctorFeatures(accessKey))
            }
        )
    }

    // Диалог изменения email
    if (state.showEmailDialog) {
        EditEmailDialog(
            currentEmail = state.userEmail,
            onDismiss = { onEvent(MainScreenEvent.HideEmailDialog) },
            onSave = { newEmail -> onEvent(MainScreenEvent.UpdateEmail(newEmail)) }
        )
    }

    // Диалог верификации email
    if (state.showEmailVerificationDialog) {
        EmailVerificationDialog(
            email = state.tempNewEmail,
            onDismiss = {
                onEvent(MainScreenEvent.HideEmailDialog)
            },
            onVerify = { code -> onEvent(MainScreenEvent.VerifyEmailCode(code)) }
        )
    }

    // 🔥 ДИАЛОГ ПОДТВЕРЖДЕНИЯ ВЫХОДА
    if (state.showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { onEvent(MainScreenEvent.HideLogoutDialog) },
            onConfirm = {
                onEvent(MainScreenEvent.ConfirmLogout)
                // Используем navController для навигации, если он доступен
                navController?.let {
                    // Очищаем весь стек навигации и переходим на экран логина
                    it.navigate(Screen.Login) {
                        // Очищаем весь стек навигации
                        popUpTo(it.graph.startDestinationId) { inclusive = true }
                        // Предотвращаем множественные переходы
                        launchSingleTop = true
                    }
                } ?: run {
                    // Fallback на старый способ, если navController не доступен
                    onNavigateTo(Screen.Login)
                }
            }
        )
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    val primaryColor = Color(0xFF006FFD)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Редактировать имя",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Полное имя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("Отмена", color = primaryColor)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = { onSave(newName) },
                        enabled = newName.isNotBlank() && newName != currentName
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun EditEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    val primaryColor = Color(0xFF006FFD)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Изменить email",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "На новый email будет отправлен код подтверждения",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Новый email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("Отмена", color = primaryColor)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = { onSave(newEmail) },
                        enabled = newEmail.isNotBlank() && newEmail != currentEmail
                    ) {
                        Text("Отправить код")
                    }
                }
            }
        }
    }
}

@Composable
fun EmailVerificationDialog(
    email: String,
    onDismiss: () -> Unit,
    onVerify: (String) -> Unit
) {
    var verificationCode by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF006FFD)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Подтвердите email",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Мы отправили 6-значный код на почту:",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )

                Text(
                    text = email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )

                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Код подтверждения") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("Отмена", color = primaryColor)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = { onVerify(verificationCode) },
                        enabled = verificationCode.length == 6
                    ) {
                        Text("Подтвердить")
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorRoleDialog(
    isDoctor: Boolean,
    onDismiss: () -> Unit,
    onSwitchRole: (String) -> Unit
) {
    var accessKey by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF006FFD)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (isDoctor) "Переключить на пациента" else "Стать врачом",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!isDoctor) {
                    Text(
                        text = "Для активации врачебных функций введите access key:",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )

                    OutlinedTextField(
                        value = accessKey,
                        onValueChange = { accessKey = it },
                        label = { Text("Access Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Подсказка для тестирования
                    Text(
                        text = "Тестовый ключ: 2",
                        fontSize = 12.sp,
                        color = Color(0xFF006FFD),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .clickable {
                                accessKey = "2"
                                println("🎯 Вставлен ключ: 2")
                            }
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = "Вы уверены, что хотите стать пациентом?",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text("Отмена", color = primaryColor)
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = {
                            println("🎯 DoctorRoleDialog: нажата кнопка стать врачом")
                            println("   Access key: '$accessKey'")
                            if (isDoctor) {
                                // Переключаем на пациента
                                onSwitchRole("")
                            } else {
                                onSwitchRole(accessKey)
                            }
                        },
                        enabled = if (isDoctor) true else accessKey.isNotBlank()
                    ) {
                        Text(if (isDoctor) "Стать пациентом" else "Стать врачом")
                    }
                }
            }
        }
    }
}

// 🔥 НОВЫЙ КОМПОЗАБЛ ДЛЯ ДИАЛОГА ПОДТВЕРЖДЕНИЯ ВЫХОДА
@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val primaryColor = Color(0xFF006FFD)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 🔥 ЖИРНЫЙ ЗАГОЛОВОК
                Text(
                    text = "Выйти из системы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Text(
                    text = "Вы уверены? Вам придется заново ввести логин и пароль для входа в систему",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 18.sp
                )

                // 🔥 ДВЕ КНОПКИ - ОТМЕНА И ВЫЙТИ
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Отмена",
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Button(
                        onClick = onConfirm,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text(
                            text = "Выйти",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}