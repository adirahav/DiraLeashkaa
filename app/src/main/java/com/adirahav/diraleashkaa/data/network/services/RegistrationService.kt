package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

class RegistrationService private constructor() {
    val registrationAPI: RegistrationAPI

    companion object {
        var instance: RegistrationService? = null
            get() {
                if (field == null) {
                    field = RegistrationService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        registrationAPI = retrofit.create(RegistrationAPI::class.java)
    }

    interface RegistrationAPI {
        @PUT("registration/{code}/coupon")
        fun coupon(
            @Header("Authorization") token: String?,
            @Path("code") code: String
        ): Call<UserResponse?>?

        @PUT("registration/{payProgramId}/payProgram")
        fun payProgram(
            @Header("Authorization") token: String?,
            @Path("payProgramId") payProgramId: String
        ): Call<UserResponse?>?
    }
}