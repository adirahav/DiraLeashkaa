package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.google.gson.annotations.SerializedName

data class DeviceDataClass(
		@SerializedName("user") var user: UserEntity? = null,
		@SerializedName("properties") val properties: List<PropertyEntity>,
		@SerializedName("calculators") val calculators: List<CalculatorEntity>? = null,
)