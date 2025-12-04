// data/api/GeneralService.kt
package com.example.bigproj.data.api

import com.example.bigproj.data.model.SendCodeOnEmailResponseDto
import com.example.bigproj.data.model.VerifyCodeResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface GeneralService {

    @Headers("apikey: apikeyvalue22a28be9ad3f484aacf6f164a501f61d820a2e7a710b4adbb3852c9da6754326efa6d329918a4fe082d781e4c02b55b31764084620106912")
    @GET("/api/general/send_register_or_authenticate_verification_code_on_email")
    suspend fun sendCodeOnEmail(
        @Query("email") email: String,
        @Query("full_name") fullName: String? = null
    ): Response<SendCodeOnEmailResponseDto>

    @Headers("apikey: apikeyvalue22a28be9ad3f484aacf6f164a501f61d820a2e7a710b4adbb3852c9da6754326efa6d329918a4fe082d781e4c02b55b31764084620106912")
    @GET("/api/general/register_or_authenticate")
    suspend fun verifyCode(
        @Query("verification_code_value") code: String
    ): Response<VerifyCodeResponseDto>
}