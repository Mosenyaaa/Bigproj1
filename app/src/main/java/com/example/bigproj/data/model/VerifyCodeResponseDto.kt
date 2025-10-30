package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeResponseDto(
    @SerialName("has_error") val hasError: Boolean? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    val value: String? = null,  // user-token
    // ↓ ДОБАВИМ ДОПОЛНИТЕЛЬНЫЕ ПОЛЯ НА ВСЯКИЙ СЛУЧАЙ ↓
    @SerialName("is_ok") val isOk: Boolean? = null,
    val success: Boolean? = null
)