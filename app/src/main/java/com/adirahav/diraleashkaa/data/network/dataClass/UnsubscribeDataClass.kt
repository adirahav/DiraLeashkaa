package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.*
import com.google.gson.annotations.SerializedName

data class UnsubscribeDataClass(
	@SerializedName("user") var user: Int? = null,
	@SerializedName("users_to_announcements") var usersToAnnouncements: Int? = null,
	@SerializedName("users_to_registrations") var usersToRegistrations: Int? = null,
	@SerializedName("properties") var properties: Int? = null
)
