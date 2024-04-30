package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.models.HomeModel
import com.adirahav.diraleashkaa.data.network.requests.PropertyArchiveRequest
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import com.google.gson.GsonBuilder
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
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create())).baseUrl(BASE_URL).build()
        propertyAPI = retrofit.create(PropertyAPI::class.java)
    }

    interface PropertyAPI {
        @POST("property")
        fun createProperty(
            @Header("Authorization") token: String?,
            @Body request: PropertyRequest
        ): Call<PropertyEntity?>?

        @PUT("property")
        fun updateProperty(
            @Header("Authorization") token: String?,
            @Body request: PropertyRequest
        ): Call<PropertyEntity?>?

        @GET("property/{_id}")
        fun getProperty(
            @Header("Authorization") token: String?,
            @Path("_id") _id: String
        ): Call<PropertyEntity?>?

        @PUT("property/{propertyId}/archive")
        fun deleteFromHome(
            @Header("Authorization") token: String?,
            @Body request: PropertyArchiveRequest,
            @Path("propertyId") propertyId: String
        ): Call<HomeModel?>?
    }
}