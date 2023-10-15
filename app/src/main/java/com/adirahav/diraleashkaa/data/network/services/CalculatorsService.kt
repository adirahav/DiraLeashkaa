package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.CalculatorModel
import com.adirahav.diraleashkaa.data.network.models.PropertyModel
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
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        calculatorsAPI = retrofit.create(CalculatorsAPI::class.java)
    }

    interface CalculatorsAPI {
        @FormUrlEncoded
        @POST("calculators/getList")
        fun getList(
            @Field("user_uuid") userUUID: String?,
        ): Call<CalculatorModel?>?

        @FormUrlEncoded
        @POST("calculators/maxPrice")
        fun maxPrice(
                @Field("user_uuid") userUUID: String?,
                @Field("field_name") fieldName: String?,
                @Field("field_value") fieldValue: String?,
        ): Call<PropertyModel?>?
    }
}