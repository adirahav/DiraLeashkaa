package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.EmailEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.google.gson.annotations.SerializedName

data class EmailDataClass(
	@SerializedName("email") var email: EmailEntity? = null
)
