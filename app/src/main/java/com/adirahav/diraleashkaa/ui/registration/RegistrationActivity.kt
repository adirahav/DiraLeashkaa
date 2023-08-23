package com.adirahav.diraleashkaa.ui.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.ui.base.BaseActivity

import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration.dateFormatter
import com.adirahav.diraleashkaa.common.Configuration.timeFormatter
import com.adirahav.diraleashkaa.common.Utilities.getMapStringValue
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.common.Utilities.setButtonDisable
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.StringEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
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

    // user id
    var roomUID: Long? = 0

    // user data
    var userData: UserEntity? = null

    // page type
    var pageType : Enums.RegistrationPageType? = null

    // fragments
    val payProgramFragment = RegistrationPayProgramFragment()
    val googlePayFragment = RegistrationGooglePayFragment()
    val couponCodeFragment = RegistrationCouponCodeFragment()
    val betaCodeFragment = RegistrationBetaCodeFragment()

    // strings
    var _roomStrings: ArrayList<StringEntity>? = null

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // room/server data loaded
    var isRoomFixedParametersLoaded: Boolean = false
    var isRoomUserLoaded: Boolean = false
    var isServerUserLoaded: Boolean = false
    var isDataInit: Boolean = false

    // beta
    private var isBeta: Boolean = false

    // expired
    private var isExpired: Boolean = true
    private var isUnlimited: Boolean = true

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // pay
    internal val payViewModel: RegistrationPayProgramViewModel by viewModels()

    // google pay
    internal val googlePayViewModel: RegistrationGooglePayViewModel by viewModels()

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
        titleText?.text =   if (isBeta)
                                Utilities.getRoomString("actionbar_title_beta")
                            else
                                Utilities.getRoomString("actionbar_title_registration_details")

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
        if (!viewModel!!.roomFixedParameters_Get.hasObservers()) viewModel!!.roomFixedParameters_Get.observe(this@RegistrationActivity, RoomFixedParametersObserver(Enums.ObserverAction.GET_ROOM))
        if (!viewModel!!.couponRegistration.hasObservers()) viewModel!!.couponRegistration.observe(this@RegistrationActivity, CouponRegistrationObserver())
        if (!viewModel!!.betaRegistration.hasObservers()) viewModel!!.betaRegistration.observe(this@RegistrationActivity, BetaRegistrationObserver())
        if (!viewModel!!.payProgramRegistration.hasObservers()) viewModel!!.payProgramRegistration.observe(this@RegistrationActivity, PayRegistrationObserver())
        if (!viewModel!!.googlePayRegistration.hasObservers()) viewModel!!.googlePayRegistration.observe(this@RegistrationActivity, GooglePayRegistrationObserver())
        if (!viewModel!!.serverUser_InsertUpdateServer.hasObservers()) viewModel!!.serverUser_InsertUpdateServer.observe(this@RegistrationActivity, ServerUserObserver(Enums.ObserverAction.INSERT_UPDATE_SERVER))
        if (!viewModel!!.roomUser_Get.hasObservers()) viewModel!!.roomUser_Get.observe(this@RegistrationActivity, RoomUserObserver(Enums.ObserverAction.GET_ROOM))
        if (!viewModel!!.roomUser_UpdateRoom.hasObservers()) viewModel!!.roomUser_UpdateRoom.observe(this@RegistrationActivity, RoomUserObserver(Enums.ObserverAction.UPDATE_ROOM))
        if (!viewModel!!.roomUser_UpdateServer.hasObservers()) viewModel!!.roomUser_UpdateServer.observe(this@RegistrationActivity, RoomUserObserver(Enums.ObserverAction.UPDATE_SERVER))
        if (!viewModel!!.unsubscribe.hasObservers()) viewModel!!.unsubscribe.observe(this@RegistrationActivity, UnsubscribeObserver())

        if (!isRoomFixedParametersLoaded && !isRoomUserLoaded && !isDataInit) {
            viewModel!!.getRoomFixedParameters(applicationContext)
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
        layout.buttons.googlePayButton.root.visibility = GONE

        // page type
        pageType = enumValueOf<Enums.RegistrationPageType>(intent.getStringExtra(EXTRA_PAGE_TYPE)!!)
    }

    private fun initData() {
        // beta
        isBeta = pageType == Enums.RegistrationPageType.BETA_CODE

        // unsubscribe
        Utilities.setTextViewHtml(layout?.unsubscribe, "signup_unsubscribe")
    }

    private fun initEvents() {
        // unsubscribe
        layout?.unsubscribe?.setOnClickListener {
            Utilities.openFancyDialog(this@RegistrationActivity, Enums.DialogType.UNSUBSCRIBE, ::responseAfterUnsubscribePositivePress, ::responseAfterUnsubscribeNegativePress, emptyArray())
            return@setOnClickListener
        }
    }

    //endregion == initialize =========

    //region == strings ============

    override fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        layout.buttons.back.text = Utilities.getRoomString("button_back")
        layout.buttons.next.text = Utilities.getRoomString("button_next")
        layout.buttons.save.text = Utilities.getRoomString("button_save")
        layout.buttons.send.text = Utilities.getRoomString("button_send")
        layout.buttons.pay.text = Utilities.getRoomString("button_pay")
        layout.buttons.googlePayButton.container.contentDescription = Utilities.getRoomString("google_pay_button_subscribe_with")

        layout.goHome.text = Utilities.getRoomString("button_home")
        Utilities.setTextViewHtml(layout?.unsubscribe, "signup_unsubscribe")

        super.setRoomStrings()
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
            if (isExpired || isBeta) {
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
                if (isBeta)
                    Utilities.getRoomString("registration_beta_title")
                else if (isExpired)
                    Utilities.getRoomString("registration_expired_title")
                else
                    Utilities.getRoomString("registration_not_expired_title")

            layout.expiredRegistrationTitle.style(
                if (isExpired && !isBeta)
                    R.style.messageTitleNegative
                else
                    R.style.messageTitlePositive
            )

            if (isBeta) {
                layout.expiredRegistrationMessage.visibility = GONE
            }
            else {
                layout.expiredRegistrationMessage.visibility = VISIBLE

                layout.expiredRegistrationMessage.text =
                    if (isUnlimited) {
                        Utilities.getRoomString("registration_not_expired_unlimited_message")
                    }
                    else if (isExpired)
                        String.format(
                            Utilities.getRoomString("registration_expired_message"),
                            dateFormatter.format(userData?.registrationExpiredTime),
                            timeFormatter.format(userData?.registrationExpiredTime)
                        )
                    else
                        String.format(
                            Utilities.getRoomString("registration_not_expired_message"),
                            dateFormatter.format(userData?.registrationExpiredTime),
                            timeFormatter.format(userData?.registrationExpiredTime)
                        )
            }

            layout.goHome.visibility =
                if (isExpired || isBeta)
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
                        layout.buttons.googlePayButton.root.visibility = GONE
                    }
            }

            Enums.RegistrationPageType.GOOGLE_PAY -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, googlePayFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.send.visibility = GONE
                    layout.buttons.pay.visibility = GONE
                    layout.buttons.googlePayButton.root.visibility = if (fixedParametersData?.googlePayObject?.isAvailable == false) GONE else VISIBLE
                }
            }

            Enums.RegistrationPageType.COUPON_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, couponCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.send.visibility = VISIBLE
                    layout.buttons.pay.visibility = GONE
                    layout.buttons.googlePayButton.root.visibility = GONE
                }
            }

            Enums.RegistrationPageType.BETA_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, betaCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.send.visibility = VISIBLE
                    layout.buttons.pay.visibility = GONE
                    layout.buttons.googlePayButton.root.visibility = GONE
                }
            }

        }

    }

    private fun loadFragment() {

        log(Enums.LogType.Debug, TAG, "loadFragment()")
        //when (pageType) {
       //     Enums.RegistrationPageType.COUPON_CODE ->
                if (isBeta) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, betaCodeFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.send.visibility = VISIBLE
                        layout.buttons.pay.visibility = GONE
                        layout.buttons.googlePayButton.root.visibility = GONE
                    }
                }
                else if (isExpired) {
                    if (fixedParametersData?.payProgramsObject?.isAvailable == true) {
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.formFragment, payProgramFragment)
                                .commitAllowingStateLoss()

                        activity.runOnUiThread {
                            layout.buttons.send.visibility = GONE
                            layout.buttons.pay.visibility = VISIBLE
                            layout.buttons.googlePayButton.root.visibility = GONE
                        }
                    }
                    else if (fixedParametersData?.googlePayObject?.isAvailable == true) {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.formFragment, googlePayFragment)
                            .commitAllowingStateLoss()

                        activity.runOnUiThread {
                            layout.buttons.send.visibility = GONE
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
                            layout.buttons.googlePayButton.root.visibility = GONE
                        }
                    }
                }
                else {
                    supportFragmentManager.beginTransaction()
                            .remove(payProgramFragment)
                            .commitAllowingStateLoss()

                    supportFragmentManager.beginTransaction()
                        .remove(googlePayFragment)
                        .commitAllowingStateLoss()

                    supportFragmentManager.beginTransaction()
                        .remove(couponCodeFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.send.visibility = GONE
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
            Enums.RegistrationPageType.BETA_CODE -> {
                betaCodeFragment.submitForm()
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

    fun updateUser(result: Map<String, Any?>?) =
        GlobalScope.launch {

            val nowUTC = Calendar.getInstance()
            nowUTC.timeZone = TimeZone.getTimeZone("UTC")

            userData = UserEntity(
                roomUID = roomUID,
                uuid = userData?.uuid,
                userName = userData?.userName,
                email = userData?.email,
                age = userData?.age,
                phoneNumber = userData?.phoneNumber,
                phoneNumberSMSVerified = userData?.phoneNumberSMSVerified,
                deviceID = userData?.deviceID,
                deviceType = userData?.deviceType,
                equity = userData?.equity,
                incomes = userData?.incomes,
                commitments = userData?.commitments,
                termsOfUseAcceptTime = userData?.termsOfUseAcceptTime,

                subscriberType =
                    if (getMapStringValue(result, "subscriber_type") == null)
                        userData?.subscriberType.toString()
                    else
                        getMapStringValue(result, "subscriber_type"),
                registrationExpiredTime =
                    if (Utilities.getMapLongValue(result, "registration_expired_time") == null)
                        userData?.registrationExpiredTime
                    else
                        Utilities.getMapLongValue(result, "registration_expired_time"),
                appVersion = userData?.appVersion,
                isFirstLogin = userData?.isFirstLogin,
                canTakeMortgage = userData?.canTakeMortgage
            )

            viewModel!!.updateRoomUser(applicationContext, lifecycleOwner!!, userData, Enums.DBCaller.ROOM)
        }

    //endregion == user data ==========

    //region == observers ==========

    private inner class RoomFixedParametersObserver(action: Enums.ObserverAction) : Observer<FixedParametersEntity?> {
        val _action = action
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_ROOM -> {
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

    private inner class CouponRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            couponCodeFragment.couponCodeCallback(registrationModel)
        }
    }

    private inner class BetaRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            if (registrationModel != null) {
                betaCodeFragment.betaCodeCallback(registrationModel)
            }
            else {
                betaCodeFragment.betaCodeCallback(registrationModel)
            }
        }
    }


    private inner class PayRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            payProgramFragment.payProgramCallback(registrationModel)
        }
    }

    private inner class GooglePayRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            googlePayFragment.googlePayCallback(registrationModel)
        }
    }

    private inner class RoomUserObserver(action: Enums.ObserverAction) : Observer<UserEntity?> {
        val _action = action
        override fun onChanged(user: UserEntity?) {
            when (_action) {
                Enums.ObserverAction.GET_ROOM -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): GET_ROOM. user = $user")

                    isRoomUserLoaded = true

                    if (user == null) {
                        return
                    }

                    userData = user

                    if (isRoomFixedParametersLoaded && isRoomUserLoaded) {
                        updatePreferences()
                        expiredRegistrationMessage()
                        loadFragment()
                    }
                }

                Enums.ObserverAction.UPDATE_ROOM -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): UPDATE_ROOM. user = $user")

                    roomUID = user?.roomUID
                    preferences?.setLong("roomUID", roomUID!!, false)

                    activity.runOnUiThread {
                        if (user?.uuid == null) {
                            viewModel!!.insertServerUser(applicationContext, lifecycleOwner!!, userData)
                        }
                        else {
                            viewModel!!.updateServerUser(applicationContext, lifecycleOwner!!, userData)
                        }
                    }

                }

                Enums.ObserverAction.INSERT_UPDATE_SERVER -> {
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

    private inner class ServerUserObserver(action: Enums.ObserverAction) : Observer<UserModel?> {
        val _action = action
        override fun onChanged(user: UserModel?) {
            when (_action) {
                Enums.ObserverAction.INSERT_UPDATE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "ServerUserObserver(): INSERT_UPDATE_SERVER. user.uid = ${user?.data?.user?.uuid}")

                    GlobalScope.launch {

                        val nowUTC = Calendar.getInstance()
                        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

                        userData = UserEntity(
                            roomUID = roomUID,
                            uuid = user?.data?.user?.uuid,
                            userName = userData?.userName,
                            email = userData?.email,
                            age = userData?.age,
                            phoneNumber = userData?.phoneNumber,
                            phoneNumberSMSVerified = userData?.phoneNumberSMSVerified,
                            deviceID = userData?.deviceID,
                            deviceType = userData?.deviceType,
                            equity = userData?.equity,
                            incomes = userData?.incomes,
                            commitments = userData?.commitments,
                            termsOfUseAcceptTime = userData?.termsOfUseAcceptTime,
                            //insertTime = userData?.insertTime,
                            //updateTime = userData?.updateTime,
                            //serverUpdateTime = nowUTC.timeInMillis,
                            subscriberType = userData?.subscriberType,
                            //registrationStartTime = userData?.registrationStartTime,
                            registrationExpiredTime = userData?.registrationExpiredTime,
                            appVersion = userData?.appVersion,
                            isFirstLogin = userData?.isFirstLogin,
                            canTakeMortgage = userData?.canTakeMortgage
                        )

                        viewModel!!.updateRoomUser(applicationContext, lifecycleOwner!!, userData, Enums.DBCaller.SERVER)

                        //
                        updatePreferences()
                        expiredRegistrationMessage()

                        if (isBeta) {
                            HomeActivity.start(context)
                        }
                        else {
                            loadFragment()
                        }

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