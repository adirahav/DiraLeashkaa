package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.fractionToPercentFormat
import com.adirahav.diraleashkaa.data.network.entities.YieldForecastEntity
import com.adirahav.diraleashkaa.ui.signup.SignUpPersonalInfoFragment
import com.airbnb.paris.extensions.style
import java.util.*


class PropertyYieldForecastAdapter(
    private val context: Context,
    private val listener: PropertyActivity
) : RecyclerView.Adapter<PropertyYieldForecastAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "PropertyYieldForecastAdapter"
    }

    private var list: List<YieldForecastEntity?>

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

        // property price
        @JvmField
        @BindView(R.id.propertyPrice)
        var propertyPrice: TextView? = null

        // rent
        @JvmField
        @BindView(R.id.rent)
        var rent: TextView? = null

        // financing costs
        @JvmField
        @BindView(R.id.financingCosts)
        var financingCosts: TextView? = null

        // valuation in realization
        @JvmField
        @BindView(R.id.valuationInRealization)
        var valuationInRealization: TextView? = null

        // commendation tax
        @JvmField
        @BindView(R.id.commendationTax)
        var commendationTax: TextView? = null

        // profit
        @JvmField
        @BindView(R.id.profit)
        var profit: TextView? = null

        // total return
        @JvmField
        @BindView(R.id.totalReturn)
        var totalReturn: TextView? = null

        // return on equity
        @JvmField
        @BindView(R.id.returnOnEquity)
        var returnOnEquity: TextView? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        list = ArrayList()
    }

    //region == strings ============

    fun setItems(list: List<YieldForecastEntity?>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_property_yield_forecast, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // year number
        if (item?.monthNo?.rem(12) == 1) {
            holder.yearNo!!.text =  HtmlCompat.fromHtml(String.format(
                Utilities.getRoomString("yield_forecast_year_value"),
                item.monthNo?.div(12).toString()
            ), HtmlCompat.FROM_HTML_MODE_LEGACY)

        }
        else {
            holder.yearNo!!.text = ""
        }

        holder.payNo!!.style(listener.getCellStyle(item?.monthNo!!, true))

        // month number
        holder.monthNo!!.text = item.monthNo.toString()

        // property price
        holder.propertyPrice!!.text = Utilities.getDecimalNumber((item.propertyPrice as Double).toInt())
        holder.propertyPrice!!.style(listener.getCellStyle(item.monthNo!!))

        // rent
        holder.rent!!.text = Utilities.getDecimalNumber((item.rent as Double).toInt())
        holder.rent!!.style(listener.getCellStyle(item.monthNo!!))

        // financing costs
        holder.financingCosts!!.text = Utilities.getDecimalNumber((item.financingCosts as Double).toInt())
        holder.financingCosts!!.style(listener.getCellStyle(item.monthNo!!))

        // valuation in realization
        holder.valuationInRealization!!.text = Utilities.getDecimalNumber((item.valuationInRealization as Double).toInt())
        holder.valuationInRealization!!.style(listener.getCellStyle(item.monthNo!!))

        // commendation tax
        holder.commendationTax!!.text = Utilities.getDecimalNumber((item.commendationTax as Double).toInt())
        holder.commendationTax!!.style(listener.getCellStyle(item.monthNo!!))
        holder.commendationTax!!.setTextColor(if (item.commendationTax!! > 0) ContextCompat.getColor(context, R.color.formText) else ContextCompat.getColor(context, R.color.textDisable))

        // profit
        holder.profit!!.text = Utilities.getDecimalNumber((item.profit as Double).toInt())
        holder.profit!!.style(listener.getCellStyle(item.monthNo!!))
        holder.profit!!.setTextColor(if (item.profit!! >= 0) ContextCompat.getColor(context, R.color.formText) else ContextCompat.getColor(context, R.color.textNegative))

        // total return
        holder.totalReturn!!.text = item.totalReturn?.fractionToPercentFormat(2)
        holder.totalReturn!!.style(listener.getCellStyle(item.monthNo!!))
        holder.totalReturn!!.setTextColor(if (item.totalReturn!! >= 0) ContextCompat.getColor(context, R.color.formText) else ContextCompat.getColor(context, R.color.textNegative))

        // return on equity
        holder.returnOnEquity!!.text = item.returnOnEquity?.fractionToPercentFormat(2)
        holder.returnOnEquity!!.style(listener.getCellStyle(item.monthNo!!))
        holder.returnOnEquity!!.setTextColor(if (item.returnOnEquity!! >= 0) ContextCompat.getColor(context, R.color.formText) else ContextCompat.getColor(context, R.color.textNegative))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}