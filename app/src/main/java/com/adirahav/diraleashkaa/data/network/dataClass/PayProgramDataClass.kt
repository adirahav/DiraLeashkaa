package com.adirahav.diraleashkaa.data.network.dataClass
import com.adirahav.diraleashkaa.data.network.entities.PayProgramTypeEntity
import com.google.gson.annotations.SerializedName


data class PayProgramDataClass(
        @SerializedName("is_available") var isAvailable: Boolean? = null,
        @SerializedName("program_types") val programTypes: List<PayProgramTypeEntity>,
)