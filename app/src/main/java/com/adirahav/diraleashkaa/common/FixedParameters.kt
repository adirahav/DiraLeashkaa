package com.adirahav.diraleashkaa.common

import com.adirahav.diraleashkaa.data.network.dataClass.*
import com.adirahav.diraleashkaa.data.network.entities.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class FixedParameters
    (fixedParameters: FixedParametersEntity?) {

    companion object {
        private const val TAG = "FixedParameters"

        fun init(fixedParameters: FixedParametersEntity?): FixedParameters {
            return FixedParameters(fixedParameters)
        }
    }

    // vat percent
    var vatPercent: Float? = null

    // mortgage max age
    var mortgageMaxAge: Int? = null

    // trial period
    private val trialPeriodType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var trialPeriodArray: List<SpinnerEntity>? = null

    // expiration alert
    private val expirationAlertType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    private var expirationAlertArray: List<SpinnerEntity>? = null

    // app version
    private val appVersionType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var appVersionArray: List<SpinnerEntity>? = null

    // google pay
    private val googlePayType: Type = GooglePayProgramDataClass::class.java
    var googlePayObject: GooglePayProgramDataClass? = null

    // best yield
    private val bestYieldType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var bestYieldArray: List<SpinnerEntity>? = null

    // property inputs
    private val propertyInputsType: Type = object : TypeToken<List<PropertyInputEntity>>() {}.type
    var propertyInputsArray: List<PropertyInputEntity>? = null

    // property values
    private val propertyValuesType: Type = object : TypeToken<List<PropertyValueEntity>>() {}.type
    var propertyValuesArray: List<PropertyValueEntity>? = null

    // indexes and interests
    private val indexesAndInterestsType: Type = object : TypeToken<List<PropertyInputEntity>>() {}.type
    var indexesAndInterestsArray: List<PropertyInputEntity>? = null

    // average interests
    private val averageInterestsType: Type = object : TypeToken<List<AverageInterestEntity>>() {}.type
    var averageInterestsArray: List<AverageInterestEntity>? = null

    // additional interests
    private val additionalInterestsType: Type = object : TypeToken<List<AdditionalInterestEntity>>() {}.type
    var additionalInterestsArray: List<AdditionalInterestEntity>? = null

    // cities
    private val cityType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var citiesArray: List<SpinnerEntity>? = null

    // apartment types
    private val apartmentTypesType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var apartmentTypesArray: List<SpinnerEntity>? = null

    // contact us
    private val contactUsType: Type = ContactUsEntity::class.java
    var contactUsObject: ContactUsEntity? = null

    // mortgage period
    private val mortgagePeriodType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var mortgagePeriodArray: List<SpinnerEntity>? = null

    // sms
    private val smsType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var smsArray: List<SpinnerEntity>? = null

    // picture
    private val pictureType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var pictureArray: List<SpinnerEntity>? = null

    // on error
    private val onErrorType: Type = object : TypeToken<List<SpinnerEntity>>() {}.type
    var onErrorArray: List<SpinnerEntity>? = null

    init {
        vatPercent = fixedParameters?.vatPercent

        mortgageMaxAge = fixedParameters?.mortgageMaxAge

        trialPeriodArray = Utilities.parseArray(
            json = fixedParameters?.trialPeriod,
            typeToken = trialPeriodType
        )
        expirationAlertArray = Utilities.parseArray(
            json = fixedParameters?.trialPeriod,
            typeToken = expirationAlertType
        )
        appVersionArray = Utilities.parseArray(
            json = fixedParameters?.appVersion,
            typeToken = appVersionType
        )
        googlePayObject = Utilities.parseObject(
            json = fixedParameters?.googlePay,
            typeToken = googlePayType
        )
        bestYieldArray = Utilities.parseArray(
            json = fixedParameters?.bestYield,
            typeToken = bestYieldType
        )
        propertyInputsArray = Utilities.parseArray(
            json = fixedParameters?.propertyInputs,
            typeToken = propertyInputsType
        )
        propertyValuesArray = Utilities.parseArray(
            json = fixedParameters?.propertyValues,
            typeToken = propertyValuesType
        )
        indexesAndInterestsArray = Utilities.parseArray(
            json = fixedParameters?.indexesAndInterests,
            typeToken = indexesAndInterestsType
        )
        averageInterestsArray = Utilities.parseArray(
            json = fixedParameters?.averageInterests,
            typeToken = averageInterestsType
        )
        additionalInterestsArray = Utilities.parseArray(
            json = fixedParameters?.additionalInterests,
            typeToken = additionalInterestsType
        )
        citiesArray = Utilities.parseArray(
            json = fixedParameters?.cities,
            typeToken = cityType
        )
        apartmentTypesArray = Utilities.parseArray(
            json = fixedParameters?.apartmentTypes,
            typeToken = apartmentTypesType
        )
        contactUsObject = Utilities.parseObject(
            json = fixedParameters?.contactUs,
            typeToken = contactUsType
        )
        mortgagePeriodArray = Utilities.parseArray(
            json = fixedParameters?.mortgagePeriods,
            typeToken = mortgagePeriodType
        )
        smsArray = Utilities.parseArray(
            json = fixedParameters?.sms,
            typeToken = smsType
        )
        pictureArray = Utilities.parseArray(
            json = fixedParameters?.picture,
            typeToken = pictureType
        )
        onErrorArray = Utilities.parseArray(
            json = fixedParameters?.onError,
            typeToken = onErrorType
        )
    }


}