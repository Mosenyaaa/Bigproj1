package com.example.bigproj.data.api

import com.example.bigproj.data.model.EnableDoctorFeaturesResponseDto
import com.example.bigproj.data.model.SendCodeOnEmailResponseDto
import com.example.bigproj.data.model.UserResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query

interface ClientService {

    @Headers("apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766")
    @GET("/api/client/get_current_user")
    suspend fun getCurrentUser(): Response<UserResponseDto>

    @Headers("apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766")
    @GET("/api/client/enable_doctor_features")
    suspend fun enableDoctorFeatures(@Query("access_key") accessKey: String): Response<EnableDoctorFeaturesResponseDto>

    @Headers("apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766")
    @PUT("/api/client/update_full_name")
    suspend fun updateFullName(@Query("new_full_name") newName: String): Response<UserResponseDto>

    @Headers("apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766")
    @GET("/api/client/send_reset_email_verification_code_on_email")
    suspend fun sendResetEmailCode(@Query("new_email") newEmail: String): Response<SendCodeOnEmailResponseDto>

    @Headers("apikey: apikeyvaluec9a9e9b1b42a4967a19e9c85cc458d93707abae3ef3e4926afee782d9d82b2ca72e15abe693742fb9fee5282282a58771760387425230766")
    @GET("/api/client/reset_email")
    suspend fun resetEmail(@Query("verification_code_value") code: String): Response<UserResponseDto>
}