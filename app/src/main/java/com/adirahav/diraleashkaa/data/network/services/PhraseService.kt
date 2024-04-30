package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Path

class PhraseService private constructor() {
    val phraseAPI: PhraseAPI

    companion object {
        var instance: PhraseService? = null
            get() {
                if (field == null) {
                    field = PhraseService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        phraseAPI = retrofit.create(PhraseAPI::class.java)
    }

    interface PhraseAPI {
        @GET("phrase/{key}")
        fun getPhrase(
                @Path("key") key: String
        ): Call<PhraseEntity?>?
    }
}