package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.AnnouncementModel
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class AnnouncementService private constructor() {
    val announcementAPI: AnnouncementAPI

    companion object {
        var instance: AnnouncementService? = null
            get() {
                if (field == null) {
                    field = AnnouncementService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        announcementAPI = retrofit.create(AnnouncementAPI::class.java)
    }

    interface AnnouncementAPI {
       @FormUrlEncoded
        @POST("announcements/confirm")
        fun confirm(
            @Field("announcement_uuid") announcementUUID: String?,
            @Field("user_uuid") userUUID: String?,
        ): Call<AnnouncementModel?>?
    }
}