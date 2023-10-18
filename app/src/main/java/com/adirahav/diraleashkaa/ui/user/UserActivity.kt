package com.adirahav.diraleashkaa.ui.user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.ui.base.BaseActivity

import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.getMapIntValue
import com.adirahav.diraleashkaa.common.Utilities.getMapStringValue
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.data.network.entities.StringEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.APIResponseModel
import com.adirahav.diraleashkaa.data.network.models.StringModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
import com.adirahav.diraleashkaa.databinding.ActivityUserBinding
import com.adirahav.diraleashkaa.ui.goodbye.GoodbyeActivity
import com.adirahav.diraleashkaa.ui.registration.RegistrationTermsOfUseFragment
import com.adirahav.diraleashkaa.ui.signin.SignInActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpFinancialInfoFragment
import com.adirahav.diraleashkaa.ui.signup.SignUpPersonalInfoFragment
import kotlinx.coroutines.*
import java.net.CacheResponse
import java.util.*

class UserActivity : BaseActivity<UserViewModel?, ActivityUserBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "UserActivity"
        const val EXTRA_PAGE_TYPE = "EXTRA_PAGE_TYPE"

        fun start(context: Context, pageType: Enums.UserPageType) {
            val intent = Intent(context, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_PAGE_TYPE, pageType.toString())
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@UserActivity

    // shared preferences
    var preferences: AppPreferences? = null

    // user id
    var roomUID: Long? = 0L

    // user data
    var userData: UserEntity? = null

    // page type
    var pageType : Enums.UserPageType? = null

    // fragments
    private val personalInfoFragment = SignUpPersonalInfoFragment()
    private val financialInfoFragment = SignUpFinancialInfoFragment()
    private val termsOfUseFragment = RegistrationTermsOfUseFragment()

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // room/server data loaded
    var isRoomUserLoaded: Boolean = false
    var isServerUserLoaded: Boolean = false
    var isDataInit: Boolean = false

    // layout
    internal lateinit var layout: ActivityUserBinding

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityUserBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }

    public override fun onResume() {
        super.onResume()
        log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()
        initData()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar(layout.drawer)
        setDrawer(layout.drawer, layout.menu)
    }

    override fun createViewModel(): UserViewModel {
        val factory = UserViewModelFactory(DataManager.instance!!.userService, DataManager.instance!!.stringsService)
        return ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.serverUserInsertUpdateServer.hasObservers()) viewModel!!.serverUserInsertUpdateServer.observe(this@UserActivity, ServerUserObserver(Enums.ObserverAction.INSERT_UPDATE_SERVER))
        if (!viewModel!!.roomUserGet.hasObservers()) viewModel!!.roomUserGet.observe(this@UserActivity, RoomUserObserver(Enums.ObserverAction.GET_ROOM))
        if (!viewModel!!.roomUserUpdateRoom.hasObservers()) viewModel!!.roomUserUpdateRoom.observe(this@UserActivity, RoomUserObserver(Enums.ObserverAction.UPDATE_ROOM))
        if (!viewModel!!.roomUserUpdateServer.hasObservers()) viewModel!!.roomUserUpdateServer.observe(this@UserActivity, RoomUserObserver(Enums.ObserverAction.UPDATE_SERVER))
        if (!viewModel!!.termsOfUse.hasObservers()) viewModel!!.termsOfUse.observe(this@UserActivity, TermsOfUseObserver())

        if (!isRoomUserLoaded && !isDataInit) {
            viewModel!!.getRoomUser(applicationContext, roomUID)
        }
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)

        // room/server data loaded
        isRoomUserLoaded = false
        isServerUserLoaded = false

        isDataInit = false

        // page type
        pageType = enumValueOf<Enums.UserPageType>(intent.getStringExtra(EXTRA_PAGE_TYPE)!!)

        // buttons
        layout.buttons.back.visibility = GONE
        layout.buttons.next.visibility = GONE
        layout.buttons.save.visibility = if (pageType == Enums.UserPageType.TERMS_OF_USE)
            GONE
        else
            VISIBLE
        layout.buttons.send.visibility = GONE
        layout.buttons.pay.visibility = GONE
    }

    private fun initData() {

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
        super.setRoomStrings()
    }

    //endregion == strings ============

    //region == fragments ==========

    private fun loadFragment() {
        log(Enums.LogType.Debug, TAG, "loadFragment()")
        when (pageType) {
            Enums.UserPageType.PERSONAL_DETAILS -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, personalInfoFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getRoomString("actionbar_title_personal_details")

                // track user
                //Log.d("ADITEST1 [user]", "GET ${preferences?.getBoolean("isTrackUser", false).toString()}")
                /*trackUser?.visibility =
                    if (preferences?.getBoolean("isTrackUser", false) == true)
                        VISIBLE
                    else
                        GONE*/
            }


            Enums.UserPageType.FINANCIAL_DETAILS -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, financialInfoFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getRoomString("actionbar_title_financial_details")

                // track user
                //Log.d("ADITEST2 [user]", "GET ${preferences?.getBoolean("isTrackUser", false).toString()}")
                /*trackUser?.visibility =
                    if (preferences?.getBoolean("isTrackUser", false) == true)
                        VISIBLE
                    else
                        GONE*/
            }

            Enums.UserPageType.TERMS_OF_USE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, termsOfUseFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getRoomString("actionbar_title_terms_of_use")

                // track user
                //Log.d("ADITEST3 [user]", "GET ${preferences?.getBoolean("isTrackUser", false).toString()}")
                /*trackUser?.visibility =
                    if (preferences?.getBoolean("isTrackUser", false) == true)
                        VISIBLE
                    else
                        GONE*/
            }

            else -> {}
        }
    }

    //endregion == fragments ==========

    //region == steps ==============

    fun submitSave(view: View?) {

        val result = when (pageType) {
            Enums.UserPageType.PERSONAL_DETAILS -> personalInfoFragment.submitForm()
            Enums.UserPageType.FINANCIAL_DETAILS -> financialInfoFragment.submitForm()
            else -> null
        }

        log(Enums.LogType.Debug, TAG, "initData(): result = $result")
        val isValid = if (result?.containsKey("isValid") == true) { result["isValid"] as Boolean } else { false }

        if (isValid) {
            Utilities.hideKeyboard(this@UserActivity)

            runBlocking {
                updateUser(result)
            }
        }
    }

    //endregion == steps ==============

    //region == user data ==========

    fun updateUser(result: Map<String, Any?>?) {
        if (getMapStringValue(result, "name").isNotEmpty()) {
            preferences?.setString("userName", getMapStringValue(result, "name"), false)

            drawerUserName?.text =
                    if (Utilities.getRoomString("drawer_user_name").isNotEmpty())
                        String.format(
                                Utilities.getRoomString("drawer_user_name"),
                                preferences?.getString("userName", ""))
                    else
                        ""
        }

        GlobalScope.launch {

            val nowUTC = Calendar.getInstance()
            nowUTC.timeZone = TimeZone.getTimeZone("UTC")

            userData = UserEntity(
                    roomUID = roomUID,
                    uuid = userData?.uuid,
                    userName = if (getMapStringValue(result, "name").isEmpty()) userData?.userName else getMapStringValue(result, "name"),
                    email = if (getMapStringValue(result, "email").isEmpty()) userData?.email else getMapStringValue(result, "email"),
                    calcAge = if (getMapIntValue(result, "calc_age") == null) userData?.calcAge else getMapIntValue(result, "calc_age"),
                    yearOfBirth = if (getMapIntValue(result, "year_of_birth") == null) userData?.yearOfBirth else getMapIntValue(result, "year_of_birth"),
                    phoneNumber = userData?.phoneNumber,
                    phoneNumberSMSVerified = userData?.phoneNumberSMSVerified,
                    deviceID = userData?.deviceID,
                    deviceType = userData?.deviceType,
                    equity = if (getMapIntValue(result, Const.EQUITY) == null) userData?.equity else getMapIntValue(result, Const.EQUITY),
                    incomes = if (getMapIntValue(result, Const.INCOMES) == null) userData?.incomes else getMapIntValue(result, Const.INCOMES),
                    commitments = if (getMapIntValue(result, Const.COMMITMENTS) == null) userData?.commitments else getMapIntValue(result, Const.COMMITMENTS),
                    termsOfUseAcceptTime = userData?.termsOfUseAcceptTime,
                    //insertTime = userData?.insertTime,
                    //updateTime = nowUTC.timeInMillis,
                    //serverUpdateTime = userData?.serverUpdateTime,
                    subscriberType = userData?.subscriberType.toString(),
                    //registrationStartTime = userData?.registrationStartTime,
                    registrationExpiredTime = userData?.registrationExpiredTime,
                    appVersion = userData?.appVersion,
                    isFirstLogin = userData?.isFirstLogin,
                    canTakeMortgage = userData?.canTakeMortgage
            )

            viewModel!!.updateServerUser(userData)
            //viewModel!!.updateRoomUser(applicationContext, userData, Enums.DBCaller.ROOM)
        }
    }


    //endregion == user data ==========

    //region == coupon code ========



    //endregion == coupon code ========

    //region == observers ==========

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

                    if (isRoomUserLoaded) {
                        loadFragment()
                    }
                }

                Enums.ObserverAction.UPDATE_ROOM -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): UPDATE_ROOM. user = $user")

                    roomUID = user?.roomUID
                    preferences?.setLong("roomUID", roomUID!!, false)

                    /*activity.runOnUiThread {
                        if (user?.uuid == null) {
                            viewModel!!.insertServerUser(userData)
                        }
                        else {
                            viewModel!!.updateServerUser(userData)
                        }
                    }*/

                    //
                    Utilities.displayActionSnackbar(activity, Utilities.getRoomString("user_save_success"))
                }

                Enums.ObserverAction.UPDATE_SERVER -> {
                    log(Enums.LogType.Debug, TAG, "RoomUserObserver(): UPDATE_SERVER. user = $user")

                    if (user == null) {
                        return
                    }

                    userData = user

                    roomUID = user?.roomUID
                    preferences?.setLong("roomUID", roomUID!!, false)

                    Utilities.displayActionSnackbar(activity, Utilities.getRoomString("user_save_success"))
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
                            calcAge = userData?.calcAge,
                            yearOfBirth = userData?.yearOfBirth,
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

    override fun attachBinding(list: MutableList<ActivityUserBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityUserBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}