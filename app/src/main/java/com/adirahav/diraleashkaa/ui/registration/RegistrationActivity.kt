package com.adirahav.diraleashkaa.ui.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.ui.base.BaseActivity

import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration.dateFormatter
import com.adirahav.diraleashkaa.common.Configuration.timeFormatter
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.common.Utilities.setButtonDisable
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.UnsubscribeModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
import com.adirahav.diraleashkaa.databinding.ActivityRegistrationBinding
import com.adirahav.diraleashkaa.ui.goodbye.GoodbyeActivity
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.splash.SplashActivity
import com.airbnb.paris.extensions.style
import kotlinx.coroutines.*
import java.util.*

class RegistrationActivity : BaseActivity<RegistrationViewModel?, ActivityRegistrationBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "RegistrationActivity"
        const val EXTRA_PAGE_TYPE = "EXTRA_PAGE_TYPE"

        fun start(context: Context, pageType: Enums.RegistrationPageType, allowBack: Boolean = true) {
            val intent = Intent(context, RegistrationActivity::class.java)
            intent.flags =  if (allowBack)
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            else
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(EXTRA_PAGE_TYPE, pageType.toString())
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@RegistrationActivity

    // shared preferences
    var preferences: AppPreferences? = null

    // loggedin user
    var userToken: String? = null

    // user id
    var roomUID: Long? = 0

    // user data
    var userData: UserEntity? = null

    // page type
    var pageType : Enums.RegistrationPageType? = null

    // fragments
    val payProgramFragment = RegistrationPayProgramFragment()
    val couponCodeFragment = RegistrationCouponCodeFragment()

    // strings
    var _roomPhrases: ArrayList<PhraseEntity>? = null

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // room/server data loaded
    var isRoomFixedParametersLoaded: Boolean = false
    var isRoomUserLoaded: Boolean = false
    var isServerUserLoaded: Boolean = false
    var isDataInit: Boolean = false

    // expired
    private var isExpired: Boolean = true
    private var isUnlimited: Boolean = true

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // layout
    internal lateinit var layout: ActivityRegistrationBinding

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }

    public override fun onResume() {
        super.onResume()
        log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()
        initData()
        initEvents()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar(layout.drawer)
        setDrawer(layout.drawer, layout.menu)

        // title text
        titleText?.text = Utilities.getLocalPhrase("actionbar_title_registration_details")


        // track user
        //Log.d("ADITEST7", "GET ${preferences?.getBoolean("isTrackUser", false).toString()} [registration]")
        /*trackUser?.visibility =
            if (preferences?.getBoolean("isTrackUser", false) == true)
                VISIBLE
            else
                GONE*/
    }

    override fun createViewModel(): RegistrationViewModel {
        val factory = RegistrationViewModelFactory(this@RegistrationActivity, DataManager.instance!!.userService, DataManager.instance!!.registrationService)
        return ViewModelProvider(this, factory)[RegistrationViewModel::class.java]
    }

    override fun onStop() {
        super.onStop()
        log(Enums.LogType.Debug, TAG, "onStop()", showToast = false)
        isRoomFixedParametersLoaded = false
        isRoomUserLoaded = false
        isDataInit = false
    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.getLocalFixedParameters.hasObservers()) viewModel!!.getLocalFixedParameters.observe(this@RegistrationActivity, LocalFixedParametersObserver(Enums.ObserverAction.GET_LOCAL))
        if (!viewModel!!.couponRegistrationCallback.hasObservers()) viewModel!!.couponRegistrationCallback.observe(this@RegistrationActivity, CouponRegistrationObserver())
        if (!viewModel!!.payProgramRegistrationCallback.hasObservers()) viewModel!!.payProgramRegistrationCallback.observe(this@RegistrationActivity, PayRegistrationObserver())
        if (!viewModel!!.saveUserCallback.hasObservers()) viewModel!!.saveUserCallback.observe(this@RegistrationActivity, UserObserver(Enums.ObserverAction.SAVE_SERVER))
        if (!viewModel!!.getLocalUserCallback.hasObservers()) viewModel!!.getLocalUserCallback.observe(this@RegistrationActivity, LocalUserObserver(Enums.ObserverAction.GET_LOCAL))
        if (!viewModel!!.updateLocalUserCallback.hasObservers()) viewModel!!.updateLocalUserCallback.observe(this@RegistrationActivity, LocalUserObserver(Enums.ObserverAction.UPDATE_LOCAL))
        if (!viewModel!!.updateServerUserCallback.hasObservers()) viewModel!!.updateServerUserCallback.observe(this@RegistrationActivity, LocalUserObserver(Enums.ObserverAction.UPDATE))
        if (!viewModel!!.unsubscribeCallback.hasObservers()) viewModel!!.unsubscribeCallback.observe(this@RegistrationActivity, UnsubscribeObserver())

        if (!isRoomFixedParametersLoaded && !isRoomUserLoaded && !isDataInit) {
            viewModel!!.getLocalFixedParameters(applicationContext)
            viewModel!!.getRoomUser(applicationContext, roomUID)
        }
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0)

        // room/server data loaded
        isRoomUserLoaded = false
        isServerUserLoaded = false

        isDataInit = false

        // buttons
        layout.buttons.back.visibility = GONE
        layout.buttons.next.visibility = GONE
        layout.buttons.save.visibility = GONE
        layout.buttons.send.visibility = GONE
        layout.buttons.pay.visibility = GONE

        // page type
        pageType = enumValueOf<Enums.RegistrationPageType>(intent.getStringExtra(EXTRA_PAGE_TYPE)!!)
    }

    private fun initData() {
        // unsubscribe
        Utilities.setTextViewHtml(layout.unsubscribe, "signup_unsubscribe")

        // loggedin user
        userToken = preferences!!.getString("token", "")
    }

    private fun initEvents() {
        // unsubscribe
        /*layout.unsubscribe.setOnClickListener {
            Utilities.openFancyDialog(this@RegistrationActivity, Enums.DialogType.UNSUBSCRIBE, ::responseAfterUnsubscribePositivePress, ::responseAfterUnsubscribeNegativePress, emptyArray())
            return@setOnClickListener
        }*/
        layout.unsubscribe.visibility = GONE // TODO
    }

    //endregion == initialize =========

    //region == strings ============

    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        layout.buttons.back.text = Utilities.getLocalPhrase("button_back")
        layout.buttons.next.text = Utilities.getLocalPhrase("button_next")
        layout.buttons.save.text = Utilities.getLocalPhrase("button_save")
        layout.buttons.send.text = Utilities.getLocalPhrase("button_send")
        layout.buttons.pay.text = Utilities.getLocalPhrase("button_pay")

        layout.goHome.text = Utilities.getLocalPhrase("button_home")
        Utilities.setTextViewHtml(layout?.unsubscribe, "signup_unsubscribe")

        super.setPhrases()
    }

    //endregion == strings ============

    //region == unsubscribe ========

    private fun responseAfterUnsubscribePositivePress() {
        viewModel?.getUnsubscribe(context, userData)
    }

    private fun responseAfterUnsubscribeNegativePress() {

    }

    //endregion == unsubscribe ========

    private fun updatePreferences() {
        preferences?.setString("subscriberType", userData?.subscriberType, false)
        preferences?.setLong("expiredTime", userData?.registrationExpiredTime ?: 0, false)
    }

    private fun expiredRegistrationMessage() {
        log(Enums.LogType.Debug, TAG, "expiredRegistrationMessage()")
        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        isUnlimited = (userData?.registrationExpiredTime ?: 0L) == 0L
        isExpired = !isUnlimited && nowUTC.timeInMillis > (userData?.registrationExpiredTime ?: 0L)

        // FIX LOCK DRAWER SWIPE ERROR - START

        activity.runOnUiThread {
            if (isExpired) {
                layout.drawer.removeView(layout.container)
                layout.drawer.removeView(layout.navigation)
                layout.root.removeView(layout.drawer)

                layout.root.addView(layout.container, 0)
                setContentView(layout.root)
            }
        }
        // FIX LOCK DRAWER SWIPE ERROR - END

        activity.runOnUiThread {
            setExpiredRegistration(isExpired)
            //setCustomActionBar(layout.drawer)
            setDrawer(layout.drawer, layout.menu)

            layout.expiredRegistrationTitle.text =
                if (isExpired)
                    Utilities.getLocalPhrase("registration_expired_title")
                else
                    Utilities.getLocalPhrase("registration_not_expired_title")

            layout.expiredRegistrationTitle.style(
                if (isExpired)
                    R.style.messageTitleNegative
                else
                    R.style.messageTitlePositive
            )

            layout.expiredRegistrationMessage.visibility = VISIBLE

            layout.expiredRegistrationMessage.text =
                if (isUnlimited) {
                    Utilities.getLocalPhrase("registration_not_expired_unlimited_message")
                }
                else if (isExpired)
                    String.format(
                        Utilities.getLocalPhrase("registration_expired_message"),
                        dateFormatter.format(userData?.registrationExpiredTime),
                        timeFormatter.format(userData?.registrationExpiredTime)
                    )
                else
                    String.format(
                        Utilities.getLocalPhrase("registration_not_expired_message"),
                        dateFormatter.format(userData?.registrationExpiredTime),
                        timeFormatter.format(userData?.registrationExpiredTime)
                    )

            layout.goHome.visibility =
                if (isExpired)
                    GONE
                else
                    VISIBLE
        }
    }

    //region == fragments ==========

    internal fun forceLoadFragment(pageType: Enums.RegistrationPageType) {

        when (pageType) {
            Enums.RegistrationPageType.PAY_PROGRAM -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, payProgramFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.send.visibility = GONE
                        layout.buttons.pay.visibility = if (fixedParametersData?.payProgramsObject?.isAvailable == false) GONE else VISIBLE
                    }
            }

            Enums.RegistrationPageType.COUPON_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, couponCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.send.visibility = VISIBLE
                    layout.buttons.pay.visibility = GONE
                }
            }
        }

    }

    private fun loadFragment() {

        log(Enums.LogType.Debug, TAG, "loadFragment()")
        //when (pageType) {
       //     Enums.RegistrationPageType.COUPON_CODE ->
                if (isExpired) {
                    if (fixedParametersData?.payProgramsObject?.isAvailable == true) {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.formFragment, payProgramFragment)
                                .commitAllowingStateLoss()

                        activity.runOnUiThread {
                            layout.buttons.send.visibility = GONE
                            layout.buttons.pay.visibility = VISIBLE
                        }
                    }
                    else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.formFragment, couponCodeFragment)
                            .commitAllowingStateLoss()

                        activity.runOnUiThread {
                            setButtonDisable(layout.buttons.send)
                            layout.buttons.send.visibility = VISIBLE
                            layout.buttons.pay.visibility = GONE
                        }
                    }
                }
                else {
                    supportFragmentManager.beginTransaction()
                            .remove(payProgramFragment)
                            .commitAllowingStateLoss()

                    supportFragmentManager.beginTransaction()
                        .remove(couponCodeFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.send.visibility = GONE
                        layout.buttons.pay.visibility = GONE
                    }
                }
        //}

    }

    //endregion == fragments ==========

    //region == steps ==============

    fun submitSend(view: View?) {

        setButtonDisable(layout.buttons.send)

        val result = when (pageType) {
            Enums.RegistrationPageType.COUPON_CODE -> {
                couponCodeFragment.submitForm(false)
            }
            else -> null
        }

        log(Enums.LogType.Debug, TAG, "initData(): result = $result")
        /*val isValid = if (result?.containsKey("isValid") == true) { result["isValid"] as Boolean } else { false }

        if (isValid) {
            Utilities.hideKeyboard(this@RegistrationActivity)

            runBlocking {
                updateUser(result)
            }
        }*/
    }

    fun goHome(view: View) {
        if (isTaskRoot) {
            SplashActivity.start(context)
        }
        else {
            HomeActivity.start(context)
        }

    }

    //endregion == steps ==============

    //region == user data ==========

    fun updateLocalUser(userData: UserEntity) =
        GlobalScope.launch {
            viewModel!!.updateLocalUser(applicationContext, userData, Enums.DBCaller.LOCAL)
        }

    //endregion == user data ==========

    //region == observers ==========

    private inner class LocalFixedParametersObserver(action: Enums.ObserverAction) : Observer<FixedParametersEntity?> {
        val _action = action
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_LOCAL -> {
                    log(Enums.LogType.Debug, TAG, "RoomFixedParametersObserver(): GET_ROOM")
                    isRoomFixedParametersLoaded = true

                    if (fixedParameters == null) {
                        return
                    }

                    fixedParametersData = FixedParameters.init(fixedParameters)

                    if (isRoomFixedParametersLoaded && isRoomUserLoaded) {
                        updatePreferences()
                        expiredRegistrationMessage()
                        loadFragment()
                    }
                }

                else -> {}
            }
        }
    }

    private inner class CouponRegistrationObserver : Observer<UserEntity?> {
        override fun onChanged(userData: UserEntity?) {
            couponCodeFragment.couponCodeAfterResponse(userData)
        }
    }

    private inner class PayRegistrationObserver : Observer<UserEntity?> {
        override fun onChanged(userData: UserEntity?) {
            payProgramFragment.payProgramAfterResponse(userData)
        }
    }

    private inner class LocalUserObserver(action: Enums.ObserverAction) : Observer<UserEntity?> {
        val _action = action
        override fun onChanged(user: UserEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_LOCAL -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): GET_ROOM. user = $user")

                    isRoomUserLoaded = true

                    if (user == null) {
                        return
                    }

                    userData = UserEntity(
                        email = user.email,
                        fullname = user.fullname,
                        yearOfBirth = user.yearOfBirth,
                        equity = user.equity,
                        incomes = user.incomes,
                        commitments = user.commitments,
                        termsOfUseAccept = user.termsOfUseAccept,
                        registrationExpiredTime = user.registrationExpiredTime,
                        subscriberType = user.subscriberType,
                        calcCanTakeMortgage = user.calcCanTakeMortgage,
                        calcAge = user.calcAge
                    )

                    if (isRoomFixedParametersLoaded && isRoomUserLoaded) {
                        updatePreferences()
                        expiredRegistrationMessage()
                        loadFragment()
                    }
                }

                Enums.ObserverAction.UPDATE_LOCAL -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): UPDATE_ROOM. user = $user")

                    roomUID = user?.roomUID
                    preferences?.setLong("roomUID", roomUID!!, false)

                    if (isTaskRoot) {
                        SplashActivity.start(context)
                    }
                    else {
                        HomeActivity.start(context)
                    }

                }

                Enums.ObserverAction.SAVE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): INSERT_UPDATE_SERVER. user = $user")

                    if (user == null) {
                        return
                    }

                    userData = user
                }

                else -> {}
            }

        }
    }

    private inner class UserObserver(action: Enums.ObserverAction) : Observer<UserModel?> {
        val _action = action
        override fun onChanged(user: UserModel?) {
            when (_action) {
                Enums.ObserverAction.SAVE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "ServerUserObserver(): INSERT_UPDATE_SERVER. user.email = ${user?.user?.email}")

                    GlobalScope.launch {

                        val nowUTC = Calendar.getInstance()
                        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

                        viewModel!!.updateLocalUser(applicationContext, userData, Enums.DBCaller.SERVER)

                        //
                        updatePreferences()
                        expiredRegistrationMessage()

                        loadFragment()

                        //displayActionSnackbar(activity, resources.getString(R.string.registration_save_success))
                    }
                }

                else -> {}
            }
        }
    }

    private inner class UnsubscribeObserver : Observer<UnsubscribeModel?> {
        override fun onChanged(response: UnsubscribeModel?) {
            if (response == null || !response.success) {
                Utilities.openFancyDialog(context,
                        Enums.DialogType.DATA_ERROR, ::responseAfterDataErrorPositivePress, null, emptyArray())
            }
            else {
                GoodbyeActivity.start(context)
            }
        }
    }

    private fun responseAfterDataErrorPositivePress() {
        finishAffinity()
    }

    //endregion == observers ==========

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityRegistrationBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityRegistrationBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}