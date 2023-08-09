package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class PropertyInputEntity(
    @SerializedName("name") val name: String,
    @SerializedName("default") var default: Float,
    @SerializedName("min") val min: Float,
    @SerializedName("max") val max: Float,
    @SerializedName("step") val step: Float,
    @SerializedName("delta") var delta: Float,
)
