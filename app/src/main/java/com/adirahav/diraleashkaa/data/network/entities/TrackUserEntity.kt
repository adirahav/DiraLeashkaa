package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class TrackUserEntity(
	@SerializedName("allow_track_user") val allowTrackUser: Boolean,
	@SerializedName("is_track_user") val isTrackUser: Boolean,
	@SerializedName("value_length") val valueLength: Int?,
)
