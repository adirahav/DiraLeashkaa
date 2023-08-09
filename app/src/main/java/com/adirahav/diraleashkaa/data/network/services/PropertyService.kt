package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.HomeModel
import com.adirahav.diraleashkaa.data.network.models.PropertyModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class PropertyService private constructor() {
    val propertyAPI: PropertyAPI

    companion object {
        var instance: PropertyService? = null
            get() {
                if (field == null) {
                    field = PropertyService()
                }
                return field
            }
            private set
    }


    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        propertyAPI = retrofit.create(PropertyAPI::class.java)
    }

    interface PropertyAPI {
        @FormUrlEncoded
        @POST("properties/insert")
        fun insertProperty(
            @Field("user_uuid") userUUID: String?,
            @Field("field_name") fieldName: String?,
            @Field("field_value") fieldValue: String?,
        ): Call<PropertyModel?>?

        @FormUrlEncoded
        @POST("properties/update")
        fun updateProperty(
            @Field("uuid") UUID: String?,
            @Field("user_uuid") userUUID: String?,
            @Field("field_name") fieldName: String?,
            @Field("field_value") fieldValue: String?,
        ): Call<PropertyModel?>?

        @FormUrlEncoded
        @POST("properties/get")
        fun getProperty(
            @Field("uuid") UUID: String?,
            @Field("user_uuid") userUUID: String?
        ): Call<PropertyModel?>?

        @FormUrlEncoded
        @POST("properties/delete")
        fun deleteFromHome(
            @Field("uuid") uuid: String?,
            @Field("user_uuid") userUUID: String?,
            @Field("return_data") returnData: String = "home",
        ): Call<HomeModel?>?
    }
}