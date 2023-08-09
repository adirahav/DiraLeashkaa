package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.StringDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class StringModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: StringDataClass?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)

