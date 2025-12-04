// data/api/FileService.kt
package com.example.bigproj.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {

    @Headers(
        "apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766",
        "accept: application/json"
    )
    @Multipart
    @POST("/api/client/upload_file")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<FileUploadResponse>
}

@Serializable
data class FileUploadResponse(
    @SerialName("filename") val filename: String
)