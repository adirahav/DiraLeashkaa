package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.data.network.models.DeviceModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class DeviceService private constructor() {
    val deviceAPI: DeviceAPI

    companion object {
        var instance: DeviceService? = null
            get() {
                if (field == null) {
                    field = DeviceService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        deviceAPI = retrofit.create(DeviceAPI::class.java)
    }

    interface DeviceAPI {
        @FormUrlEncoded
        @POST("device/restore")
        fun restoreDevice(
            @Field("device_id") deviceID: String?,
        ): Call<DeviceModel?>?
    }
}