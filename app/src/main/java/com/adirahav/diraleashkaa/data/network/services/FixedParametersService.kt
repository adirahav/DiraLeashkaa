package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.FixedParametersModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

class FixedParametersService private constructor() {
    val fixedParametersAPI: FixedParametersAPI

    companion object {
        var instance: FixedParametersService? = null
            get() {
                if (field == null) {
                    field = FixedParametersService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        fixedParametersAPI = retrofit.create(FixedParametersAPI::class.java)
    }

    interface FixedParametersAPI {
        @POST("fixedParameters/getAll")
        fun getFixedParameters(): Call<FixedParametersModel?>?
    }
}