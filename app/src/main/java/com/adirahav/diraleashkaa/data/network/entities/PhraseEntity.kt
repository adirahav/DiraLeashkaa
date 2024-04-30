package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class PhraseEntity(
	@PrimaryKey(autoGenerate = true) var roomSID: Long? = null,
	@SerializedName("key") val key: String,
	@SerializedName("value") val value: String,
)
