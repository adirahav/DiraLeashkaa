package com.adirahav.diraleashkaa.ui.calculators

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView
import java.util.*


class CalculatorsAdapter(
        private val context: Context,
        private val adapterListener: OnCalculatorAdapter,
        private val recyclerViewReadyCallback: RecyclerViewReadyCallback) : RecyclerView.Adapter<CalculatorsAdapter.ViewHolder>() {

    //region == companion ==========

    companion object {
        private const val TAG = "CalculatorAdapter"
    }

    //endregion == companion ==========

    private var calculators: List<CalculatorEntity>

    private var _activity = context as CalculatorsActivity

    val resources: Resources = AppApplication.context.resources

    interface OnCalculatorAdapter {
        fun onCalculatorClicked(calculator: CalculatorEntity)
    }

    interface RecyclerViewReadyCallback {
        fun onLayoutReady()
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        // container
        @JvmField
        @BindView(R.id.container)
        var container: CardView? = null

        // data container - lock icon
        @JvmField
        @BindView(R.id.lockIcon)
        var lockIcon: ImageView? = null

        // data container - picture
        @JvmField
        @BindView(R.id.picture)
        var picture: ImageView? = null

        // data container - title
        @JvmField
        @BindView(R.id.title)
        var title: TextView? = null

        // coming soon
        @JvmField
        @BindView(R.id.comingSoonMask)
        var comingSoonMask: ConstraintLayout? = null

        // coming soon - text
        @JvmField
        @BindView(R.id.comingSoonText)
        var comingSoonText: TextView? = null


        init {
            // bind views
            ButterKnife.bind(this, view!!)

            view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    recyclerViewReadyCallback.onLayoutReady()
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    init {
        calculators = ArrayList()
    }

    fun setItems(calculators: List<CalculatorEntity?>?) {
        this.calculators = calculators as List<CalculatorEntity>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calculator, parent, false)
        return ViewHolder(view)
    }

    @DelicateCoroutinesApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calculator = calculators[position]

        // container
        holder.container!!.setOnClickListener {
            calculator.let { adapterListener.onCalculatorClicked(it) }
        }

        // data container - lock icon
        holder.lockIcon!!.visibility =
                if (calculator.isLock == true)
                    VISIBLE
                else
                    GONE

        // data container - coming soon
        if (calculator.isComingSoon == true) {
            holder.comingSoonMask!!.visibility = VISIBLE
            holder.comingSoonText!!.text = Utilities.getRoomString("calculator_coming_soon")
        }
        else {
            holder.comingSoonMask!!.visibility = GONE
        }

        // data container - picture
        holder.picture!!.setImageResource(Utilities.findDrawableByName("icon_calculator_${calculator.type}"))

        // data container - title
        holder.title!!.text = Utilities.getRoomString("calculator_title_${calculator.type}")
    }

    override fun getItemCount(): Int {
        return calculators.size
    }
}