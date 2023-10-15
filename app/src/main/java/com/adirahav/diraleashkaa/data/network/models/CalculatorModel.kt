package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.CalculatorDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class CalculatorModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: CalculatorDataClass?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)

