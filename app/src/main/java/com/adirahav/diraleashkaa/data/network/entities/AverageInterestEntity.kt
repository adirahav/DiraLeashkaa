package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class AverageInterestEntity(
	@SerializedName("up_to_years") val upToYears: Int,		// תקופת משכנתא
	@SerializedName("prime") val prime: Float,				// פריים
	@SerializedName("linked") val linked: Float,			// צמודה
	@SerializedName("not_linked") val notLinked: Float,		// לא צמודה
	@SerializedName("average") val average: Boolean,		// ריבית ממוצעת
)
