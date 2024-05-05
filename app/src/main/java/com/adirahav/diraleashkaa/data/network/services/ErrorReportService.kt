package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.EmailModel
import com.adirahav.diraleashkaa.data.network.requests.ErrorReportRequest
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class ErrorReportService private constructor() {
    val errorReportAPI: ErrorReportAPI

    companion object {
        var instance: ErrorReportService? = null
            get() {
                if (field == null) {
                    field = ErrorReportService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        errorReportAPI = retrofit.create(ErrorReportAPI::class.java)
    }

    interface ErrorReportAPI {
        @POST("errorReport")
        fun reportError(
            @Body request: ErrorReportRequest
        ): Call<Void>?
    }
}