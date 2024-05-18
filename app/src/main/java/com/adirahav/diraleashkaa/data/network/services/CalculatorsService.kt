package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.models.CalculatorModel
import com.adirahav.diraleashkaa.data.network.models.PropertyModel
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class CalculatorsService private constructor() {
    val calculatorsAPI: CalculatorsAPI

    companion object {
        var instance: CalculatorsService? = null
            get() {
                if (field == null) {
                    field = CalculatorsService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create())).baseUrl(BASE_URL).build()
        calculatorsAPI = retrofit.create(CalculatorsAPI::class.java)
    }

    interface CalculatorsAPI {
        @PUT("calculator/maxPrice")
        fun maxPrice(
                @Header("Authorization") token: String?,
                @Body request: PropertyRequest
        ): Call<PropertyEntity?>?
    }
}