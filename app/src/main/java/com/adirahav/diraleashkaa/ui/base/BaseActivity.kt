package com.adirahav.diraleashkaa.ui.base

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.ExceptionHandler
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.showEnvironmentSnackMessageIfNeeded
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.databinding.ActivityCalculatorBinding
import com.adirahav.diraleashkaa.databinding.ActivityCalculatorsBinding
import com.adirahav.diraleashkaa.databinding.ActivityContactusBinding
import com.adirahav.diraleashkaa.databinding.ActivityHomeBinding
import com.adirahav.diraleashkaa.databinding.ActivityPropertyBinding
import com.adirahav.diraleashkaa.databinding.ActivityRegistrationBinding
import com.adirahav.diraleashkaa.databinding.ActivityUserBinding
import com.adirahav.diraleashkaa.databinding.IncludeMenuBinding
import com.adirahav.diraleashkaa.ui.calculators.CalculatorsActivity
import com.adirahav.diraleashkaa.ui.contactus.ContactUsActivity
import com.adirahav.diraleashkaa.ui.copyright.CopyrightActivity
import com.adirahav.diraleashkaa.ui.login.LoginActivity
import com.adirahav.diraleashkaa.ui.registration.RegistrationActivity
import com.adirahav.diraleashkaa.ui.user.UserActivity
import com.google.android.material.navigation.NavigationView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


abstract class BaseActivity<VM : BaseViewModel?, VB : ViewBinding> internal constructor(): AppCompatActivity() {

    //region == companion ==========

    companion object {
        private const val TAG = "BaseActivity"
    }

    //endregion == companion ==========

    private var binding: VB? = null

    // shared preferences
    private var preferences: AppPreferences? = null

    // view model
    @JvmField
    var viewModel: VM? = null

    // beta
    private var isBeta: Boolean? = null

    // expired registration
    private var isExpiredRegistration: Boolean? = null

    // action bar views
    var titleText: TextView? = null
    var trackUser: ImageView? = null

    // drawer views
    var drawerFullname: TextView? = null
    var drawerCalculators: TextView? = null
    var drawerPersonalDetails: TextView? = null
    var drawerFinancialDetails: TextView? = null
    var drawerRegistrationDetails: TextView? = null
    var drawerTermsOfUse: TextView? = null
    var drawerContactUs: TextView? = null
    var drawerShare: TextView? = null
    var drawerLogout: TextView? = null
    var drawerVersion: TextView? = null
    var drawerCopyright: TextView? = null

    init {
        // shared preferences
        preferences = AppPreferences.instance
    }

    //region == lifecycle methods ==

    protected abstract fun createViewModel(): VM

    private lateinit var layoutBase: VB

    // mixpanel
    private var mixpanel: MixpanelAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isFullScreen()) {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        forceHebrew()

        super.onCreate(savedInstanceState)
        layoutBase = getInflatedLayout(layoutInflater)
        //setContentView(layoutBase.root)

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        viewModel = createViewModel()
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        initBaseObserver()

        initMixpanel()
    }

    ////

    private fun initMixpanel() {
    //    mixpanel = MixpanelAPI.getInstance(applicationContext, "2b3a3e89207d8209a6da4dd03bb2f473")
    }

    private fun getInflatedLayout(inflater: LayoutInflater): VB {
        val tempList = mutableListOf<VB>()
        attachBinding(tempList, inflater)
        this.binding = tempList[0]

        return binding!!
    }
    abstract fun attachBinding(list: MutableList<VB>, layoutInflater: LayoutInflater)
    ////

    override fun onResume() {
        super.onResume()

        // expired registration
        val expiredTime : Long = preferences?.getLong("expiredTime", 0L) ?: 0L

        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        isExpiredRegistration = expiredTime != null && expiredTime > 0L && nowUTC.timeInMillis > expiredTime
        if (isExpiredRegistration == true) {
            if (!allowPageWhenExpired()) {
                if (!(this::class.java.simpleName.equals("SignUpActivity"))) {
                    RegistrationActivity.start(context, Enums.RegistrationPageType.COUPON_CODE)
                    return
                }
            }
        }

        // beta
        isBeta = preferences?.getBoolean("isBetaVersion", false)

        //setCustomActionBar()
        //setDrawer()

        // soon expired registration snack message
        //https://currentmillis.com/
        if (allowShowExpiredSnack()) {
            val subscriberType = preferences?.getString("subscriberType", "")

            val snackType = when {
                subscriberType.equals(Enums.SubscriberType.TRIAL.toString(), true) -> Enums.SnackType.EXPIRED_TRIAL
                subscriberType.equals(Enums.SubscriberType.COUPON_PAID.toString(), true) -> Enums.SnackType.EXPIRED_PAID_COUPON
                subscriberType.equals(Enums.SubscriberType.PAY_PROGRAM_PAID.toString(), true) -> Enums.SnackType.EXPIRED_PAID_PAID_PROGRAM
                else -> Enums.SnackType.UNKNOWN
            }

            Utilities.showSnackMessageIfNeeded(this@BaseActivity, snackType, expiredTime!!)
        }

        // environment
        showEnvironmentSnackMessageIfNeeded(this@BaseActivity)
    }

    fun initBaseObserver() {

        Utilities.log(Enums.LogType.Debug, TAG, "initObserver()")
        if (!viewModel!!.localBasePhrasesCallback.hasObservers()) viewModel!!.localBasePhrasesCallback.observe(this@BaseActivity, PhrasesObserver())

        Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): getLocalPhrases")
        viewModel!!.getLocalPhrases(applicationContext)
    }

    //endregion == lifecycle methods ==

    protected fun setCustomActionBar(drawer: DrawerLayout? = null) {
        if (isFullScreen()) {
            this.window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                statusBarColor = Color.TRANSPARENT
            }

            //this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            supportActionBar?.hide()
            return
        }

        supportActionBar?.show()
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.actionbar)

        val view: View? = supportActionBar?.customView
        val toolbar: Toolbar? = view?.parent as Toolbar?
        toolbar?.setContentInsetsAbsolute(0, 0)

        toolbar?.navigationIcon = null

        //window?.navigationBarColor = resources.getColor(R.color.navigationbarBackground)
        //window?.decorView?.light .windowLightNavigationBar = false

        // title text
        titleText = view?.findViewWithTag<TextView>("titleText")

        // track user
        /*trackUser = view?.findViewWithTag<ImageView>("trackUser")
        //Log.d("ADITEST4", "GET ${preferences?.getBoolean("isTrackUser", false).toString()} [base]")
        trackUser?.visibility =
            if (preferences?.getBoolean("isTrackUser", false) == true)
                VISIBLE
            else
                GONE*/

        // back
        val back = view?.findViewWithTag<ImageView>("back")

        if (isTaskRoot) {
            back?.visibility = INVISIBLE
        }
        else {
            back?.visibility = VISIBLE

            back?.setOnClickListener {

                if (drawer != null) {
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START)
                    }
                }

                super.onBackPressed()
            }
        }

        // menu
        val menu = view?.findViewWithTag<ImageView>("menu")
        menu?.visibility =
            if (!allowShowDrawer())
                GONE
            else
                VISIBLE
    }

    protected fun setDrawer(drawer: DrawerLayout? = null, menu: IncludeMenuBinding? = null) {

        if (isFullScreen()) {
            return
        }

        // drawer
        if (!allowShowDrawer()) {
            drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            return
        }
        else {
            drawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }

        val view: View? = supportActionBar?.customView

        val menuIcon = view?.findViewWithTag<ImageView>("menu")
        menuIcon?.setOnClickListener {
            if (drawer != null) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    drawer.openDrawer(GravityCompat.START)
                }
            }
        }

        // drawer - avatar
        /*val avatar = view?.findViewWithTag<de.hdodenhof.circleimageview.CircleImageView>("avatar")
        if (avatar != null) {
            Picasso.with(applicationContext)
                .load(preferences?.getString("userAvatar", null))
                .error(R.drawable.picture_missing_avatar)
                .into(avatar)
        }

        avatar?.setOnClickListener(View.OnClickListener {
            if (this.drawer != null) {

                if (this.drawer!!.isDrawerOpen(GravityCompat.START)) {
                    this.drawer!!.closeDrawer(GravityCompat.START)
                }
                else {
                    this.drawer!!.openDrawer(GravityCompat.START)
                }
            }
        })*/

        // drawer - fullname
        drawerFullname = menu?.drawerFullname

        // drawer - calculators
        drawerCalculators = menu?.drawerCalculators

        drawerCalculators?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            CalculatorsActivity.start(context)
        }

        // drawer - personal details
        drawerPersonalDetails = menu?.drawerPersonalDetails

        drawerPersonalDetails?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            UserActivity.start(context, Enums.UserPageType.PERSONAL_DETAILS)
        }

        // drawer - financial details
        drawerFinancialDetails = menu?.drawerFinancialDetails

        drawerFinancialDetails?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            UserActivity.start(context, Enums.UserPageType.FINANCIAL_DETAILS)
        }

        // drawer - registration
        drawerRegistrationDetails = menu?.drawerRegistrationDetails

        drawerRegistrationDetails?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            RegistrationActivity.start(context, Enums.RegistrationPageType.COUPON_CODE)
        }

        // drawer - terms of use
        drawerTermsOfUse = menu?.drawerTermsOfUse

        drawerTermsOfUse?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            UserActivity.start(context, Enums.UserPageType.TERMS_OF_USE)
        }


        // drawer - contact us
        drawerContactUs = menu?.drawerContactUs

        drawerContactUs?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            ContactUsActivity.start(context, Enums.ContactUsPageType.MAIL_FORM)
        }

        // drawer - share
        drawerShare = menu?.drawerShare

        drawerShare?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)

            val intent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT,
                        String.format(
                                Utilities.getLocalPhrase("share_text"),
                                preferences?.getString("appURL", ""))
                )
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)
        }

        // drawer - logout
        drawerLogout = menu?.drawerLogout

        drawerLogout?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)

            CoroutineScope(Dispatchers.IO).launch {

                val authService = DataManager.instance!!.authService
                val call: Call<Boolean?>? = authService.authAPI.logout()

                call?.enqueue(object : Callback<Boolean?> {
                    override fun onResponse(call: Call<Boolean?>, response: Response<Boolean?>) {
                        Utilities.log(Enums.LogType.Debug, TAG, "setLogout(): response = $response")

                        val result: Boolean? = response.body()

                        if (response.code() == 200) {
                            try {
                                if (result === true) {
                                    preferences?.setString("token", null, false)
                                    LoginActivity.start(context, preferences?.getString("email", ""))
                                }
                            } catch (e: Exception) {
                                Utilities.log(Enums.LogType.Error, TAG, "setLogout(): e = ${e.message} ; result.data = ${result?.toString()}")
                            }
                        }
                        else {
                            Utilities.log(Enums.LogType.Error, TAG, "setLogout(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<Boolean?>, t: Throwable) {
                        Utilities.log(Enums.LogType.Error, TAG, "setLogout(): onFailure = $t")
                        call.cancel()
                    }
                })
            }


        }

        // drawer - version
        drawerVersion = menu?.drawerVersion

        // drawer - copyright
        drawerCopyright = menu?.drawerCopyright

        drawerCopyright?.setOnClickListener {
            drawer?.closeDrawer(GravityCompat.START)
            CopyrightActivity.start(context)
        }
    }

    fun setExpiredRegistration(isExpired: Boolean) {
        isExpiredRegistration = isExpired
    }

    private fun forceHebrew() {
        val configuration: Configuration = resources.configuration
        configuration.setLayoutDirection(Locale("iw"))

        if (Build.VERSION.SDK_INT >= 17) {
            configuration.setLocale(Locale("iw"))
        } else {
            configuration.locale = Locale("iw")
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    //region == rules ==============

    private fun getActionBarTitle() : String {
        when (this::class.java.simpleName) {
            "UserActivity" ->
                if (intent.getStringExtra(UserActivity.EXTRA_PAGE_TYPE)!!
                        .equals(Enums.UserPageType.PERSONAL_DETAILS.toString()))
                    return Utilities.getLocalPhrase("actionbar_title_personal_details")
                else if (intent.getStringExtra(UserActivity.EXTRA_PAGE_TYPE)!!
                        .equals(Enums.UserPageType.FINANCIAL_DETAILS.toString()))
                    return Utilities.getLocalPhrase("actionbar_title_financial_details")
            "RegistrationActivity" ->
                return  if (isBeta ?: false)
                            Utilities.getLocalPhrase("actionbar_title_beta")
                        else
                            Utilities.getLocalPhrase("actionbar_title_registration_details")
            "ContactUsActivity" ->
                return Utilities.getLocalPhrase("actionbar_title_contact_us")
            "CopyrightActivity" ->
                return Utilities.getLocalPhrase("actionbar_title_copyright")
            "CalculatorsActivity" ->
                return Utilities.getLocalPhrase("actionbar_title_calculators")
        }
        return Utilities.getLocalPhrase("actionbar_title_home")
    }

    private fun isFullScreen() : Boolean {
        if (this::class.java.simpleName.equals("SplashActivity")) return true
        if (this::class.java.simpleName.equals("SignUpActivity")) return true
        if (this::class.java.simpleName.equals("LoginActivity")) return true
        if (this::class.java.simpleName.equals("ForgotPasswordActivity")) return true
        if (this::class.java.simpleName.equals("GoodbyeActivity")) return true
        return false
    }

    private fun allowShowExpiredSnack() : Boolean {
        if (this::class.java.simpleName.equals("SplashActivity")) return false
        if (this::class.java.simpleName.equals("SignUpActivity")) return false
        if (this::class.java.simpleName.equals("LoginActivity")) return false
        if (this::class.java.simpleName.equals("ForgotPasswordActivity")) return false
        if (this::class.java.simpleName.equals("RegistrationActivity")) return false
        return true
    }

    private fun allowShowDrawer() : Boolean {
        if (this::class.java.simpleName.equals("SplashActivity")) return false
        if (this::class.java.simpleName.equals("SignUpActivity")) return false
        if (this::class.java.simpleName.equals("LoginActivity")) return false
        if (this::class.java.simpleName.equals("ForgotPasswordActivity")) return false
        if (this::class.java.simpleName.equals("RegistrationActivity")) {
            return !(isExpiredRegistration?: false || isBeta?: false)
        }
        if (this::class.java.simpleName.equals("ContactUsActivity")) {
            return !(isExpiredRegistration?: false)
        }
        return true
    }

    private fun allowPageWhenExpired() : Boolean {
        if (this::class.java.simpleName.equals("SplashActivity")) return true
        if (this::class.java.simpleName.equals("RegistrationActivity")) return true
        if (this::class.java.simpleName.equals("ContactUsActivity")) return true
        if (this::class.java.simpleName.equals("GoodbyeActivity")) return true
        return false
    }

    //endregion == rules ==============

    //region == views ==============

    private fun getDrawerView() : DrawerLayout? {
        when (this::class.java.simpleName) {
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).drawer
            "UserActivity" -> return (layoutBase as ActivityUserBinding).drawer
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).drawer
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).drawer
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).drawer
        }
        return null
    }

    private fun getDrawerFullnameView() : TextView? {
        when (this::class.java.simpleName) {
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerFullname
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerFullname
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerFullname
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerFullname
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerFullname
        }
        return null
    }


    private fun getDrawerPersonalDetailsView() : TextView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).menu.drawerPersonalDetails
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).menu.drawerPersonalDetails
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerPersonalDetails
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerPersonalDetails
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerPersonalDetails
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerPersonalDetails
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerPersonalDetails
        }
        return null
    }

    private fun getDrawerFinancialDetailsView() : TextView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).menu.drawerFinancialDetails
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).menu.drawerFinancialDetails
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerFinancialDetails
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerFinancialDetails
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerFinancialDetails
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerFinancialDetails
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerFinancialDetails
        }
        return null
    }

    private fun getDrawerContactUsView() : TextView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).menu.drawerContactUs
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).menu.drawerContactUs
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerContactUs
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerContactUs
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerContactUs
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerContactUs
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerContactUs
        }
        return null
    }

    private fun getDrawerRegistrationDetailsView() : TextView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).menu.drawerRegistrationDetails
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).menu.drawerRegistrationDetails
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerRegistrationDetails
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerRegistrationDetails
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerRegistrationDetails
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerRegistrationDetails
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerRegistrationDetails
        }
        return null
    }

    private fun getDrawerVersionView() : TextView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).menu.drawerVersion
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).menu.drawerVersion
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).menu.drawerVersion
            "UserActivity" -> return (layoutBase as ActivityUserBinding).menu.drawerVersion
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).menu.drawerVersion
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).menu.drawerVersion
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).menu.drawerVersion
        }
        return null
    }

    private fun getNavigationView() : NavigationView? {
        when (this::class.java.simpleName) {
            "CalculatorsActivity" -> return (layoutBase as ActivityCalculatorsBinding).navigation
            "CalculatorActivity" -> return (layoutBase as ActivityCalculatorBinding).navigation
            "HomeActivity" -> return (layoutBase as ActivityHomeBinding).navigation
            "UserActivity" -> return (layoutBase as ActivityUserBinding).navigation
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).navigation
            "ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).navigation
            "PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).navigation
        }
        return null
    }

    private fun getContainerView() : ConstraintLayout? {
        when (this::class.java.simpleName) {
            //"HomeActivity" -> return (layoutBase as ActivityHomeBinding).navigation
            //"UserActivity" -> return (layoutBase as ActivityUserBinding).navigation
            "RegistrationActivity" -> return (layoutBase as ActivityRegistrationBinding).container
            //"ContactUsActivity" -> return (layoutBase as ActivityContactusBinding).navigation
            //"PropertyActivity" -> return (layoutBase as ActivityPropertyBinding).navigation
        }
        return null
    }


    //endregion == views ==============

    //region observers
    private inner class PhrasesObserver : Observer<ArrayList<PhraseEntity>?> {
        override fun onChanged(phrases: ArrayList<PhraseEntity>?) {
            Utilities.log(Enums.LogType.Debug, TAG, "PhrasesObserver(): onChanged")

            if (phrases == null) {
                return
            }

            Utilities.localPhrase = phrases

            setPhrases()
        }
    }

    open fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        // title text
        if (!this::class.java.simpleName.equals("CalculatorActivity")) {
            titleText?.text = getActionBarTitle()
        }


        // drawer - fullname
        drawerFullname?.text =
            if (Utilities.getLocalPhrase("drawer_fullname").isNotEmpty())
                String.format(
                    Utilities.getLocalPhrase("drawer_fullname"),
                    preferences?.getString("fullname", ""))
            else
                ""

        // drawer - calculators
        drawerCalculators?.text = Utilities.getLocalPhrase("drawer_calculators")

        // drawer - personal details
        drawerPersonalDetails?.text = Utilities.getLocalPhrase("drawer_personal_details")

        // drawer - financial details
        drawerFinancialDetails?.text = Utilities.getLocalPhrase("drawer_financial_details")

        // drawer - registration
        drawerRegistrationDetails?.text = Utilities.getLocalPhrase("drawer_registration_details")

        // drawer - terms of use
        drawerTermsOfUse?.text = Utilities.getLocalPhrase("drawer_terms_of_use")

        // drawer - contact us
        drawerContactUs?.text = Utilities.getLocalPhrase("drawer_contact_us")

        // drawer - share
        drawerShare?.text = Utilities.getLocalPhrase("drawer_share")

        // drawer - logout
        drawerLogout?.text = Utilities.getLocalPhrase("drawer_logout")

        // drawer - version
        if (preferences?.getBoolean("isNewVersionAvailable", false) == true) {
            drawerVersion?.text =
                if (Utilities.getLocalPhrase("drawer_version_update").isNotEmpty())
                    HtmlCompat.fromHtml(
                        String.format(Utilities.getLocalPhrase("drawer_version_update"), BuildConfig.VERSION_NAME), HtmlCompat.FROM_HTML_MODE_LEGACY)
                else
                    ""

            val appID = preferences?.getString("appID", "")

            drawerVersion?.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appID")
                    )
                )
            }
        }
        else {
            drawerVersion?.text =
                if (Utilities.getLocalPhrase("drawer_version").isNotEmpty())
                    String.format(Utilities.getLocalPhrase("drawer_version"), BuildConfig.VERSION_NAME)
                else
                    ""
        }

        // drawer - copyright
        drawerCopyright?.text = Utilities.getLocalPhrase("drawer_copyright")

    }

    //endregion observers

/*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.drawer_menu, menu)

        // user name
        val itemUserName: MenuItem = menu.findItem(R.id.drawerUserName)
        itemUserName.title = String.format(resources!!.getString(R.string.drawer_user_name), preferences?.getString("userName", ""))
        itemUserName.setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            drawer!!.closeDrawer(GravityCompat.START)
            false
        })

        // logout
        val itemLogout: MenuItem = menu.findItem(R.id.drawerLogout)
        itemLogout.setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            val editor: SharedPreferences.Editor = AppPreferences.sharedPreferences!!.edit()
            editor.remove("userAvatar")
            editor.remove("userName")

            SignInActivity.start(this)
            false
        })

        return super.onPrepareOptionsMenu(menu)
    }*/
}