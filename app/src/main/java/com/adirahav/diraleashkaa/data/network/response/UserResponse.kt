package com.adirahav.diraleashkaa.data.network.response

import androidx.room.Entity
import com.adirahav.diraleashkaa.common.Configuration
import com.google.gson.annotations.SerializedName

@Entity
data class  UserResponse(
        @SerializedName("_id") var _id: String?,
        @SerializedName("token") var token: String?,

        @SerializedName("email") var email: String?,
        @SerializedName("fullname") var fullname: String?,
        @SerializedName("yearOfBirth") var yearOfBirth: Int?,

        @SerializedName("equity") var equity: Int?,
        @SerializedName("incomes") var incomes: Int?,
        @SerializedName("commitments") var commitments: Int?,
        @SerializedName("termsOfUseAccept") var termsOfUseAccept: Boolean?,

        @SerializedName("subscriberType") var subscriberType: String?,
        @SerializedName("registrationExpiredTime") var registrationExpiredTime: Long?,

        @SerializedName("isAdmin") var isAdmin: Boolean?,

        @SerializedName("calcAge") var calcAge: Int?,
        @SerializedName("calcCanTakeMortgage") var calcCanTakeMortgage: Boolean?,
) {
    override fun toString(): String {
        val newLine = " ; "//"\n"

        return if (this == null)
            "null"
        else
            "email = ${email}${newLine}" +
            "fullname = ${fullname}${newLine}" +
            "yearOfBirth = ${yearOfBirth}${newLine}" +
            "equity = ${equity}${newLine}" +
            "incomes = ${incomes}${newLine}" +
            "commitments = ${commitments}${newLine}" +
            "termsOfUseAccept = ${termsOfUseAccept}${newLine}" +
            "subscriberType = ${subscriberType}${newLine}" +
            "registrationExpiredTime = ${Configuration.dateTimeLogFormatter.format(registrationExpiredTime ?: 0)} (${registrationExpiredTime})${newLine}" +
            "calcAge = ${calcAge}${newLine}" +
            "calcCanTakeMortgage = ${calcCanTakeMortgage}${newLine}"
    }
}
