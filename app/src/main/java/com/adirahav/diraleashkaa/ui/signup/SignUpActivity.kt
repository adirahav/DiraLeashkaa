package com.adirahav.diraleashkaa.ui.signup

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.ui.base.BaseActivity

import com.adirahav.diraleashkaa.common.Utilities.getMapBooleanValue
import com.adirahav.diraleashkaa.common.Utilities.getMapIntValue
import com.adirahav.diraleashkaa.common.Utilities.getMapLongValue
import com.adirahav.diraleashkaa.common.Utilities.getMapStringValue
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.databinding.ActivitySignupBinding
import com.adirahav.diraleashkaa.ui.registration.*
import com.kofigyan.stateprogressbar.StateProgressBar
import kotlinx.coroutines.*
import java.util.*

class SignUpActivity : BaseActivity<SignUpViewModel?, ActivitySignupBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "SignUpActivity"
        private const val PERMISSION_CODE = 101

        fun start(context: Context) {
            val intent = Intent(context, SignUpActivity::class.java)
            intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }

        fun submitNext() {

        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@SignUpActivity

    // shared preferences
    var preferences: AppPreferences? = null

    // user id
    var roomUID: Long? = null

    // user data
    var userData: UserEntity? = null

    // state progress bar
    var currentStepProgressBar: Int = 1
    var stepsCount: Int? = null

    // fragments
    val personalInfoFragment = SignUpPersonalInfoFragment()
    private val financialInfoFragment = SignUpFinancialInfoFragment()
    private val termsOfUseFragment = RegistrationTermsOfUseFragment()
    val payProgramFragment = RegistrationPayProgramFragment()
    val googlePayFragment = RegistrationGooglePayFragment()
    val couponCodeFragment = RegistrationCouponCodeFragment()
    val betaCodeFragment = RegistrationBetaCodeFragment()
    private val welcomeInfoFragment = SignUpWelcomeFragment()

    private var paymentPageType = Enums.RegistrationPageType.PAY_PROGRAM

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // room/server data loaded
    var isRoomFixedParametersLoaded: Boolean = false
    var isRoomUserLoaded: Boolean = false

    var isServerFixedParametersLoaded: Boolean = false
    var isServerUserLoaded: Boolean = false

    var isDataInit: Boolean = false

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // pay
    internal val payViewModel: RegistrationPayProgramViewModel by viewModels()

    // google pay
    internal val googlePayViewModel: RegistrationGooglePayViewModel by viewModels()

    // layout
    internal lateinit var layout: ActivitySignupBinding

    // beta version
    var isBetaVersion: Boolean = false

    var showSMSNotification = true

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }

    public override fun onResume() {
        super.onResume()
        log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()
        initStateProgressBar()
        initData()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar()
        setDrawer(drawer = null, layout.menu)
    }

    override fun createViewModel(): SignUpViewModel {
        val factory = SignUpViewModelFactory(
            this@SignUpActivity,
            DataManager.instance!!.userService,
            DataManager.instance!!.registrationService,
            DataManager.instance!!.stringsService
        )
        return ViewModelProvider(this, factory)[SignUpViewModel::class.java]


    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.roomFixedParametersGet.hasObservers()) viewModel!!.roomFixedParametersGet.observe(this@SignUpActivity, RoomFixedParametersObserver(Enums.ObserverAction.GET_ROOM))
        if (!viewModel!!.couponRegistration.hasObservers()) viewModel!!.couponRegistration.observe(this@SignUpActivity, CouponRegistrationObserver())
        if (!viewModel!!.betaRegistration.hasObservers()) viewModel!!.betaRegistration.observe(this@SignUpActivity, BetaRegistrationObserver())
        if (!viewModel!!.payProgramRegistration.hasObservers()) viewModel!!.payProgramRegistration.observe(this@SignUpActivity, PayRegistrationObserver())
        if (!viewModel!!.googlePayRegistration.hasObservers()) viewModel!!.googlePayRegistration.observe(this@SignUpActivity, GooglePayRegistrationObserver())
        if (!viewModel!!.skipRegistration.hasObservers()) viewModel!!.skipRegistration.observe(this@SignUpActivity, SkipRegistrationObserver())
        if (!viewModel!!.smsCodeValidation.hasObservers()) viewModel!!.smsCodeValidation.observe(this@SignUpActivity, SMSCodeValidationObserver())
        if (!viewModel!!.serverUserInsertUpdateServer.hasObservers()) viewModel!!.serverUserInsertUpdateServer.observe(this@SignUpActivity, ServerUserObserver(Enums.ObserverAction.INSERT_UPDATE_SERVER))
        if (!viewModel!!.roomUserGet.hasObservers()) viewModel!!.roomUserGet.observe(this@SignUpActivity, RoomUserObserver(Enums.ObserverAction.GET_ROOM))
        if (!viewModel!!.roomUserInsertUpdateRoom.hasObservers()) viewModel!!.roomUserInsertUpdateRoom.observe(this@SignUpActivity, RoomUserObserver(Enums.ObserverAction.INSERT_UPDATE_ROOM))
        if (!viewModel!!.roomUserInsertUpdateServer.hasObservers()) viewModel!!.roomUserInsertUpdateServer.observe(this@SignUpActivity, RoomUserObserver(Enums.ObserverAction.INSERT_UPDATE_SERVER))
        if (!viewModel!!.termsOfUse.hasObservers()) viewModel!!.termsOfUse.observe(this@SignUpActivity, TermsOfUseObserver())

        if (!isRoomFixedParametersLoaded && !isRoomUserLoaded && !isDataInit) {
            viewModel!!.getRoomFixedParameters(applicationContext)

            if (roomUID == 0L) {
                isRoomUserLoaded = true
            }
            else {
                viewModel!!.getRoomUser(applicationContext, roomUID)
            }

        }
    }

    private fun initGlobal() {

        /*// format date
        originalDateFormat = SimpleDateFormat(Configuration.DATETIME_ORIGINAL_PATTERN, Locale.ENGLISH)
*/
        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)

        // room/server data loaded
        isRoomFixedParametersLoaded = false
        isRoomUserLoaded = false

        isServerFixedParametersLoaded = false
        isServerUserLoaded = false

        isDataInit = false

        // buttons
        layout.buttons.back.visibility = View.VISIBLE
        layout.buttons.next.visibility = View.VISIBLE
        layout.buttons.save.visibility = View.GONE
        layout.buttons.send.visibility = View.GONE

        // beta version
        isBetaVersion = preferences?.getBoolean("isBetaVersion") ?: false

        // permissions
        //grandPermissionsIfNeeded()
    }

    private fun initStateProgressBar() {
        val descriptionData = resources.getStringArray(R.array.state_progress_bar_description)

        stepsCount = descriptionData.size

        when (stepsCount) {
            1 ->  layout.stepsProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.ONE)
            2 ->  layout.stepsProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.TWO)
            3 ->  layout.stepsProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.THREE)
            4 ->  layout.stepsProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.FOUR)
            5 ->  layout.stepsProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.FIVE)
        }

        layout.stepsProgressBar.setStateDescriptionData(descriptionData)
        layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)


        // circle text
        layout.stepsProgressBar.stateNumberTextSize = 20f

        layout.stepsProgressBar.descriptionLinesSpacing = 5f

    }

    private fun initData() {
        /*val steps = arrayOf("a", "b", "c")
        stepsView?.setLabels(steps)
            ?.setBarColorIndicator(ContextCompat.getColor(applicationContext, R.color.material_blue_grey_800))
            ?.setProgressColorIndicator(ContextCompat.getColor(applicationContext, R.color.orange))
            ?.setLabelColorIndicator(ContextCompat.getColor(applicationContext, R.color.orange))
            ?.setCompletedPosition(3)
            ?.drawView();*/
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

        super.setRoomStrings()
    }

    //endregion == strings ============

    //region == fragments ==========

    private fun loadFragment() {
        log(Enums.LogType.Debug, TAG, "loadFragment()")
        log(Enums.LogType.Debug, TAG, "updateStep3: currentStepProgressBar = $currentStepProgressBar")

        if (userData == null ||
            userData?.userName.isNullOrEmpty() ||
            userData?.phoneNumber.isNullOrEmpty() ||
            userData?.email.isNullOrEmpty() ||
            (showSMSNotification && userData?.phoneNumberSMSVerified ?: false == false) ||
            userData?.age == null) {

            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, personalInfoFragment)
                .commitAllowingStateLoss()

            currentStepProgressBar = 1

            layout.buttons.pay.visibility = View.GONE
            layout.buttons.googlePayButton.root.visibility = View.GONE
        }
        else if (userData?.equity == null || userData?.incomes == null || userData?.commitments == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, financialInfoFragment)
                .commitAllowingStateLoss()

            layout.buttons.pay.visibility = View.GONE
            layout.buttons.googlePayButton.root.visibility = View.GONE

            currentStepProgressBar = 2
        }
        else if (userData?.termsOfUseAcceptTime == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, termsOfUseFragment)
                .commitAllowingStateLoss()

            layout.buttons.pay.visibility = View.GONE
            layout.buttons.googlePayButton.root.visibility = View.GONE

            currentStepProgressBar = 3
        }
        else if (userData?.subscriberType == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.formFragment,
                    if (isBetaVersion) betaCodeFragment else payProgramFragment)
                .commitAllowingStateLoss()

            currentStepProgressBar = 4
        }

        updateStep()
    }

    internal fun forceLoadFragment(pageType: Enums.RegistrationPageType) {

        when (pageType) {
            Enums.RegistrationPageType.PAY_PROGRAM -> {
                if (fixedParametersData?.payProgramsObject?.isAvailable == true) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, payProgramFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.pay.visibility = View.VISIBLE
                    }
                }
                else {
                    activity.runOnUiThread {
                        layout.buttons.pay.visibility = View.GONE
                    }
                }

                activity.runOnUiThread {
                    layout.buttons.back.visibility = View.GONE
                    layout.buttons.next.visibility = View.GONE
                }

                paymentPageType = Enums.RegistrationPageType.PAY_PROGRAM
            }

            Enums.RegistrationPageType.GOOGLE_PAY -> {
                if (fixedParametersData?.googlePayObject?.isAvailable == true) {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.formFragment, googlePayFragment)
                            .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.googlePayButton.root.visibility = View.VISIBLE
                    }
                }
                else {
                    activity.runOnUiThread {
                        layout.buttons.googlePayButton.root.visibility = View.GONE
                    }
                }

                activity.runOnUiThread {
                    layout.buttons.back.visibility = View.GONE
                    layout.buttons.next.visibility = View.GONE
                }

                paymentPageType = Enums.RegistrationPageType.GOOGLE_PAY
            }

            Enums.RegistrationPageType.COUPON_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, couponCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.back.visibility = View.GONE
                    layout.buttons.next.visibility = View.VISIBLE
                    layout.buttons.pay.visibility = View.GONE
                    layout.buttons.googlePayButton.root.visibility = View.GONE
                }

                paymentPageType = Enums.RegistrationPageType.COUPON_CODE
            }

            Enums.RegistrationPageType.BETA_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, betaCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.send.visibility = View.GONE
                    layout.buttons.pay.visibility = View.GONE
                    layout.buttons.googlePayButton.root.visibility = View.GONE
                }

                paymentPageType = Enums.RegistrationPageType.BETA_CODE
            }
        }

    }

    //endregion == fragments ==========

    //region == steps ==============

    fun submitNext(view: View?) {

        if (currentStepProgressBar == 4) {
            when (paymentPageType) {
                Enums.RegistrationPageType.COUPON_CODE ->
                    couponCodeFragment.submitForm(skip = (view == null))
                Enums.RegistrationPageType.PAY_PROGRAM ->
                    payProgramFragment.submitForm(skip = (view == null))
                Enums.RegistrationPageType.GOOGLE_PAY ->
                    googlePayFragment.submitForm(skip = (view == null))
                Enums.RegistrationPageType.BETA_CODE ->
                    betaCodeFragment.submitForm()
            }

            return
        }

        val result = when (currentStepProgressBar) {
            1 -> personalInfoFragment.submitForm()
            2 -> financialInfoFragment.submitForm()
            3 -> termsOfUseFragment.submitForm()
            //4 -> codeFragment.submitForm(skip = if (view == null) true else false)
            //5 -> welcomeInfoFragment.submitForm()
            else -> null
        }

        log(Enums.LogType.Debug, TAG, "submitNext(): result = $result")
        val isValid = if (result?.containsKey("isValid") == true) { result["isValid"] as Boolean } else { false }

        if (isValid) {
            when (currentStepProgressBar) {
                1, 2, 3 -> runBlocking {
                    insertUpdateUser(result)
                }

                4 -> runBlocking {
                    updateCouponCode()
                }

                5 -> runBlocking {
                    updateWelcome()
                }
            }
        }
    }

    fun submitBack(view: View?) {

        if (currentStepProgressBar > 1) {
            log(Enums.LogType.Debug, TAG, "updateStep4: currentStepProgressBar = $currentStepProgressBar")
            currentStepProgressBar--

            updateStep()
        }
    }

    fun updateStep() {
        when (currentStepProgressBar) {
            1 -> {
                layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, personalInfoFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    Utilities.setButtonDisable(layout.buttons.back)
                    Utilities.setButtonEnable(layout.buttons.next)
                    layout.buttons.pay.visibility = View.GONE
                    layout.buttons.googlePayButton.root.visibility = View.GONE
                }
            }
            2 -> {
                layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, financialInfoFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    Utilities.setButtonEnable(layout.buttons.back)
                    Utilities.setButtonEnable(layout.buttons.next)
                }
            }
            3 ->  {
                layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, termsOfUseFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    Utilities.setButtonEnable(layout.buttons.back)
                    Utilities.setButtonEnable(layout.buttons.next)
                }
            }
            4 ->  {
                layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)

                if (isBetaVersion) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, betaCodeFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.back.visibility = View.VISIBLE
                        layout.buttons.next.visibility = View.VISIBLE
                        layout.buttons.pay.visibility = View.GONE
                        layout.buttons.googlePayButton.root.visibility = View.GONE
                    }

                    paymentPageType = Enums.RegistrationPageType.BETA_CODE
                }
                else {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, payProgramFragment)
                        .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.back.visibility = View.GONE
                        layout.buttons.next.visibility = View.GONE
                        layout.buttons.pay.visibility = View.VISIBLE
                    }

                    /*supportFragmentManager.beginTransaction()
                            .replace(R.id.formFragment, googlePayFragment)
                            .commitAllowingStateLoss()

                    activity.runOnUiThread {
                        layout.buttons.back.visibility = View.GONE
                        layout.buttons.next.visibility = View.GONE
                        layout.buttons.googlePayButton.root.visibility =
                                if (fixedParametersData?.googlePayObject?.isAvailable == true)
                                    View.VISIBLE
                                else
                                    View.GONE
                    }*/
                }

            }
            5 ->  {
                layout.stepsProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FIVE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, welcomeInfoFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    Utilities.setButtonDisable(layout.buttons.back)
                    Utilities.setButtonDisable(layout.buttons.next)
                    layout.buttons.pay.visibility = View.GONE
                    layout.buttons.googlePayButton.root.visibility = View.GONE
                }
            }
        }

        updatePreferences()
    }

    private fun updatePreferences() {
        preferences?.setString("userUUID", userData?.uuid, false)
        preferences?.setString("subscriberType", userData?.subscriberType, false)
        preferences?.setLong("expiredTime", userData?.registrationExpiredTime ?: 0, false)
    }

    //endregion == steps ==============

    //region == user data ==========

    fun insertUpdateUser(result: Map<String, Any?>?) =
        GlobalScope.launch {

            val nowUTC = Calendar.getInstance()
            nowUTC.timeZone = TimeZone.getTimeZone("UTC")

            userData = UserEntity(
                roomUID = roomUID,
                uuid = userData?.uuid,
                userName = if (getMapStringValue(result, "name").isEmpty()) userData?.userName else getMapStringValue(result, "name"),
                email = if (getMapStringValue(result, "email").isEmpty()) userData?.email else getMapStringValue(result, "email"),
                age = if (getMapIntValue(result, "age") == null) userData?.age else getMapIntValue(result, "age"),
                phoneNumber = if (getMapStringValue(result, "phone_number").isEmpty()) userData?.phoneNumber else getMapStringValue(result, "phone_number"),
                phoneNumberSMSVerified =
                    if (getMapBooleanValue(result, "phone_number_sms_verified") == null)
                        userData?.phoneNumberSMSVerified
                    else
                        getMapBooleanValue(result, "phone_number_sms_verified"),
                deviceID = if (userData?.deviceID == null) Settings.Secure.getString(AppApplication.context.contentResolver, Utilities.getDeviceID(context)) else userData?.deviceID,
                deviceType = if (userData?.deviceType == null) Utilities.getDeviceType() else userData?.deviceType,
                equity = if (getMapIntValue(result, Const.EQUITY) == null) userData?.equity else getMapIntValue(result, Const.EQUITY),
                incomes = if (getMapIntValue(result, Const.INCOMES) == null) userData?.incomes else getMapIntValue(result, Const.INCOMES),
                commitments = if (getMapIntValue(result, Const.COMMITMENTS) == null) userData?.commitments else getMapIntValue(result, Const.COMMITMENTS),
               // termsOfUseAcceptTime = if (termsOfUseAcceptTime == 0L) userData?.termsOfUseAcceptTime else getMapLongValue(result, "termsOfUseAcceptTime"),
                termsOfUseAcceptTime = if (getMapLongValue(result, "terms_of_use_accept_time") == 0L) userData?.termsOfUseAcceptTime else getMapLongValue(result, "terms_of_use_accept_time"),
                subscriberType = if (getMapStringValue(result, "subscriber_type").isEmpty()) userData?.subscriberType else getMapStringValue(result, "subscriber_type"),
                registrationExpiredTime = if (getMapLongValue(result, "registration_expired_time") == 0L) userData?.registrationExpiredTime else getMapLongValue(result, "registration_expired_time"),
                appVersion = BuildConfig.VERSION_NAME,
                isFirstLogin = true,
                canTakeMortgage = userData?.canTakeMortgage
            )

            log(Enums.LogType.Debug, TAG, "insertUpdateUser(): userData = {${userData}}")

            activity.runOnUiThread {
                if (userData?.uuid == null) {
                    viewModel!!.insertServerUser(userData)
                }
                else {
                    //if (getMapBooleanValue(result, "phone_number_sms_verified") != null) {
                        viewModel!!.updateServerUser(userData)
                    //}

                }
            }
        }

    //endregion == user data ==========

    //region == coupon code ========

    fun updateCouponCode() =
        GlobalScope.launch {
            if (currentStepProgressBar <= stepsCount!!) {
                log(Enums.LogType.Debug, TAG, "updateStep6: currentStepProgressBar = $currentStepProgressBar")
                currentStepProgressBar++
                activity.runOnUiThread {
                    updateStep()
                }

            }
        }

    //endregion == coupon code ========

    //region == skip registration ==

    fun skipRegistrationCallback(trialRegistration: RegistrationModel?) {

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        entities["registration_expired_time"] = trialRegistration?.data?.registration?.registrationExpireDate
        entities["subscriber_type"] = trialRegistration?.data?.registration?.subscriberType

        map["isValid"] = true
        map["entities"] = entities

        Utilities.hideKeyboard(this@SignUpActivity)

        runBlocking {
            insertUpdateUser(map)
        }
    }

    //endregion == skip registration ==


    //region == welcome ============

    private fun updateWelcome() =
        GlobalScope.launch {
            if (currentStepProgressBar <= stepsCount!!) {
                log(Enums.LogType.Debug, TAG, "updateStep7: currentStepProgressBar = $currentStepProgressBar")
                currentStepProgressBar++
                log(Enums.LogType.Debug, TAG, "updateWelcome()")
                activity.runOnUiThread {
                    updateStep()
                }

            }
        }

    //endregion == welcome ============

    //region == observers ==========

    private inner class CouponRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            if (registrationModel != null) {
                couponCodeFragment.couponCodeCallback(registrationModel)
            }
            else {
                couponCodeFragment.couponCodeCallback(registrationModel)
            }
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
            if (registrationModel != null) {
                payProgramFragment.payProgramCallback(registrationModel)
            }
            else {
                payProgramFragment.payProgramCallback(registrationModel)
            }
        }
    }

    private inner class GooglePayRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            if (registrationModel != null) {
                googlePayFragment.googlePayCallback(registrationModel)
            }
            else {
                googlePayFragment.googlePayCallback(registrationModel)
            }
        }
    }

    private inner class SMSCodeValidationObserver : Observer<SMSCodeValidationModel?> {
        override fun onChanged(smsCodeValidation: SMSCodeValidationModel?) {
            if (smsCodeValidation != null) {
                personalInfoFragment.afterCheckSMSCode(smsCodeValidation)
            }
            else {
                personalInfoFragment.afterCheckSMSCode(null)
            }
        }
    }

    private inner class SkipRegistrationObserver : Observer<RegistrationModel?> {
        override fun onChanged(registrationModel: RegistrationModel?) {
            skipRegistrationCallback(registrationModel)
        }
    }

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
                    showSMSNotification = fixedParametersData?.smsArray?.find { it.key == "show_verification" }?.value?.toBoolean() ?: true


                    if (isRoomFixedParametersLoaded && isRoomUserLoaded) {
                        loadFragment()
                    }
                }

                else -> {}
            }
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
                        loadFragment()
                    }

                    loadFragment()
                }

                Enums.ObserverAction.INSERT_UPDATE_ROOM -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): INSERT_UPDATE_ROOM. user = $user")

                    roomUID = user?.roomUID
                    preferences?.setLong("roomUID", roomUID!!, false)

                    if (currentStepProgressBar <= stepsCount!!) {
                        activity.runOnUiThread {
                            if (showSMSNotification && !(userData?.phoneNumberSMSVerified == true) && currentStepProgressBar == 1) {
                                personalInfoFragment.openSMSDialog()
                            }
                        }
                    }
                }

                Enums.ObserverAction.INSERT_UPDATE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): INSERT_UPDATE_SERVER. user = $user")

                    if (user == null) {
                        return
                    }

                    userData = user

                    if (currentStepProgressBar <= stepsCount!!) {
                        activity.runOnUiThread {
                            log(Enums.LogType.Debug, TAG, "RoomUserObserver(): INSERT_UPDATE_SERVER. showSMSNotification = $showSMSNotification, currentStepProgressBar = $currentStepProgressBar, phoneNumberSMSVerified = ${userData?.phoneNumberSMSVerified}")

                            log(Enums.LogType.Debug, TAG, "RoomUserObserver(): INSERT_UPDATE_SERVER. " +
                                    "${currentStepProgressBar != 1}, ${showSMSNotification && userData?.phoneNumberSMSVerified == true && currentStepProgressBar == 1}, ${!showSMSNotification && (userData?.phoneNumberSMSVerified ?: false) && currentStepProgressBar == 1}")

                            if (currentStepProgressBar == 1 && !showSMSNotification) {
                                currentStepProgressBar++

                                updateStep()
                            }
                            else if ( (currentStepProgressBar > 1) ||
                                 (showSMSNotification && userData?.phoneNumberSMSVerified == true && currentStepProgressBar == 1)
                               ) {
                                log(Enums.LogType.Debug, TAG, "updateStep2: currentStepProgressBar = $currentStepProgressBar , userData?.phoneNumberSMSVerified = ${userData?.phoneNumberSMSVerified}")
                                currentStepProgressBar++

                                updateStep()
                            }
                        }
                    }

                }

                else -> {}
            }

        }
    }

    private inner class ServerUserObserver(action: Enums.ObserverAction) : Observer<UserEntity?> {
        val _action = action

        override fun onChanged(user: UserEntity?) {
            when (_action) {
                Enums.ObserverAction.INSERT_UPDATE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "ServerUserObserver(): INSERT_UPDATE_SERVER. user = {${user}}")

                    GlobalScope.launch {

                        val nowUTC = Calendar.getInstance()
                        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

                        user?.roomUID = roomUID
                        userData = user

                        viewModel!!.updateRoomUser(
                            applicationContext,
                            userData,
                            Enums.DBCaller.SERVER
                        )
                    }
                }

                else -> {}
            }
        }
    }

    private inner class TermsOfUseObserver : Observer<StringModel?> {
        override fun onChanged(termsOfUse: StringModel?) {
            termsOfUseFragment.termsOfUseCallback(termsOfUse)
        }
    }

    //endregion == observers ==========

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivitySignupBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivitySignupBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======

    //region == permissions ========

    /*fun grandPermissionsIfNeeded() {
        // phone number
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE), PERMISSION_CODE)
        }

        //Utilities.log(Enums.LogType.Debug, TAG, "telephonyManager.line1Number = ${telephonyManager.line1Number}")
        //Utilities.log(Enums.LogType.Debug, TAG, "telephonyManager.allCellInfo = ${telephonyManager.allCellInfo}")
        //Utilities.log(Enums.LogType.Debug, TAG, "telephonyManager.phoneType = ${telephonyManager.phoneType}")
        //Utilities.log(Enums.LogType.Debug, TAG, "telephonyManager.simSerialNumber = ${telephonyManager.simSerialNumber}")
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //endregion == permissions ========
}