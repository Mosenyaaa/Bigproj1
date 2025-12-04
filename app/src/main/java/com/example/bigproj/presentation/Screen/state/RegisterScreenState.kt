// presentation/Screen/state/RegisterScreenState.kt
package com.example.bigproj.presentation.Screen.state


sealed class RegisterScreenEvent {
    data class EmailUpdated(val newEmail: String): RegisterScreenEvent()
    data class NameUpdated(val newName: String): RegisterScreenEvent()
}

data class RegisterScreenState(
    var email: String = "",
    var name: String = "",
    val errorMessage: String? = null
)