package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.data.network.requests.LoginRequest
import com.adirahav.diraleashkaa.data.network.requests.SignUpRequest
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

class ForgotPasswordService private constructor() {
    val forgotPasswordAPI: ForgotPasswordAPI

    companion object {
        var instance: ForgotPasswordService? = null
            get() {
                if (field == null) {
                    field = ForgotPasswordService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        forgotPasswordAPI = retrofit.create(ForgotPasswordAPI::class.java)
    }

    interface ForgotPasswordAPI {
        @POST("forgotPassword/generateCode")
        fun generateCode(
            @Body request: JSONObject
        ): Call<JsonObject?>?

        @POST("forgotPassword/validateCode")
        fun validateCode(
            @Header("Authorization") token: String?,
            @Body request: JSONObject
        ): Call<JsonObject?>?

        @PUT("forgotPassword/changePassword")
        fun changePassword(
            @Header("Authorization") token: String?,
            @Body request: JSONObject
        ): Call<JsonObject?>?

    }
}