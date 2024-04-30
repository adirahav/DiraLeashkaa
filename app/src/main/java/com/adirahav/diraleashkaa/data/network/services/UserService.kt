package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.data.network.models.HomeModel
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import com.adirahav.diraleashkaa.data.network.models.UnsubscribeModel
import com.adirahav.diraleashkaa.data.network.requests.SignUpRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

class UserService private constructor() {
    val userAPI: UserAPI

    companion object {
        var instance: UserService? = null
            get() {
                if (field == null) {
                    field = UserService()
                }
                return field
            }
            private set
    }

    init {
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build()
        userAPI = retrofit.create(UserAPI::class.java)
    }

    interface UserAPI {
        @PUT("user")
        fun update(
            @Header("Authorization") token: String?,
            @Body request: SignUpRequest
        ): Call<UserResponse?>?

        @GET("user/splash")
        fun getSplashData(
            @Query("platform") platform: String = "android",
            @Query("appVersionName") appVersionName: String,
            @Query("email") email: String?
        ): Call<SplashModel?>

        @GET("user/home")
        fun getHomeData(
            @Header("Authorization") token: String?,
            @Query("platform") platform: String = "android",
        ): Call<HomeModel?>?

        @FormUrlEncoded
        @POST("users/delete")
        fun deleteUser(): Call<UnsubscribeModel?>?
    }
}