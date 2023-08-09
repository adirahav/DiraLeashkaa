package com.adirahav.diraleashkaa.ui.home

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
import com.adirahav.diraleashkaa.common.Utilities.dpToPx
import com.adirahav.diraleashkaa.common.Utilities.getDecimalNumber
import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView
import java.util.*


class HomePropertiesAdapter(
        private val context: Context,
        private val adapterListener: OnPropertyAdapter,
        private val recyclerViewReadyCallback: RecyclerViewReadyCallback) : RecyclerView.Adapter<HomePropertiesAdapter.ViewHolder>() {

    //region == companion ==========

    companion object {
        private const val TAG = "HomePropertiesAdapter"
        private const val PICTURE_SIZE_RATIO = 1.0F
    }

    //endregion == companion ==========

    private var properties: List<PropertyEntity>
    private var bestYields: List<BestYieldEntity>

    private var _activity = context as HomeActivity

    val resources: Resources = AppApplication.context.resources

    // pictures
    var pictureWidth: Int? = null
    var pictureHeight: Int? = null

    interface OnPropertyAdapter {
        fun onPropertyClicked(property: PropertyEntity)
        fun onDeletedPropertyClicked(property: PropertyEntity)
    }

    interface RecyclerViewReadyCallback {
        fun onLayoutReady()
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

        // data container - picture
        @JvmField
        @BindView(R.id.picture)
        var picture: de.hdodenhof.circleimageview.CircleImageView? = null

        // data container - medal icon
        @JvmField
        @BindView(R.id.medalIcon)
        var medalIcon: ImageView? = null

        // data container - medal icon
        @JvmField
        @BindView(R.id.missingIcon)
        var missingIcon: ImageView? = null

        // data container - address
        @JvmField
        @BindView(R.id.address)
        var address: TextView? = null

        // data container - price
        @JvmField
        @BindView(R.id.price)
        var price: TextView? = null

        // container mask
        @JvmField
        @BindView(R.id.containerMask)
        var containerMask: ConstraintLayout? = null

        // container mask - delete animation
        @JvmField
        @BindView(R.id.animDelete)
        var animDelete: GifImageView? = null

        // container mask - archive icon
        @JvmField
        @BindView(R.id.delete)
        var delete: LinearLayout? = null

        // container add
        @JvmField
        @BindView(R.id.containerAdd)
        var containerAdd: ConstraintLayout? = null

        // container add - add icon
        @JvmField
        @BindView(R.id.add)
        var add: ImageView? = null

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
        properties = ArrayList()
        bestYields = ArrayList()

        // picture
        pictureWidth = dpToPx(context, resources.getDimension(R.dimen.home_property_width))
        pictureHeight = pictureWidth!!.times(PICTURE_SIZE_RATIO).toInt()
    }

    fun setItems(properties: List<PropertyEntity?>?, bestYields: List<BestYieldEntity?>?) {
        this.properties = properties as List<PropertyEntity>
        this.bestYields = bestYields as List<BestYieldEntity>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_property, parent, false)
        return ViewHolder(view)
    }

    @DelicateCoroutinesApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property = properties[position]
        val bestYield = if (bestYields != null && bestYields.size > 0) bestYields[0] else null

        // container
        holder.container!!.setOnClickListener {
            if (property.cityElse.equals("addProperty")) {
                PropertyActivity.start(_activity, 0, "", property.city)
            }
            else  if (_activity.maskHolder == null || (_activity.maskHolder != null && _activity.maskHolder!! != holder)) {
                property.let { adapterListener.onPropertyClicked(it) }
            }
        }

        holder.container!!.setOnLongClickListener {
            if (!property.cityElse.equals("addProperty")) {
                if (_activity.maskHolder != null) {
                    if (_activity.maskHolder == holder) {
                        _activity.hideMask()
                    }
                    else {
                        _activity.hideMask()
                        _activity.maskHolder = holder
                        _activity.showMask()
                    }
                }
                else {
                    _activity.maskHolder = holder
                    _activity.showMask()
                }
            }

            return@setOnLongClickListener true
        }

        // data container - picture
        /*var drawableID =
            if (property.city.isNullOrEmpty()) 0
            else Utilities.findDrawableByName("icon_city_" + property.city!!)

        if (drawableID > 0) {
            Picasso.with(context)
                .load(Utilities.findDrawableByName("icon_city_" + property.city!!))
                .error(R.drawable.icon_city_missing)
                .into(holder.picture)
        }
        else {
            Picasso.with(context)
                .load(R.drawable.icon_city_missing)
                .error(R.drawable.icon_city_missing)
                .into(holder.picture)
        }*/

        val showPicture = _activity.fixedParametersData?.pictureArray?.find { it.key == "allow_upload" }?.value?.toBoolean() ?: true

        if (showPicture) {
            if (!property.cityElse.equals("addProperty")) {
                if (property.pictures.isNullOrEmpty()) {
                    holder.picture?.visibility = GONE
                }
                else {
                    Utilities.setPropertyPicture(
                        holder.picture!!,
                        property.pictures!!,
                        PICTURE_SIZE_RATIO,
                        pictureWidth!!,
                        pictureHeight!!,
                        R.drawable.property
                    )
                }
            }
        }
        else {
            holder.picture?.visibility = GONE
        }

        // data container - medal icon
        holder.medalIcon?.visibility =
            if (bestYield != null && property.uuid != null && property.uuid!!.equals(bestYield.uuid))
                VISIBLE
            else
                GONE

        // data container - missing icon
        holder.missingIcon?.visibility =
            if (property.calcYieldForecastList == null)
                VISIBLE
            else
                GONE

        // data container - city address
        holder.address!!.text =
            if (property.city.equals("else"))
                if (property.address != null && property.cityElse != null)
                    String.format(
                        Utilities.getRoomString("home_properties_city_else"),
                        property.address, property.cityElse)
                else if (property.address != null && property.cityElse == null)
                    property.address
                else if (property.address == null && property.cityElse != null)
                    property.cityElse
                else
                    ""
            else
                property.address

        // data container - price
        if (property.price != null) {
            val priceFormat = getDecimalNumber(property.price)

            holder.price!!.text = String.format(
                Utilities.getRoomString("home_properties_price"),
                priceFormat)
        }

        // mask container - delete
        holder.delete!!.setOnClickListener {
            property.let {
                holder.delete?.visibility = GONE
                holder.animDelete?.visibility = VISIBLE
                adapterListener.onDeletedPropertyClicked(it)
            }
        }

        // add container
        if (property.cityElse.equals("addProperty")) {
            holder.containerData?.visibility = GONE
            holder.containerMask?.visibility = GONE
            holder.containerAdd?.visibility = VISIBLE
        }
        else {
            holder.containerData?.visibility = VISIBLE
            holder.containerMask?.visibility = GONE
            holder.containerAdd?.visibility = GONE
        }

    }

    override fun getItemCount(): Int {
        return properties.size
    }
}