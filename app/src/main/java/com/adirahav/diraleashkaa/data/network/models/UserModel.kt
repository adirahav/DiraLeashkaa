package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.google.gson.annotations.SerializedName

data class UserModel(
        @SerializedName("user") var user: UserEntity? = null
)
