package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class PropertyValueEntity(
	@SerializedName("key") val key: String,
	@SerializedName("value") val value: Int,
)
