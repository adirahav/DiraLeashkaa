package com.adirahav.diraleashkaa.ui.user

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
import com.adirahav.diraleashkaa.common.Utilities.getMapStringValue
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.ActivityUserBinding
import com.adirahav.diraleashkaa.ui.registration.RegistrationTermsOfUseFragment
import com.adirahav.diraleashkaa.ui.signup.SignUpFinancialInfoFragment
import com.adirahav.diraleashkaa.ui.signup.SignUpPersonalInfoFragment
import kotlinx.coroutines.*

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

    // loggedin user
    var loggedinUser: UserEntity? = null
    var userToken: String? = null
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

    // local/server data loaded
    var isLocalUserLoaded: Boolean = false
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
        val factory = UserViewModelFactory(this@UserActivity, DataManager.instance!!.userService, DataManager.instance!!.phraseService)
        return ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.setUserCallback.hasObservers()) viewModel!!.setUserCallback.observe(this@UserActivity, SaveUserObserver())
        if (!viewModel!!.getLocalUserCallback.hasObservers()) viewModel!!.getLocalUserCallback.observe(this@UserActivity, GetLocalUserObserver())
        if (!viewModel!!.updateLocalUserCallback.hasObservers()) viewModel!!.updateLocalUserCallback.observe(this@UserActivity, UpdateLocalUserObserver())
        if (!viewModel!!.updateUserCallback.hasObservers()) viewModel!!.updateUserCallback.observe(this@UserActivity, UpdateUserObserver())
        if (!viewModel!!.termsOfUseCallback.hasObservers()) viewModel!!.termsOfUseCallback.observe(this@UserActivity, TermsOfUseObserver())

        if (!isLocalUserLoaded && !isDataInit) {
            viewModel!!.getLocalUser(applicationContext, roomUID)
        }
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)

        // loggein user
        CoroutineScope(Dispatchers.IO).launch {
            val existLocalUser = DatabaseClient.getInstance(activity.applicationContext)?.appDatabase?.userDao()?.getFirst()
            loggedinUser = if (existLocalUser != null) existLocalUser else null
        }

        // local/server data loaded
        isLocalUserLoaded = false
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
        userToken = preferences!!.getString("token", "")
    }

    //endregion == initialize =========

    //region == phrases ============

    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        layout.buttons.back.text = Utilities.getLocalPhrase("button_back")
        layout.buttons.next.text = Utilities.getLocalPhrase("button_next")
        layout.buttons.save.text = Utilities.getLocalPhrase("button_save")
        layout.buttons.send.text = Utilities.getLocalPhrase("button_send")
        layout.buttons.pay.text = Utilities.getLocalPhrase("button_pay")
        super.setPhrases()
    }

    //endregion == phrases ============

    //region == fragments ==========

    private fun loadFragment() {
        log(Enums.LogType.Debug, TAG, "loadFragment()")
        when (pageType) {
            Enums.UserPageType.PERSONAL_DETAILS -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, personalInfoFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getLocalPhrase("actionbar_title_personal_details")

            }


            Enums.UserPageType.FINANCIAL_DETAILS -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, financialInfoFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getLocalPhrase("actionbar_title_financial_details")
            }

            Enums.UserPageType.TERMS_OF_USE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, termsOfUseFragment)
                    .commitAllowingStateLoss()

                // title text
                titleText?.text = Utilities.getLocalPhrase("actionbar_title_terms_of_use")

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
        if (getMapStringValue(result, "fullname")?.isNotEmpty() ?: true) {
            preferences?.setString("fullname", getMapStringValue(result, "fullname"), false)

            drawerFullname?.text =
                    if (Utilities.getLocalPhrase("drawer_fullname").isNotEmpty())
                        String.format(
                                Utilities.getLocalPhrase("drawer_fullname"),
                                preferences?.getString("fullname", ""))
                    else
                        ""
        }

        GlobalScope.launch {
            viewModel!!.updateUser(result)
        }
    }


    //endregion == user data ==========

    //region == coupon code ========



    //endregion == coupon code ========

    //region == observers ==========

    private inner class GetLocalUserObserver : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "GetLocalUserObserver(): user = $user")

            isLocalUserLoaded = true

            if (user == null) {
                return
            }

            userData = user

            if (isLocalUserLoaded) {
                loadFragment()
            }
        }
    }

    private inner class UpdateLocalUserObserver : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "LocalUserObserver(): user = $user")

            roomUID = user?.roomUID
            preferences?.setLong("roomUID", roomUID!!, false)

            //
            Utilities.displayActionSnackbar(activity, Utilities.getLocalPhrase("user_save_success"))
        }
    }

    private inner class UpdateUserObserver : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "UpdateUserObserver(): user = $user")

            if (user == null) {
                return
            }

            userData = user

            roomUID = user?.roomUID
            preferences?.setLong("roomUID", roomUID!!, false)

            Utilities.displayActionSnackbar(activity, Utilities.getLocalPhrase("user_save_success"))
        }
    }

    private inner class SaveUserObserver : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "SaveUserObserver() user.email = ${user?.email}")
            GlobalScope.launch {
                viewModel!!.updateLocalUser(
                    applicationContext,
                    userData,
                    Enums.DBCaller.SERVER
                )
            }
        }
    }

    private inner class TermsOfUseObserver : Observer<PhraseEntity?> {
        override fun onChanged(termsOfUse: PhraseEntity?) {
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