package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.dataClass.DeviceDataClass
import com.adirahav.diraleashkaa.data.network.entities.AnnouncementEntity
import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.google.gson.annotations.SerializedName

data class SplashModel(
        @SerializedName("fixedParameters") var fixedParameters: FixedParametersEntity? = null,
        @SerializedName("phrases") var phrases: ArrayList<PhraseEntity?>,
        @SerializedName("user") var user: UserEntity? = null,
        @SerializedName("restore") val restore: DeviceDataClass? = null,
        @SerializedName("announcements") val announcements: List<AnnouncementEntity>? = null,
        @SerializedName("calculators") val calculators: List<CalculatorEntity>? = null,
        @SerializedName("newVersionAvailable") val newVersionAvailable: Boolean? = null,
        @SerializedName("serverDown") val serverDown: Boolean? = null
)