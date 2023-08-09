package com.adirahav.diraleashkaa.common

import com.adirahav.diraleashkaa.data.network.entities.AmortizationScheduleEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.YieldForecastEntity
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Property
    (property: PropertyEntity?) {

    companion object {
        private const val TAG = "PropertyEntity"

        fun init(property: PropertyEntity?): Property {
            return Property(property)
        }
    }

    // amortization schedule
    private val amortizationScheduleType: Type = object : TypeToken<List<AmortizationScheduleEntity>>() {}.type
    var amortizationScheduleArray: List<AmortizationScheduleEntity>? = null

    // yield forecast
    private val yieldForecastType: Type = object : TypeToken<List<YieldForecastEntity>>() {}.type
    var yieldForecastListArray: List<YieldForecastEntity>? = null

    init {
        amortizationScheduleArray = Utilities.parseArray(
            json = property?.calcAmortizationScheduleList,
            typeToken = amortizationScheduleType
        )

        yieldForecastListArray = Utilities.parseArray(
            json = property?.calcYieldForecastList,
            typeToken = yieldForecastType
        )
    }
}