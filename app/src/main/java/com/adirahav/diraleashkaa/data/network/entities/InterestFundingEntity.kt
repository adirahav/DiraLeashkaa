package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class InterestFundingEntity(
	@SerializedName("up_to_funding") val upToFunding: String,
	@SerializedName("delta") val delta: Float,
)
