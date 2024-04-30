package com.adirahav.diraleashkaa.ui.registration

import android.icu.text.DateTimePatternGenerator.PatternInfo.OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.PayProgramTypeEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.FragmentRegistrationPayProgramBinding
import com.adirahav.diraleashkaa.ui.contactus.ContactUsActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.runBlocking


class RegistrationPayProgramFragment : Fragment(),
    RegistrationPayProgramsAdapter.OnProgramAdapter {

    //region == companion ==========

    companion object {
        private const val TAG = "RegistrationPayFragment"
        fun newInstance() = RegistrationPayProgramFragment()
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _signupActivity: SignUpActivity? = null
    internal var _registrationActivity: RegistrationActivity? = null
    internal var isSignUpActivity: Boolean? = null

    // layout
    internal lateinit var layout: FragmentRegistrationPayProgramBinding

    // user data
    var registerUser: UserEntity? = null

    // in-app products
    private lateinit var billingClient: BillingClient
    private lateinit var consoleProgramDetails: ProductDetails
    private lateinit var purchase: Purchase
    private var _programs : List<PayProgramTypeEntity>? = null
    private var _consolePrograms : List<ProductDetails>? = null

    //private var programs : List<PayProgramTypeEntity>? = null

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentRegistrationPayProgramBinding.inflate(layoutInflater)
        /*activity?.setContentView(layout.root)*/

        //val view = inflater.inflate(R.layout.fragment_google_pay, container, false)
        //activity?.setContentView(view)



        return layout.root
    }

    override fun onResume() {
        initGlobal()
        initData()
        initEvents()

        super.onResume()
    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initGlobal() {
        // activity
        isSignUpActivity = activity?.javaClass?.simpleName.equals("SignUpActivity")

        _signupActivity = if (isSignUpActivity!!) activity as SignUpActivity else null
        _registrationActivity = if (!isSignUpActivity!!) activity as RegistrationActivity else null

        // user data
        registerUser = if (isSignUpActivity!!) _signupActivity?.loggingUser else _registrationActivity?.userData

        // hide keyboard
        //hideKeyboard(requireContext())

        // Check Google Pay availability
        /*if (isSignUpActivity!!) {
            _signupActivity?.payViewModel?.canUsePay?.observe(viewLifecycleOwner, Observer(::setPayAvailable))
        }
        else {
            _registrationActivity?.payViewModel?.canUsePay?.observe(viewLifecycleOwner, Observer(::setPayAvailable))
        }*/

        // adjust container padding
        if (isSignUpActivity == true) {
            layout.container.setPadding(
                resources.getDimension(R.dimen.padding).toInt(),
                0,
                resources.getDimension(R.dimen.padding).toInt(),
                0
            )
        }

        // strings
        setPhrases()

        // in-app products
        billingSetup()
    }

    fun initData() {

        _programs =
                if (isSignUpActivity!!)
                    _signupActivity?.fixedParametersData?.payProgramsObject?.programTypes
                else
                    _registrationActivity?.fixedParametersData?.payProgramsObject?.programTypes

        // contact us
        /*if (isSignUpActivity!!) {
            layout.contactUs.visibility = GONE
        }
        else {
            Utilities.setTextViewHtml(layout.contactUs, "signup_contact_us")
        }*/
        layout.contactUs.visibility = GONE  // TODO

        // skip
        if (isSignUpActivity!!) {
            Utilities.setTextViewHtml(layout.skip, "signup_code_skip")
            layout.skip.visibility = VISIBLE
        }
        else {
            layout.skip.visibility = GONE
        }

        // pay
        Utilities.setButtonDisable(
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.pay
            else
                _registrationActivity?.layout?.buttons?.pay
        )
    }

    fun initEvents() {

        // pay
        if (isSignUpActivity!!) {
            _signupActivity?.layout?.buttons?.pay?.setOnClickListener {
                if (selectedPosition != null) {
                    makePurchase(_programs?.get(selectedPosition!!)!!.programID)
                }
            }
        }
        else {
            _registrationActivity?.layout?.buttons?.pay?.setOnClickListener {
                if (selectedPosition != null) {
                    makePurchase(_programs?.get(selectedPosition!!)!!.programID)
                }
            }
        }

        // register with coupon
        val isPayAvailable =
            if (isSignUpActivity!!)
                _signupActivity?.fixedParametersData?.payProgramsObject?.isAvailable ?: false
            else
                _registrationActivity?.fixedParametersData?.payProgramsObject?.isAvailable ?: false

        val supportCoupons =
            if (isSignUpActivity!!)
                _signupActivity?.fixedParametersData?.appVersionArray?.find { it.key == "supportCoupons" }?.value?.toBoolean() ?: false
            else
                _registrationActivity?.fixedParametersData?.appVersionArray?.find { it.key == "supportCoupons" }?.value?.toBoolean() ?: false

        if (isPayAvailable && supportCoupons) {
            layout.registerWithCoupon.visibility = VISIBLE
            Utilities.setTextViewHtml(layout.registerWithCoupon, "signup_register_with_coupon")
        }
        else {
            layout.registerWithCoupon.visibility = GONE
        }

        layout.registerWithCoupon.setOnClickListener {
            if (isSignUpActivity!!) {
                _signupActivity?.forceLoadFragment(Enums.RegistrationPageType.COUPON_CODE)
            } else {
                _registrationActivity?.forceLoadFragment(Enums.RegistrationPageType.COUPON_CODE)
            }
        }

        // contact us
        layout.contactUs.setOnClickListener {
            ContactUsActivity.start(AppApplication.context, Enums.ContactUsPageType.MAIL_FORM)
        }

        // skip
        layout.skip.setOnClickListener {
            _signupActivity?.submitNext(view = null)
        }
    }

    //endregion == initialize =========

    //region == in-app products ====
    private var retryCount = 0
    private fun billingSetup() {
        billingClient = BillingClient.newBuilder(if (isSignUpActivity!!) { _signupActivity!! } else { _registrationActivity!! })
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                retryCount = 0
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Utilities.log(Enums.LogType.Debug, TAG, "billingSetup(): OnBillingSetupFinish connected")

                    activity?.runOnUiThread {
                        val productList: MutableList<QueryProductDetailsParams.Product> = ArrayList()

                        _programs?.forEach { program ->
                            val product = QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(program.programID)
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build()
                            Utilities.log(Enums.LogType.Debug, "ADITEST", program.programID)
                            productList.add(product)
                        }

                        queryProduct(productList)

                        var programsAdapter: RegistrationPayProgramsAdapter? = null
                        programsAdapter = RegistrationPayProgramsAdapter(requireContext(), this@RegistrationPayProgramFragment, this@RegistrationPayProgramFragment)
                        layout.programsList.adapter = programsAdapter
                        programsAdapter.setItems(_programs)
                    }

                    reloadPurchase()
                }
                else {
                    Utilities.log(Enums.LogType.Debug, TAG, "billingSetup(): OnBillingSetupFinish failed")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                retryCount++
                if (retryCount <= 3) {
                    billingClient.startConnection(this)
                }

                Utilities.log(Enums.LogType.Error, TAG, "billingSetup(): onBillingServiceDisconnected connection lost")
            }
        })
    }

    private fun queryProduct(productList: List<QueryProductDetailsParams.Product>) {
        val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

        if (billingClient.isFeatureSupported(PRODUCT_DETAILS).responseCode == OK) {
             billingClient.queryProductDetailsAsync(params) { billingResult, consolePrograms ->
                _consolePrograms = consolePrograms
                /*if (consolePrograms.isNotEmpty()) {
                    consolePrograms?.forEach ({ consoleProgram ->
                        _programs?.find { it.programID == consoleProgram.productId }?.price = consoleProgram.oneTimePurchaseOfferDetails?.formattedPrice
                    })
                }
                else {
                    Utilities.log(Enums.LogType.Debug, TAG, "queryProduct(): onProductDetailsResponse: No products")
                }*/
                if (consolePrograms.isEmpty()) {
                    activity?.runOnUiThread {
                        Utilities.log(Enums.LogType.Debug, TAG, "queryProduct(): onProductDetailsResponse: No products")
                        layout.payMessage.visibility = View.VISIBLE
                        layout.payMessage.text = Utilities.getLocalPhrase("signup_pay_program_error")
                    }
                }
            }
        }
        else {
            activity?.runOnUiThread {
                Utilities.log(Enums.LogType.Debug, TAG, "queryProduct(): onProductDetailsResponse: Device not supported")
                layout.payMessage.visibility = View.VISIBLE
                layout.payMessage.text = Utilities.getLocalPhrase("signup_pay_program_error")
            }
        }


    }

    fun makePurchase(programID: String) {
        val consoleProgram = _consolePrograms?.find { it.productId == programID }

        Utilities.log(Enums.LogType.Notify, TAG, "makePurchase(): programID = ${programID}", registerUser)

        if (consoleProgram != null) {
            val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(consoleProgram)
                                            .build()
                            )
                    )
                    .build()

            billingClient.launchBillingFlow(
                    if (isSignUpActivity!!) { _signupActivity!! } else { _registrationActivity!! },
                    billingFlowParams)

            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.pay?.isClickable = false
            else
                _registrationActivity?.layout?.buttons?.pay?.isClickable = false

            Utilities.setButtonDisable(
                    if (isSignUpActivity!!)
                        _signupActivity?.layout?.buttons?.pay
                    else
                        _registrationActivity?.layout?.buttons?.pay
            )
        }
    }

    private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        completePurchase(purchase)
                    }
                }
                else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Utilities.log(Enums.LogType.Notify, TAG, "PurchasesUpdatedListener(): onPurchasesUpdated: Purchase Canceled")
                }
                else {
                    Utilities.log(Enums.LogType.Error, TAG, "PurchasesUpdatedListener(): onPurchasesUpdated: Error")
                }
            }

    private fun completePurchase(item: Purchase) {
        purchase = item
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            Utilities.log(Enums.LogType.Notify, TAG, "completePurchase()", registerUser)
            activity?.runOnUiThread {
                if (isSignUpActivity!!) {
                    _signupActivity?.layout?.buttons?.pay?.isEnabled = false
                }
                else {
                    _registrationActivity?.layout?.buttons?.pay?.isEnabled = false
                }

                if (isSignUpActivity!!)
                    _signupActivity?.viewModel?.payProgramRegistration(requireContext(), registerUser, _programs?.get(selectedPosition!!)?._id)
                else
                    _registrationActivity?.viewModel?.payProgramRegistration(requireContext(), registerUser, _programs?.get(selectedPosition!!)?._id)

            }
        }
    }
    private fun reloadPurchase() {

        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

        billingClient.queryPurchasesAsync(
                queryPurchasesParams,
                purchasesListener
        )
    }

    private val purchasesListener =
            PurchasesResponseListener { billingResult, purchases ->
                if (purchases.isNotEmpty()) {
                    purchase = purchases.first()

                    if (isSignUpActivity!!) {
                        _signupActivity?.layout?.buttons?.pay?.isEnabled = false
                    }
                    else {
                        _registrationActivity?.layout?.buttons?.pay?.isEnabled = false
                    }
                }
            }

    //endregion == in-app products ====

    //region == strings ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setTextViewString(layout.title, "signup_pay_program_label")
        Utilities.setTextViewString(layout.registerWithCoupon, "signup_register_with_coupon")
        Utilities.setTextViewString(layout.contactUs, "signup_contact_us")
        Utilities.setTextViewString(layout.skip, "signup_code_skip")
    }

    //endregion == strings ============

    fun submitForm(skip: Boolean) {
        var isValid = true
        var errorCode: Int? = null
        //var _code = CharArray(CODE_SIZE)

        if (!skip) {
            /*for (i in 0..CODE_SIZE.minus(1)) {
                val _char = ((codeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text

                if (_char.isNullOrEmpty()) {
                    isValid = false
                    errorCode = 1
                    codeList!!.getChildAt(i)?.requestFocus()
                    break
                }
                else {
                    _code.set(i, _char?.toString()?.first() ?: Char(0))
                }
            }*/
        }

        if (isValid) {
            if (isSignUpActivity!!) {
                if (skip) {
                    _signupActivity?.viewModel?.skipRegistration(registerUser)
                }
                else {
                    //_signupActivity?.viewModel?.couponRegistration(requireContext(), registerUser, String(_code))
                }
            }
            else {
                //_registrationActivity?.viewModel?.couponRegistration(requireContext(), registerUser, String(_code))
            }
        }
        else {
            /*var registrationCodeValidation = couponRegistrationModel()
            registrationCodeValidation.isValidCode = false
            registrationCodeValidation.errorCode = errorCode
            afterCheckCouponCode(registrationCodeValidation)*/
        }
    }

    // selected holder
    var selectedPosition: Int? = null
    override fun onProgramClicked(property: PayProgramTypeEntity?, position: Int) {
        if (isSignUpActivity!!)
            _signupActivity?.layout?.buttons?.pay?.isClickable = true
        else
            _registrationActivity?.layout?.buttons?.pay?.isClickable = true

        Utilities.setButtonEnable(
                if (isSignUpActivity!!)
                    _signupActivity?.layout?.buttons?.pay
                else
                    _registrationActivity?.layout?.buttons?.pay
        )

        selectedPosition = position
    }

    // server callback
    fun payProgramAfterResponse(userData: UserEntity?) {
        //PAYPROGRAM-4
        Utilities.hideKeyboard(requireContext())

        if (userData != null) {

            runBlocking {
                layout.payMessage.visibility = View.INVISIBLE
                if (isSignUpActivity!!) {
                    userData.roomUID = _signupActivity?.roomUID
                    //_signupActivity?.signupLocalUser(userData)
                }
                else {
                    //PAYPROGRAM-5
                    Utilities.setButtonDisable(_registrationActivity?.layout?.buttons?.send)
                    userData.roomUID = _registrationActivity?.roomUID
                    _registrationActivity?.updateLocalUser(userData)
                }
            }
        }
        else {
            String.format(Utilities.getLocalPhrase("signup_code_error"))

            if (isSignUpActivity!!) {
                Utilities.setButtonEnable(_signupActivity?.layout?.buttons?.next)
            }
            else {
                Utilities.setButtonEnable(_registrationActivity?.layout?.buttons?.send)
            }

            Utilities.log(Enums.LogType.Warning, TAG, "payProgramAfterResponse(): ${layout.payMessage.text}", registerUser)
        }
    }
}