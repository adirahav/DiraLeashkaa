package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class BestYieldEntity(
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,

	@SerializedName("_id")	var _id: String? = null,

	@SerializedName("city") var city: String? = null,
	@SerializedName("cityElse") var cityElse: String? = null,
	@SerializedName("address") var address: String? = null,
	@SerializedName("price") var price: Int? = null,
	@SerializedName("mortgagePeriod") var mortgagePeriod: Int? = null,

	@SerializedName("profit") var profit: Float? = null,
	@SerializedName("profitNpv") var profitNpv: Float? = null,
	@SerializedName("averageReturn") var averageReturn: Float? = null,
	@SerializedName("averageReturnOnEquity") var averageReturnOnEquity: Float? = null,

	@SerializedName("yieldForecast") var yieldForecast: String? = null,
)