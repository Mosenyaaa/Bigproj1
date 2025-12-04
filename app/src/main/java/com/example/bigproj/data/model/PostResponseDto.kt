// data/model/PostResponseDto.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PostResponseDto(

    @SerialName("is_ok") val isOk: Boolean,
    val datetime: String
)
