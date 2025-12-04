// data/model/FileUploadResponse.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    @SerialName("filename") val filename: String
)
