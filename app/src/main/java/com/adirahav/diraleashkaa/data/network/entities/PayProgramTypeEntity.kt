package com.adirahav.diraleashkaa.data.network.entities
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

data class PayProgramTypeEntity(
        @SerializedName("uuid") var uuid: String?,
        @SerializedName("program_id") val programID: String,
        @SerializedName(Const.PRICE) val price: Float,
        @SerializedName("duration_value") val durationValue: Int,
        @SerializedName("duration_unit") val durationUnit: String
)