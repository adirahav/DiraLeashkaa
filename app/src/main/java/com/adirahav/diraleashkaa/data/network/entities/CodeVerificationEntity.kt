package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class CodeVerificationEntity(
    @SerializedName("verified") var verified: Boolean,
)
