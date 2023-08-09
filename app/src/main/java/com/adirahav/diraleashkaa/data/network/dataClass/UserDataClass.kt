package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.google.gson.annotations.SerializedName

data class UserDataClass(
	@SerializedName("user") var user: UserEntity? = null
)
