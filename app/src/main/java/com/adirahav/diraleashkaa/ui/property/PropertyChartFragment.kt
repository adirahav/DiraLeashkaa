package com.adirahav.diraleashkaa.ui.property

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.fractionToFloatFormat
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.YieldForecastEntity
import com.adirahav.diraleashkaa.ui.base.BaseFragment
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.home.HomeViewModelFactory
import com.github.angads25.toggle.widget.LabeledSwitch
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.utils.ColorTemplate

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor


class PropertyChartFragment(
    /*val _activity: Activity?,
    val mortgagePeriod: Int?,
    val yieldForecastList: MutableList<YieldForecastModel>*/) : BaseFragment<BaseViewModel?>() {

    companion object {
        private const val TAG = "PropertyChartFragment"

        var _activity: Activity? = null
        var mortgagePeriod: Int? = null
        var yieldForecastList: MutableList<YieldForecastEntity>? = null

        fun instance(_activity: Activity?, mortgagePeriod: Int?, yieldForecastList: MutableList<YieldForecastEntity>): PropertyChartFragment {
            this._activity = _activity
            this.mortgagePeriod = mortgagePeriod
            this.yieldForecastList = yieldForecastList
            val fragment = PropertyChartFragment()
            return fragment
        }
    }

    // context
    var _context: Context? = null

    // switcher
    @JvmField
    @BindView(R.id.switchChart)
    var switchChartView: LabeledSwitch? = null

    // chart
    @JvmField
    @BindView(R.id.lineChart)
    var lineChartView: LineChart? = null

    @JvmField
    @BindView(R.id.barChart)
    var barChartView: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreateView()", showToast = false)

        // orientation
        if (_activity!!.localClassName.equals("ui.property.PropertyActivity")) {
            _activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

         val layout =
             if (_activity?.localClassName.equals("ui.home.HomeActivity"))
                 R.layout.fragment_home_chart
             else
                 R.layout.fragment_property_chart

        val view = inflater.inflate(layout, container, false)
        ButterKnife.bind(this, view)

        initGlobal()
        initViews(view)
        initData()
        initEvents()
        if (_activity!!.localClassName.equals("ui.property.PropertyActivity")) {
            (_activity!! as PropertyActivity).initIndexesAndInterests()
        }
        initObserver()

        return view
    }

    private fun initGlobal() {
        // context
        _context = context

        // view model
        viewModel = createViewModel()

        // indexes and interests
        if (_activity?.localClassName.equals("ui.property.PropertyActivity")) {
            (_activity!! as PropertyActivity)?.displayIndexesAndInterests()
        }

        // actions menu
        if (_activity?.localClassName.equals("ui.property.PropertyActivity")) {
            (_activity!! as PropertyActivity)?.updateActionsMenuVisibility(PropertyActivity.ActionsMenuButtonType.CHART)
        }
    }

    private fun initObserver() {
        //if (viewModel!!.propertyData.hasObservers() == false) viewModel!!.propertyData.observe(this, ProcedureObserver())
    }

    private fun initViews(view: View) {

    }

    private fun initData() {
        initLineChart()
        //initBarChart()
    }

    private fun initEvents() {

        switchChartView?.setOnToggledListener {
                toggleableView,
                isOn -> if (isOn)
                            initLineChart()
                        else
                            initBarChart()
        }
    }

    // line chart
    private fun initLineChart() {
        lineChartView?.visibility = VISIBLE
        barChartView?.visibility = GONE

        if (yieldForecastList == null) {
            return
        }

        // description (disable)
        lineChartView?.getDescription()!!.setEnabled(false)

        // touch gestures
        lineChartView?.setTouchEnabled(true)
        lineChartView?.dragDecelerationFrictionCoef = 0.9f

        // scaling and dragging
        lineChartView?.setDragEnabled(true)
        lineChartView?.setScaleEnabled(true)
        lineChartView?.setDrawGridBackground(false)
        lineChartView?.setHighlightPerDragEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        lineChartView?.setPinchZoom(true)

        // background color
        lineChartView?.setBackgroundColor(ContextCompat.getColor(_context!!, R.color.chartBackground))

        // animation
        lineChartView?.animateX(1500)

        // legend
        val legend: Legend = lineChartView!!.getLegend()
        legend.form = LegendForm.LINE
        legend.textSize = 11f
        legend.textColor = ContextCompat.getColor(_context!!, R.color.chartLegand)
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.direction = Legend.LegendDirection.RIGHT_TO_LEFT
        legend.setDrawInside(false)

        // bottom scale
        val xAxis: XAxis = lineChartView!!.getXAxis()
        //xAxis.typeface = tfLight
        xAxis.textSize = 11f
        xAxis.textColor = ContextCompat.getColor(_context!!, R.color.chartBottomScale)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.position = XAxisPosition.BOTTOM

        var months = mortgagePeriod?.times(12) ?: 0
        if (months == 0) {
            return
        }

        var firstTotalReturn = (yieldForecastList!!.get(0).totalReturn)!!.fractionToFloatFormat(1)
        var firstReturnOnEquity = (yieldForecastList!!.get(0).returnOnEquity)!!.fractionToFloatFormat(1)
        var lastTotalReturn = (yieldForecastList!!.get(months-1).totalReturn)!!.fractionToFloatFormat(1)
        var lastReturnOnEquity = (yieldForecastList!!.get(months-1).returnOnEquity)!!.fractionToFloatFormat(1)

        // left scale
        val leftAxis: YAxis = lineChartView!!.getAxisLeft()
        leftAxis.textColor = ContextCompat.getColor(_context!!, R.color.chartLeftScale)
        leftAxis.axisMaximum = if (lastTotalReturn > lastReturnOnEquity) floor(lastTotalReturn) else floor(lastReturnOnEquity)
        leftAxis.axisMinimum = if (firstTotalReturn < firstReturnOnEquity) floor(firstTotalReturn) else floor(firstReturnOnEquity)
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        // -25.67   -9.63
        // -14.10   -5.29
        // -2.07    -0.78
        // 10.44    3.92
        // 23.45    8.8

        // 296.04   111.01

        // right scale (disable)
        val rightAxis: YAxis = lineChartView!!.getAxisRight()
        rightAxis.textColor = Color.RED
        rightAxis.axisMaximum = mortgagePeriod!!.toFloat()
        rightAxis.axisMinimum = 0f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false
        rightAxis.isEnabled = false

        setLineChartData()
    }

    private fun setLineChartData() {
        var months = mortgagePeriod?.times(12) ?: 0

        if (months == 0) {
            return
        }

        val arrTotalReturn = ArrayList<Entry>()
        val arrReturnOnEquity = ArrayList<Entry>()
        for (monthNo in 0..months step 12) {

            val totalReturnItem = (yieldForecastList!!.get(monthNo).totalReturn)!!.fractionToFloatFormat(1)
            arrTotalReturn.add(Entry(monthNo.div(12).toFloat(), totalReturnItem))

            val returnOnEquityItem = (yieldForecastList!!.get(monthNo).returnOnEquity)!!.fractionToFloatFormat(1)
            arrReturnOnEquity.add(Entry(monthNo.div(12).toFloat(), returnOnEquityItem))
        }

        val setTotalReturn: LineDataSet
        val setReturnOnEquity: LineDataSet

        if (null != lineChartView?.getData() && lineChartView?.getData()?.getDataSetCount()!! > 0) {
            setTotalReturn = lineChartView?.getData()?.getDataSetByIndex(0) as LineDataSet
            setReturnOnEquity = lineChartView?.getData()?.getDataSetByIndex(1) as LineDataSet

            setTotalReturn.values = arrTotalReturn
            setReturnOnEquity.values = arrReturnOnEquity

            lineChartView?.getData()!!.notifyDataChanged()
            lineChartView?.notifyDataSetChanged()
        }
        else {
            // total return
            setTotalReturn = initLineDataSet(
                Utilities.getRoomString("chart_legend_total_return"),
                arrTotalReturn, ColorTemplate.getHoloBlue())

            // return on equity
            setReturnOnEquity = initLineDataSet(
                Utilities.getRoomString("chart_legend_return_on_equity"),
                arrReturnOnEquity, ColorTemplate.getHoloRed())

            // create a data object with the data sets
            val data = LineData(setTotalReturn, setReturnOnEquity)
            data.setValueTextColor(ContextCompat.getColor(_context!!, R.color.chartTextValue))
            data.setValueFormatter(DefaultValueFormatter(1))
            data.setValueTextSize(10f)

            // set data
            lineChartView?.setData(data)
        }
    }

    private fun initLineDataSet(label: String, values: ArrayList<Entry>, lineColor: Int) : LineDataSet {
        val dataSet = LineDataSet(values, label)
        dataSet.axisDependency = AxisDependency.LEFT

        dataSet.setCircleColor(ContextCompat.getColor(_context!!, R.color.chartCircle))
        dataSet.color = lineColor
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 3f
        dataSet.fillAlpha = 65
        dataSet.fillColor = ColorTemplate.getHoloBlue()
        dataSet.highLightColor = Color.rgb(244, 117, 117)
        dataSet.setDrawCircleHole(false)

        return dataSet
    }

    // bar chart
    private fun initBarChart() {
        barChartView?.visibility = VISIBLE
        lineChartView?.visibility = GONE

        // description (disable)
        barChartView?.getDescription()!!.setEnabled(false)

        // touch gestures
        barChartView?.setTouchEnabled(true)
        barChartView?.dragDecelerationFrictionCoef = 0.9f

        // scaling and dragging
        barChartView?.setDragEnabled(true)
        barChartView?.setScaleEnabled(true)
        barChartView?.setDrawGridBackground(false)
        barChartView?.setHighlightPerDragEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        barChartView?.setPinchZoom(true)

        // background color
        barChartView?.setBackgroundColor(ContextCompat.getColor(_context!!, R.color.chartBackground))

        // animation
        barChartView?.animateX(1500)

        // legend
        val legend: Legend = barChartView!!.getLegend()
        legend.form = LegendForm.LINE
        legend.textSize = 11f
        legend.textColor = ContextCompat.getColor(_context!!, R.color.chartLegand)
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        // bottom scale
        val xAxis: XAxis = barChartView!!.getXAxis()
        xAxis.textSize = 11f
        xAxis.textColor = ContextCompat.getColor(_context!!, R.color.chartBottomScale)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setCenterAxisLabels(true)

        var months = mortgagePeriod?.times(12) ?: 0
        if (months == 0) {
            return
        }

        var firstTotalReturn = (yieldForecastList!!.get(0).totalReturn)!!.fractionToFloatFormat(1)
        var firstReturnOnEquity = (yieldForecastList!!.get(0).returnOnEquity)!!.fractionToFloatFormat(1)
        var lastTotalReturn = (yieldForecastList!!.get(months-1).totalReturn)!!.fractionToFloatFormat(1)
        var lastReturnOnEquity = (yieldForecastList!!.get(months-1).returnOnEquity)!!.fractionToFloatFormat(1)

        // left scale
        val leftAxis: YAxis = barChartView!!.getAxisLeft()
        leftAxis.textColor = ContextCompat.getColor(_context!!, R.color.chartLeftScale)
        leftAxis.axisMaximum = if (lastTotalReturn > lastReturnOnEquity) floor(lastTotalReturn) else floor(lastReturnOnEquity)
        leftAxis.axisMinimum = if (firstTotalReturn < firstReturnOnEquity) floor(firstTotalReturn) else floor(firstReturnOnEquity)
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        // right scale (disable)
        val rightAxis: YAxis = barChartView!!.getAxisRight()
        rightAxis.textColor = Color.RED
        rightAxis.axisMaximum = mortgagePeriod!!.toFloat()
        rightAxis.axisMinimum = 0f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false
        rightAxis.isEnabled = false

        setBarChartData()
    }

    fun setBarChartData() {
        val groupSpace = 0.12f
        val barSpace = 0.0f
        val barWidth = 0.85f

        val yearsCount = mortgagePeriod
        var months = yearsCount?.times(12) ?: 0

        val startPeriod = 0

        val arrTotalReturn = ArrayList<BarEntry>()
        val arrReturnOnEquity = ArrayList<BarEntry>()
        for (yearNo in 0..yearsCount!! step 2) {
            val totalReturnItem = (yieldForecastList!!.get(yearNo.times(12)).totalReturn)!!.fractionToFloatFormat(1)
            arrTotalReturn.add(BarEntry(yearNo.toFloat(), totalReturnItem))

            val returnOnEquityItem = (yieldForecastList!!.get(yearNo.times(12)).returnOnEquity)!!.fractionToFloatFormat(1)
            arrReturnOnEquity.add(BarEntry(yearNo.toFloat(), returnOnEquityItem))
        }

        val setTotalReturn: BarDataSet
        val setReturnOnEquity: BarDataSet

        if (null != barChartView?.getData() && barChartView!!.getData().getDataSetCount() > 0) {
            setTotalReturn = barChartView!!.getData().getDataSetByIndex(0) as BarDataSet
            setReturnOnEquity = barChartView!!.getData().getDataSetByIndex(1) as BarDataSet

            setTotalReturn.values = arrTotalReturn
            setReturnOnEquity.values = arrReturnOnEquity

            barChartView!!.getData().notifyDataChanged()
            barChartView!!.notifyDataSetChanged()
        }
        else {
            // total return
            setTotalReturn = BarDataSet(
                arrTotalReturn,
                Utilities.getRoomString("chart_legend_total_return")
            )
            setTotalReturn.setColor(ColorTemplate.getHoloBlue())

            // return on equity
            setReturnOnEquity = BarDataSet(
                arrReturnOnEquity,
                Utilities.getRoomString("chart_legend_return_on_equity")
            )
            setReturnOnEquity.setColor(ColorTemplate.getHoloRed())

            val data = BarData(setTotalReturn, setReturnOnEquity)
            data.setValueFormatter(LargeValueFormatter())
            //data.setValueTypeface(tfLight)
            barChartView?.setData(data)
        }

        // specify the width each bar should have
        barChartView?.getBarData()!!.setBarWidth(barWidth)

        // restrict the x-axis range
        barChartView?.getXAxis()!!.setAxisMinimum(startPeriod.toFloat())

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        barChartView?.getXAxis()!!.setAxisMaximum(yearsCount.toFloat())

        barChartView?.groupBars(startPeriod.toFloat(), groupSpace, barSpace)
        barChartView?.invalidate()
    }

    // observers
    private inner class ChartObserver : Observer<List<YieldForecastEntity?>?> {
        override fun onChanged(chartList: List<YieldForecastEntity?>?) {
            if (chartList == null) {
                return
            }

            // items
            //adapter?.setItems(chartList)
        }
    }

    override fun createViewModel(): BaseViewModel? {
        val factory =
            if (_activity?.localClassName.equals("ui.home.HomeActivity"))
                HomeViewModelFactory(DataManager.instance!!.propertyService, DataManager.instance!!.homeService)
            else
                PropertyViewModelFactory(DataManager.instance!!.propertyService)

        return ViewModelProvider(this, factory).get(BaseViewModel::class.java)
    }


}