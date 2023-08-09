package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class RegistrationEntity(
	@SerializedName("registration_expired_time") val registrationExpireDate: Long,
	@SerializedName("subscriber_type") val subscriberType: String,
)
