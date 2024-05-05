package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.EmailModel
import com.adirahav.diraleashkaa.data.network.requests.ContactUsRequest
import com.adirahav.diraleashkaa.data.network.requests.ErrorReportRequest
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class ContactUsService private constructor() {
    val contactUsAPI: ContactUsAPI

    companion object {
        var instance: ContactUsService? = null
            get() {
                if (field == null) {
                    field = ContactUsService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        contactUsAPI = retrofit.create(ContactUsAPI::class.java)
    }

    interface ContactUsAPI {
        @POST("contactUs")
        fun sendMessage(
            @Body request: ContactUsRequest
        ): Call<Boolean>?
    }
}