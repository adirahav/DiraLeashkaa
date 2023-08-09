package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

data class TransferTaxApartmentTypeEntity(
	@SerializedName("min") val min: Int,
	@SerializedName("max") val max: Int,
	@SerializedName("tax_percent") val taxPercent: Float,
)
