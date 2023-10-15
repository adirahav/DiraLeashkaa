package com.adirahav.diraleashkaa.ui.calculators

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.NOT_FOCUSABLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.floatFormat
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.databinding.ActivityCalculatorBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.property.CalculatorMaxPriceFragment
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import com.adirahav.diraleashkaa.ui.property.PropertyAmortizationScheduleFragment
import com.adirahav.diraleashkaa.ui.property.PropertyChartFragment
import com.adirahav.diraleashkaa.ui.property.PropertyInfoFragment
import com.adirahav.diraleashkaa.views.PropertyInput
import com.adirahav.diraleashkaa.views.PropertyPercent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class CalculatorActivity : BaseActivity<CalculatorsViewModel?, ActivityCalculatorBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "CalculatorActivity"
        private const val EXTRA_CALCULATOR_TYPE = "EXTRA_CALCULATOR_TYPE"

        fun start(context: Context, calculatorType: String?) {
            Utilities.log(Enums.LogType.Debug, TAG, "start(): type = ${calculatorType}", showToast = false)
            val intent = Intent(context, CalculatorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_CALCULATOR_TYPE, calculatorType)
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // shared preferences
    var preferences: AppPreferences? = null

    // room/server data loaded
    var isRoomFixedParametersLoaded: Boolean = false
    var isRoomUserLoaded: Boolean = false

    var isServerFixedParametersLoaded: Boolean = false
    var isServerUserLoaded: Boolean = false

    var isDataInit: Boolean = false

    // user id
    var roomUID: Long? = 0L
    var userUUID: String? = null

    // calculator type
    var argCalculatorType: String? = null

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // user data
    var userData: UserEntity? = null

    // layout
    internal var layout: ActivityCalculatorBinding? = null

    // fragments
    private var calculatorMaxPriceFragment: CalculatorMaxPriceFragment? = null

    //endregion == variables ==========

    //region == lifecycle methods ========

    override fun onCreate(savedInstanceState: Bundle?) {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)

        super.onCreate(savedInstanceState)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        layout = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(layout?.root)
    }

    override fun onResume() {
        super.onResume()
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar(layout?.drawer)
        setDrawer(layout?.drawer, layout?.menu)

        // title text
        titleText?.text = Utilities.getRoomString("actionbar_title_calculator_${argCalculatorType}")
    }
    override fun createViewModel(): CalculatorsViewModel {
        Utilities.log(Enums.LogType.Debug, TAG, "createViewModel()", showToast = false)
        val factory = CalculatorsViewModelFactory(DataManager.instance!!.calculatorsService)
        return ViewModelProvider(this, factory)[CalculatorsViewModel::class.java]
    }

    override fun onPause() {
        super.onPause()

        Utilities.log(Enums.LogType.Debug, TAG, "onPause()", showToast = false)
    }

    override fun onStop() {
        super.onStop()
        Utilities.log(Enums.LogType.Debug, TAG, "onStop()", showToast = false)

        when (argCalculatorType) {
            "max_price" -> {
                calculatorMaxPriceFragment?.removeObservers()
            }
        }
    }

    //endregion == lifecycle methods ========

    //region == instance state ===========
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        Utilities.log(Enums.LogType.Debug, TAG, "onSaveInstanceState()", showToast = false)
        savedInstanceState.putBoolean("isRoomFixedParametersLoaded", isRoomFixedParametersLoaded)
        savedInstanceState.putBoolean("isRoomUserLoaded", isRoomUserLoaded)
        savedInstanceState.putBoolean("isServerFixedParametersLoaded", isServerFixedParametersLoaded)
        savedInstanceState.putBoolean("isDataInit", isDataInit)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Utilities.log(Enums.LogType.Debug, TAG, "onRestoreInstanceState()", showToast = false)
        isRoomFixedParametersLoaded = savedInstanceState.getBoolean("isRoomFixedParametersLoaded")
        isRoomUserLoaded = savedInstanceState.getBoolean("isRoomUserLoaded")
        isServerFixedParametersLoaded = savedInstanceState.getBoolean("isServerFixedParametersLoaded")
        isDataInit = savedInstanceState.getBoolean("isDataInit")

        when (argCalculatorType) {
            "max_price" -> {
                calculatorMaxPriceFragment = CalculatorMaxPriceFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, calculatorMaxPriceFragment!!)
                        .commitAllowingStateLoss()
            }
        }
    }
    //endregion == instance state ===========

    //region == initialize ===============
    fun initGlobal() {
        Utilities.log(Enums.LogType.Debug, TAG, "initGlobal()", showToast = false)

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)
        userUUID = preferences?.getString("userUUID", null)

        // calculator type
        argCalculatorType = intent.getStringExtra(EXTRA_CALCULATOR_TYPE)

        // room/server data loaded
        isRoomFixedParametersLoaded = false
        isRoomUserLoaded = false

        isServerFixedParametersLoaded = false
        isServerUserLoaded = false

        isDataInit = false
    }

    fun initViews() {
        Utilities.log(Enums.LogType.Debug, TAG, "initViews()", showToast = false)
    }

    fun initData() {
        Utilities.log(Enums.LogType.Debug, TAG, "initData()", showToast = false)

        when (argCalculatorType) {
            "max_price" -> {
                calculatorMaxPriceFragment = CalculatorMaxPriceFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment, calculatorMaxPriceFragment!!)
                        .commitAllowingStateLoss()
            }
        }
    }

    fun initObserver() {
        if (Utilities.getNetworkStatus() != Enums.NetworkStatus.NOT_CONNECTED) {
            Utilities.log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
            if (!viewModel!!.roomFixedParameters.hasObservers()) viewModel!!.roomFixedParameters.observe(this@CalculatorActivity, RoomFixedParametersObserver())
            if (!viewModel!!.roomUser.hasObservers()) viewModel!!.roomUser.observe(this@CalculatorActivity, RoomUserObserver())

            if (!isRoomFixedParametersLoaded && !isRoomUserLoaded && !isServerFixedParametersLoaded && !isServerUserLoaded && !isDataInit) {
                viewModel!!.getRoomFixedParameters(applicationContext)
                viewModel!!.getRoomUser(applicationContext, roomUID)
            }
        }
        else {
            Utilities.openFancyDialog(this@CalculatorActivity, Enums.DialogType.NO_INTERNET, ::responseAfterInternetConnectionPositivePress, null, emptyArray())
        }
    }

    //endregion == initialize ===============

    private fun responseAfterInternetConnectionPositivePress() {
        initObserver()
    }

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

    //endregion == input number picker ======

    //region == observers ================
    private inner class RoomFixedParametersObserver() : Observer<FixedParametersEntity?> {
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            Utilities.log(Enums.LogType.Debug, TAG, "RoomFixedParametersObserver()")

            isRoomFixedParametersLoaded = true

            if (fixedParameters == null) {
                return
            }

            // fixed parameters
            fixedParametersData = FixedParameters.init(fixedParameters)

            if (isRoomFixedParametersLoaded && isRoomUserLoaded && !isDataInit) {
                initData()
            }
        }
    }

    private inner class RoomUserObserver() : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            Utilities.log(Enums.LogType.Debug, TAG, "RoomUserObserver()")

            isRoomUserLoaded = true

            if (user != null) {
                userData = user
            }

            if (isRoomFixedParametersLoaded && isRoomUserLoaded && !isDataInit) {
                initData()
            }
        }
    }

    //endregion == observers ================

    //region == base abstract ============

    override fun attachBinding(list: MutableList<ActivityCalculatorBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityCalculatorBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ============

    //region == freeze screen ============

    fun freezeScreen() {
        runOnUiThread {
            this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            //layout?.containerMask?.visibility = VISIBLE
        }
    }

    fun unfreezeScreen() {
        runOnUiThread {
            //layout?.containerMask?.visibility = GONE  // TODO this line destroy the NavigationView
            this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    //endregion == freeze screen ============
}