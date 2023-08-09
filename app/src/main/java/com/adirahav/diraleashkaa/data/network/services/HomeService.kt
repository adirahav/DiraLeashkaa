package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.HomeModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class HomeService private constructor() {
    val homeAPI: HomeAPI

    companion object {
        var instance: HomeService? = null
            get() {
                if (field == null) {
                    field = HomeService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        homeAPI = retrofit.create(HomeAPI::class.java)
    }

    interface HomeAPI {
        @FormUrlEncoded
        @POST("home/get")
        fun get(
            @Field("user_uuid") userUUID: String?,
        ): Call<HomeModel?>?
    }
}