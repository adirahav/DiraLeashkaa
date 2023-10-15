package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.google.gson.annotations.SerializedName

data class CalculatorDataClass(
        @SerializedName("calculators") var calculators: List<CalculatorEntity>? = null,
)