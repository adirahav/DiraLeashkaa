package com.adirahav.diraleashkaa.data.network.dataClass

import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.google.gson.annotations.SerializedName

data class HomeDataClass(
    @SerializedName("properties") var properties: List<PropertyEntity>? = null,
    @SerializedName("empty_property") var emptyProperty: PropertyEntity? = null,
    @SerializedName("best_yields") var bestYields: List<BestYieldEntity>? = null,
    @SerializedName("is_properties_need_to_refresh") var isPropertiesNeedToRefresh: Boolean = true,
    @SerializedName("is_best_yields_need_to_refresh") var isBestYieldNeedToRefresh: Boolean = true
)