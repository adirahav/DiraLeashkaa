package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.floatFormat
import com.adirahav.diraleashkaa.data.network.entities.AmortizationScheduleEntity
import com.airbnb.paris.extensions.style
import java.util.*

class PropertyAmortizationScheduleAdapter(
    private val context: Context,
    private val listener: PropertyActivity
) : RecyclerView.Adapter<PropertyAmortizationScheduleAdapter.ViewHolder>() {

    private var list: List<AmortizationScheduleEntity?>

    var textViewParam: LinearLayout.LayoutParams? = null

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        // pay no
        @JvmField
        @BindView(R.id.payNo)
        var payNo: LinearLayout? = null

        // year number
        @JvmField
        @BindView(R.id.yearNo)
        var yearNo: TextView? = null

        // month number
        @JvmField
        @BindView(R.id.monthNo)
        var monthNo: TextView? = null

        // beginning of period fund
        @JvmField
        @BindView(R.id.fundBOP)
        var fundBOP: TextView? = null

        // interest
        @JvmField
        @BindView(R.id.interest)
        var interest: TextView? = null

        // monthly repayments
        @JvmField
        @BindView(R.id.monthlyRepayments)
        var monthlyRepayments: TextView? = null

        // fund refund
        @JvmField
        @BindView(R.id.fundRefund)
        var fundRefund: TextView? = null

        // interest repayment
        @JvmField
        @BindView(R.id.interestRepayment)
        var interestRepayment: TextView? = null

        // end of period fund
        @JvmField
        @BindView(R.id.fundEOP)
        var fundEOP: TextView? = null

        // beginning of period interest discounting
        //@JvmField
        //@BindView(R.id.interestDiscountingBOP)
        //var interestDiscountingBOP: TextView? = null

        // end of period interest discounting
        //@JvmField
        //@BindView(R.id.interestDiscountingEOP)
        //var interestDiscountingEOP: TextView? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        list = ArrayList()
    }

    fun setItems(list: List<AmortizationScheduleEntity?>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property_amortization_schedule, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // year number
        if (item?.monthNo?.rem(12) == 1) {
            holder.yearNo!!.text =  HtmlCompat.fromHtml(String.format(
                Utilities.getRoomString("amortization_schedule_year_value"),
                item.monthNo?.div(12).toString()
            ), HtmlCompat.FROM_HTML_MODE_LEGACY)

        }
        else {
            holder.yearNo!!.text = ""
        }

        holder.payNo!!.style(listener.getCellStyle(item?.monthNo!!, true))

        // month number
        holder.monthNo!!.text = item.monthNo.toString()

        // beginning of period fund
        holder.fundBOP!!.text = Utilities.getDecimalNumber((item.fundBOP as Double).toInt())
        holder.fundBOP!!.style(listener.getCellStyle(item.monthNo!!))

        // interest
        holder.interest!!.text = item.interest?.floatFormat(1).toString() + "%"
        holder.interest!!.style(listener.getCellStyle(item.monthNo!!))

        // monthly repayments
        holder.monthlyRepayments!!.text = Utilities.getDecimalNumber((item.monthlyRepayments)!!.toInt())
        holder.monthlyRepayments!!.style(listener.getCellStyle(item.monthNo!!))

        // fund refund
        holder.fundRefund!!.text = Utilities.getDecimalNumber((item.fundRefund)!!.toInt())
        holder.fundRefund!!.style(listener.getCellStyle(item.monthNo!!))

        // interest repayment
        holder.interestRepayment!!.text = Utilities.getDecimalNumber((item.interestRepayment)!!.toInt())
        holder.interestRepayment!!.style(listener.getCellStyle(item.monthNo!!))

        // end of period fund
        holder.fundEOP!!.text = Utilities.getDecimalNumber((item.fundEOP as Double).toInt())
        holder.fundEOP!!.style(listener.getCellStyle(item.monthNo!!))

        // beginning of period interest discounting
        //holder.interestDiscountingBOP!!.text = Utilities.getDecimalNumber((item?.interestDiscountingBOP as Double).toInt())
        //holder.interestDiscountingBOP!!.style(listener.getCellStyle(item?.monthNo!!))

        // end of period interest discounting
        //holder.interestDiscountingEOP!!.text = Utilities.getDecimalNumber((item?.interestDiscountingEOP as Double).toInt())
        //holder.interestDiscountingEOP!!.style(listener.getCellStyle(item?.monthNo!!))
    }



    override fun getItemCount(): Int {
        return list.size
    }
}