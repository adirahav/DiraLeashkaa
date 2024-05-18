package com.adirahav.diraleashkaa.ui.calculators

import android.content.*
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Utilities.getNetworkStatus
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.databinding.ActivityCalculatorsBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity


class CalculatorsActivity : BaseActivity<CalculatorsViewModel?, ActivityCalculatorsBinding>(),
        CalculatorsAdapter.OnCalculatorAdapter,
        CalculatorsAdapter.RecyclerViewReadyCallback {

    companion object {
        private const val TAG = "CalculatorsActivity"
        private const val ITEMS_COUNT_IN_ROW = 2

        fun start(context: Context) {
            val intent = Intent(context, CalculatorsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    // shared preferences
    var preferences: AppPreferences? = null

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // max property price
    var calculatorsData: List<CalculatorEntity>? = null

    // network connection
    var lastNetworkStatus: Enums.NetworkStatus? = null

    // layout
    private var layout: ActivityCalculatorsBinding? = null

    // data loaded
    var isDataInit: Boolean = false

    // user data
    var userId: String? = null

    // calculators
    private var calculatorsAdapter: CalculatorsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityCalculatorsBinding.inflate(layoutInflater)
        setContentView(layout?.root)

        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)

        initGlobal()

        initObserver()
    }

    public override fun onResume() {
        super.onResume()
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        setCustomActionBar()
        setDrawer(layout?.drawer, layout?.menu)

        // title text
        titleText?.text = Utilities.getLocalPhrase("actionbar_title_calculators")


    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        userId = preferences?.getString("userId", null)
        Utilities.log(Enums.LogType.Debug, TAG, "preferences userId = ${userId}")

        // data loaded
        isDataInit = false

        freezeScreen()
    }

    override fun createViewModel(): CalculatorsViewModel {
        val factory = CalculatorsViewModelFactory(this@CalculatorsActivity, DataManager.instance!!.calculatorsService)
        return ViewModelProvider(this, factory)[CalculatorsViewModel::class.java]
    }

    private fun initObserver() {
        if (getNetworkStatus() != Enums.NetworkStatus.NOT_CONNECTED) {
            if (!viewModel!!.localCalculatorsCallback.hasObservers()) viewModel!!.localCalculatorsCallback.observe(this, LocalCalculatorsObserver())

            if (!isDataInit) {
                Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): getServerCalculators")
                viewModel!!.getLocalCalculators(applicationContext, userId)
            }
        }
        else {
            Utilities.openFancyDialog(this@CalculatorsActivity, Enums.DialogType.NO_INTERNET, ::responseAfterInternetConnectionPositivePress, null, emptyArray())
        }
    }

    //phrases
    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")
        super.setPhrases()
    }
    //phrases

    private fun setCalculators(calculator: List<CalculatorEntity>) {

        // calculators
        calculatorsAdapter = CalculatorsAdapter(this@CalculatorsActivity, this, this)
        layout?.calculatorsList?.adapter = calculatorsAdapter
        layout?.calculatorsList?.setLayoutManager(GridLayoutManager(context, ITEMS_COUNT_IN_ROW))
        calculatorsAdapter!!.setItems(calculator)
    }

    private fun responseAfterInternetConnectionPositivePress() {
        initObserver()
    }

    //region observers
    private inner class LocalCalculatorsObserver : Observer<List<CalculatorEntity>?> {
        override fun onChanged(calculators: List<CalculatorEntity>?) {

            if (calculators != null) {
                calculatorsData = calculators
                isDataInit = true
                setCalculators(calculatorsData!!)
            }

            unfreezeScreen()
        }
    }

    //endregion observers

    //region == freeze screen ============

    fun freezeScreen() {
        this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        layout?.containerMask?.visibility = View.VISIBLE
    }

    fun unfreezeScreen() {
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        //layout.containerMask.visibility = GONE

        layout?.drawer?.removeView(layout?.containerMask)
    }

    //endregion == freeze screen ============

    /////
    private var networkChangeReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val status: Enums.NetworkStatus = getNetworkStatus()

            if (lastNetworkStatus == null) {
                lastNetworkStatus = status
            }

            if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action) {
                if (status != Enums.NetworkStatus.NOT_CONNECTED) {
                    if (lastNetworkStatus != status) {
                        initObserver()
                    }
                }
            }
        }
    }

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityCalculatorsBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityCalculatorsBinding.inflate(layoutInflater))
    }

    override fun onCalculatorClicked(calculator: CalculatorEntity) {
        if (calculator.isLock == false && calculator.isComingSoon == false) {
            CalculatorActivity.start(context, calculator.type)
        }
    }

    override fun onLayoutReady() {
        unfreezeScreen()
    }

    //endregion == base abstract ======
}