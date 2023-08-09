package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.RegistrationDataClass
import com.adirahav.diraleashkaa.data.network.dataClass.UnsubscribeDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class UnsubscribeModel(
        @SerializedName("success") var success: Boolean,
        @SerializedName("data") val data: UnsubscribeDataClass?,
        @SerializedName("error") var error: APIResponseErrorEntity?,
)

