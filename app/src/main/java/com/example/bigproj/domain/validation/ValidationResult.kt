// domain/validation/ValidationResult.kt
package com.example.bigproj.domain.validation

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error
}