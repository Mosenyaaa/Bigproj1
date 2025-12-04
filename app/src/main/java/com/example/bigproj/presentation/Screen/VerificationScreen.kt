// presentation/Screen/VerificationScreen.kt
package com.example.bigproj.presentation.Screen

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.viewmodel.VerificationScreenViewModel
import com.example.bigproj.presentation.navigation.Screen

@Composable
fun VerificationScreen(
    onNavigateTo: (Screen) -> Unit,
    email: String,
    onDifferentEmail: () -> Unit,
    context: Context
) {
    val viewModel: VerificationScreenViewModel = viewModel()

    // 🔥 ПРОВЕРЯЕМ, ЕСТЬ ЛИ ИМЯ ДЛЯ РЕГИСТРАЦИИ
    val displayEmail = remember {
        if (email.isNotBlank()) email else EmailHolder.currentEmail
    }
    val registrationName = remember { RegistrationHolder.tempName }

    println("🎯 VerificationScreen: email='$displayEmail', имя='$registrationName'")

    var verificationCode by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(60) }
    val primaryColor = Color(0xFF006FFD)
    val codeLength = 6

    LaunchedEffect(Unit) {
        viewModel.setupTokenManager(context, displayEmail, registrationName)
    }

    // 🔥 ИСПРАВЛЕННЫЙ LaunchedEffect
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VerificationScreenViewModel.VerificationEvent.NavigateToMain ->
                    onNavigateTo(Screen.Main)
            }
        }
    }

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
                Spacer(modifier = Modifier.height(25.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Проверка почты",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Мы отправили 6-значный код на почту",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🔥 ОБЯЗАТЕЛЬНО ПОКАЗЫВАЕМ EMAIL ДАЖЕ ЕСЛИ ОН ПУСТОЙ
                    Text(
                        text = displayEmail,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Введите его ниже:",
                        fontSize = 16.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        Column {
                            Text(
                                text = "Код подтверждения",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            VerificationCodeInput(
                                code = verificationCode,
                                onCodeChange = { newCode ->
                                    if (newCode.length <= codeLength) {
                                        verificationCode = newCode.uppercase()
                                    }
                                },
                                codeLength = codeLength,
                                primaryColor = primaryColor
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.verifyCode(verificationCode, context)
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
                            ),
                            enabled = verificationCode.length == codeLength && !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Подтвердить почту",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        viewModel.errorMessage?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("Неверная почта? ")
                                withStyle(
                                    style = SpanStyle(
                                        color = primaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append("Отправить на другую почту")
                                }
                            },
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDifferentEmail() }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (countdown > 0) {
                                    "Отправить код повторно через $countdown сек"
                                } else {
                                    "Не получили код? "
                                },
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            if (countdown == 0) {
                                Text(
                                    text = "Отправить снова",
                                    fontSize = 14.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            countdown = 60
                                        }
                                        .padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    codeLength: Int,
    primaryColor: Color
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = code,
            onValueChange = { newValue ->
                if (newValue.length <= codeLength) {
                    onCodeChange(newValue.uppercase())
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show()
                    }
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = 0.sp
            ),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(codeLength) { index ->
                        val char = if (index < code.length) code[index] else ' '
                        val isFocused = index == code.length

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isFocused) primaryColor.copy(alpha = 0.1f)
                                    else Color(0xFFF5F5F5)
                                )
                                .clickable { focusRequester.requestFocus() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (char != ' ') char.toString() else "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )

                            if (isFocused) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(primaryColor)
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
                innerTextField()
            }
        )

        Text(
            text = "Введите 6 символов из письма",
            fontSize = 12.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}