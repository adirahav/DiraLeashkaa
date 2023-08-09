package com.adirahav.diraleashkaa.data.network.services

import com.adirahav.diraleashkaa.common.Configuration.API_BASE_URL
import com.adirahav.diraleashkaa.common.Const
import com.adirahav.diraleashkaa.data.network.models.APIResponseModel
import com.adirahav.diraleashkaa.data.network.models.SMSCodeValidationModel
import com.adirahav.diraleashkaa.data.network.models.UnsubscribeModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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
        val retrofit: Retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(API_BASE_URL).build()
        userAPI = retrofit.create(UserAPI::class.java)
    }

    interface UserAPI {
        @FormUrlEncoded
        @POST("users/insert")
        fun insertUser(
            @Field("user_name") userName: String?,
            @Field("email") email: String?,
            @Field("age") age: Int?,
            @Field("phone_number") phoneNumber: String?,
            @Field("phone_number_sms_verified") phoneNumberSMSVerified: Boolean?,
            @Field("device_id") deviceID: String?,
            @Field("device_type") deviceType: String?,
            @Field(Const.EQUITY) equity: Int?,
            @Field(Const.INCOMES) incomes: Int?,
            @Field(Const.COMMITMENTS) commitments: Int?,
            @Field("terms_of_use_accept_time") termsOfUseAcceptTime: Long?,
            //@Field("insert_time") insertTime: Long?,
            //@Field("update_time") updateTime: Long?,
            @Field("subscriber_type") subscriberType: String?,
            //@Field("registration_start_time") registrationStartTime: Long?,
            @Field("registration_expired_time") registrationExpiredTime: Long?,
            @Field("app_version") appVersion: String?,
            @Field("is_first_login") isFirstLogin: Boolean?,
        ): Call<UserModel?>?

        @FormUrlEncoded
        @POST("users/update")
        fun updateUser(
            @Field("uuid") uuid: String?,
            @Field("user_name") userName: String?,
            @Field("email") email: String?,
            @Field("age") age: Int?,
            @Field("phone_number") phoneNumber: String?,
            @Field("phone_number_sms_verified") phoneNumberSMSVerified: Boolean?,
            @Field("device_id") deviceID: String?,
            @Field("device_type") deviceType: String?,
            @Field(Const.EQUITY) equity: Int?,
            @Field(Const.INCOMES) incomes: Int?,
            @Field(Const.COMMITMENTS) commitments: Int?,
            @Field("terms_of_use_accept_time") termsOfUseAcceptTime: Long?,
            //@Field("insert_time") insertTime: Long?,
            //@Field("update_time") updateTime: Long?,
            @Field("subscriber_type") subscriberType: String?,
            //@Field("registration_start_time") registrationStartTime: Long?,
            @Field("registration_expired_time") registrationExpiredTime: Long?,
            @Field("app_version") appVersion: String?,
            @Field("is_first_login") isFirstLogin: Boolean?,
        ): Call<UserModel?>?

        @FormUrlEncoded
        @POST("users/delete")
        fun deleteUser(
                @Field("uuid") uuid: String?,
                @Field("user_name") userName: String?,
                @Field("email") email: String?,
                @Field("phone_number") phoneNumber: String?,
                @Field("device_id") deviceID: String?,
        ): Call<UnsubscribeModel?>?

        @FormUrlEncoded
        @POST("users/smsCodeValidation")
        fun smsCodeValidation(
            @Field("user_uuid") userUUID: String?,
            @Field("sms_code") smsCode: String?,
            @Field("phone_number") phoneNumber: String?
        ): Call<SMSCodeValidationModel?>?
    }
}