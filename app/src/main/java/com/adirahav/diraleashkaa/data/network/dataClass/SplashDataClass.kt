package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.*
import com.google.gson.annotations.SerializedName

data class SplashDataClass(
    @SerializedName("fixed_parameters") var fixedParameters: FixedParametersEntity? = null,
    @SerializedName("strings") var strings: ArrayList<StringEntity?>,
    @SerializedName("user") var user: UserEntity? = null,
    @SerializedName("restore") val restore: DeviceDataClass? = null,
    @SerializedName("announcements") val announcements: List<AnnouncementEntity>? = null,
    @SerializedName("new_version_available") val newVersionAvailable: Boolean? = null,
    @SerializedName("server_down") val serverDown: Boolean? = null,
    @SerializedName("track_user") val trackUser: TrackUserEntity? = null,
)