package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Configuration
import com.google.gson.annotations.SerializedName

@Entity
data class UserEntity(
        @PrimaryKey(autoGenerate = true) var roomUID: Long? = null,
        @SerializedName("email") var email: String?,
        @SerializedName("fullname") var fullname: String?,
        @SerializedName("yearOfBirth") var yearOfBirth: Int?,
        @SerializedName("equity") var equity: Int?,
        @SerializedName("incomes") var incomes: Int?,
        @SerializedName("commitments") var commitments: Int?,
        @SerializedName("termsOfUseAccept") var termsOfUseAccept: Boolean?,
        @SerializedName("registrationExpiredTime") var registrationExpiredTime: Long?,
        @SerializedName("subscriberType") var subscriberType: String?,
        @SerializedName("calcCanTakeMortgage") var calcCanTakeMortgage: Boolean?,
        @SerializedName("calcAge") var calcAge: Int?
) {
    override fun toString(): String {
        val newLine = " ; "//"\n"

        return if (this == null)
            "null"
        else
            "roomUID = ${roomUID}${newLine}" +
            "email = ${email}${newLine}" +
            "fullname = ${fullname}${newLine}" +
            "yearOfBirth = ${yearOfBirth}${newLine}" +
            "equity = ${equity}${newLine}" +
            "incomes = ${incomes}${newLine}" +
            "commitments = ${commitments}${newLine}" +
            "termsOfUseAccept = ${termsOfUseAccept}${newLine}" +
            "registrationExpiredTime = ${Configuration.dateTimeLogFormatter.format(registrationExpiredTime ?: 0)} (${registrationExpiredTime})${newLine}" +
            "subscriberType = ${subscriberType}${newLine}"
    }
}
