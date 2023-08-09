package com.adirahav.diraleashkaa.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.squareup.picasso.Picasso
import java.util.*

class HomeCitiesAdapter(
    private val context: Context,
    private val listener: HomeActivity,
) : RecyclerView.Adapter<HomeCitiesAdapter.ViewHolder>() {

    private var citiesOfProperties: List<PropertyEntity>

    interface OnCityAdapter {
        fun onCityClicked(city: PropertyEntity, selectedPosition: Int)
    }

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        // container
        @JvmField
        @BindView(R.id.container)
        var container: ConstraintLayout? = null

        // icon
        @JvmField
        @BindView(R.id.icon)
        var icon: de.hdodenhof.circleimageview.CircleImageView? = null

        // name
        @JvmField
        @BindView(R.id.period)
        var name: TextView? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        citiesOfProperties = ArrayList()
    }

    fun setItems(citiesOfProperties: List<PropertyEntity?>) {
        this.citiesOfProperties = citiesOfProperties as List<PropertyEntity>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val propertyCity = citiesOfProperties[position]
        val city = listener.activity.fixedParametersData?.citiesArray?.find { it.key == propertyCity.city }

        // container
        holder.container!!.setOnClickListener {
            if (position == listener.selectedCityPosition) {
                return@setOnClickListener
            }

            holder.name?.setTextColor(ContextCompat.getColor(context, R.color.textSelected))

            if (listener.selectedCityPosition != null) {
                val _selectedHolder = listener.layout.myCitiesList.findViewHolderForAdapterPosition(listener.selectedCityPosition!!)  as HomeCitiesAdapter.ViewHolder?
                _selectedHolder?.name?.setTextColor(ContextCompat.getColor(context, R.color.formText))
            }

            listener.onCityClicked(propertyCity, position)
        }

        // icon
        val drawableID =
            if (propertyCity.city.isNullOrEmpty()) 0
            else Utilities.findDrawableByName("icon_city_" + propertyCity.city!!)

        if (drawableID > 0) {
            Picasso.with(context)
                .load(Utilities.findDrawableByName("icon_city_" + propertyCity.city!!))
                .error(R.drawable.icon_city_missing)
                .into(holder.icon)
        }
        else {
            Picasso.with(context)
                .load(R.drawable.icon_city_missing)
                .error(R.drawable.icon_city_missing)
                .into(holder.icon)
        }

        // name
        holder.name!!.text = (city?.value ?: (propertyCity.city ?: listener.activity.fixedParametersData?.citiesArray?.find { it.key == "else" }?.value)).toString()

        if (position == 0 && listener.selectedCityPosition == null) {
            holder.name?.setTextColor(ContextCompat.getColor(context, R.color.textSelected))
            listener.onCityClicked(propertyCity, position)
        }

        holder.name?.setTextColor(ContextCompat.getColor(context,
            if (position == listener.selectedCityPosition)
                R.color.textSelected
            else
                R.color.text
        ))

    }

    override fun getItemCount(): Int {
        return citiesOfProperties.size
    }
}