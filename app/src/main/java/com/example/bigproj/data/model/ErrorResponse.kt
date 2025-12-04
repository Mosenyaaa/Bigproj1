// data/model/ErrorResponse.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @SerialName("has_error") val hasError: Boolean? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_specification_code") val errorSpecificationCode: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("error_description_data") val errorDescriptionData: Map<String, String>? = null,
    @SerialName("error_data") val errorData: Map<String, String>? = null
)
