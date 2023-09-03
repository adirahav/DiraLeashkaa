package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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
        @FormUrlEncoded
        @POST("registration/coupon")
        fun coupon(
            @Field("coupon_code") couponCode: String?,
            @Field("user_uuid") userUUID: String?
        ): Call<RegistrationModel?>?

        @FormUrlEncoded
        @POST("registration/trial")
        fun trial(
            @Field("user_uuid") userUUID: String?
        ): Call<RegistrationModel?>?

        @FormUrlEncoded
        @POST("registration/beta")
        fun beta(
            @Field("beta_code") couponCode: String?,
            @Field("user_uuid") userUUID: String?
        ): Call<RegistrationModel?>?

        @FormUrlEncoded
        @POST("registration/payProgram")
        fun payProgram(
                @Field("pay_program_uuid") payUUID: String?,
                @Field("user_uuid") userUUID: String?
        ): Call<RegistrationModel?>?
    }
}