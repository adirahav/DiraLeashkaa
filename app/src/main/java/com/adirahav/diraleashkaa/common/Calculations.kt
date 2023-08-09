package com.adirahav.diraleashkaa.common

import com.adirahav.diraleashkaa.common.Utilities.floatFormat
import com.adirahav.diraleashkaa.common.Utilities.fractionToPercent
import com.adirahav.diraleashkaa.common.Utilities.percentToFraction
import com.adirahav.diraleashkaa.common.Utilities.percentToMultipleFraction
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.*
import kotlin.math.pow
import kotlin.math.roundToInt

object Calculations {

    private const val TAG = "Calculations"

    //region calculation
    fun calcAmortizationSchedule(propertyData: PropertyEntity?, indexesAndInterestsArray: List<PropertyInputEntity>?, averageInterestsArray: List<AverageInterestEntity>?) : MutableList<AmortizationScheduleEntity> {

        val amortizationScheduleList = mutableListOf<AmortizationScheduleEntity>()

        /////
        val indexPercent =
            if (propertyData?.calcIndexPercent == null)
                (indexesAndInterestsArray?.find { it.name == Const.INDEX_PERCENT })?.default ?: 0F
            else
                propertyData.calcIndexPercent!!.floatFormat(1)

        val interestPercent =
            if (propertyData?.calcInterestPercent == null) {
                val routesAverageInterest = (averageInterestsArray?.find { it.upToYears == propertyData?.calcMortgagePeriod })

                indexesAndInterestsArray?.find { it.name == Const.INTEREST_PERCENT }?.default =
                    ((routesAverageInterest?.prime ?: 0F)
                        .plus(routesAverageInterest?.linked ?: 0F)
                        .plus(routesAverageInterest?.notLinked ?: 0F)).div(3).floatFormat(1)

                (indexesAndInterestsArray?.find { it.name == Const.INTEREST_PERCENT })?.default ?: 0F
            }
            else
                propertyData.calcInterestPercent!!.floatFormat(1)

        val interestIn5YearsDeltaPercent =
            if (propertyData?.calcInterestIn5YearsPercent == null)
                (indexesAndInterestsArray?.find { it.name == "interest_in_5_years_delta_percent" })?.default ?: 0F
            else
                propertyData.calcInterestIn5YearsPercent!!.floatFormat(1)

        val interestIn10YearsDeltaPercent =
            if (propertyData?.calcInterestIn10YearsPercent == null)
                (indexesAndInterestsArray?.find { it.name == "interest_in_10_years_delta_percent" })?.default ?: 0F
            else
                propertyData.calcInterestIn10YearsPercent!!.floatFormat(1)

        val averageInterestAtTakingPercent =
            if (propertyData?.calcAverageInterestAtTakingPercent == null)
                (indexesAndInterestsArray?.find { it.name == Const.AVERAGE_INTEREST_AT_TAKING_PERCENT })?.default ?: 0F
            else
                propertyData.calcAverageInterestAtTakingPercent!!.floatFormat(1)

        val averageInterestAtMaturityPercent =
            if (propertyData?.calcAverageInterestAtMaturityPercent == null)
                (indexesAndInterestsArray?.find { it.name == Const.AVERAGE_INTEREST_AT_MATURITY_PERCENT })?.default ?: 0F
            else
                propertyData.calcAverageInterestAtMaturityPercent!!.floatFormat(1)

        /////

        val months = propertyData?.calcMortgagePeriod!!.times(12)
        val pmt = AmortizationScheduleFormulas.PMT(
            interestPercent.toDouble(),
            months,
            propertyData?.calcMortgageRequired!!.toDouble()
        )

        var str1 = "\n=== Interests ======================="
        str1 += "\n indexPercent = " + indexPercent
        str1 += "\n interestPercent = " + interestPercent
        str1 += "\n interestIn5YearsDeltaPercent = " + interestIn5YearsDeltaPercent
        str1 += "\n interestIn10YearsDeltaPercent = " + interestIn10YearsDeltaPercent
        str1 += "\n averageInterestAtTakingPercent = " + averageInterestAtTakingPercent
        str1 += "\n averageInterestAtMaturityPercent = " + averageInterestAtMaturityPercent
        str1 += "\n months = " + months
        str1 += "\n pmt = " + pmt
        Utilities.log(Enums.LogType.Debug, TAG, "str = ${str1}")

        for (monthNo in 0..propertyData.calcMortgagePeriod!!.times(12)) {

            val prevMonth = if (monthNo > 0) amortizationScheduleList[monthNo-1] else null

            val item = AmortizationScheduleEntity()

            // monthNo
            item.monthNo = monthNo

            // beginning of period fund קרן תחילת תקופה
            item.fundBOP =
                if (monthNo == 0) propertyData.calcMortgageRequired!!.toDouble()
                else prevMonth?.fundEOP!!
                    .plus(prevMonth.fundEOP!!
                        .times(((prevMonth.index!!.percentToFraction()
                            .plus(1)).pow(1.0f.div(12))).minus(1)))

            // index מדד
            item.index = indexPercent

            // interest ריבית
            item.interest =
                when {
                    monthNo >= 120 -> interestIn10YearsDeltaPercent
                    monthNo >= 60 -> interestIn5YearsDeltaPercent
                    else -> interestPercent
                }

            // fund refund החזר קרן
            item.fundRefund =
                if (monthNo == 0) 0.0f
                else AmortizationScheduleFormulas.PPMT(
                    interestPercent.toDouble(),
                    1,
                    months.minus(monthNo).plus(1),
                    item.fundBOP!!
                ).toFloat()

            // interest repayment החזר ריבית
            item.interestRepayment =
                if (monthNo == 0) 0.0f
                else (item.fundBOP!!.times(item.interest!!.percentToFraction()).div(12)).toFloat()

            // monthly repayments החזר חודשי
            item.monthlyRepayments =
                if (monthNo == 0) 0.0f
                else item.fundRefund!!.plus(item.interestRepayment!!)

            // early repayment פרעון מוקדם
            item.earlyRepayment = 0.0

            // end of period fund קרן סוף תקופה
            item.fundEOP =
                if (monthNo == 0) item.fundBOP
                else item.fundBOP!!.minus(item.fundRefund!!).minus(item.earlyRepayment!!)

            // beginning of period interest discounting היוון ריבית תחילת תקופה
            item.interestDiscountingBOP =
                if (monthNo == 0) 0.0
                else pmt.div(1.plus(averageInterestAtTakingPercent.percentToFraction().div(12)).pow(monthNo))


            // end of period interest discounting היוון ריבית סוף תקופה
            item.interestDiscountingEOP =
                if (monthNo == 0) 0.0
                else pmt.div(((averageInterestAtMaturityPercent.percentToFraction().div(12)).plus(1)).pow(monthNo))

            // discount הנחה
            item.discount =
                when {
                    monthNo >= 60-1 -> 30
                    monthNo >= 36-1 -> 20
                    else -> 0
                }

            // early repayment fee עמלת פרעון מוקדם // TODO
            item.earlyRepaymentFee = 0.0
            /*if (monthNo > 0) {
                earlyRepaymentFee = fundRefund.plus(interestRepayment)
                // SUM(X12:$X$371) - SUM(W12:$W$371))*(1-discount)
                //=MAX((SUM(X12:$X$371)-SUM(W12:$W$371))*(1-[@הנחה]),0)
            }*/

            amortizationScheduleList.add(item)
        }

        return amortizationScheduleList
    }

    // הון עצמי בניכוי הוצאות נלוות
    fun calcEquityCleaningExpenses(equity: Int?, incidentalsTotal: Int?): Int? {

        return  if (equity == null || incidentalsTotal == null)
                    null
                else
                    equity.minus(incidentalsTotal)
    }

    // משכנתא נדרשת
    fun calcMortgageRequired(price: Int?, equityCleaningExpenses: Int?) : Int? {

        return  if (price == null || equityCleaningExpenses == null)
                    null
                else {
                    if (price.minus(equityCleaningExpenses) < 0)
                        0
                    else
                        price.minus(equityCleaningExpenses)
        }

    }

    // הכנסה פנויה
    fun calcDisposableIncome(incomes: Int?, commitments: Int?): Int? {

        return  if (incomes == null || commitments == null)
                    null
                else
                    incomes.minus(commitments)
    }

    // החזר חודשי אפשרי
    fun calcPossibleMonthlyRepayment(
        disposableIncome: Int?,
        possibleMonthlyRepaymentActualValue: Float?
    ): Int? {

        return  if (disposableIncome == null)
                    null
                else
                    (possibleMonthlyRepaymentActualValue?.times(disposableIncome.toDouble()))?.toInt()
    }

    // אחוז מימון מקסימלי
    fun calcMaxPercentOfFinancing(
        apartmentType: String?,
        maxPercentOfFinancingArray: List<SpinnerEntity>?
    ): Int? {

        return  if (apartmentType == null)
                    null
                else
                    maxPercentOfFinancingArray?.find { it.key == apartmentType }?.value?.toInt()
    }

    // אחוז מימון בפועל
    fun calcActualPercentOfFinancing(
        apartmentType: String?,
        price: Int?,
        equityCleaningExpenses: Int?
    ): Int? {

        return  if (apartmentType == null || price == null || equityCleaningExpenses == null)
                    null
                else
                    if ((1 - equityCleaningExpenses.toFloat().div(price.toFloat())).fractionToPercent() < 0)
                        0
                    else
                        (1 - equityCleaningExpenses.toFloat().div(price.toFloat())).fractionToPercent()
    }

    // מס רכישה
    fun calcTransferTax(apartmentType: String?, price: Int?, transferTaxArray: List<TransferTaxModel>?) : Int? {

        val taxBrackets =
            if (price == null || apartmentType == null)
                null
            else
                (transferTaxArray?.find {
                    it.apartmentType == apartmentType
                })?.taxBrackets


        if (taxBrackets != null) {
            for (i in taxBrackets.indices) {
                if ((price!! > taxBrackets[i].min && price <= taxBrackets[i].max) ||
                    (price > taxBrackets[i].min && i == taxBrackets.size - 1)) {

                    var transferTax =
                        (price - taxBrackets[i].min) * taxBrackets[i].taxPercent.percentToFraction()

                    for (j in i - 1 downTo 0) {
                        transferTax += (taxBrackets[j].max - taxBrackets[j].min) * taxBrackets[j].taxPercent.percentToFraction()
                    }

                    return transferTax.toInt()
                }
            }
        }

        return null
    }

    // עלות עורך דין
    fun calcLawyer(
        price: Int?,
        lawyerCustomValue: Int?,
        lawyerActualValue: Float?,
        vatPercent: Float?
    ): Int? {

        return  when {
                    price == null -> null
                    lawyerCustomValue == null -> {
                        val lawyerPercent = lawyerActualValue?.percentToFraction()
                        val lawyerExcludingVAT = (price.times(lawyerPercent!!.toDouble()))
                        lawyerExcludingVAT.times(vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()
                    }
                    else -> null
        }
    }

    // עלות מתווך
    fun calcRealEstateAgent(price: Int?, realEstateAgentCustomValue: Int?, realEstateAgentActualValue: Float?, vatPercent: Float?) : Int? {

        return  when {
                    price == null -> null
                    realEstateAgentCustomValue == null -> {
                        val realEstateAgentPercent = realEstateAgentActualValue?.percentToFraction()
                        val realEstateAgentExcludingVAT = (price.times(realEstateAgentPercent!!.toDouble()))
                        realEstateAgentExcludingVAT.times(vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()
                    }
                    else -> null
        }
    }

    // סה"כ הוצאות נלוות
    fun calcIncidentalsTotal(transferTax: Int?, lawyer: Int?, lawyerCustomValue: Int?, realEstateAgent: Int?, realEstateAgentCustomValue: Int?, brokerMortgage: Int?, repairing: Int?) : Int {

        var incidentalsTotal = 0

        if (transferTax != null) {
            incidentalsTotal = incidentalsTotal.plus(transferTax)
        }

        if (lawyer != null) {
            incidentalsTotal = incidentalsTotal.plus(lawyer)
        }
        else if (lawyerCustomValue != null) {
            incidentalsTotal = incidentalsTotal.plus(lawyerCustomValue)
        }

        if (realEstateAgent != null) {
            incidentalsTotal = incidentalsTotal.plus(realEstateAgent)
        }
        else if (realEstateAgentCustomValue != null) {
            incidentalsTotal = incidentalsTotal.plus(realEstateAgentCustomValue)
        }

        if (brokerMortgage != null) {
            incidentalsTotal = incidentalsTotal.plus(brokerMortgage)
        }

        if (repairing != null) {
            incidentalsTotal = incidentalsTotal.plus(repairing)
        }

        return incidentalsTotal
    }

    // צפי לשכר דירה
    fun calcRent(
        price: Int?,
        rentCustomValue: Int?,
        rentPercent: Float?,
        propertyInputsArray: List<PropertyInputEntity>?
    ): Int? {

        return  if (price == null) {
                    null
                }
                else if (rentCustomValue == null) {
                    if (rentPercent != null) {
                        val rentActualPerYear = price.times(rentPercent.percentToFraction())
                        val rentActualPerMonth = rentActualPerYear.div(12)

                        rentActualPerMonth.roundToInt()
                    }
                    else {
                        val rentDefaultPercent =
                            ((propertyInputsArray?.find { it.name == Const.RENT_PERCENT })?.default
                                ?: 0f).percentToFraction()
                        val rentDefaultPerYear = (price.times(rentDefaultPercent.toDouble()))
                        val rentDefaultPerMonth = rentDefaultPerYear.div(12)

                        rentDefaultPerMonth.roundToInt()
                    }
                }
                else {
                    null
                }
    }

    // החזר חודשי של המשכנתא
    fun calcMortgageMonthlyRepayment(mortgagePeriod: Int?, mortgageRequired: Int?) : Int? {

        return  if (mortgagePeriod != null && mortgagePeriod != 0 && mortgageRequired != null)
                    mortgageRequired.toFloat()
                        .div(100000)
                        .times(400.plus((30.minus(mortgagePeriod))
                            .times(13))).toInt()
                else
                    null
    }

    // תשואה חודשית
    fun calcMortgageMonthlyYield(rentCleaningExpenses: Int?, mortgageMonthlyRepayment: Int?) : Int? {

        return  if (rentCleaningExpenses != null && mortgageMonthlyRepayment != null)
                    rentCleaningExpenses.minus(mortgageMonthlyRepayment)
                else
                    null
    }

    // אחוז ריבית
    fun calcInterestPercent(mortgagePeriod: Int?, percentOfFinancing: Int?, fixedParameters: FixedParameters?): Float {
        val routesAverageInterest = (fixedParameters?.averageInterestsArray?.find { it.upToYears == mortgagePeriod })

        return ((routesAverageInterest?.prime ?: 0F)
            .plus(routesAverageInterest?.linked ?: 0F)
            .plus(routesAverageInterest?.notLinked ?: 0F))
            .div(3)
            .plus((fixedParameters?.additionalInterestsArray?.find { it.upToFunding >= percentOfFinancing!! })?.delta ?: 0F)
            .floatFormat(1)
    }
    //endregion calculation



}