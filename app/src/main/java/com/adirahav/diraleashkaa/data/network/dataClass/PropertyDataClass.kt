package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.YieldForecastEntity
import com.adirahav.diraleashkaa.data.network.entities.AmortizationScheduleEntity
import com.google.gson.annotations.SerializedName

data class PropertyDataClass(
	@SerializedName("property") var property: PropertyEntity? = null
)
