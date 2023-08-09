package com.adirahav.diraleashkaa.common

import com.adirahav.diraleashkaa.common.Utilities.percentToFraction
import kotlin.math.pow

object AmortizationScheduleFormulas {
    /*
    » pmt: calculated mortgage or annuity payment / yield per period.
    » fv: future value of loan or annuity.
    » nper: number of total payments / periods.
    » pv: present value -- borrowed or invested principal.
    » period: period (payment number)
    » rate: periodic interest rate represented as a decimal.
    » type: when payment is made: beginning of period is 1; end, 0.
    */

    private const val TAG = "AmortizationScheduleFormulas"

    // payment amount per period (how much each payment will be)
    fun PMT(interest: Double, nper: Int, mortgage: Double/*, type: Int*/) : Double {

        val rate = interest.percentToFraction().div(12)
        val compoundInterest = (rate.plus(1)).pow(nper)
        val pmt = mortgage.div(
            1.minus((1.div(compoundInterest))).div(rate)
        )
        //Utilities.log(Enums.LogType.Debug, TAG, "PMT: pmt = ${pmt}")
        return pmt
    }

    // interest portion of a loan payment
    fun  IPMT(interest: Double, per: Int, nper: Int, pv: Double/*, fv: Double, type: Int*/) : Double {
        val rate = interest.percentToFraction().div(12)

        val ipmt = ((pv.times(rate)).times(((rate.plus(1)).pow(nper.plus(1))).minus((rate.plus(1)).pow(per)))).div(
            (rate.plus(1)).times(((rate.plus(1)).pow(nper)).minus(1))
        )

        return ipmt

        //=(pv*rate*((rate + 1)^(nper + 1) - (rate + 1)^per)) / ((rate + 1)* ((rate + 1)^nper - 1))
    }

    // principal payment amount per period (how much of the principal is being paid in any given pay period)
    fun PPMT(interest: Double, per: Int, nper: Int, pv: Double) : Double {
        val pmt = PMT(interest, nper, pv/*, fv, type*/)
        val impt = IPMT(interest, per, nper, pv/*, fv, type*/)
        val ppmt = pmt.minus(impt)
        return ppmt
    }
}