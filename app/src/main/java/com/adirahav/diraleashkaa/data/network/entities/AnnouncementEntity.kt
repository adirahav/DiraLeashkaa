package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class AnnouncementEntity(
	@SerializedName("_id")	var _id: String? = null,
	@SerializedName("title") val title: String? = null,
	@SerializedName("message") val message: String? = null,
	@SerializedName("positive_button_text") val positiveButtonText: String? = null,
	@SerializedName("confirm") val confirm: Boolean? = null,
)