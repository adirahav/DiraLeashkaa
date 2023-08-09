package com.adirahav.diraleashkaa.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.squareup.picasso.Picasso
import java.util.*

class HomeBestYieldAdapter(
    private val context: Context,
    private val listener: HomeActivity
) : RecyclerView.Adapter<HomeBestYieldAdapter.ViewHolder>() {

    private var bestYields: List<BestYieldEntity>

    interface OnBestYieldAdapter {
        fun onBestYieldClicked(bestYield: BestYieldEntity)
    }

    var view : View? = null

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        // score
        @JvmField
        @BindView(R.id.score)
        var score: ImageView? = null

        // address
        @JvmField
        @BindView(R.id.address)
        var address: TextView? = null

        // chart
        @JvmField
        @BindView(R.id.chart)
        var chart: FrameLayout? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        bestYields = ArrayList<BestYieldEntity>()
    }

    fun setItems(bestYields: ArrayList<BestYieldEntity>) {
        this.bestYields = bestYields
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_best_yield, parent, false)
        this.view = view
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bestYield = bestYields[position]

        // icon
        val drawableID = Utilities.findDrawableByName("icon_score_" + position.plus(1))

        if (drawableID > 0) {
            Picasso.with(context)
                .load(drawableID)
                .into(holder.score)

            holder.score?.visibility = VISIBLE
        }
        else {
            holder.score?.visibility = GONE
        }

        // address
        holder.address!!.text = bestYield.address

        // chart
        /*val chartFragment : PropertyChartFragment = PropertyChartFragment.instance(
            listener,
            bestYield.property,
            bestYield.yieldForecastList)

        holder.chart?.tag = "chart$position"
        view?.findViewWithTag<View>("chart${position}")?.id = View.generateViewId()

        (listener as FragmentActivity).supportFragmentManager.beginTransaction()
            .add(holder.chart!!.id, chartFragment)
            .commit()*/
    }

    override fun getItemCount(): Int {
        return bestYields.size
    }
}