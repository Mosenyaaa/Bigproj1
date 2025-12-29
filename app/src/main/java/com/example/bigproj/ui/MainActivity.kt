// ui/MainActivity.kt
package com.example.bigproj.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.bigproj.domain.repository.TokenManager
import com.example.bigproj.presentation.navigation.MainNav
import com.example.bigproj.presentation.navigation.Screen
import com.example.bigproj.presentation.ui.theme.BigprojTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BigprojTheme {
                val navController = rememberNavController()
                val tokenManager = TokenManager(this)

                // Проверяем авторизацию синхронно при создании composable
                val startDestination = remember {
                    val userToken = tokenManager.getUserToken()
                    if (userToken != null) {
                        println("🎯 Пользователь авторизован, переходим в профиль")
                        Screen.Main
                    } else {
                        println("🎯 Пользователь не авторизован, остаемся на логине")
                        Screen.Login
                    }
                }

                MainNav(
                    navHostController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}