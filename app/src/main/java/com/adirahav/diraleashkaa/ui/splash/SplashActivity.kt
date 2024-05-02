package com.adirahav.diraleashkaa.ui.splash

import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration.dateFormatter
import com.adirahav.diraleashkaa.common.Configuration.timeFormatter
import com.adirahav.diraleashkaa.common.Utilities.await
import com.adirahav.diraleashkaa.common.Utilities.getNetworkStatus
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.dataClass.DeviceDataClass
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import com.adirahav.diraleashkaa.databinding.ActivitySplashBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.dialog.FancyDialog
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.registration.RegistrationActivity
import com.adirahav.diraleashkaa.ui.login.LoginActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import java.util.*

class SplashActivity : BaseActivity<SplashViewModel?, ActivitySplashBinding>() {

    companion object {
        private const val TAG = "SplashActivity"
        private const val MIN_SPLASH_AWAIT_SECONDS = 3
        private const val MIN_RESTORE_AWAIT_SECONDS = 3
        private const val MIN_ALLOW_TRACK_USER_AWAIT_SECONDS = 5

        fun start(context: Context) {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    // shared preferences
    var preferences: AppPreferences? = null

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // data loaded
    var isLocalUserLoaded: Boolean = false

    // fixed parameters
    var fixedParameters: FixedParameters? = null
    var fixedParametersData: FixedParametersEntity? = null

    // network connection
    var lastNetworkStatus: Enums.NetworkStatus? = null

    // layout
    internal var layout: ActivitySplashBinding? = null

    // start time
    var startTime = Date()

    // user data
    var userId: String? = null
    var localUser: UserEntity? = null

    // device data
    var deviceData: DeviceDataClass? = null

    // calculators data
    var calculatorsData: List<CalculatorEntity>? = null

    // announcements data
    var announcementsData: List<AnnouncementEntity>? = null
    var announcementIndex: Int = 0
    var announcementDialog: FancyDialog? = null

    // new version available
    var isNewVersionAvailable: Boolean? = null

    // server down
    var isServerDown: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(layout?.root)

        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)

        // remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // remove notification bar
        //this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // remove action bar
        //supportActionBar?.hide()

        initGlobal()

        if (savedInstanceState == null) {
            lifecycleOwner = this
            initObserver()
        }
    }

    public override fun onResume() {
        super.onResume()
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)

        setCustomActionBar()
        setDrawer()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkChangeReceiver)
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance
        preferences?.setString("homeSelectedCity", null, false)

        startLoadData()
    }

    override fun createViewModel(): SplashViewModel {
        val factory = SplashViewModelFactory(this@SplashActivity, DataManager.instance!!.userService, DataManager.instance!!.announcementService)
        return ViewModelProvider(this, factory)[SplashViewModel::class.java]
    }

    private fun initObserver() {
        if (getNetworkStatus() != Enums.NetworkStatus.NOT_CONNECTED) {
            if (!viewModel!!.splashCallback.hasObservers()) viewModel!!.splashCallback.observe(this, SplashObserver())
            if (!viewModel!!.localSplashCallback.hasObservers()) viewModel!!.localSplashCallback.observe(this, LocalSplashObserver())
            if (!viewModel!!.localUserCallback.hasObservers()) viewModel!!.localUserCallback.observe(this, LocalUserObserver())
            if (!viewModel!!.localRestoreData.hasObservers()) viewModel!!.localRestoreData.observe(this, LocalRestoreDataObserver())

            if (!isLocalUserLoaded) {
                viewModel!!.getLocalUser(applicationContext, lifecycleOwner!!)
            }
        }
        else {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.NO_INTERNET, ::responseAfterInternetConnectionPositivePress, null, emptyArray())
        }
    }

    //strings
    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        layout?.loaderText?.text =
            if (Utilities.localPhrase.isNullOrEmpty() == false)
                Utilities.getLocalPhrase("splash_loading")
            else
                resources.getString(R.string.splash_loading)

        super.setPhrases()
    }
    //strings

    private fun startLoadData() {
        layout?.loader?.visibility = View.VISIBLE
    }

    fun endLoadData() {
        Log.d(TAG, "endLoadData()")
        await(startTime, MIN_SPLASH_AWAIT_SECONDS, ::responseAfterAwait)
    }

    private fun responseAfterAwait() {
        Log.d(TAG, "responseAfterAwait()")

        fixedParameters = FixedParameters.init(fixedParametersData)

        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        val isBetaVersion = fixedParameters?.appVersionArray?.find { it.key == "isBeta" }?.value?.toBoolean() ?: true
        val isNewVersionRequired = fixedParameters?.appVersionArray?.find { it.key == "newVersionRequired" }?.value?.toBoolean() ?: true

        preferences?.setBoolean("isBetaVersion", isBetaVersion, isAsync = false)

        if (localUser != null && localUser?.subscriberType != null && localUser?.subscriberType!!.equals(Enums.SubscriberType.BLOCKED.toString(), true)) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.BLOCKED, ::responseAfterBlockedPositivePress, null, emptyArray())
            return
        }

        if (isServerDown == true) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.SERVER_DOWN, ::responseAfterDataErrorPositivePress, null, emptyArray())
            return
        }

        if (fixedParametersData == null) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.DATA_ERROR, ::responseAfterDataErrorPositivePress, null, emptyArray())
            return
        }

        if (isBetaVersion) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.BETA_VERSION, ::responseAfterBetaVersionPositivePress, ::responseAfterBetaVersionNegativePress, emptyArray())
            return
        }

        if (localUser != null && localUser?.registrationExpiredTime != null && localUser?.registrationExpiredTime!! > 0L && nowUTC.timeInMillis > localUser?.registrationExpiredTime!!) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.EXPIRED_REGISTRATION, ::responseAfterExpiredCodePositivePress, null, arrayOf(dateFormatter.format(localUser?.registrationExpiredTime), timeFormatter.format(localUser?.registrationExpiredTime)))
            return
        }

        if (isNewVersionAvailable == true) {
            if (isNewVersionRequired) {
                Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.NEW_VERSION_AVAILABLE_REQUIRED, ::responseAfterNewVersionAvailableRequiredPositivePress, null, emptyArray())
                return
            }
        }

        if (localUser == null && deviceData != null) {
            startTime = Date()
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.RESTORE, null, null, emptyArray())
            restoreData(deviceData)
            return
        }

        if (localUser == null && calculatorsData != null) {
            startTime = Date()
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.RESTORE, null, null, emptyArray())
            restoreData(deviceData)
            return
        }

        if (isNewVersionAvailable == true) {
            if (!isNewVersionRequired) {
                Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.NEW_VERSION_AVAILABLE_NOT_REQUIRED, ::responseAfterNewVersionAvailableNotRequiredPositivePress, ::responseAfterNewVersionAvailableNotRequiredNegativePress, emptyArray())
                return
            }
        }

        if (announcementsData != null && announcementsData!!.size > 0) {
            announcementIndex = 0
            showAnnouncements(announcementsData!![announcementIndex])
            return
        }

        continueToActivity(isBetaVersion, false)
    }

    private fun showAnnouncements(announcement: AnnouncementEntity) {
        Utilities.localPhrase!!.remove(PhraseEntity(key = "dialog_announcement_title", value = Utilities.getLocalPhrase("dialog_announcement_title")))
        Utilities.localPhrase!!.remove(PhraseEntity(key = "dialog_announcement_message", value = Utilities.getLocalPhrase("dialog_announcement_message")))
        Utilities.localPhrase!!.remove(PhraseEntity(key = "dialog_announcement_positive", value = Utilities.getLocalPhrase("dialog_announcement_positive")))
        Utilities.localPhrase!!.add(PhraseEntity(key = "dialog_announcement_title", value = announcement.title!!))
        Utilities.localPhrase!!.add(PhraseEntity(key = "dialog_announcement_message", value = announcement.message!!))
        Utilities.localPhrase!!.add(PhraseEntity(key = "dialog_announcement_positive", value = announcement.positiveButtonText!!))
        announcementDialog = Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.ANNOUNCEMENT, ::responseAfterAnnouncementPositivePress, null, emptyArray())
    }

    private fun continueToActivity(isBetaVersion: Boolean, isExpired: Boolean) {
        Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity()", showToast = false)

        if (localUser == null) {
            preferences?.deleteAll()
            preferences?.setBoolean("isBetaVersion", isBetaVersion, isAsync = false)

            Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): SignUpActivity.start", showToast = false)
            SignUpActivity.start(context)
        }
        else {
            val subscriberType = localUser?.subscriberType
            val expiredTime = localUser?.registrationExpiredTime
            val appURL = fixedParameters?.appVersionArray?.find { it.key == "url" }?.value ?: ""
            val appID = Utilities.getAppID(fixedParameters)
            val onErrorSendEmail = fixedParameters?.onErrorArray?.find { it.key == "send_email" }?.value?.toBoolean() ?: true
            val onErrorMailTo = fixedParameters?.onErrorArray?.find { it.key == "mail_to" }?.value ?: ""

            preferences?.setString("subscriberType", subscriberType, false)
            expiredTime?.let { preferences?.setLong("expiredTime", it, false) }
            preferences?.setBoolean("expiredDialogHasShown", b = false, isAsync = false)
            preferences?.setString("fullname", localUser?.fullname, false)
            preferences?.setString("email", localUser?.email, false)
            preferences?.setString("appVersion", BuildConfig.VERSION_NAME, false)
            preferences?.setString("appURL", appURL, false)
            preferences?.setString("appID", appID, false)
            preferences?.setBoolean("isNewVersionAvailable", isNewVersionAvailable == true, false)
            preferences?.setBoolean("onErrorSendEmail", onErrorSendEmail, false)
            preferences?.setString("onErrorMailTo", onErrorMailTo, false)


            if (preferences!!.getString("token", "").isNullOrEmpty()) {
                LoginActivity.start(context, localUser?.email)
            }
            else if (localUser?.fullname.isNullOrEmpty() || localUser?.email.isNullOrEmpty() || localUser?.yearOfBirth == null ||
                localUser?.equity == null || localUser?.incomes == null || localUser?.commitments == null ||
                localUser?.termsOfUseAccept == null ||
                localUser?.subscriberType == null) {

                Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): SignUpActivity.start (missing data)", showToast = false)
                SignUpActivity.start(context)
            }
            else {
                Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): HomeActivity.start", showToast = false)

                if (isExpired) {
                    RegistrationActivity.start(context, Enums.RegistrationPageType.COUPON_CODE, false)
                }
                else {
                    HomeActivity.start(context)
                }
            }
        }
    }

    private fun responseAfterInternetConnectionPositivePress() {
        initObserver()
    }

    private fun responseAfterAnnouncementPositivePress() {

        if (announcementsData!![announcementIndex].confirm!!) {
            viewModel!!.confirmAnnouncement(announcementsData!![announcementIndex]._id!!)
        }

        if (announcementIndex + 1 < announcementsData!!.size) {
            showAnnouncements(announcementsData!![++announcementIndex])
        }
        else {
            continueToActivity(false, false)
        }

    }

    private fun responseAfterBetaVersionPositivePress() {
        continueToActivity(true, false)
    }

    private fun responseAfterBetaVersionNegativePress() {
        this.finishAffinity()
    }

    private fun responseAfterNewVersionAvailableRequiredPositivePress() {
        responseAfterNewVersionAvailableNotRequiredPositivePress()
    }

    private fun responseAfterNewVersionAvailableNotRequiredPositivePress() {
        val appID = Utilities.getAppID(fixedParameters)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appID")))
        } catch (e: ActivityNotFoundException) {
            Utilities.log(Enums.LogType.Error, TAG, "responseAfterNewVersionAvailableNotRequiredPositivePress(): ActivityNotFoundException = ${e.message}", localUser)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appID")))
        }
    }

    private fun responseAfterNewVersionAvailableNotRequiredNegativePress() {
        continueToActivity(false, false)
    }

    private fun responseAfterExpiredCodePositivePress() {
        continueToActivity(false, true)
    }

    private fun responseAfterDataErrorPositivePress() {
        this.finishAffinity()
    }

    private fun responseAfterBlockedPositivePress() {
        this.finishAffinity()
    }

    //region observers
    private inner class SplashObserver : Observer<SplashModel?> {
        override fun onChanged(splashData: SplashModel?) {

            if (splashData == null) {
                Log.d(TAG, "SplashObserver(): splashData == null)")
                endLoadData()
                return
            }

            fixedParametersData = splashData.fixedParameters
            localUser = splashData.user
            deviceData = splashData.restore
            calculatorsData = splashData.calculators
            announcementsData = splashData.announcements
            isNewVersionAvailable = splashData.newVersionAvailable
            isServerDown = splashData.serverDown

            viewModel!!.saveLocalSplash(applicationContext, lifecycleOwner!!, splashData)
        }
    }

    private inner class LocalSplashObserver : Observer<MutableList<Any?>> {
        override fun onChanged(localData: MutableList<Any?>) {
            Log.d(TAG, "LocalSplashObserver()")
            endLoadData()
        }
    }

    private inner class LocalUserObserver() : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            isLocalUserLoaded = true

            viewModel!!.getSplash()
        }
    }

    //endregion observers

    //region restore
    fun restoreData(deviceData: DeviceDataClass?) {
        val restoreUser = deviceData?.user
        val restoreProperties = deviceData?.properties
        val restoreCalculators = deviceData?.calculators

        if (restoreUser != null || restoreProperties != null || restoreCalculators != null) {
            viewModel!!.restoreLocalData(applicationContext, restoreUser, restoreProperties, restoreCalculators)
        }
    }

    private inner class LocalRestoreDataObserver : Observer<UserEntity?> {
        override fun onChanged(localUserData: UserEntity?) {
            if (localUserData != null) {
                localUser = localUserData
            }

            endRestoreData()
        }
    }

    fun endRestoreData() {
        await(startTime, MIN_RESTORE_AWAIT_SECONDS, ::responseAfterAwait)
    }

    //endregion restore

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

    override fun attachBinding(list: MutableList<ActivitySplashBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivitySplashBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}