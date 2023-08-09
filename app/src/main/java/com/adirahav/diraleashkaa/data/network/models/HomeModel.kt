package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.dataClass.HomeDataClass
import com.google.gson.annotations.SerializedName

data class HomeModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: HomeDataClass?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)

