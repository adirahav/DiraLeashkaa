package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) var roomUID: Long? = null,
    @SerializedName("uuid") var uuid: String?,
    @SerializedName("user_name") var userName: String?,
    @SerializedName("email") var email: String?,
    @SerializedName("age") var age: Int?,
    @SerializedName("phone_number") var phoneNumber: String?,
    @SerializedName("phone_number_sms_verified") var phoneNumberSMSVerified: Boolean?,
    @SerializedName("device_id") var deviceID: String?,
    @SerializedName("device_type") var deviceType: String?,
    @SerializedName(Const.EQUITY) var equity: Int?,
    @SerializedName(Const.INCOMES) var incomes: Int?,
    @SerializedName(Const.COMMITMENTS) var commitments: Int?,
    @SerializedName("terms_of_use_accept_time") var termsOfUseAcceptTime: Long?,
    @SerializedName("subscriber_type") var subscriberType: String?,
    //@SerializedName("registration_start_time") var registrationStartTime: Long?,
    @SerializedName("registration_expired_time") var registrationExpiredTime: Long?,
    @SerializedName("app_version") var appVersion: String?,
    @SerializedName("is_first_login") var isFirstLogin: Boolean?,
    @SerializedName("can_take_mortgage") var canTakeMortgage: Boolean?,
) {
    override fun toString(): String {
        val newLine = " ; "//"\n"

        return if (this == null)
            "null"
        else
            "roomUID = ${roomUID}${newLine}" +
            "serverUUID = ${uuid}${newLine}" +
            "userName = ${userName}${newLine}" +
            "appVersion = ${appVersion}${newLine}" +
            "email = ${email}${newLine}" +
            "age = ${age}${newLine}" +
            "phoneNumber = ${phoneNumber}${newLine}" +
            "phoneNumberSMSVerified = ${phoneNumberSMSVerified}${newLine}" +
            "deviceID = ${deviceID}${newLine}" +
            "deviceType = ${deviceType}${newLine}" +
            "equity = ${equity}${newLine}" +
            "incomes = ${incomes}${newLine}" +
            "commitments = ${commitments}${newLine}" +
            "termsOfUseAcceptTime = ${termsOfUseAcceptTime}${newLine}" +
            //"insertTime = ${Configuration.dateTimeLogFormatter.format(insertTime ?: 0)} (${insertTime})${newLine}" +
            //"updateTime = ${Configuration.dateTimeLogFormatter.format(updateTime ?: 0)} (${updateTime})${newLine}" +
            //"serverUpdateTime = ${Configuration.dateTimeLogFormatter.format(serverUpdateTime ?: 0)} (${serverUpdateTime})${newLine}" +
            "subscriberType = ${subscriberType}${newLine}" +
            //"registrationStartTime = ${Configuration.dateTimeLogFormatter.format(registrationStartTime ?: 0)} (${registrationStartTime})${newLine}" +
            "registrationExpiredTime = ${Configuration.dateTimeLogFormatter.format(registrationExpiredTime ?: 0)} (${registrationExpiredTime})${newLine}" +
            "appVersion = ${appVersion}${newLine}" +
            "isFirstLogin = ${isFirstLogin}${newLine}"
    }
}
