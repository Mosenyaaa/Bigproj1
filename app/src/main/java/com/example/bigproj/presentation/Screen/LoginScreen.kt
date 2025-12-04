// presentation/Screen/LoginScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.state.LoginScreenEvent
import com.example.bigproj.presentation.Screen.viewmodel.AuthEvent
import com.example.bigproj.presentation.Screen.viewmodel.LoginScreenViewModel
import com.example.bigproj.presentation.navigation.Screen

object EmailHolder {
    var currentEmail: String = ""
        set(value) {
            println("📧 EmailHolder изменен: '$field' -> '$value'")
            field = value
        }
}

@Composable
fun LoginScreen(
    onNavigateTo: (Screen) -> Unit,
) {
    val viewModel = viewModel<LoginScreenViewModel>()
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToVerification -> {
                    println("🚀 Навигация на Verification с email из состояния: '${state.email}'")
                    onNavigateTo(Screen.Verification(state.email))
                }
                is AuthEvent.NavigateToRegistration -> onNavigateTo(Screen.Register)
            }
        }
    }

    val primaryColor = Color(0xFF2196F3)
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8FAFF), Color(0xFFE8F4FF))
                    )
                )
        ) {
            // Декоративные элементы
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent),
                            radius = 150f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Логотип/иконка
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                    }
                }

                // Заголовок
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Добро пожаловать!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Введите почту для входа в аккаунт",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Карточка с формой
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Вход в аккаунт",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )

                        Column {
                            Text(
                                text = "Почта",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = state.email,
                                onValueChange = {
                                    viewModel.onEvent(LoginScreenEvent.EmailUpdated(it))
                                },
                                placeholder = {
                                    Text(
                                        "Введите почту",
                                        color = Color(0xFF999999)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedTextColor = Color(0xFF1A1A1A),
                                    cursorColor = primaryColor
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email
                                ),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 16.sp
                                )
                            )
                        }

                        Button(
                            onClick = {
                                EmailHolder.currentEmail = state.email
                                println("📧 Email сохранен в holder: '${state.email}'")
                                viewModel.goToVerification() // 🔥 ИЗМЕНЕНИЕ ЗДЕСЬ
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text(
                                text = "Продолжить",
                                fontSize = 16.sp,
                                modifier = Modifier,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!state.errorMessage.isNullOrBlank()) {
                            Text(
                                text = state.errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // Текст с ссылкой
                Text(
                    text = buildAnnotatedString {
                        append("Еще не зарегистрированы? ")
                        withStyle(
                            style = SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Создать аккаунт")
                        }
                    },
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable {
                            viewModel.onEvent(
                                LoginScreenEvent.NavigateToScreen(Screen.Register)
                            )
                        }
                )

                // Информационный текст
                Text(
                    text = "На вашу почту будет отправлен код подтверждения",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}