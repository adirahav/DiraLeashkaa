package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.SplashDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class SplashModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: SplashDataClass?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)