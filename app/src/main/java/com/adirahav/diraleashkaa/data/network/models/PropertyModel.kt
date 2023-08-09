package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.dataClass.PropertyDataClass
import com.google.gson.annotations.SerializedName

data class PropertyModel(
	@SerializedName("success") var success: Boolean,
	@SerializedName("data") val data: PropertyDataClass?,
	@SerializedName("error") var error: APIResponseErrorEntity?,
)

