package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class BestYieldEntity(
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,

	@SerializedName("uuid")	var uuid: String? = null,

	@SerializedName(Const.CITY) var city: String? = null,
	@SerializedName(Const.CITY_ELSE) var cityElse: String? = null,
	@SerializedName(Const.ADDRESS) var address: String? = null,
	@SerializedName(Const.PRICE) var price: Int? = null,
	@SerializedName(Const.MORTGAGE_PERIOD) var mortgagePeriod: Int? = null,

	@SerializedName("profit") var profit: Float? = null,
	@SerializedName("profit_npv") var profitNpv: Float? = null,
	@SerializedName("average_return") var averageReturn: Float? = null,
	@SerializedName("average_return_on_equity") var averageReturnOnEquity: Float? = null,

	@SerializedName("yield_forecast") var yieldForecast: String? = null,
)