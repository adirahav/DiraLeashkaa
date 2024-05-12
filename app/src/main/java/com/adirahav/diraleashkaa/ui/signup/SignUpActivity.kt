package com.adirahav.diraleashkaa.ui.signup

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.ui.base.BaseActivity

import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.ActivitySignupBinding
import com.adirahav.diraleashkaa.ui.registration.*
import com.kofigyan.stateprogressbar.StateProgressBar
import kotlinx.coroutines.*

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
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@SignUpActivity

    // shared preferences
    var preferences: AppPreferences? = null

    // logging user
    var loggingUser: UserEntity? = null
    var userToken: String? = null
    var roomUID: Long? = 0

    // state progress bar
    var currentStepProgressBar: Int = 1
    var stepsCount: Int? = null

    // fragments
    val personalInfoFragment = SignUpPersonalInfoFragment()
    private val financialInfoFragment = SignUpFinancialInfoFragment()
    private val termsOfUseFragment = RegistrationTermsOfUseFragment()
    val payProgramFragment = RegistrationPayProgramFragment()
    val couponCodeFragment = RegistrationCouponCodeFragment()
    private val welcomeInfoFragment = SignUpWelcomeFragment()

    private var paymentPageType = Enums.RegistrationPageType.PAY_PROGRAM

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // layout
    internal lateinit var layout: ActivitySignupBinding

    // beta version
    var isBetaVersion: Boolean = false

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
            DataManager.instance!!.authService,
            DataManager.instance!!.userService,
            DataManager.instance!!.registrationService,
            DataManager.instance!!.phraseService
        )
        return ViewModelProvider(this, factory)[SignUpViewModel::class.java]


    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.fixedParametersCallback.hasObservers()) viewModel!!.fixedParametersCallback.observe(this@SignUpActivity, LocalFixedParametersObserver())
        if (!viewModel!!.couponRegistration.hasObservers()) viewModel!!.couponRegistration.observe(this@SignUpActivity, CouponRegistrationObserver())
        if (!viewModel!!.payProgramRegistration.hasObservers()) viewModel!!.payProgramRegistration.observe(this@SignUpActivity, PayRegistrationObserver())
        if (!viewModel!!.skipRegistrationCallback.hasObservers()) viewModel!!.skipRegistrationCallback.observe(this@SignUpActivity, SkipRegistrationObserver())
        //if (!viewModel!!.smsCodeValidation.hasObservers()) viewModel!!.smsCodeValidation.observe(this@SignUpActivity, SMSCodeValidationObserver())
        if (!viewModel!!.setSignupCallback.hasObservers()) viewModel!!.setSignupCallback.observe(this@SignUpActivity, SignupObserver())
        if (!viewModel!!.termsOfUse.hasObservers()) viewModel!!.termsOfUse.observe(this@SignUpActivity, TermsOfUseObserver())

        if (fixedParametersData == null) {
            viewModel!!.getLocalFixedParameters(applicationContext)
        }
    }

    private fun initGlobal() {

        /*// format date
        originalDateFormat = SimpleDateFormat(Configuration.DATETIME_ORIGINAL_PATTERN, Locale.ENGLISH)
*/
        // shared preferences
        preferences = AppPreferences.instance

        // logging user
        CoroutineScope(Dispatchers.IO).launch {
            val existLocalUser = DatabaseClient.getInstance(activity.applicationContext)?.appDatabase?.userDao()?.getFirst()
            loggingUser = if (existLocalUser != null) existLocalUser else null
        }

        // buttons
        layout.buttons.back.visibility = View.VISIBLE
        layout.buttons.next.visibility = View.VISIBLE
        layout.buttons.save.visibility = View.GONE
        layout.buttons.send.visibility = View.GONE
        layout.buttons.pay.visibility = View.GONE

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

            userToken = preferences!!.getString("token", "")
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

        super.setPhrases()
    }

    //endregion == strings ============

    //region == fragments ==========

    private fun loadFragment() {
        log(Enums.LogType.Debug, TAG, "loadFragment()")
        log(Enums.LogType.Debug, TAG, "updateStep3: currentStepProgressBar = $currentStepProgressBar")

        if (loggingUser?.fullname.isNullOrEmpty() ||
            loggingUser?.email.isNullOrEmpty() ||
            loggingUser?.yearOfBirth == null) {

            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, personalInfoFragment)
                .commitAllowingStateLoss()

            currentStepProgressBar = 1

            layout.buttons.pay.visibility = View.GONE
        }
        else if (loggingUser?.equity == null || loggingUser?.incomes == null || loggingUser?.commitments == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, financialInfoFragment)
                .commitAllowingStateLoss()

            layout.buttons.pay.visibility = View.GONE

            currentStepProgressBar = 2
        }
        else if (loggingUser?.termsOfUseAccept == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.formFragment, termsOfUseFragment)
                .commitAllowingStateLoss()

            layout.buttons.pay.visibility = View.GONE

            currentStepProgressBar = 3
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

            Enums.RegistrationPageType.COUPON_CODE -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, couponCodeFragment)
                    .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.back.visibility = View.GONE
                    layout.buttons.next.visibility = View.VISIBLE
                    layout.buttons.pay.visibility = View.GONE
                }

                paymentPageType = Enums.RegistrationPageType.COUPON_CODE
            }
        }

    }

    //endregion == fragments ==========

    //region == steps ==============

    fun submitNext(view: View?) {
        //SKIP-2
        if (currentStepProgressBar == 4) {
            if (view == null) {
                currentStepProgressBar++
                updateStep()
                return
            }

            when (paymentPageType) {
                Enums.RegistrationPageType.COUPON_CODE ->
                    couponCodeFragment.submitForm(false)
                Enums.RegistrationPageType.PAY_PROGRAM ->
                    payProgramFragment.submitForm(false)
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
                    signupLocalUser(result)
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

                supportFragmentManager.beginTransaction()
                        .replace(R.id.formFragment, payProgramFragment)
                        .commitAllowingStateLoss()

                activity.runOnUiThread {
                    layout.buttons.back.visibility = View.GONE
                    layout.buttons.next.visibility = View.GONE
                    layout.buttons.pay.visibility = View.VISIBLE
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
                }
            }
        }

        updatePreferences()
    }

    private fun updatePreferences() {
        preferences?.setString("email", loggingUser?.email, false)
        preferences?.setString("subscriberType", loggingUser?.subscriberType, false)
        preferences?.setLong("expiredTime", loggingUser?.registrationExpiredTime ?: 0, false)
    }

    //endregion == steps ==============

    //region == user data ==========

    fun signupLocalUser(result: Map<String, Any?>?) =
        GlobalScope.launch {

            log(Enums.LogType.Debug, TAG, "signupUser(): userData = {${result!!.entries.toString()}}")

            activity.runOnUiThread {
                viewModel!!.signupUser(result)
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

    fun skipRegistrationCallback(uerData: UserEntity?) {
        runBlocking {
            submitNext(null)
        }
    }

    //endregion == skip registration ==


    //region == welcome ============

    private fun updateWelcome() =
        GlobalScope.launch {
            if (currentStepProgressBar <= stepsCount!!) {
                currentStepProgressBar++
                log(Enums.LogType.Debug, TAG, "updateWelcome()")
                activity.runOnUiThread {
                    updateStep()
                }

            }
        }

    //endregion == welcome ============

    //region == observers ==========

    private inner class CouponRegistrationObserver : Observer<UserEntity?> {
        override fun onChanged(userData: UserEntity?) {
            if (userData != null) {
                activity.runOnUiThread {
                    currentStepProgressBar++
                    updateStep()
                }
            }
        }
    }

    private inner class PayRegistrationObserver : Observer<UserEntity?> {
        override fun onChanged(userData: UserEntity?) {
            payProgramFragment.payProgramAfterResponse(userData)
        }
    }

    private inner class SkipRegistrationObserver : Observer<UserEntity?> {
        override fun onChanged(userData: UserEntity?) {
            skipRegistrationCallback(userData)
        }
    }

    private inner class LocalFixedParametersObserver : Observer<FixedParametersEntity?> {
        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            log(Enums.LogType.Debug, TAG, "LocalFixedParametersObserver()")

            if (fixedParameters == null) {
                return
            }

            fixedParametersData = FixedParameters.init(fixedParameters)

            loadFragment()
        }
    }

    private inner class SignupObserver : Observer<UserEntity?> {
        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "SignupObserver() user = {${user}}")

            GlobalScope.launch {
                if (user != null) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
                    roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(user)!!
                    activity.preferences?.setLong("roomUID", roomUID!!, false)

                    loggingUser = user

                    if (currentStepProgressBar <= stepsCount!!) {
                        activity.runOnUiThread {
                            currentStepProgressBar++
                            updateStep()
                        }
                    }
                }
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

    override fun attachBinding(list: MutableList<ActivitySignupBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivitySignupBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======

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