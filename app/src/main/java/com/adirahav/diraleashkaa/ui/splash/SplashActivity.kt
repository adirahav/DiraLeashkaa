package com.adirahav.diraleashkaa.ui.splash

import android.content.*
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration.dateFormatter
import com.adirahav.diraleashkaa.common.Configuration.timeFormatter
import com.adirahav.diraleashkaa.common.Utilities.await
import com.adirahav.diraleashkaa.common.Utilities.getDeviceID
import com.adirahav.diraleashkaa.common.Utilities.getNetworkStatus
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.dataClass.DeviceDataClass
import com.adirahav.diraleashkaa.data.network.dataClass.SplashDataClass
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.databinding.ActivitySplashBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.dialog.FancyDialog
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.registration.RegistrationActivity
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

    // room/server data loaded
    var isRoomUserLoaded: Boolean = false

    // fixed parameters
    var fixedParameters: FixedParameters? = null
    var fixedParametersData: FixedParametersEntity? = null

    // network connection
    var lastNetworkStatus: Enums.NetworkStatus? = null

    // layout
    private var layout: ActivitySplashBinding? = null

    // start time
    var startTime = Date()

    // user data
    var userUUID: String? = null
    var userData: UserEntity? = null

    // device data
    var deviceData: DeviceDataClass? = null

    // announcements data
    var announcementsData: List<AnnouncementEntity>? = null
    var announcementIndex: Int = 0
    var announcementDialog: FancyDialog? = null

    // new version available
    var isNewVersionAvailable: Boolean? = null

    // server down
    var isServerDown: Boolean? = null

    // track user
    var openTrackUserDialog: Boolean? = null
    var trackUserData: TrackUserEntity? = null

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

        // track user
        openTrackUserDialog = false

        startLoadData()
    }

    override fun createViewModel(): SplashViewModel {
        val factory = SplashViewModelFactory(this@SplashActivity, DataManager.instance!!.splashService, DataManager.instance!!.announcementService)
        return ViewModelProvider(this, factory)[SplashViewModel::class.java]
    }

    private fun initObserver() {
        if (getNetworkStatus() != Enums.NetworkStatus.NOT_CONNECTED) {
            if (!viewModel!!.serverSplash.hasObservers()) viewModel!!.serverSplash.observe(this, ServerSplashObserver())
            if (!viewModel!!.roomSplash.hasObservers()) viewModel!!.roomSplash.observe(this, RoomSplashObserver())
            if (!viewModel!!.roomUser.hasObservers()) viewModel!!.roomUser.observe(this, RoomUserObserver())
            if (!viewModel!!.roomRestoreData.hasObservers()) viewModel!!.roomRestoreData.observe(this, RoomRestoreDataObserver())

            if (!isRoomUserLoaded) {
                viewModel!!.getRoomUser(applicationContext, lifecycleOwner!!)
            }
        }
        else {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.NO_INTERNET, ::responseAfterInternetConnectionPositivePress, null, emptyArray())
        }
    }

    //strings
    override fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        layout?.loaderText?.text =
            if (Utilities.roomStrings.isNullOrEmpty())
                Utilities.getRoomString("splash_loading")
            else
                resources.getString(R.string.splash_loading)

        super.setRoomStrings()
    }
    //strings

    private fun startLoadData() {
        layout?.loader?.visibility = View.VISIBLE
    }

    fun endLoadData() {
        Log.d(TAG, "endLoadData()")
        //await(startTime, MIN_SPLASH_AWAIT_SECONDS, ::checkIfCanTrack)
        await(startTime, MIN_SPLASH_AWAIT_SECONDS, ::responseAfterAwait)
    }

    private fun checkIfCanTrack() {

        Log.d(TAG, "checkIfCanTrack()")

        if (trackUserData?.allowTrackUser == true) {
            layout?.progressBar?.getIndeterminateDrawable()?.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY)
            await(Date(), MIN_ALLOW_TRACK_USER_AWAIT_SECONDS, ::responseAfterAwait)

            layout?.loader?.setOnClickListener {
                trackUser()
            }
        }
        else {
            //Log.d("ADITEST8", "SET ${trackUserData?.isTrackUser} [splash]")
            //preferences?.setBoolean("isTrackUser", trackUserData?.isTrackUser == true, isAsync = false)
            layout?.loader?.setOnClickListener(null)
            responseAfterAwait()
        }
    }

    private fun responseAfterAwait() {
        Log.d(TAG, "responseAfterAwait()")

        if (openTrackUserDialog == true) {
            return
        }

        fixedParameters = FixedParameters.init(fixedParametersData)

        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        val isBetaVersion = fixedParameters?.appVersionArray?.find { it.key == "is_beta" }?.value?.toBoolean() ?: true
        val isNewVersionRequired = fixedParameters?.appVersionArray?.find { it.key == "new_version_required" }?.value?.toBoolean() ?: true

        preferences?.setBoolean("isBetaVersion", isBetaVersion, isAsync = false)

        if (userData != null && userData?.subscriberType != null && userData?.subscriberType!!.equals(Enums.SubscriberType.BLOCKED.toString(), true)) {
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

        if (userData != null && userData?.registrationExpiredTime != null && userData?.registrationExpiredTime!! > 0L && nowUTC.timeInMillis > userData?.registrationExpiredTime!!) {
            Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.EXPIRED_REGISTRATION, ::responseAfterExpiredCodePositivePress, null, arrayOf(dateFormatter.format(userData?.registrationExpiredTime), timeFormatter.format(userData?.registrationExpiredTime)))
            return
        }

        if (isNewVersionAvailable == true) {
            if (isNewVersionRequired) {
                Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.NEW_VERSION_AVAILABLE_REQUIRED, ::responseAfterNewVersionAvailableRequiredPositivePress, null, emptyArray())
                return
            }
        }

        if (userData == null && deviceData != null) {
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
        Utilities.roomStrings!!.remove(StringEntity(key = "dialog_announcement_title", value = Utilities.getRoomString("dialog_announcement_title")))
        Utilities.roomStrings!!.remove(StringEntity(key = "dialog_announcement_message", value = Utilities.getRoomString("dialog_announcement_message")))
        Utilities.roomStrings!!.remove(StringEntity(key = "dialog_announcement_positive", value = Utilities.getRoomString("dialog_announcement_positive")))
        Utilities.roomStrings!!.add(StringEntity(key = "dialog_announcement_title", value = announcement.title!!))
        Utilities.roomStrings!!.add(StringEntity(key = "dialog_announcement_message", value = announcement.message!!))
        Utilities.roomStrings!!.add(StringEntity(key = "dialog_announcement_positive", value = announcement.positiveButtonText!!))
        announcementDialog = Utilities.openFancyDialog(this@SplashActivity, Enums.DialogType.ANNOUNCEMENT, ::responseAfterAnnouncementPositivePress, null, emptyArray())
    }

    private fun continueToActivity(isBetaVersion: Boolean, isExpired: Boolean) {
        Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity()", showToast = false)

        if (userData == null) {
            preferences?.deleteAll()
            preferences?.setBoolean("isBetaVersion", isBetaVersion, isAsync = false)

            Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): SignUpActivity.start", showToast = false)
            SignUpActivity.start(context)
        }
        else {
            if (userData?.userName.isNullOrEmpty() || userData?.email.isNullOrEmpty() || userData?.age == null ||
                userData?.equity == null || userData?.incomes == null || userData?.commitments == null ||
                userData?.termsOfUseAcceptTime == null ||
                userData?.subscriberType == null) {

                Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): SignUpActivity.start (missing data)", showToast = false)
                SignUpActivity.start(context)
            }
            else {
                Utilities.log(Enums.LogType.Debug, TAG, "continueToActivity(): HomeActivity.start", showToast = false)

                preferences?.setString("userUUID", userData?.uuid, false)

                val subscriberType = userData?.subscriberType
                val expiredTime = userData?.registrationExpiredTime
                val appURL = fixedParameters?.appVersionArray?.find { it.key == "url" }?.value ?: ""
                val appID = Utilities.getAppID(fixedParameters)
                val onErrorSendEmail = fixedParameters?.onErrorArray?.find { it.key == "send_email" }?.value?.toBoolean() ?: true
                val onErrorMailTo = fixedParameters?.onErrorArray?.find { it.key == "mail_to" }?.value ?: ""

                preferences?.setString("subscriberType", subscriberType, false)
                expiredTime?.let { preferences?.setLong("expiredTime", it, false) }
                preferences?.setBoolean("expiredDialogHasShown", b = false, isAsync = false)
                preferences?.setString("userName", userData?.userName, false)
                preferences?.setString("appVersion", userData?.appVersion, false)
                preferences?.setString("appURL", appURL, false)
                preferences?.setString("appID", appID, false)
                preferences?.setBoolean("isNewVersionAvailable", isNewVersionAvailable == true, false)
                preferences?.setBoolean("onErrorSendEmail", onErrorSendEmail, false)
                preferences?.setString("onErrorMailTo", onErrorMailTo, false)

                if (isBetaVersion) {
                    RegistrationActivity.start(context, Enums.RegistrationPageType.BETA_CODE, allowBack = false)
                }
                else if (isExpired) {
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
            viewModel!!.confirmAnnouncement(announcementsData!![announcementIndex].uuid!!, userUUID)
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
        val appURL = fixedParameters?.appVersionArray?.find { it.key == "url" }?.value ?: ""
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appURL")))
        } catch (e: ActivityNotFoundException) {
            Utilities.log(Enums.LogType.Error, TAG, "responseAfterNewVersionAvailableRequiredPositivePress(): ActivityNotFoundException = ${e.message}", userData)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appURL")))
        }
    }

    private fun responseAfterNewVersionAvailableNotRequiredPositivePress() {
        val appURL = fixedParameters?.appVersionArray?.find { it.key == "url" }?.value ?: ""

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appURL")))
        } catch (e: ActivityNotFoundException) {
            Utilities.log(Enums.LogType.Error, TAG, "responseAfterNewVersionAvailableNotRequiredPositivePress(): ActivityNotFoundException = ${e.message}", userData)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appURL")))
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
    private inner class ServerSplashObserver : Observer<SplashDataClass?> {
        override fun onChanged(splashData: SplashDataClass?) {
            if (splashData == null) {
                Log.d(TAG, "ServerSplashObserver(): splashData == null)")
                endLoadData()
                return
            }

            fixedParametersData = splashData.fixedParameters
            userData = splashData.user
            deviceData = splashData.restore
            announcementsData = splashData.announcements
            isNewVersionAvailable = splashData.newVersionAvailable
            isServerDown = splashData.serverDown
            trackUserData = splashData.trackUser

            viewModel!!.saveRoomSplash(applicationContext, lifecycleOwner!!, splashData)
        }
    }

    private inner class RoomSplashObserver : Observer<MutableList<Any?>> {
        override fun onChanged(roomData: MutableList<Any?>) {
            Log.d(TAG, "RoomSplashObserver()")
            endLoadData()
        }
    }

    private inner class RoomUserObserver() : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            isRoomUserLoaded = true
            userUUID = user?.uuid

            //Log.d("ADITEST9","GET ${preferences?.getBoolean("isTrackUser", false).toString()} SET false [splash]")
            /*if (preferences?.getBoolean("isTrackUser", false) == true) {
                preferences?.setBoolean("isTrackUser", false, isAsync = false)
                userUUID = null
            }*/

            viewModel!!.getServerSplash(userUUID, Settings.Secure.getString(contentResolver, getDeviceID(context))!!)
        }
    }

    //endregion observers

    //region restore
    fun restoreData(deviceData: DeviceDataClass?) {
        val restoreUser = deviceData?.user
        val restoreProperties = deviceData?.properties

        if (restoreUser != null || restoreProperties != null) {
            viewModel!!.restoreRoomData(applicationContext, restoreUser, restoreProperties)
        }

    }

    private inner class RoomRestoreDataObserver : Observer<UserEntity?> {
        override fun onChanged(roomUserData: UserEntity?) {
            if (roomUserData != null) {
                userData = roomUserData
            }

            endRestoreData()
        }
    }

    fun endRestoreData() {
        await(startTime, MIN_RESTORE_AWAIT_SECONDS, ::responseAfterAwait)
    }

    //endregion restore

    //region track user
    fun trackUser() {
        openTrackUserDialog = true;
        Utilities.openTrackUserDialog(this@SplashActivity, trackUserData?.valueLength, ::responseAfterTrackUserPositivePress, ::responseAfterTrackUserNegativePress)
    }

    private fun responseAfterTrackUserPositivePress(trackUserUUID: String) {
        openTrackUserDialog = false
        viewModel!!.getServerSplash(trackUserUUID, "", true)
    }

    private fun responseAfterTrackUserNegativePress() {
        openTrackUserDialog = false
        /*if (isRoomUserLoaded) {
            Log.d(TAG, "responseAfterTrackUserNegativePress(): isRoomUserLoaded = ${isRoomUserLoaded}")
            endLoadData()
        }*/
    }
    //endregion track user

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