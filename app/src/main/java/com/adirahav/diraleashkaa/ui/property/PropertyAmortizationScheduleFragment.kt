package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.AmortizationScheduleEntity
import com.adirahav.diraleashkaa.databinding.FragmentPropertyAmortizationScheduleBinding
import com.adirahav.diraleashkaa.ui.base.BaseFragment

class PropertyAmortizationScheduleFragment : BaseFragment<PropertyViewModel?>() {

    companion object {
        private const val TAG = "PropertyAmortizationScheduleFragment"
    }

    // activity
    var _activity: PropertyActivity? = null

    // context
    var _context: Context? = null

    // layout
    private var layout: FragmentPropertyAmortizationScheduleBinding? = null

    private lateinit var linearLayoutManager: LinearLayoutManager
    private var adapter: PropertyAmortizationScheduleAdapter? = null

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

        layout = FragmentPropertyAmortizationScheduleBinding.inflate(layoutInflater)


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
        _activity?.updateActionsMenuVisibility(PropertyActivity.ActionsMenuButtonType.AMORTIZATION_SCHEDULE)

        // strings
        setPhrases()
    }

    private fun initObserver() {
        viewModel!!.amortizationScheduleList.observe(viewLifecycleOwner, AmortizationScheduleObserver())
    }

    private fun initData() {
        linearLayoutManager = LinearLayoutManager(context)
        layout?.recyclerView?.layoutManager = linearLayoutManager

        adapter = PropertyAmortizationScheduleAdapter(_context!!, _activity!!)
        layout?.recyclerView?.adapter = adapter
        viewModel?.getAmortizationSchedule(_activity!!)

        /*Handler().postDelayed({
            recyclerView?.scrollToPosition(_activity!!.propertyData!!.saleYearsPeriod!!.times(12) + 7)
        }, 1000)*/
    }

    private fun initEvents() {

    }

    // observers
    private inner class AmortizationScheduleObserver : Observer<List<AmortizationScheduleEntity?>?> {
        override fun onChanged(amortizationScheduleList: List<AmortizationScheduleEntity?>?) {
            if (amortizationScheduleList == null) {
                return
            }

            // items
            adapter?.setItems(amortizationScheduleList)
        }
    }

    //region == strings ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        layout?.monthNo?.text = Utilities.getLocalPhrase("amortization_schedule_month_label")
        layout?.fundBOP?.text = Utilities.getLocalPhrase("amortization_schedule_bop_fund_label")
        layout?.interest?.text = Utilities.getLocalPhrase("amortization_schedule_interest_label")
        layout?.monthlyRepayments?.text = Utilities.getLocalPhrase("amortization_schedule_monthly_repayments_label")
        layout?.fundRefund?.text = Utilities.getLocalPhrase("amortization_schedule_fund_refund_label")
        layout?.interestRepayment?.text = Utilities.getLocalPhrase("amortization_schedule_interest_repayment_label")
        layout?.fundEOP?.text = Utilities.getLocalPhrase("amortization_schedule_eop_fund_label")
    }

    override fun createViewModel(): PropertyViewModel {
        val factory = PropertyViewModelFactory(activity as PropertyActivity, DataManager.instance!!.propertyService)
        return ViewModelProvider(this, factory)[PropertyViewModel::class.java]
    }
}