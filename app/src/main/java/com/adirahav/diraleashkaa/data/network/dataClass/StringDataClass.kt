package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.*
import com.google.gson.annotations.SerializedName

data class StringDataClass(
	@SerializedName("strings") var strings: StringEntity? = null
)
