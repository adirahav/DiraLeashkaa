package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class APIResponseErrorEntity(
	@SerializedName("error_code") var errorCode: String,
	@SerializedName("error_message") var errorMessage: String,
)