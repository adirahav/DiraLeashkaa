package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class AdditionalInterestEntity(
	@SerializedName("up_to_funding") val upToFunding: Int,	// עד אחוז מימון
	@SerializedName("delta") val delta: Float,				// הפרש ריבית
)
