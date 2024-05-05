package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Utilities.floatFormat
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.databinding.ActivityPropertyBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.views.PropertyInput
import com.adirahav.diraleashkaa.views.PropertyPercent
import kotlinx.coroutines.*
import java.util.*

class PropertyActivity : BaseActivity<PropertyViewModel?, ActivityPropertyBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "PropertyActivity"
        private const val EXTRA_PROPERTY_ROOMID = "EXTRA_PROPERTY_ROOMID"
        private const val EXTRA_PROPERTY_ID = "EXTRA_PROPERTY_ID"
        private const val EXTRA_PROPERTY_CITY = "EXTRA_PROPERTY_CITY"
        private const val EXTRA_PROPERTY_EMPTY = "EXTRA_PROPERTY_EMPTY"

        fun start(context: Context, roomPID: Long, propertyId: String, city: String?) {
            Utilities.log(Enums.LogType.Debug, TAG, "start(): roomPID = ${roomPID}, : propertyId = ${propertyId}", showToast = false)
            val intent = Intent(context, PropertyActivity::class.java)
            intent.putExtra(EXTRA_PROPERTY_ROOMID, roomPID)
            intent.putExtra(EXTRA_PROPERTY_ID, propertyId)
            intent.putExtra(EXTRA_PROPERTY_CITY, city)
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // shared preferences
    var preferences: AppPreferences? = null

    // loggedin user
    var userToken: String? = null

    // room/server data loaded
    var isLocalFixedParametersLoaded: Boolean = false
    var isLocalPropertyLoaded: Boolean = false
    var isLocalUserLoaded: Boolean = false

    private var isPropertyLoaded: Boolean = false

    var isDataInit: Boolean = false

    // user id
    var roomUID: Long? = 0L
    var userId: String? = null

    // property id
    var roomPID: Long? = 0L
    var propertyId: String? = null

    // property city
    var argPropertyCity: String? = null
    var isCityUpdatedFromArgs: Boolean = false

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // property data
    var propertyData: PropertyEntity? = null

    // user data
    var userData: UserEntity? = null

    // has calc
    private var didCalcAmortizationSchedule: Boolean = false
    private var didCalcYieldForecast: Boolean = false

    // layout
    internal var layout: ActivityPropertyBinding? = null

    // amortization schedule
    var amortizationScheduleList = mutableListOf<AmortizationScheduleEntity>()

    // yield forecast
    var yieldForecastList = mutableListOf<YieldForecastEntity>()

    // fragments
    private var infoFragment: PropertyInfoFragment? = null
    private var yieldForecastFragment: PropertyYieldForecastFragment? = null
    private var amortizationScheduleFragment: PropertyAmortizationScheduleFragment? = null
    private var chartFragment: PropertyChartFragment? = null
    private var visibleFragment: String = ""

    //endregion == variables ==========

    //region == lifecycle methods ==
    override fun onResume() {
        super.onResume()
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        setCustomActionBar(layout?.drawer)
        setDrawer(layout?.drawer, layout?.menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)

        super.onCreate(savedInstanceState)


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        //actionBar?.hide()
        //supportActionBar?.hide()
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // And don't forget to clear the flag when exiting the activity
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        layout = ActivityPropertyBinding.inflate(layoutInflater)
        setContentView(layout?.root)

        initGlobal()
        initEvents()
        initViews()

        if (savedInstanceState == null) {
            lifecycleOwner = this
            initObserver()
        }
    }

    override fun createViewModel(): PropertyViewModel {
        Utilities.log(Enums.LogType.Debug, TAG, "createViewModel()", showToast = false)
        val factory = PropertyViewModelFactory(this@PropertyActivity, DataManager.instance!!.propertyService)
        return ViewModelProvider(this, factory)[PropertyViewModel::class.java]
    }

    override fun onPause() {
        super.onPause()

        Utilities.log(Enums.LogType.Debug, TAG, "onPause()", showToast = false)
    }

    override fun onStop() {
        super.onStop()
        Utilities.log(Enums.LogType.Debug, TAG, "onStop()", showToast = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utilities.log(Enums.LogType.Debug, TAG, "onDestroy()", showToast = false)
    }

    override fun onBackPressed() {

        if (visibleFragment.equals("infoFragment") ) {
            super.onBackPressed()
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            loadInfoFragment()
        }
    }
    //endregion == lifecycle methods ==

    //region == instance state ====
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Utilities.log(Enums.LogType.Debug, TAG, "onSaveInstanceState()", showToast = false)
        savedInstanceState.putLong("roomPID", propertyData?.roomID ?: 0)
        savedInstanceState.putString("_id", propertyData?._id)
        savedInstanceState.putString(Const.CITY, propertyData?.city)
        savedInstanceState.putString(Const.ADDRESS, propertyData?.address)
        savedInstanceState.putString("apartmentType", propertyData?.apartmentType)
        savedInstanceState.putInt(Const.PRICE, propertyData?.price ?: 0)
        savedInstanceState.putInt(Const.EQUITY, propertyData?.calcEquity ?: 0)
        savedInstanceState.putInt("equityCleaningExpenses", propertyData?.calcEquityCleaningExpenses ?: 0)
        savedInstanceState.putInt("mortgageRequired", propertyData?.calcMortgageRequired ?: 0)
        savedInstanceState.putInt(Const.INCOMES, propertyData?.calcIncomes ?: 0)
        savedInstanceState.putInt(Const.COMMITMENTS, propertyData?.calcCommitments ?: 0)
        savedInstanceState.putInt("disposableIncome", propertyData?.calcDisposableIncome ?: 0)
        savedInstanceState.putInt("possibleMonthlyRepayment", propertyData?.calcPossibleMonthlyRepayment ?: 0)
        savedInstanceState.putFloat("possibleMonthlyRepaymentPercent", propertyData?.possibleMonthlyRepaymentPercent ?: 0F)
        savedInstanceState.putInt("maxPercentOfFinancing", propertyData?.calcMaxPercentOfFinancing ?: 0)
        savedInstanceState.putInt("actualPercentOfFinancing", propertyData?.calcActualPercentOfFinancing ?: 0)
        savedInstanceState.putInt("transferTax", propertyData?.calcTransferTax ?: 0)
        savedInstanceState.putInt("lawyer", propertyData?.calcLawyer ?: 0)
        savedInstanceState.putFloat("lawyerPercent", propertyData?.calcLawyerPercent ?: 0F)
        savedInstanceState.putInt("realEstateAgent", propertyData?.calcRealEstateAgent ?: 0)
        savedInstanceState.putFloat("realEstateAgentPercent", propertyData?.calcRealEstateAgentPercent ?: 0F)
        savedInstanceState.putInt("brokerMortgage", propertyData?.calcBrokerMortgage ?: 0)
        savedInstanceState.putInt(Const.REPAIRING, propertyData?.calcRepairing ?: 0)
        savedInstanceState.putInt("incidentalsTotal", propertyData?.calcIncidentalsTotal ?: 0)
        savedInstanceState.putInt("rent", propertyData?.calcRent ?: 0)
        savedInstanceState.putFloat("rentPercent", propertyData?.rentPercent ?: 0F)
        savedInstanceState.putInt("lifeInsurance", propertyData?.calcLifeInsurance ?: 0)
        savedInstanceState.putInt("structureInsurance", propertyData?.calcStructureInsurance ?: 0)
        savedInstanceState.putInt("rentCleaningExpenses", propertyData?.calcRentCleaningExpenses ?: 0)
        savedInstanceState.putInt("mortgagePeriod", propertyData?.calcMortgagePeriod ?: 0)
        savedInstanceState.putInt("mortgageMonthlyRepayment", propertyData?.calcMortgageMonthlyRepayment ?: 0)
        savedInstanceState.putInt("mortgageMonthlyYield", propertyData?.calcMortgageMonthlyYield ?: 0)

        savedInstanceState.putBoolean("showInterestsContainer", propertyData?.showInterestsContainer ?: true)
        savedInstanceState.putFloat("indexPercent", propertyData?.calcIndexPercent ?: 0F)
        savedInstanceState.putFloat("interestPercent", propertyData?.calcInterestPercent ?: 0F)
        savedInstanceState.putFloat("interestIn5YearsDeltaPercent", propertyData?.calcInterestIn5YearsPercent ?: 0F)
        savedInstanceState.putFloat("interestIn10YearsDeltaPercent", propertyData?.calcInterestIn10YearsPercent ?: 0F)
        savedInstanceState.putFloat("averageInterestAtTakingPercent", propertyData?.calcAverageInterestAtTakingPercent ?: 0F)
        savedInstanceState.putFloat("averageInterestAtMaturityPercent", propertyData?.calcAverageInterestAtMaturityPercent ?: 0F)
        savedInstanceState.putFloat("forecastAnnualPriceIncreasePercent", propertyData?.calcForecastAnnualPriceIncreasePercent ?: 0F)
        savedInstanceState.putFloat("salesCostsPercent", propertyData?.calcSalesCostsPercent ?: 0F)
        savedInstanceState.putFloat("depreciationForTaxPurposesPercent", propertyData?.calcDepreciationForTaxPurposesPercent ?: 0F)
        savedInstanceState.putInt("saleYearsPeriod", propertyData?.calcSaleYearsPeriod ?: 0)

        savedInstanceState.putBoolean("didCalcAmortizationSchedule", true)
        savedInstanceState.putBoolean("didCalcYieldForecast", true)

        savedInstanceState.putString("visibleFragment", visibleFragment)

        savedInstanceState.putBoolean("isLocalFixedParametersLoaded", isLocalFixedParametersLoaded)
        savedInstanceState.putBoolean("isLocalPropertyLoaded", isLocalPropertyLoaded)
        savedInstanceState.putBoolean("isLocalUserLoaded", isLocalUserLoaded)
        savedInstanceState.putBoolean("isPropertyLoaded", isPropertyLoaded)
        savedInstanceState.putBoolean("isDataInit", isDataInit)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Utilities.log(Enums.LogType.Debug, TAG, "onRestoreInstanceState()", showToast = false)
        propertyData?.roomID = savedInstanceState.getLong("roomPID")
        propertyData?._id = savedInstanceState.getString("propertyId")
        propertyData?.city = savedInstanceState.getString(Const.CITY)
        propertyData?.address = savedInstanceState.getString(Const.ADDRESS)
        propertyData?.apartmentType = savedInstanceState.getString("apartmentType")
        propertyData?.price = savedInstanceState.getInt(Const.PRICE)
        propertyData?.calcEquity = savedInstanceState.getInt(Const.EQUITY)
        propertyData?.calcEquityCleaningExpenses = savedInstanceState.getInt("equityCleaningExpenses")
        propertyData?.calcMortgageRequired = savedInstanceState.getInt("equitymortgageRequiredaningExpenses")
        propertyData?.calcIncomes = savedInstanceState.getInt(Const.INCOMES)
        propertyData?.calcCommitments = savedInstanceState.getInt(Const.COMMITMENTS)
        propertyData?.calcDisposableIncome = savedInstanceState.getInt("disposableIncome")
        propertyData?.calcPossibleMonthlyRepayment = savedInstanceState.getInt("possibleMonthlyRepayment")
        propertyData?.possibleMonthlyRepaymentPercent = savedInstanceState.getFloat("possibleMonthlyRepaymentPercent")
        propertyData?.calcMaxPercentOfFinancing = savedInstanceState.getInt("maxPercentOfFinancing")
        propertyData?.calcActualPercentOfFinancing = savedInstanceState.getInt("actualPercentOfFinancing")
        propertyData?.calcTransferTax = savedInstanceState.getInt("transferTax")
        propertyData?.calcLawyer = savedInstanceState.getInt("lawyer")
        propertyData?.calcLawyerPercent = savedInstanceState.getFloat("lawyerPercent")
        propertyData?.calcRealEstateAgent = savedInstanceState.getInt("realEstateAgent")
        propertyData?.calcRealEstateAgentPercent = savedInstanceState.getFloat("realEstateAgentPercent")
        propertyData?.calcBrokerMortgage = savedInstanceState.getInt("brokerMortgage")
        propertyData?.calcRepairing = savedInstanceState.getInt(Const.REPAIRING)
        propertyData?.calcIncidentalsTotal = savedInstanceState.getInt("incidentalsTotal")
        propertyData?.calcRent = savedInstanceState.getInt("rent")
        propertyData?.rentPercent = savedInstanceState.getFloat("rentPercent")
        propertyData?.calcLifeInsurance = savedInstanceState.getInt("lifeInsurance")
        propertyData?.calcStructureInsurance = savedInstanceState.getInt("structureInsurance")
        propertyData?.calcRentCleaningExpenses = savedInstanceState.getInt("rentCleaningExpenses")
        propertyData?.calcMortgagePeriod = savedInstanceState.getInt("mortgagePeriod")
        propertyData?.calcMortgageMonthlyRepayment = savedInstanceState.getInt("mortgageMonthlyRepayment")
        propertyData?.calcMortgageMonthlyYield = savedInstanceState.getInt("mortgageMonthlyYield")

        propertyData?.showInterestsContainer = savedInstanceState.getBoolean("showInterestsContainer")
        propertyData?.calcIndexPercent = savedInstanceState.getFloat("indexPercent")
        propertyData?.calcInterestPercent = savedInstanceState.getFloat("interestPercent")
        propertyData?.calcInterestIn5YearsPercent = savedInstanceState.getFloat("interestIn5YearsDeltaPercent")
        propertyData?.calcInterestIn10YearsPercent = savedInstanceState.getFloat("interestIn10YearsDeltaPercent")
        propertyData?.calcAverageInterestAtTakingPercent = savedInstanceState.getFloat("averageInterestAtTakingPercent")
        propertyData?.calcAverageInterestAtMaturityPercent = savedInstanceState.getFloat("averageInterestAtMaturityPercent")
        propertyData?.calcForecastAnnualPriceIncreasePercent = savedInstanceState.getFloat("forecastAnnualPriceIncreasePercent")
        propertyData?.calcSalesCostsPercent = savedInstanceState.getFloat("salesCostsPercent")
        propertyData?.calcDepreciationForTaxPurposesPercent = savedInstanceState.getFloat("depreciationForTaxPurposesPercent")
        propertyData?.calcSaleYearsPeriod = savedInstanceState.getInt("saleYearsPeriod")

        didCalcAmortizationSchedule = savedInstanceState.getBoolean("didCalcAmortizationSchedule")
        didCalcYieldForecast = savedInstanceState.getBoolean("didCalcYieldForecast")

        visibleFragment = savedInstanceState.getString("visibleFragment").toString()

        isLocalFixedParametersLoaded = savedInstanceState.getBoolean("isLocalFixedParametersLoaded")
        isLocalPropertyLoaded = savedInstanceState.getBoolean("isLocalPropertyLoaded")
        isLocalUserLoaded = savedInstanceState.getBoolean("isLocalUserLoaded")
        isPropertyLoaded = savedInstanceState.getBoolean("isPropertyLoaded")
        isDataInit = savedInstanceState.getBoolean("isDataInit")

        if (didCalcAmortizationSchedule) {
            if (Property.init(propertyData).amortizationScheduleArray != null) {
                amortizationScheduleList = Property.init(propertyData).amortizationScheduleArray as MutableList<AmortizationScheduleEntity>
                didCalcAmortizationSchedule = true
            }
        }

        if (didCalcYieldForecast) {
            yieldForecastList = Property.init(propertyData).yieldForecastListArray as MutableList<YieldForecastEntity>
            didCalcYieldForecast = true
        }

        when (visibleFragment) {
            "infoFragment" -> {
                infoFragment = PropertyInfoFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, infoFragment!!)
                    .commitAllowingStateLoss()
            }
            "amortizationScheduleFragment" -> {
                amortizationScheduleFragment = PropertyAmortizationScheduleFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, amortizationScheduleFragment!!)
                    .commitAllowingStateLoss()
            }
            "chartFragment" -> {
                chartFragment = PropertyChartFragment.instance(
                    this@PropertyActivity,
                    propertyData?.calcMortgagePeriod ?: yieldForecastList.size.div(12),
                    yieldForecastList)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, chartFragment!!)
                    .commitAllowingStateLoss()
            }
        }
    }
    //endregion == instance state ====

    //region == initialize =========
    fun initGlobal() {
        Utilities.log(Enums.LogType.Debug, TAG, "initGlobal()", showToast = false)

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)
        userId = preferences?.getString("userId", null)

        // property id
        roomPID = intent.getLongExtra(EXTRA_PROPERTY_ROOMID, 0)
        propertyId = intent.getStringExtra(EXTRA_PROPERTY_ID)

        // property city
        argPropertyCity = intent.getStringExtra(EXTRA_PROPERTY_CITY)

        // property data
        propertyData = PropertyEntity()

        // local/server data loaded
        isLocalFixedParametersLoaded = false
        isLocalPropertyLoaded = false
        isLocalUserLoaded = false

        isPropertyLoaded = false

        isDataInit = false
    }

    fun initViews() {
        Utilities.log(Enums.LogType.Debug, TAG, "initViews()", showToast = false)

        // actions menu
        /*snackContainerView = findViewById(R.id.snackContainer)
        snackYieldForecastView = findViewById(R.id.snackYieldForecast)
        snackAmortizationScheduleView = findViewById(R.id.snackAmortizationSchedule)
        snackChartView = findViewById(R.id.snackChart)*/
    }

    fun initData() {
        Utilities.log(Enums.LogType.Debug, TAG, "initData()", showToast = false)

        isDataInit = true
        userToken = preferences!!.getString("token", "")

        loadInfoFragment()
    }

    fun initEvents() {
        Utilities.log(Enums.LogType.Debug, TAG, "initEvents()", showToast = false)

        // interests
        layout?.interestsShow?.setOnClickListener {
            onShowInterests()
        }

        layout?.interestsHide?.setOnClickListener {
            onHideInterests()
        }

        // actions menu bottom
        layout?.actionsMenuYieldForecastBottom?.setOnClickListener {
            if (layout?.actionsMenuYieldForecastBottomIcon?.isEnabled == true) {
                onSnackYieldForecastClick()
            }
        }

        layout?.actionsMenuAmortizationScheduleBottom?.setOnClickListener {
            if (layout?.actionsMenuAmortizationScheduleBottomIcon?.isEnabled == true) {
                onSnackAmortizationScheduleClick()
            }
        }

        layout?.actionsMenuChartBottom?.setOnClickListener {
            if (layout?.actionsMenuChartBottomIcon?.isEnabled == true) {
                onSnackChartClick()
            }
        }

        // actions menu side
        layout?.actionsMenuYieldForecastSide?.setOnClickListener {
            onSnackYieldForecastClick()
        }

        layout?.actionsMenuAmortizationScheduleSide?.setOnClickListener {
            onSnackAmortizationScheduleClick()
        }

        layout?.actionsMenuChartSide?.setOnClickListener {
            onSnackChartClick()
        }
    }

    fun initObserver() {
        Utilities.log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.fixedParametersCallback.hasObservers()) viewModel!!.fixedParametersCallback.observe(this@PropertyActivity, LocalFixedParametersObserver(Enums.ObserverAction.GET_LOCAL))
        if (!viewModel!!.roomUserGet.hasObservers()) viewModel!!.roomUserGet.observe(this@PropertyActivity, LocalUserObserver(Enums.ObserverAction.GET_LOCAL))
        if (!viewModel!!.propertyGet.hasObservers()) viewModel!!.propertyGet.observe(this@PropertyActivity, ServerPropertyObserver(Enums.ObserverAction.GET))
        if (!viewModel!!.propertyInsert.hasObservers()) viewModel!!.propertyInsert.observe(this@PropertyActivity, ServerPropertyObserver(Enums.ObserverAction.CREATE))
        if (!viewModel!!.propertyUpdate.hasObservers()) viewModel!!.propertyUpdate.observe(this@PropertyActivity, ServerPropertyObserver(Enums.ObserverAction.UPDATE))
        if (!viewModel!!.localPropertyGet.hasObservers()) viewModel!!.localPropertyGet.observe(this@PropertyActivity, LocalPropertyObserver(Enums.ObserverAction.GET_LOCAL))
        if (!viewModel!!.localPropertyInsert.hasObservers()) viewModel!!.localPropertyInsert.observe(this@PropertyActivity, LocalPropertyObserver(Enums.ObserverAction.INSERT_LOCAL))
        if (!viewModel!!.localPropertyUpdate.hasObservers()) viewModel!!.localPropertyUpdate.observe(this@PropertyActivity, LocalPropertyObserver(Enums.ObserverAction.UPDATE_LOCAL))

        if (!isLocalFixedParametersLoaded && !isLocalUserLoaded && !isDataInit) {
            viewModel!!.getRoomFixedParameters(applicationContext)
            viewModel!!.getRoomUser(applicationContext, roomUID)
            viewModel!!.getLocalProperty(applicationContext, propertyId)
        }
    }

    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setPropertyPercentViewString(layout?.interest, "property_interest_label")
        Utilities.setPropertyPercentViewString(layout?.interestIn5Years, "property_interest_in_5_years_label")
        Utilities.setPropertyPercentViewString(layout?.interestIn10Years, "property_interest_in_10_years_label")
        Utilities.setPropertyPercentViewString(layout?.averageInterestAtTaking, "property_average_interest_at_taking_label")
        Utilities.setPropertyPercentViewString(layout?.averageInterestAtMaturity, "property_average_interest_at_maturity_label")
        Utilities.setPropertyPercentViewString(layout?.index, "property_index_label")
        Utilities.setPropertyPercentViewString(layout?.forecastAnnualPriceIncrease, "property_forecast_annual_price_increase_label")
        Utilities.setPropertyPercentViewString(layout?.salesCosts, "property_sales_costs_label")
        Utilities.setPropertyPercentViewString(layout?.depreciationForTaxPurposes, "property_depreciation_for_tax_purposes_label")
        Utilities.setPropertyPercentViewString(layout?.saleYearsPeriod, "property_sale_years_period_label")

        layout?.actionsMenuYieldForecastBottomText?.text = Utilities.getLocalPhrase("property_yield_forecast_label")
        layout?.actionsMenuAmortizationScheduleBottomText?.text = Utilities.getLocalPhrase("property_amortization_schedule_label")
        layout?.actionsMenuChartBottomText?.text = Utilities.getLocalPhrase("property_actions_menu_graph_label")
        layout?.actionsMenuYieldForecastSideText?.text = Utilities.getLocalPhrase("property_yield_forecast_label")
        layout?.actionsMenuAmortizationScheduleSideText?.text = Utilities.getLocalPhrase("property_amortization_schedule_label")
        layout?.actionsMenuChartSideText?.text = Utilities.getLocalPhrase("property_actions_menu_graph_label")

        super.setPhrases()
    }

    fun initIndexesAndInterests() {

        // index מדד
        if (roomPID == 0L || propertyData?.calcIndexPercent == null) {
            propertyData?.calcIndexPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INDEX_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.index, Const.INDEX_PERCENT, propertyData?.defaultIndexPercent, propertyData?.calcIndexPercent)

        layout?.index?.numberPickerAcceptView?.setOnClickListener {
            layout?.index?.numberPickerActualValue = layout?.index?.numberPickerView?.progress
            updateIndex()
            layout?.index?.onNumberPickerAccept()
            onIndexChanged()
        }

        layout?.index?.rollbackView?.setOnClickListener {
            layout?.index?.onRollbackClick()
            layout?.index?.numberPickerActualValue = null
            updateIndex()
            onIndexChanged()
        }

        // interest ריבית
        fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_PERCENT }?.default = propertyData?.calcInterestPercent ?: 0F

        if (roomPID == 0L || propertyData?.calcInterestPercent == null) {
            propertyData?.calcInterestPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.interest, Const.INTEREST_PERCENT, propertyData?.defaultInterestPercent, propertyData?.calcInterestPercent)

        layout?.interest?.numberPickerAcceptView?.setOnClickListener {
            layout?.interest?.numberPickerActualValue = layout?.interest?.numberPickerView?.progress
            updateInterest()
            layout?.interest?.onNumberPickerAccept()
            onInterestChanged()
        }

        layout?.interest?.rollbackView?.setOnClickListener {
            layout?.interest?.onRollbackClick()
            layout?.interest?.numberPickerActualValue = null
            updateInterest()
            onInterestChanged()
        }

        // interest in 5 years ריבית בעוד 5 שנים
        if (roomPID == 0L || propertyData?.calcInterestIn5YearsPercent == null) {
            (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_5_YEARS_PERCENT })?.default =
                propertyData?.calcInterestPercent?.plus(
                    (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_5_YEARS_PERCENT })?.delta!!
                )!!

            propertyData?.calcInterestIn5YearsPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_5_YEARS_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.interestIn5Years, Const.INTEREST_IN_5_YEARS_PERCENT, propertyData?.defaultInterestIn5YearsPercent, propertyData?.calcInterestIn5YearsPercent)

        layout?.interestIn5Years?.numberPickerAcceptView?.setOnClickListener {
            layout?.interestIn5Years?.numberPickerActualValue = layout?.interestIn5Years?.numberPickerView?.progress
            updateInterestIn5Years()
            layout?.interestIn5Years?.onNumberPickerAccept()
            onInterestIn5YearsChanged()
        }

        layout?.interestIn5Years?.rollbackView?.setOnClickListener {
            layout?.interestIn5Years?.onRollbackClick()
            layout?.interestIn5Years?.numberPickerActualValue = null
            updateInterestIn5Years()
            onInterestIn5YearsChanged()
        }

        // interest in 10 years ריבית בעוד 10 שנים
        if (roomPID == 0L || propertyData?.calcInterestIn10YearsPercent == null) {
            (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_10_YEARS_PERCENT })?.default =
                propertyData?.calcInterestPercent?.plus(
                    (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_10_YEARS_PERCENT })?.delta!!
                )!!

            propertyData?.calcInterestIn10YearsPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.INTEREST_IN_10_YEARS_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.interestIn10Years, Const.INTEREST_IN_10_YEARS_PERCENT, propertyData?.defaultInterestIn10YearsPercent, propertyData?.calcInterestIn10YearsPercent)

        layout?.interestIn10Years?.numberPickerAcceptView?.setOnClickListener {
            layout?.interestIn10Years?.numberPickerActualValue = layout?.interestIn10Years?.numberPickerView?.progress
            updateInterestIn10Years()
            layout?.interestIn10Years?.onNumberPickerAccept()
            onInterestIn10YearsChanged()
        }

        layout?.interestIn10Years?.rollbackView?.setOnClickListener {
            layout?.interestIn10Years?.onRollbackClick()
            layout?.interestIn10Years?.numberPickerActualValue = null
            updateInterestIn10Years()
            onInterestIn10YearsChanged()
        }

        // average interest at taking ריבית ממוצעת בזמן הלקיחה
        val routesAverageInterestAtTaking = (fixedParametersData?.averageInterestsArray?.find { it.average })

        fixedParametersData?.indexesAndInterestsArray?.find { it.name ==Const.AVERAGE_INTEREST_AT_TAKING_PERCENT }?.default =
            ((routesAverageInterestAtTaking?.prime ?: 0F)
            .plus(routesAverageInterestAtTaking?.linked ?: 0F)
            .plus(routesAverageInterestAtTaking?.notLinked ?: 0F)).div(3).floatFormat(1)

        if (roomPID == 0L || propertyData?.calcAverageInterestAtTakingPercent == null) {
            propertyData?.calcAverageInterestAtTakingPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.AVERAGE_INTEREST_AT_TAKING_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.averageInterestAtTaking, Const.AVERAGE_INTEREST_AT_TAKING_PERCENT, propertyData?.defaultAverageInterestAtTakingPercent, propertyData?.calcAverageInterestAtTakingPercent)

        layout?.averageInterestAtTaking?.numberPickerAcceptView?.setOnClickListener {
            layout?.averageInterestAtTaking?.numberPickerActualValue = layout?.averageInterestAtTaking?.numberPickerView?.progress
            updateAverageInterestAtTaking()
            layout?.averageInterestAtTaking?.onNumberPickerAccept()
            onAverageInterestAtTakingChanged()
        }

        layout?.averageInterestAtTaking?.rollbackView?.setOnClickListener {
            layout?.averageInterestAtTaking?.onRollbackClick()
            layout?.averageInterestAtTaking?.numberPickerActualValue = null
            updateAverageInterestAtTaking()
            onAverageInterestAtTakingChanged()
        }

        // average interest at maturity ריבית ממוצעת במועד הפרעון
        if (roomPID == 0L || propertyData?.calcAverageInterestAtMaturityPercent == null) {
            propertyData?.calcAverageInterestAtMaturityPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.AVERAGE_INTEREST_AT_MATURITY_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.averageInterestAtMaturity, Const.AVERAGE_INTEREST_AT_MATURITY_PERCENT, propertyData?.defaultAverageInterestAtMaturityPercent, propertyData?.calcAverageInterestAtMaturityPercent)

        layout?.averageInterestAtMaturity?.numberPickerAcceptView?.setOnClickListener {
            layout?.averageInterestAtMaturity?.numberPickerActualValue = layout?.averageInterestAtMaturity?.numberPickerView?.progress
            updateAverageInterestAtMaturity()
            layout?.averageInterestAtMaturity?.onNumberPickerAccept()
            onAverageInterestAtMaturityChanged()
        }

        layout?.averageInterestAtMaturity?.rollbackView?.setOnClickListener {
            layout?.averageInterestAtMaturity?.onRollbackClick()
            layout?.averageInterestAtMaturity?.numberPickerActualValue = null
            updateAverageInterestAtMaturity()
            onAverageInterestAtMaturityChanged()
        }

        // forecast annual price increase צפי עליית ערך שנתית של הנכס
        if (roomPID == 0L || propertyData?.calcForecastAnnualPriceIncreasePercent == null) {
            propertyData?.calcForecastAnnualPriceIncreasePercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.FORECAST_ANNUAL_PRICE_INCREASE_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.forecastAnnualPriceIncrease, Const.FORECAST_ANNUAL_PRICE_INCREASE_PERCENT, propertyData?.defaultForecastAnnualPriceIncreasePercent, propertyData?.calcForecastAnnualPriceIncreasePercent)

        layout?.forecastAnnualPriceIncrease?.numberPickerAcceptView?.setOnClickListener {
            layout?.forecastAnnualPriceIncrease?.numberPickerActualValue = layout?.forecastAnnualPriceIncrease?.numberPickerView?.progress
            updateForecastAnnualPriceIncrease()
            layout?.forecastAnnualPriceIncrease?.onNumberPickerAccept()
            onForecastAnnualPriceIncreaseChanged()
        }

        layout?.forecastAnnualPriceIncrease?.rollbackView?.setOnClickListener {
            layout?.forecastAnnualPriceIncrease?.onRollbackClick()
            layout?.forecastAnnualPriceIncrease?.numberPickerActualValue = null
            updateForecastAnnualPriceIncrease()
            onForecastAnnualPriceIncreaseChanged()
        }

        // sales costs עלויות מכירה
        if (roomPID == 0L || propertyData?.calcSalesCostsPercent == null) {
            propertyData?.calcSalesCostsPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.SALES_COSTS_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.salesCosts, Const.SALES_COSTS_PERCENT, propertyData?.defaultSalesCostsPercent, propertyData?.calcSalesCostsPercent)

        layout?.salesCosts?.numberPickerAcceptView?.setOnClickListener {
            layout?.salesCosts?.numberPickerActualValue = layout?.salesCosts?.numberPickerView?.progress
            updateSalesCosts()
            layout?.salesCosts?.onNumberPickerAccept()
            onSalesCostsChanged()
        }

        layout?.salesCosts?.rollbackView?.setOnClickListener {
            layout?.salesCosts?.onRollbackClick()
            layout?.salesCosts?.numberPickerActualValue = null
            updateSalesCosts()
            onSalesCostsChanged()
        }

        // depreciation for tax purposes פחת לצורך מס
        if (roomPID == 0L || propertyData?.calcDepreciationForTaxPurposesPercent == null) {
            propertyData?.calcDepreciationForTaxPurposesPercent = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.DEPRECIATION_FOR_TAX_PURPOSES_PERCENT })?.default
        }

        setIndexesAndInterestsPercentAttributes(layout?.depreciationForTaxPurposes, Const.DEPRECIATION_FOR_TAX_PURPOSES_PERCENT, propertyData?.defaultDepreciationForTaxPurposesPercent, propertyData?.calcDepreciationForTaxPurposesPercent)

        layout?.depreciationForTaxPurposes?.numberPickerAcceptView?.setOnClickListener {
            layout?.depreciationForTaxPurposes?.numberPickerActualValue = layout?.depreciationForTaxPurposes?.numberPickerView?.progress
            updateDepreciationForTaxPurposes()
            layout?.depreciationForTaxPurposes?.onNumberPickerAccept()
            onDepreciationForTaxPurposesChanged()
        }

        layout?.depreciationForTaxPurposes?.rollbackView?.setOnClickListener {
            layout?.depreciationForTaxPurposes?.onRollbackClick()
            layout?.depreciationForTaxPurposes?.numberPickerActualValue = null
            updateDepreciationForTaxPurposes()
            onDepreciationForTaxPurposesChanged()
        }

        // sale years period
        if (roomPID == 0L || propertyData?.calcSaleYearsPeriod == null) {
            propertyData?.calcSaleYearsPeriod = (fixedParametersData?.indexesAndInterestsArray?.find { it.name == Const.SALE_YEARS_PERIOD })?.default?.toInt()
        }

        setIndexesAndInterestsPercentAttributes(layout?.saleYearsPeriod, Const.SALE_YEARS_PERIOD, propertyData?.defaultSaleYearsPeriod?.toFloat(), propertyData?.calcSaleYearsPeriod?.toFloat())

        layout?.saleYearsPeriod?.numberPickerAcceptView?.setOnClickListener {
            layout?.saleYearsPeriod?.numberPickerActualValue = layout?.saleYearsPeriod?.numberPickerView?.progress
            updateSaleYearsPeriod()
            layout?.saleYearsPeriod?.onNumberPickerAccept()
            onSaleYearsPeriodChanged()
        }

        layout?.saleYearsPeriod?.rollbackView?.setOnClickListener {
            layout?.saleYearsPeriod?.onRollbackClick()
            layout?.saleYearsPeriod?.numberPickerActualValue = null
            updateSaleYearsPeriod()
            onSaleYearsPeriodChanged()
        }
    }

    fun displayIndexesAndInterests() {
        if (propertyData?.showInterestsContainer != false) {
            layout?.interestsContainer?.visibility = VISIBLE
            layout?.interestsHide?.visibility = VISIBLE
        }
        else {
            layout?.interestsContainer?.visibility = GONE
            layout?.interestsHide?.visibility = GONE
        }
    }

    //endregion == initialize =========

    private fun loadInfoFragment() {
        Utilities.log(Enums.LogType.Debug, TAG, "loadInfoFragment()")

        infoFragment = PropertyInfoFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, infoFragment!!)
            .commitAllowingStateLoss()

        visibleFragment = "infoFragment"
    }

    fun getCellStyle(monthNo: Int, isFirst: Boolean = false) : Int {

        //initIndexesAndInterests()

        if (monthNo.rem(12) == 1 && monthNo.div(12) == propertyData?.calcSaleYearsPeriod) {
            return  if (isFirst)
                R.style.table_cell_first_selected
            else
                R.style.table_cell_selected
        }
        else {
            return if (isFirst) {
                if (monthNo.rem(12) == 1)
                    R.style.table_cell_first_bold
                else
                    R.style.table_cell_first
            } else {
                if (monthNo.rem(12) == 1)
                    R.style.table_cell_bold
                else
                    R.style.table_cell
            }
        }

    }

    //region == interests input updates ==
    private fun updateIndex() {
        propertyData?.calcIndexPercent = layout?.index?.numberPickerActualValue
        saveProperty(Const.INDEX_PERCENT, propertyData?.calcIndexPercent)
    }

    private fun updateInterest() {
        propertyData?.calcInterestPercent = layout?.interest?.numberPickerActualValue
        saveProperty(Const.INTEREST_PERCENT, propertyData?.calcInterestPercent)
    }

    private fun updateInterestIn5Years() {
        propertyData?.calcInterestIn5YearsPercent = layout?.interestIn5Years?.numberPickerActualValue
        saveProperty(Const.INTEREST_IN_5_YEARS_PERCENT, propertyData?.calcInterestIn5YearsPercent)
    }

    private fun updateInterestIn10Years() {
        propertyData?.calcInterestIn10YearsPercent = layout?.interestIn10Years?.numberPickerActualValue
        saveProperty(Const.INTEREST_IN_10_YEARS_PERCENT, propertyData?.calcInterestIn10YearsPercent)
    }

    private fun updateAverageInterestAtTaking() {
        propertyData?.calcAverageInterestAtTakingPercent = layout?.averageInterestAtTaking?.numberPickerActualValue
        saveProperty(Const.AVERAGE_INTEREST_AT_TAKING_PERCENT, propertyData?.calcAverageInterestAtTakingPercent)
    }

    private fun updateAverageInterestAtMaturity() {
        propertyData?.calcAverageInterestAtMaturityPercent = layout?.averageInterestAtMaturity?.numberPickerActualValue
        saveProperty(Const.AVERAGE_INTEREST_AT_MATURITY_PERCENT, propertyData?.calcAverageInterestAtMaturityPercent)
    }

    private fun updateForecastAnnualPriceIncrease() {
        propertyData?.calcForecastAnnualPriceIncreasePercent = layout?.forecastAnnualPriceIncrease?.numberPickerActualValue
        saveProperty(Const.FORECAST_ANNUAL_PRICE_INCREASE_PERCENT, propertyData?.calcForecastAnnualPriceIncreasePercent)
    }

    private fun updateSalesCosts() {
        propertyData?.calcSalesCostsPercent = layout?.salesCosts?.numberPickerActualValue
        saveProperty(Const.SALES_COSTS_PERCENT, propertyData?.calcSalesCostsPercent)
    }

    private fun updateDepreciationForTaxPurposes() {
        propertyData?.calcDepreciationForTaxPurposesPercent = layout?.depreciationForTaxPurposes?.numberPickerActualValue
        saveProperty(Const.DEPRECIATION_FOR_TAX_PURPOSES_PERCENT, propertyData?.calcDepreciationForTaxPurposesPercent)
    }

    private fun updateSaleYearsPeriod() {
        propertyData?.calcSaleYearsPeriod = layout?.saleYearsPeriod?.numberPickerActualValue?.toInt()
        saveProperty(Const.SALE_YEARS_PERIOD, propertyData?.calcSaleYearsPeriod)
    }
    //endregion == interests input updates ==

    //region == input events =============
    private fun onIndexChanged() {
        amortizationScheduleList = mutableListOf()
        yieldForecastList = mutableListOf()

        initIndexesAndInterests()

        if (Property.init(propertyData).amortizationScheduleArray != null) {
            amortizationScheduleList = Property.init(propertyData).amortizationScheduleArray as MutableList<AmortizationScheduleEntity>
            didCalcAmortizationSchedule = true
        }

        yieldForecastList = Property.init(propertyData).yieldForecastListArray as MutableList<YieldForecastEntity>
        didCalcYieldForecast = true

        when (visibleFragment) {

            "infoFragment" -> {
                infoFragment = PropertyInfoFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, infoFragment!!)
                    .commitAllowingStateLoss()
            }
            "yieldForecastFragment" -> {
                yieldForecastFragment = PropertyYieldForecastFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, yieldForecastFragment!!)
                    .commitAllowingStateLoss()
            }
            "amortizationScheduleFragment" -> {
                amortizationScheduleFragment = PropertyAmortizationScheduleFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, amortizationScheduleFragment!!)
                    .commitAllowingStateLoss()
            }
            "chartFragment" -> {
                chartFragment = PropertyChartFragment.instance(
                    this@PropertyActivity,
                    propertyData?.calcMortgagePeriod ?: yieldForecastList.size.div(12),
                    yieldForecastList)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, chartFragment!!)
                    .commitAllowingStateLoss()
            }
        }

    }

    private fun onInterestChanged() {
        onIndexChanged()
    }

    private fun onInterestIn5YearsChanged() {
        onIndexChanged()
    }

    private fun onInterestIn10YearsChanged() {
        onIndexChanged()
    }

    private fun onAverageInterestAtTakingChanged() {
        onIndexChanged()
    }

    private fun onAverageInterestAtMaturityChanged() {
        onIndexChanged()
    }

    private fun onForecastAnnualPriceIncreaseChanged() {
        onIndexChanged()
    }

    private fun onSalesCostsChanged() {
        onIndexChanged()
    }

    private fun onDepreciationForTaxPurposesChanged() {
        onIndexChanged()
    }

    private fun onSaleYearsPeriodChanged() {
        onIndexChanged()
    }
    //endregion == input events =============

    //region == input number picker ======
    fun setNumberPickerAttributes(view: PropertyInput?, name: String, actualValue: Float?) {
        val attributes = fixedParametersData?.propertyInputsArray?.find { it.name == name }
        view?.setNumberPickerDefaultValue(attributes?.default ?: 0f)
        view?.setNumberPickerMinValue(attributes?.min ?: 0f)
        view?.setNumberPickerMaxValue(attributes?.max ?: 0f)
        view?.setNumberPickerStepsSize(attributes?.step ?: 0f)
        if (actualValue == null) {
            view?.setNumberPickerWithoutValue()
        }
        else {
            view?.setNumberPickerActualValue(actualValue ?: (attributes?.default ?: 0f))
        }
    }

    private fun setIndexesAndInterestsPercentAttributes(view: PropertyPercent?, name: String, defaultValue: Float?, calcValue: Float?) {
        val attributes = fixedParametersData?.indexesAndInterestsArray?.find { it.name == name }
        view?.setNumberPickerDefaultValue(defaultValue?.floatFormat(1) ?: 0f)
        view?.setNumberPickerMinValue(attributes?.min ?: 0f)
        view?.setNumberPickerMaxValue(attributes?.max ?: 0f)
        view?.setNumberPickerStepsSize(attributes?.step ?: 0f)
        view?.setNumberPickerActualValue(calcValue?.floatFormat(1) ?: (defaultValue?.floatFormat(1) ?: 0f))
    }
    //endregion == input number picker ======

    //region == snack ====================
    private fun onSnackYieldForecastClick() {
        Utilities.log(Enums.LogType.Debug, TAG, "onSnackYieldForecastClick()")

        initIndexesAndInterests()

        if (Property.init(propertyData).amortizationScheduleArray != null) {
            amortizationScheduleList = Property.init(propertyData).amortizationScheduleArray as MutableList<AmortizationScheduleEntity>
            didCalcAmortizationSchedule = true
        }

        yieldForecastList = Property.init(propertyData).yieldForecastListArray as MutableList<YieldForecastEntity>
        didCalcYieldForecast = true

        yieldForecastFragment = PropertyYieldForecastFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, yieldForecastFragment!!)
            .commitAllowingStateLoss()

        visibleFragment = "yieldForecastFragment"
    }

    private fun onSnackAmortizationScheduleClick() {
        Utilities.log(Enums.LogType.Debug, TAG, "onSnackAmortizationScheduleClick()")

        initIndexesAndInterests()

        if (Property.init(propertyData).amortizationScheduleArray != null) {
            amortizationScheduleList = Property.init(propertyData).amortizationScheduleArray as MutableList<AmortizationScheduleEntity>
            didCalcAmortizationSchedule = true
        }

        yieldForecastList = Property.init(propertyData).yieldForecastListArray as MutableList<YieldForecastEntity>
        didCalcYieldForecast = true

        amortizationScheduleFragment = PropertyAmortizationScheduleFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, amortizationScheduleFragment!!)
            .commitAllowingStateLoss()

        visibleFragment = "amortizationScheduleFragment"
    }

    private fun onSnackChartClick() {
        initIndexesAndInterests()

        if (Property.init(propertyData).amortizationScheduleArray != null) {
            amortizationScheduleList = Property.init(propertyData).amortizationScheduleArray as MutableList<AmortizationScheduleEntity>
            didCalcAmortizationSchedule = true
        }

        yieldForecastList = Property.init(propertyData).yieldForecastListArray as MutableList<YieldForecastEntity>
        didCalcYieldForecast = true

        chartFragment = PropertyChartFragment.instance(
            this@PropertyActivity,
            propertyData?.calcMortgagePeriod ?: yieldForecastList.size.div(12),
            yieldForecastList)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, chartFragment!!)
            .commitAllowingStateLoss()

        visibleFragment = "chartFragment"
    }

    fun updateActionsMenuVisibility(selectedButton: ActionsMenuButtonType) {
        layout?.actionsMenuContainerBottom?.visibility = GONE
        layout?.actionsMenuContainerSide?.visibility = VISIBLE

        layout?.actionsMenuYieldForecastSideIcon?.setImageDrawable(ContextCompat.getDrawable(context, if (selectedButton == ActionsMenuButtonType.YIELD_FORECAST) R.drawable.icon_yield_forecast_on else R.drawable.icon_yield_forecast_off))
        layout?.actionsMenuYieldForecastSideText?.setTextColor(ContextCompat.getColor(context, if (selectedButton == ActionsMenuButtonType.YIELD_FORECAST) R.color.textActionsMenuSelected else R.color.textActionsMenu))

        if (propertyData?.showMortgagePrepayment == false || userData?.calcCanTakeMortgage == false) {
            layout?.actionsMenuAmortizationScheduleSide?.visibility = GONE
        }
        else {
            layout?.actionsMenuAmortizationScheduleSide?.visibility = VISIBLE
            layout?.actionsMenuAmortizationScheduleSideIcon?.setImageDrawable(ContextCompat.getDrawable(context, if (selectedButton == ActionsMenuButtonType.AMORTIZATION_SCHEDULE) R.drawable.icon_amortization_schedule_on else R.drawable.icon_amortization_schedule_off))
            layout?.actionsMenuAmortizationScheduleSideText?.setTextColor(ContextCompat.getColor(context, if (selectedButton == ActionsMenuButtonType.AMORTIZATION_SCHEDULE) R.color.textActionsMenuSelected else R.color.textActionsMenu))
        }

        layout?.actionsMenuChartSideIcon?.setImageDrawable(ContextCompat.getDrawable(context, if (selectedButton == ActionsMenuButtonType.CHART) R.drawable.icon_chart_on else R.drawable.icon_chart_off))
        layout?.actionsMenuChartSideText?.setTextColor(ContextCompat.getColor(context, if (selectedButton == ActionsMenuButtonType.CHART) R.color.textActionsMenuSelected else R.color.textActionsMenu))
    }

    enum class ActionsMenuButtonType {
        CHART, AMORTIZATION_SCHEDULE, YIELD_FORECAST
    }

    //endregion == snack ====================

    //region == interests ================
    private fun onShowInterests() {
        layout?.interestsContainer?.visibility = VISIBLE
        layout?.interestsHide?.visibility = VISIBLE

        propertyData?.showInterestsContainer = true
        saveProperty("showInterestsContainer", propertyData?.showInterestsContainer)
    }

    private fun onHideInterests() {
        layout?.interestsContainer?.visibility = GONE
        layout?.interestsHide?.visibility = GONE

        propertyData?.showInterestsContainer = false
        saveProperty("showInterestsContainer", propertyData?.showInterestsContainer)
    }
    //endregion == interests ================

    //region == observers ================
    private inner class LocalFixedParametersObserver(action: Enums.ObserverAction) : Observer<FixedParametersEntity?> {
        val _action = action
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_LOCAL -> {
                    Utilities.log(Enums.LogType.Debug, TAG, "LocalFixedParametersObserver(): onChanged. GET_ROOM")

                    isLocalFixedParametersLoaded = true

                    if (fixedParameters == null) {
                        return
                    }

                    // fixed parameters
                    fixedParametersData = FixedParameters.init(fixedParameters)

                    if (isLocalFixedParametersLoaded && isLocalUserLoaded && isLocalPropertyLoaded && !isDataInit) {
                        initData()
                    }
                }

                else -> {}
            }
        }
    }

    private inner class LocalUserObserver(action: Enums.ObserverAction) : Observer<UserEntity?> {
        val _action = action
        override fun onChanged(user: UserEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_LOCAL -> {
                    Utilities.log(Enums.LogType.Debug, TAG, "LocalUserObserver(): GET_LOCAL. onChanged")

                    isLocalUserLoaded = true

                    if (user != null) {
                        userData = user
                    }

                    if (isLocalFixedParametersLoaded && isLocalUserLoaded && isLocalPropertyLoaded && !isDataInit) {
                        initData()
                    }
                }

                else -> {}
            }
        }
    }

    private inner class ServerPropertyObserver(action: Enums.ObserverAction) : Observer<PropertyEntity?> {
        val _action = action
        override fun onChanged(property: PropertyEntity?) {
            Utilities.log(Enums.LogType.Debug, TAG, "ServerPropertyObserver(): ${if (_action == Enums.ObserverAction.GET) "GET_SERVER" else if (_action == Enums.ObserverAction.CREATE) "INSERT_SERVER" else "UPDATE_SERVER"}. property._id = ${property?._id}")

            GlobalScope.launch {

                val nowUTC = Calendar.getInstance()
                nowUTC.timeZone = TimeZone.getTimeZone("UTC")

                propertyData = property
                propertyData?.roomID = if (roomPID == 0L) null else  roomPID

                viewModel!!.actionLocalProperty(
                    applicationContext,
                    propertyData,
                )
            }

            unfreezeScreen()
        }
    }

    private inner class LocalPropertyObserver(action: Enums.ObserverAction) : Observer<PropertyEntity?> {
        val _action = action
        override fun onChanged(property: PropertyEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_LOCAL -> {
                    Utilities.log(Enums.LogType.Debug, TAG, "LocalPropertyObserver(): GET_LOCAL. onChanged. property")

                    isLocalPropertyLoaded = true

                    if (property != null) {
                        propertyData = property
                    }

                    if (isLocalFixedParametersLoaded && isLocalUserLoaded && isLocalPropertyLoaded && !isDataInit) {
                        initData()
                    }
                }

                Enums.ObserverAction.INSERT_LOCAL, Enums.ObserverAction.UPDATE_LOCAL -> {
                    Utilities.log(Enums.LogType.Debug, TAG, "LocalPropertyObserver(): ${if (_action == Enums.ObserverAction.INSERT_LOCAL) "INSERT_LOCAL" else "UPDATE_LOCAL"}. onChanged. property?.roomID = ${property?.roomID}")

                    propertyData?.roomID = property?.roomID
                    roomPID = property?.roomID

                    infoFragment?.initData()

                    infoFragment?.activateActionsMenuIfNeeded()

                    initIndexesAndInterests()
                }

                else -> {}
            }
        }
    }

    //endregion == observers ================

    //region == general ==================
    fun saveProperty(fieldName: String? = "", fieldValue: Boolean? =  null) {
        saveProperty(fieldName, fieldValue.toString())
    }

    fun saveProperty(fieldName: String? = "", fieldValue: Int? =  null) {
        saveProperty(fieldName, fieldValue.toString())
    }

    fun saveProperty(fieldName: String? = "", fieldValue: Float? =  null) {
        saveProperty(fieldName, fieldValue.toString())
    }

    fun saveProperty(fieldName: String? = "", fieldValue: String? =  null) {
        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        if (roomPID == 0L) {
            //val ignoreCity = false// !argPropertyCity.isNullOrEmpty() && propertyData?.city.equals(argPropertyCity)

            //if (!isAnyRequireFieldNotEmpty(ignoreCity)) {
                freezeScreen()
                viewModel!!.actionProperty(propertyData?._id, fieldName, if (fieldValue.isNullOrEmpty() || fieldValue.equals("null")) null else fieldValue)
            //}
        }
        else {
            freezeScreen()
            propertyData?.roomID = roomPID
            viewModel!!.actionProperty(propertyData?._id, fieldName, if (fieldValue.isNullOrEmpty() || fieldValue.equals("null")) null else fieldValue)
        }
    }

    private fun isAnyRequireFieldNotEmpty(ignoreCity: Boolean) : Boolean {
        var isRequireFieldEmpty = true

        if (!ignoreCity && !(propertyData?.city.isNullOrEmpty())) isRequireFieldEmpty = false
        else if (!(propertyData?.address.isNullOrEmpty())) isRequireFieldEmpty = false
        else if (!(propertyData?.apartmentType.isNullOrEmpty())) isRequireFieldEmpty = false
        else if (propertyData?.price != null) isRequireFieldEmpty = false

        return isRequireFieldEmpty
    }
    //endregion == general ==================

    //region == base abstract ============

    override fun attachBinding(list: MutableList<ActivityPropertyBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityPropertyBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ============

    //region == freeze screen ============

    fun freezeScreen() {
        this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        layout?.containerMask?.visibility = VISIBLE
    }

    fun unfreezeScreen() {
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        layout?.containerMask?.visibility = GONE
    }

    //endregion == freeze screen ============
}