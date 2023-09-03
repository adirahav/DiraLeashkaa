package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class SplashService private constructor() {
    val splashAPI: SplashAPI

    companion object {
        var instance: SplashService? = null
            get() {
                if (field == null) {
                    field = SplashService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        splashAPI = retrofit.create(SplashAPI::class.java)
    }

    interface SplashAPI {
        @FormUrlEncoded
        @POST("splash/get")
        fun get(
            @Field("user_uuid") userUUID: String?,
            @Field("device_id") deviceID: String?,
            @Field("app_version_name") appVersionName: String,
            @Field("track_user") trackUser: Boolean?,
        ): Call<SplashModel?>?
    }
}