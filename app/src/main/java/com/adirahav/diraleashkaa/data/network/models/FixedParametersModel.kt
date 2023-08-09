package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.google.gson.annotations.SerializedName

data class FixedParametersModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: FixedParametersEntity?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)


