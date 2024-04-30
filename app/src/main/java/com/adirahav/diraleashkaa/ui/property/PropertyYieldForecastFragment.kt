package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.YieldForecastEntity
import com.adirahav.diraleashkaa.databinding.FragmentPropertyYieldForecastBinding
import com.adirahav.diraleashkaa.ui.base.BaseFragment

class PropertyYieldForecastFragment : BaseFragment<PropertyViewModel?>() {

    //region == companion ==========

    companion object {
        private const val TAG = "PropertyYieldForecastFragment"
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _activity: PropertyActivity? = null

    // context
    var _context: Context? = null

    // layout
    private var layout: FragmentPropertyYieldForecastBinding? = null

    // recycler view
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var adapter: PropertyYieldForecastAdapter? = null

    //endregion == variables ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreateView()", showToast = false)

        // activity
        _activity = activity as PropertyActivity

        // orientation
        _activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        layout = FragmentPropertyYieldForecastBinding.inflate(layoutInflater)

        initGlobal()
        initEvents()
        _activity!!.initIndexesAndInterests()
        initObserver()

        return layout?.root
    }

    override fun onResume() {
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)
        super.onResume()
        initData()
    }

    override fun onAttach(context: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "onAttach()", showToast = false)
        super.onAttach(context)
    }

    override fun onDetach() {
        Utilities.log(Enums.LogType.Debug, TAG, "onDetach()", showToast = false)
        super.onDetach()
    }

    private fun initGlobal() {
        // context
        _context = context

        // view model
        viewModel = createViewModel()

        // indexes and interests
        _activity?.displayIndexesAndInterests()

        // actions menu
        _activity?.updateActionsMenuVisibility(PropertyActivity.ActionsMenuButtonType.YIELD_FORECAST)

        // strings
        setPhrases()
    }

    private fun initObserver() {
        viewModel!!.yieldForecastList.observe(viewLifecycleOwner, YieldForecastObserver())
    }

    private fun initData() {
        linearLayoutManager = LinearLayoutManager(context)
        layout?.recyclerView?.layoutManager = linearLayoutManager

        adapter = PropertyYieldForecastAdapter(_context!!, _activity!!)
        layout?.recyclerView?.adapter = adapter
        viewModel?.getYieldForecast(_activity!!)

        /*Handler().postDelayed({
            layout.recyclerView.scrollToPosition(_activity!!.propertyData!!.saleYearsPeriod!!.times(12) + 7)
        }, 1000)*/
    }

    private fun initEvents() {

    }

    // observers
    private inner class YieldForecastObserver : Observer<List<YieldForecastEntity?>?> {
        override fun onChanged(yieldForecastList: List<YieldForecastEntity?>?) {
            if (yieldForecastList == null) {
                return
            }

            // items
            adapter?.setItems(yieldForecastList)
        }
    }

    override fun createViewModel(): PropertyViewModel {
        val factory = PropertyViewModelFactory(_activity!!, DataManager.instance!!.propertyService)
        return ViewModelProvider(this, factory)[PropertyViewModel::class.java]
    }

    //region == strings ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        layout?.monthNo?.text = Utilities.getLocalPhrase("yield_forecast_month_label")
        layout?.propertyPrice?.text = Utilities.getLocalPhrase("yield_forecast_property_price_label")
        layout?.rent?.text = Utilities.getLocalPhrase("yield_forecast_rent_label")
        layout?.financingCosts?.text = Utilities.getLocalPhrase("yield_forecast_financing_costs_label")
        layout?.valuationInRealization?.text = Utilities.getLocalPhrase("yield_forecast_valuation_in_realization_label")
        layout?.commendationTax?.text = Utilities.getLocalPhrase("yield_forecast_commendation_tax_label")
        layout?.profit?.text = Utilities.getLocalPhrase("yield_forecast_profit_label")
        layout?.totalReturn?.text = Utilities.getLocalPhrase("yield_forecast_total_return_label")
        layout?.returnOnEquity?.text = Utilities.getLocalPhrase("yield_forecast_return_on_equity_label")

    }
}