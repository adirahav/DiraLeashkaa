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

class AuthService private constructor() {
    val authAPI: AuthAPI

    companion object {
        var instance: AuthService? = null
            get() {
                if (field == null) {
                    field = AuthService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        authAPI = retrofit.create(AuthAPI::class.java)
    }

    interface AuthAPI {
        @POST("auth/signup")
        fun signup(
            @Body request: SignUpRequest
        ): Call<UserResponse?>?

        @POST("auth/login")
        fun login(
            @Body request: LoginRequest
        ): Call<UserResponse?>?

        @POST("auth/logout")
        fun logout(): Call<Boolean?>?
    }
}