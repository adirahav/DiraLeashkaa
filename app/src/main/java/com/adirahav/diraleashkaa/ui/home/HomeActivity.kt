package com.adirahav.diraleashkaa.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Utilities.await
import com.adirahav.diraleashkaa.common.Utilities.percentFormat
import com.adirahav.diraleashkaa.common.Utilities.toNIS
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.dataClass.HomeDataClass
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.databinding.ActivityHomeBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import com.adirahav.diraleashkaa.ui.property.PropertyChartFragment
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.lang.reflect.Type
import java.util.*

class HomeActivity : BaseActivity<HomeViewModel?, ActivityHomeBinding>(),
    HomePropertiesAdapter.OnPropertyAdapter,
    HomePropertiesAdapter.RecyclerViewReadyCallback,
    HomeCitiesAdapter.OnCityAdapter,
    HomeBestYieldAdapter.OnBestYieldAdapter {

    //region == companion ==========

    companion object {
        private const val TAG = "HomeActivity"
        private const val MIN_DELETE_AWAIT_SECONDS = 1
        private const val MIN_HOME_AWAIT_SECONDS = 2
        private const val BEST_YIELD_YEARS_PERIOD_DEFAULT = 10
        private const val BEST_YIELD_PICTURE_SIZE_RATIO = 0.5F
        private const val LOAD_FROM_ROOM = true

        fun start(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@HomeActivity

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // room/server data loaded
    var isRoomFixedParametersLoaded: Boolean = false
    var isRoomMyCitiesLoaded: Boolean = false
    var isRoomBestYieldLoaded: Boolean = false

    var isServerHomeLoaded: Boolean = false

    var isDataInit: Boolean = false

    // room user id
    var roomUID: Long? = 0L

    // server user uuid
    var userUUID: String? = null

    // shared preferences
    var preferences: AppPreferences? = null

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // delete start time
    var deleteStartTime: Date? = null

    // layout
    internal lateinit var layout: ActivityHomeBinding

    // actions mask
    var maskHolder: HomePropertiesAdapter.ViewHolder? = null

    // my cities
    private var myCitiesAdapter: HomeCitiesAdapter? = null
    var myCitiesData: List<PropertyEntity?>? = null
    var myOldCitiesData: List<PropertyEntity?>? = null
    private var isCitiesNeedToRefresh: Boolean = true

    // city's properties
    private var cityPropertiesAdapter: HomePropertiesAdapter? = null
    private var isPropertiesNeedToRefresh: Boolean = true

    // best yield
    var bestYieldArray = ArrayList<BestYieldEntity>()
    var bestYieldData: List<BestYieldEntity?>? = null
    private var isBestYieldNeedToRefresh: Boolean = true

    // home
    var homeData: HomeDataClass? = null

    //endregion == variables ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(layout.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // my cities
        //preferences?.setString(null, null, false)
    }

    public override fun onResume() {
        super.onResume()

        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()
        initEvents()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar(layout.drawer)
        setDrawer(layout.drawer, layout.menu)

        // title text
        titleText?.text = Utilities.getRoomString("actionbar_title_home")

        // track user
        //Log.d("ADITEST6", "GET ${preferences?.getBoolean("isTrackUser", false).toString()} [home]")
        /*trackUser?.visibility =
            if (preferences?.getBoolean("isTrackUser", false) == true)
                VISIBLE
            else
                GONE*/

        freezeScreen()
    }

    private fun initGlobal() {

        // room/server data loaded
        isRoomFixedParametersLoaded = false
        isRoomMyCitiesLoaded = false
        isRoomBestYieldLoaded = false

        isServerHomeLoaded = false

        isDataInit = false

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)
        userUUID = preferences?.getString("userUUID", null)
        Utilities.log(Enums.LogType.Debug, TAG, "preferences userUUID = ${userUUID}")

        // actions maskonRe
        maskHolder = null
    }

    private fun initEvents() {
        // first login add property
        layout.firstLoginAddProperty.setOnClickListener {
            Utilities.log(Enums.LogType.Debug, TAG, "firstLoginAddProperty click")
            PropertyActivity.start(this, 0, "", city = null)
        }

        // add property
        layout.fabAddProperty.setOnClickListener {
            Utilities.log(Enums.LogType.Debug, TAG, "fabAddProperty click")
            PropertyActivity.start(this, 0, "", null)
        }

        // swipe to refresh
        layout.swipeToRefresh.setOnRefreshListener {
            Utilities.log(Enums.LogType.Debug, TAG, "swipeToRefresh refresh")
            viewModel!!.getServerHome(applicationContext, userUUID)
            selectedCityPosition = null
            preferences?.setString("homeSelectedCity", null, false)
        }
    }

    fun initObserver() {
        Utilities.log(Enums.LogType.Debug, TAG, "initObserver()")
        if (!viewModel!!.roomFixedParameters.hasObservers()) viewModel!!.roomFixedParameters.observe(this@HomeActivity, RoomFixedParametersObserver())
        if (!viewModel!!.roomMyCities.hasObservers()) viewModel!!.roomMyCities.observe(this@HomeActivity, RoomMyCitiesObserver())
        if (!viewModel!!.roomCityProperties.hasObservers()) viewModel!!.roomCityProperties.observe(this@HomeActivity, RoomCityPropertiesObserver() as Observer<in ArrayList<PropertyEntity>>)
        if (!viewModel!!.roomBestYield.hasObservers()) viewModel!!.roomBestYield.observe(this@HomeActivity, RoomBestYieldObserver())
        if (!viewModel!!.serverDeleteProperty.hasObservers()) viewModel!!.serverDeleteProperty.observe(this@HomeActivity, ServerDeletePropertyObserver())
        if (!viewModel!!.serverHome.hasObservers()) viewModel!!.serverHome.observe(this@HomeActivity, ServerHomeObserver())

        if (LOAD_FROM_ROOM) {
            if (!isRoomFixedParametersLoaded && !isRoomMyCitiesLoaded && !isRoomBestYieldLoaded && !isServerHomeLoaded && !isDataInit) {
                Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): getRoomFixedParameters, getRoomMyCities, getRoomBestYield")
                viewModel!!.getRoomFixedParameters(applicationContext)
                viewModel!!.getRoomMyCities(applicationContext)
                viewModel!!.getRoomBestYield(applicationContext)
            }
        }
        else {
            if (!isServerHomeLoaded && !isDataInit) {
                Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): getServerHome")
                viewModel!!.getServerHome(applicationContext, userUUID)
            }

        }
    }

    override fun createViewModel(): HomeViewModel {
        val factory = HomeViewModelFactory(DataManager.instance!!.propertyService, DataManager.instance!!.homeService)
        return ViewModelProvider(this, factory)[HomeViewModel::class.java]
    }

    fun setMyCities() {

        if (myCitiesData == null) {
            return
        }

        if (myCitiesData?.size == 0) {
            layout.firstLoginContainer.visibility = VISIBLE
            layout.dataContainer.visibility = GONE
        }
        else {
            layout.firstLoginContainer.visibility = GONE
            layout.dataContainer.visibility = VISIBLE

            myCitiesData!!.forEach {
                city -> city?.cityElse = fixedParametersData?.citiesArray?.find {
                        it.key == city?.city
                    }?.value ?: city?.city
            }

            myCitiesData = myCitiesData!!.sortedBy { it?.cityElse }

            val myNotElseCitiesData = arrayListOf<PropertyEntity?>()
            myCitiesData!!.filterTo(myNotElseCitiesData, { it?.city != "else" })

            val myElseCitiesData = arrayListOf<PropertyEntity?>()
            myCitiesData!!.filterTo(myElseCitiesData, { it?.city == "else" })

            val myNewCitiesData: ArrayList<PropertyEntity?> = ArrayList()
            myNewCitiesData.addAll(myNotElseCitiesData)
            myNewCitiesData.addAll(myElseCitiesData)

            myCitiesData = myNewCitiesData

            isCitiesNeedToRefresh = myOldCitiesData?.equals(myCitiesData) != true
            myOldCitiesData = myCitiesData

        }

        // my cities
        myCitiesAdapter = HomeCitiesAdapter(context, this@HomeActivity)
        layout.myCitiesList.adapter = myCitiesAdapter

        if (isCitiesNeedToRefresh) {
            selectedCityPosition = null
            preferences?.setString("homeSelectedCity", null, false)
        }

        myCitiesAdapter!!.setItems(myCitiesData!!)

        // city's properties
        if (myCitiesData?.size!! > 0) {
            layout.cityPropertiesTitle.visibility = VISIBLE
            layout.cityPropertiesList.visibility = VISIBLE

            val selectedCity = preferences?.getString("homeSelectedCity", null)
            if (isPropertiesNeedToRefresh && myCitiesData?.filter { it?.city == selectedCity }?.size == 1) {
                setCityProperties(myCitiesData!!.filter { it?.city == selectedCity }.get(0)!!)
            }
            else {
                setCityProperties(myCitiesData?.get(0)!!)
            }
        }
        else {
            layout.cityPropertiesTitle.visibility = GONE
            layout.cityPropertiesList.visibility = GONE
        }
    }

    private fun setCityProperties(property: PropertyEntity) {

        if (!isPropertiesNeedToRefresh) {
            return
        }
        val city = fixedParametersData?.citiesArray?.find { it.key == property.city ?: "else" }

        layout.cityPropertiesTitle.text = HtmlCompat.fromHtml(String.format(
            Utilities.getRoomString("home_properties_title"),
            city?.value ?: property.city
        ), HtmlCompat.FROM_HTML_MODE_LEGACY)

        // city properties adapter
        cityPropertiesAdapter = HomePropertiesAdapter(this@HomeActivity, this, this)
        layout.cityPropertiesList.adapter = cityPropertiesAdapter
        viewModel!!.getCityProperties(applicationContext, property.city)
    }

    fun setBestYield() {

        if (!isBestYieldNeedToRefresh) {
            return
        }

        bestYieldArray = ArrayList()

        val yearsPeriod = fixedParametersData?.bestYieldArray?.find { it.key == "years_period" }?.value?.toInt() ?: BEST_YIELD_YEARS_PERIOD_DEFAULT

        if (bestYieldData != null && bestYieldData!!.size > 0) {

            for (bestYield in bestYieldData!!) {
                bestYieldArray.add(bestYield!!)
            }
        }

        layout.bestYieldTitle.text = HtmlCompat.fromHtml(String.format(
            Utilities.getRoomString("home_best_yield_title"),
            yearsPeriod
        ), HtmlCompat.FROM_HTML_MODE_LEGACY)


        //bestYieldArray.sortByDescending { it.averageReturnOnEquity }

        if (bestYieldArray.size > 0) {

            val bestYield = bestYieldArray[0]

            // address
            layout.bestYieldAddress.text =
                if (bestYield.address.isNullOrEmpty())
                    if (bestYield.city.equals("else"))
                        bestYield.cityElse
                    else
                        fixedParametersData?.citiesArray?.find {
                        it.key == bestYield.city
                    }?.value ?: bestYield.city
                else
                    String.format(
                        Utilities.getRoomString("home_best_yield_address"),
                        bestYield.address,
                        if (bestYield.city.equals("else"))
                            bestYield.cityElse
                        else
                            fixedParametersData?.citiesArray?.find {
                                    it.key == bestYield.city
                                }?.value ?: bestYield.city
                    )

            // chart
            val yieldForecastType: Type = object : TypeToken<List<YieldForecastEntity>>() {}.type
            val yieldForecastArray: ArrayList<YieldForecastEntity> = Utilities.parseArray(
                json = bestYield.yieldForecast,
                typeToken = yieldForecastType
            )
            val chartFragment : PropertyChartFragment = PropertyChartFragment.instance(
                this@HomeActivity,
                bestYield.mortgagePeriod ?: yieldForecastArray?.size?.div(12),
                yieldForecastArray)



            supportFragmentManager.beginTransaction()
                .add(R.id.bestYieldChart, chartFragment)
                .commit()

            // details
            layout.bestYieldAverageReturnValue.text = bestYield.averageReturn?.percentFormat(1)
            layout.bestYieldAverageReturnOnEquityValue.text = bestYield.averageReturnOnEquity?.percentFormat(1)
            layout.bestYieldTotalProfitValue.text = bestYield.profit?.toInt()?.toNIS()
            layout.bestYieldProfitNPVValue.text = bestYield.profitNpv?.toInt()?.toNIS()

            layout.bestYieldDetails.visibility = VISIBLE
            layout.bestYieldNoDetails.visibility = GONE

            /*// picture
            val pictureWidth = Resources.getSystem().displayMetrics.widthPixels
            val pictureHeight = pictureWidth.times(BEST_YIELD_PICTURE_SIZE_RATIO).toInt()

            Utilities.setPropertyPicture(
                layout.bestYieldPicture,
                bestYieldProperty.pictures,
                BEST_YIELD_PICTURE_SIZE_RATIO,
                pictureWidth,
                pictureHeight
            )

            layout.bestYieldPicture.layoutParams?.height = pictureHeight*/
        }
        else {
            layout.bestYieldDetails.visibility = GONE
            layout.bestYieldPicture.visibility = GONE
            layout.bestYieldNoDetails.visibility = VISIBLE
        }

        /*bestYieldArray = bestYieldArray.dropLast(bestYieldArray.size - propertiesShowCount) as ArrayList<BestYieldModel>

        bestYieldAdapter = HomeBestYieldAdapter(context, this@HomeActivity)
        bestYieldList!!.adapter = bestYieldAdapter
        bestYieldAdapter!!.setItems(bestYieldArray)

        bestYieldList!!.getLayoutParams().height =
            propertiesShowCount.times(dpToPx(context, + resources.getDimension(R.dimen.chart_small_height)
                                                      + resources.getDimension(R.dimen.score_size)
                                                      + resources.getDimension(R.dimen.padding)
                                                      + resources.getDimension(R.dimen.padding) ))*/


    }

    // events
    // selected holder
    var selectedCityPosition: Int? = null
    override fun onCityClicked(city: PropertyEntity, position: Int) {
        selectedCityPosition = position
        preferences?.setString("homeSelectedCity", city.city, false)

        isPropertiesNeedToRefresh = true
        setCityProperties(city)
    }

    override fun onPropertyClicked(property: PropertyEntity) {
        if (maskHolder == null) {
            PropertyActivity.start(this, property.roomID!!, property.uuid!!, property.city)
        }
    }

    override fun onBestYieldClicked(bestYield: BestYieldEntity) {

    }

    override fun onDeletedPropertyClicked(property: PropertyEntity) {
        freezeScreen()
        deleteStartTime = Date()
        viewModel!!.deleteServerProperty(applicationContext, property, userUUID)
    }

    //
    override fun onLayoutReady() {
        unfreezeScreen()
    }

    //region observers
    private inner class RoomFixedParametersObserver : Observer<FixedParametersEntity?> {
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            Utilities.log(Enums.LogType.Debug, TAG, "FixedParametersObserver(): onChanged")
            isRoomFixedParametersLoaded = true

            if (fixedParameters == null) {
                return
            }

            // fixed parameters
            fixedParametersData = FixedParameters.init(fixedParameters)

            if (isRoomFixedParametersLoaded && isRoomMyCitiesLoaded) {
                setMyCities()
            }

            if (isRoomFixedParametersLoaded && isRoomBestYieldLoaded) {
                setBestYield()
            }

            if (isRoomFixedParametersLoaded && isRoomMyCitiesLoaded && isRoomBestYieldLoaded && !isServerHomeLoaded) {
                viewModel!!.getServerHome(applicationContext, userUUID)
            }
        }
    }

    private inner class ContentObserver : Observer<java.util.ArrayList<StringEntity>?> {
        override fun onChanged(content: java.util.ArrayList<StringEntity>?) {
            Utilities.log(Enums.LogType.Debug, TAG, "ContentObserver(): onChanged")

            if (content == null) {
                return
            }

        }
    }

    private inner class RoomMyCitiesObserver : Observer<List<PropertyEntity?>?> {
        override fun onChanged(cities: List<PropertyEntity?>?) {
            Utilities.log(Enums.LogType.Debug, TAG, "MyCitiesObserver(): onChanged")
            isRoomMyCitiesLoaded = true
            myCitiesData = cities

            if (isRoomFixedParametersLoaded && isRoomMyCitiesLoaded) {
                setMyCities()
            }

            if (isRoomFixedParametersLoaded && isRoomMyCitiesLoaded && isRoomBestYieldLoaded && !isServerHomeLoaded) {
                viewModel!!.getServerHome(applicationContext, userUUID)
            }
        }
    }

    private inner class RoomCityPropertiesObserver : Observer<ArrayList<PropertyEntity?>?> {
        override fun onChanged(cityProperties: ArrayList<PropertyEntity?>?) {
            if (cityProperties == null) {
                return
            }

            cityProperties.add(
                PropertyEntity(city = cityProperties[0]?.city, cityElse = "addProperty")
            )

            // city's properties
            cityPropertiesAdapter!!.setItems(cityProperties, bestYieldData)
        }
    }

    private inner class RoomBestYieldObserver : Observer<List<BestYieldEntity?>?> {
        override fun onChanged(bestYields: List<BestYieldEntity?>?) {
            Utilities.log(Enums.LogType.Debug, TAG, "BestYieldObserver(): onChanged")
            isRoomBestYieldLoaded = true
            bestYieldData = bestYields

            if (isRoomFixedParametersLoaded && isRoomBestYieldLoaded) {
                setBestYield()
            }

            if (isRoomFixedParametersLoaded && isRoomMyCitiesLoaded && isRoomBestYieldLoaded && !isServerHomeLoaded) {
                viewModel!!.getServerHome(applicationContext, userUUID)
            }
        }
    }

    private inner class ServerDeletePropertyObserver : Observer<Int> {
        override fun onChanged(deletedItemsCount: Int?) {
            Utilities.log(Enums.LogType.Debug, TAG, "ServerDeletePropertyObserver(): onChanged")

            isServerHomeLoaded = false
            viewModel!!.getServerHome(applicationContext, userUUID)
        }
    }

    private inner class ServerHomeObserver : Observer<HomeDataClass> {
        override fun onChanged(home: HomeDataClass?) {
            Utilities.log(Enums.LogType.Debug, TAG, "ServerHomeObserver(): onChanged")
            isServerHomeLoaded = true
            isPropertiesNeedToRefresh = home?.isPropertiesNeedToRefresh == true
            isBestYieldNeedToRefresh = home?.isBestYieldNeedToRefresh == true

            if (!isPropertiesNeedToRefresh && !isBestYieldNeedToRefresh) {
                unfreezeScreen()
            }

            await(Date(), MIN_HOME_AWAIT_SECONDS, ::responseAfterGetHomeAwait)
        }
    }

    private fun responseAfterGetHomeAwait() {

        viewModel!!.getRoomFixedParameters(applicationContext)
        viewModel!!.getRoomMyCities(applicationContext)
        viewModel!!.getRoomBestYield(applicationContext)

        if (layout.swipeToRefresh.isRefreshing) {
            layout.swipeToRefresh.isRefreshing = false
        }
    }

    //endregion observers

    //strings
    override fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        layout.firstLoginText.text = Utilities.getRoomString("home_lets_start")
        layout.myCitiesTitle.text = Utilities.getRoomString("home_cities_title")
        layout.bestYieldAverageReturn.text = Utilities.getRoomString("home_best_yield_average_return")
        layout.bestYieldAverageReturnOnEquity.text = Utilities.getRoomString("home_best_yield_average_return_on_equity")
        layout.bestYieldTotalProfit.text = Utilities.getRoomString("home_best_yield_total_profit")
        layout.bestYieldTotalProfitNPV.text = Utilities.getRoomString("home_best_yield_total_profit_npv")

        super.setRoomStrings()
    }
    //strings

    //region mask
    fun showMask() {
        maskHolder?.containerData?.alpha = 0.3f

        maskHolder?.containerMask?.visibility = VISIBLE
        maskHolder?.delete?.visibility = VISIBLE
        maskHolder?.animDelete?.visibility = GONE

        maskHolder?.containerAdd?.visibility = GONE
    }

    fun hideMask() {
        maskHolder?.containerData?.alpha = 1.0f

        maskHolder?.containerMask?.visibility = GONE
        maskHolder?.delete?.visibility = GONE
        maskHolder?.animDelete?.visibility = GONE
        maskHolder = null

        maskHolder?.containerAdd?.visibility = GONE
    }
    //endregion mask

    //region == freeze screen ============

    fun freezeScreen() {
        this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        layout.containerMask.visibility = VISIBLE
    }

    fun unfreezeScreen() {
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        //layout.containerMask.visibility = GONE

        layout.drawer.removeView(layout.containerMask)
    }

    //endregion == freeze screen ============

    override fun onBackPressed() {
        if (maskHolder != null) {
            hideMask()
        }
        else {
            super.onBackPressed()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (MotionEvent.ACTION_DOWN == event.action) {
            if (maskHolder != null) {
                if (!isViewInBounds(maskHolder!!.itemView, event.rawX.toInt(), event.rawY.toInt())) {
                    return false
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (MotionEvent.ACTION_UP == event?.action) {
            if (maskHolder != null) {
                hideMask()
            }
        }

        return super.onTouchEvent(event)
    }


    private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        val rect = Rect()
        view.getLocalVisibleRect(rect)
        view.getGlobalVisibleRect(rect)

        if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
            return true
        }

        return false
    }

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityHomeBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityHomeBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}