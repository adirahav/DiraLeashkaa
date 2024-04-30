package com.adirahav.diraleashkaa.data.network.models

import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.google.gson.annotations.SerializedName

data class HomeModel(
    @SerializedName("properties") var properties: List<PropertyEntity>? = null,
    @SerializedName("emptyProperty") var emptyProperty: PropertyEntity? = null,
    @SerializedName("bestYields") var bestYields: List<BestYieldEntity>? = null,
    @SerializedName("isPropertiesNeedToRefresh") var isPropertiesNeedToRefresh: Boolean = true,
    @SerializedName("isBestYieldsNeedToRefresh") var isBestYieldNeedToRefresh: Boolean = true
)

