package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.UserDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") val data: UserDataClass?,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)
