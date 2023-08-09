package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class SMSCodeValidationEntity(
    @SerializedName("is_valid_code") var isValidCode: Boolean,
)
