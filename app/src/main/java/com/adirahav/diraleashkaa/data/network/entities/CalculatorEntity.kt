package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class CalculatorEntity(
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,
	@SerializedName("_id")	var _id: String? = null,
	@SerializedName("type") var type: String? = null,
	@SerializedName("is_lock") var isLock: Boolean? = null,
	@SerializedName("is_coming_soon") var isComingSoon: Boolean? = null,
)
