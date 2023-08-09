package com.adirahav.diraleashkaa.ui.registration

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.GooglePayProgramTypeEntity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.util.*


class RegistrationGooglePayProgramsAdapter(
        private val context: Context,
        private val listener: OnProgramAdapter,
        private val fragment: RegistrationGooglePayFragment
) : RecyclerView.Adapter<RegistrationGooglePayProgramsAdapter.ViewHolder>() {

    //region == companion ==========

    companion object {
        private const val TAG = "SignUpGooglePayProgramsAdapter"
    }

    //endregion == companion ==========

    // layout
    //private var layout: AdapterGooglePayProgramsBinding? = null

    // resources
    val resources: Resources = AppApplication.context.resources

    // program
    private var programs: List<GooglePayProgramTypeEntity?>?

    interface OnProgramAdapter {
        fun onProgramClicked(property: GooglePayProgramTypeEntity?, selectedPosition: Int)
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        // container
        @JvmField
        @BindView(R.id.container)
        var container: CardView? = null

        // data container
        @JvmField
        @BindView(R.id.containerData)
        var containerData: ConstraintLayout? = null

        // data container - border
        @JvmField
        @BindView(R.id.imageSelected)
        var imageSelected: ImageView? = null

        // data container - period
        @JvmField
        @BindView(R.id.period)
        var period: TextView? = null

        // data container - picture
        @JvmField
        @BindView(R.id.picture)
        var picture: de.hdodenhof.circleimageview.CircleImageView? = null

        // data container - price
        @JvmField
        @BindView(R.id.price)
        var price: TextView? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        programs = ArrayList()
    }

    fun setItems(programs: List<GooglePayProgramTypeEntity?>?) {
        this.programs = programs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_google_pay_program, parent, false)
        return ViewHolder(view)
    }

    @DelicateCoroutinesApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property = programs?.get(position)

        // container
        holder.container!!.setOnClickListener {
            if (position == fragment.selectedPosition) {
                return@setOnClickListener
            }

            holder.imageSelected!!.visibility = VISIBLE

            if (holder.picture?.isVisible!!) {
                Picasso.with(context)
                    .load(R.drawable.icon_program_selected)
                    .into(holder.picture)
            }

            if (fragment.selectedPosition != null) {
                val _selectedHolder = fragment.layout.programsList.findViewHolderForAdapterPosition(fragment.selectedPosition!!)  as RegistrationGooglePayProgramsAdapter.ViewHolder?

                if (_selectedHolder != null) {
                    _selectedHolder.imageSelected!!.visibility = GONE

                    if (_selectedHolder.picture?.isVisible!!) {
                        Picasso.with(context)
                            .load(R.drawable.icon_program)
                            .into(_selectedHolder.picture)
                    }
                }
            }

            listener.onProgramClicked(property, position)
        }

        // data container - border
        holder.imageSelected!!.visibility =
            if (position == fragment.selectedPosition)
                VISIBLE
            else
                GONE

        // data container - period
        holder.period!!.text = getPeriod(property?.durationValue, property?.durationUnit)

        // data container - picture
        if (property?.durationUnit?.lowercase().equals("u")) {
            holder.picture?.visibility = GONE
        }
        else {
            Picasso.with(context)
                .load(
                    if (position == fragment.selectedPosition)
                        R.drawable.icon_program_selected
                    else
                        R.drawable.icon_program)

                .into(holder.picture)

            holder.picture?.visibility = VISIBLE
        }

        // data container - price
        if (property?.price != null) {
            val priceString = Utilities.getRoomString("signup_google_pay_program_price")
            if (priceString.isNotEmpty()) {
                holder.price!!.text = String.format(priceString, property.price)
            }
        }
    }



    private fun getPeriod(count: Int?, unit: String?) : String {
        when (unit?.lowercase()) {
            "d" ->
                when {
                    count == 1 -> return Utilities.getRoomString("signup_google_pay_program_1_day")
                    count == 2 -> return Utilities.getRoomString("signup_google_pay_program_2_days")
                    count ?: 0 > 2 -> {
                        val daysString = Utilities.getRoomString("signup_google_pay_program_many_days")
                        if (daysString.isNotEmpty()) {
                            return String.format(daysString, count)
                        }
                    }
                }
            "w" ->
                when {
                    count == 1 -> return Utilities.getRoomString("signup_google_pay_program_1_week")
                    count == 2 -> return Utilities.getRoomString("signup_google_pay_program_2_weeks")
                    count ?: 0 > 2 -> {
                        val weeksString = Utilities.getRoomString("signup_google_pay_program_many_weeks")
                        if (weeksString.isNotEmpty()) {
                            return String.format(weeksString, count)
                        }
                    }
                }

            "m" ->
                when {
                    count == 1 -> return Utilities.getRoomString("signup_google_pay_program_1_month")
                    count == 2 -> return Utilities.getRoomString("signup_google_pay_program_2_monthes")
                    count ?: 0 > 2 -> {
                        val monthsString = Utilities.getRoomString("signup_google_pay_program_many_monthes")
                        if (monthsString.isNotEmpty()) {
                            return String.format(monthsString, count)
                        }
                    }
                }
            "y" ->
                when {
                    count == 1 -> return Utilities.getRoomString("signup_google_pay_program_1_year")
                    count == 2 -> return Utilities.getRoomString("signup_google_pay_program_2_years")
                    count ?: 0 > 2 -> {
                        val yearsString = Utilities.getRoomString("signup_google_pay_program_many_years")
                        if (yearsString.isNotEmpty()) {
                            return String.format(yearsString, count)
                        }
                    }
                }
            "u" -> return Utilities.getRoomString("signup_google_pay_program_unlimited")
        }

        return ""
    }

    override fun getItemCount(): Int {
        return programs?.size ?: 0
    }
}