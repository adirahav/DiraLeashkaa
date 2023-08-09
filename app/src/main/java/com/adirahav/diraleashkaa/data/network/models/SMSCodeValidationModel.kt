package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.entities.SMSCodeValidationEntity
import com.google.gson.annotations.SerializedName

data class SMSCodeValidationModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: SMSCodeValidationEntity?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)
