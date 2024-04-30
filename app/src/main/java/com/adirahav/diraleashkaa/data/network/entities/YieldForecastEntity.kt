package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class YieldForecastEntity (
    @SerializedName("monthNo") var monthNo: Int? = null,
    @SerializedName("propertyPrice") var propertyPrice: Double? = null,
    @SerializedName("rent") var rent: Double? = null,
    @SerializedName("accumulatedRent") var accumulatedRent: Double? = null,
    @SerializedName("financingCosts") var financingCosts: Double? = null,
    @SerializedName("accumulatedFinancingCosts") var accumulatedFinancingCosts: Double? = null,
    @SerializedName("insuranceCost") var insuranceCost: Double? = null,
    @SerializedName("valuationInRealization") var valuationInRealization: Double? = null,
    @SerializedName("commendationTax") var commendationTax: Double? = null,
    @SerializedName("profit") var profit: Double? = null,
    @SerializedName("profitNpv") var profitNPV: Double? = null,
    @SerializedName("totalReturn") var totalReturn: Double? = null,
    @SerializedName("returnOnEquity") var returnOnEquity: Double? = null

)