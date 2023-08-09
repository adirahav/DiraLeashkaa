package com.adirahav.diraleashkaa.data.network.entities

import android.util.Log
import com.adirahav.diraleashkaa.common.Utilities
import com.google.gson.annotations.SerializedName

data class AmortizationScheduleEntity (
    @SerializedName("month_no") var monthNo: Int? = null,
    @SerializedName("fund_bop") var fundBOP: Double? = null,
    @SerializedName("index") var index: Float? = null,
    @SerializedName("interest") var interest: Float? = null,
    @SerializedName("fund_refund") var fundRefund: Float? = null,
    @SerializedName("interest_repayment") var interestRepayment: Float? = null,
    @SerializedName("monthly_repayments") var monthlyRepayments: Float? = null,
    @SerializedName("early_repayment") var earlyRepayment: Double? = null,
    @SerializedName("fund_eop") var fundEOP: Double? = null,
    @SerializedName("interest_discounting_bop") var interestDiscountingBOP: Double? = null,
    @SerializedName("interest_discounting_eop") var interestDiscountingEOP: Double? = null,
    @SerializedName("discount") var discount: Int? = null,
    @SerializedName("early_repaymentFee") var earlyRepaymentFee: Double? = null
)