package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

data class TransferTaxModel(
	@SerializedName(Const.APARTMENT_TYPE) var apartmentType: String? = null,
	@SerializedName("tax_brackets") val taxBrackets: List<TransferTaxApartmentTypeEntity>,
)