package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.EmailDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class EmailModel(
        @SerializedName("success") var success: Boolean,
        @SerializedName("data") val data: EmailDataClass?,
        @SerializedName("error") var error: APIResponseErrorEntity?,
)
