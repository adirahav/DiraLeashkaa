package com.adirahav.diraleashkaa.data.network.entities
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

data class PayProgramTypeEntity(
        @SerializedName("_id") var _id: String?,
        @SerializedName("programId") val programID: String,
        @SerializedName("price") val price: Float,
        @SerializedName("durationValue") val durationValue: Int,
        @SerializedName("durationUnit") val durationUnit: String
)