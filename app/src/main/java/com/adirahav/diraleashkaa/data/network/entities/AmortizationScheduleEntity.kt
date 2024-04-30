package com.adirahav.diraleashkaa.data.network.entities

import android.util.Log
import com.adirahav.diraleashkaa.common.Utilities
import com.google.gson.annotations.SerializedName

data class AmortizationScheduleEntity (
    @SerializedName("monthNo") var monthNo: Int? = null,
    @SerializedName("fundBop") var fundBOP: Double? = null,
    @SerializedName("index") var index: Float? = null,
    @SerializedName("interest") var interest: Float? = null,
    @SerializedName("fundRefund") var fundRefund: Float? = null,
    @SerializedName("interestRepayment") var interestRepayment: Float? = null,
    @SerializedName("monthlyRepayments") var monthlyRepayments: Float? = null,
    @SerializedName("earlyRepayment") var earlyRepayment: Double? = null,
    @SerializedName("fundEop") var fundEOP: Double? = null,
    @SerializedName("interestDiscountingBop") var interestDiscountingBOP: Double? = null,
    @SerializedName("interestDiscountingEop") var interestDiscountingEOP: Double? = null,
    @SerializedName("discount") var discount: Int? = null,
    @SerializedName("earlyRepaymentFee") var earlyRepaymentFee: Double? = null
)