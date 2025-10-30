package com.example.bigproj.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.bigproj.presentation.Screen.LoginScreen
import com.example.bigproj.presentation.Screen.RegisterScreen
import com.example.bigproj.presentation.Screen.main.MainScreen
import com.example.bigproj.presentation.Screen.VerificationScreen
import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    @Serializable
    data object Main : Screen()

    @Serializable
    data class Verification(val email: String) : Screen()
}

@Composable
fun MainNav(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: Screen
) {
    val context = LocalContext.current

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                }
            )
        }
        composable<Screen.Register> {
            RegisterScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                }
            )
        }
        composable<Screen.Main> {
            MainScreen(
                onNavigateTo = { navigateTo ->
                    when (navigateTo) {
                        is Screen.Login -> {
                            navHostController.navigate(Screen.Login) {
                                popUpTo(Screen.Main) { inclusive = true }
                            }
                        }
                        else -> navHostController.navigate(navigateTo)
                    }
                }
            )
        }
        composable<Screen.Verification> { backStackEntry ->
            val email = backStackEntry.toRoute<Screen.Verification>().email

            VerificationScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                },
                email = email,
                onDifferentEmail = {
                    navHostController.navigate(Screen.Login)
                },
                context = context
            )
        }
    }
}