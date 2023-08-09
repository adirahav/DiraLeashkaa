package com.adirahav.diraleashkaa.data.network.entities

import com.google.gson.annotations.SerializedName

data class YieldForecastEntity (
    @SerializedName("month_no") var monthNo: Int? = null,
    @SerializedName("property_price") var propertyPrice: Double? = null,
    @SerializedName("rent") var rent: Double? = null,
    @SerializedName("accumulated_rent") var accumulatedRent: Double? = null,
    @SerializedName("financing_costs") var financingCosts: Double? = null,
    @SerializedName("accumulated_financing_costs") var accumulatedFinancingCosts: Double? = null,
    @SerializedName("insurance_cost") var insuranceCost: Double? = null,
    @SerializedName("valuation_in_realization") var valuationInRealization: Double? = null,
    @SerializedName("commendation_tax") var commendationTax: Double? = null,
    @SerializedName("profit") var profit: Double? = null,
    @SerializedName("profit_npv") var profitNPV: Double? = null,
    @SerializedName("total_return") var totalReturn: Double? = null,
    @SerializedName("return_on_equity") var returnOnEquity: Double? = null
)