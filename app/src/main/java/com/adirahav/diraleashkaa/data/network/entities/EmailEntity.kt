package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class EmailEntity(
    @SerializedName("user_uuid") var userUUID: String?,
    @SerializedName("type") var type: String?,
    @SerializedName("subject") var subject: String?,
    @SerializedName("message") var message: Int?
)