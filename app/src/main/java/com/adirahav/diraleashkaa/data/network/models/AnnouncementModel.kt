package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.DeviceDataClass
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.google.gson.annotations.SerializedName

data class AnnouncementModel(
    @SerializedName("success") var success: Boolean,
    @SerializedName("error") var error: APIResponseErrorEntity?,
)

