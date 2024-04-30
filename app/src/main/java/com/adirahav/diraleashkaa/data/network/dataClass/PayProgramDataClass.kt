package com.adirahav.diraleashkaa.data.network.dataClass
import com.adirahav.diraleashkaa.data.network.entities.PayProgramTypeEntity
import com.google.gson.annotations.SerializedName


data class PayProgramDataClass(
        @SerializedName("isAvailable") var isAvailable: Boolean? = null,
        @SerializedName("programTypes") val programTypes: List<PayProgramTypeEntity>,
)