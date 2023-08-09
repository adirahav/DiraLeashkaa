package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.StringModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class StringsService private constructor() {
    val stringsAPI: StringsAPI

    companion object {
        var instance: StringsService? = null
            get() {
                if (field == null) {
                    field = StringsService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        stringsAPI = retrofit.create(StringsAPI::class.java)
    }

    interface StringsAPI {
        @FormUrlEncoded
        @POST("strings/get")
        fun getString(
            @Field("key") key: String?
        ): Call<StringModel?>?
    }
}