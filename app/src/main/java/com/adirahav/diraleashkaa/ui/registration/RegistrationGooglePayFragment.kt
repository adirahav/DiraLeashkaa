package com.adirahav.diraleashkaa.ui.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.GooglePayProgramTypeEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import com.adirahav.diraleashkaa.databinding.FragmentRegistrationGooglePayBinding
import com.adirahav.diraleashkaa.ui.contactus.ContactUsActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import com.airbnb.paris.extensions.style
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class RegistrationGooglePayFragment : Fragment(),
    RegistrationGooglePayProgramsAdapter.OnProgramAdapter {

    //region == companion ==========

    companion object {
        private const val TAG = "RegistrationGooglePayFragment"
        //private val addToGoogleWalletRequestCode = 1000
        fun newInstance() = RegistrationGooglePayFragment()
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _signupActivity: SignUpActivity? = null
    internal var _registrationActivity: RegistrationActivity? = null
    internal var isSignUpActivity: Boolean? = null

    // layout
    internal lateinit var layout: FragmentRegistrationGooglePayBinding

    // user data
    var userData: UserEntity? = null

    // programs
    private var programs : List<GooglePayProgramTypeEntity>? = null

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentRegistrationGooglePayBinding.inflate(layoutInflater)
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
        userData = if (isSignUpActivity!!) _signupActivity?.userData else _registrationActivity?.userData

        // hide keyboard
        //hideKeyboard(requireContext())

        // Check Google Pay availability
        if (isSignUpActivity!!) {
            _signupActivity?.googlePayViewModel?.canUseGooglePay?.observe(viewLifecycleOwner, Observer(::setGooglePayAvailable))
        }
        else {
            _registrationActivity?.googlePayViewModel?.canUseGooglePay?.observe(viewLifecycleOwner, Observer(::setGooglePayAvailable))
        }

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
        setRoomStrings()

        // in-app products
        var purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases -> // Handle purchase updates, acknowledge purchases, etc.
            handlePurchaseUpdates(billingResult, purchases)
        }

        val billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener) // Implement this listener
                .enablePendingPurchases()
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Utilities.log(Enums.LogType.Debug, "ADITEST", "onBillingSetupFinished(): billingResult = ${billingResult}")
            }

            override fun onBillingServiceDisconnected() {
                Utilities.log(Enums.LogType.Debug, "ADITEST", "onBillingServiceDisconnected()")
            }
        })

        val skuList: List<String> = mutableListOf("product_id_1", "product_id_2")
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP) // or SkuType.SUBS for subscriptions
                .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Handle the response and display the available products
        }

        /*val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails) // SkuDetails for the selected product
                .build()


        val responseCode: BillingResult = billingClient.launchBillingFlow(
                if (isSignUpActivity!!) {
                    _signupActivity!!
                }
                else {
                    _registrationActivity!!
                },
                flowParams)

        Utilities.log(Enums.LogType.Debug, "ADITEST", "responseCode = ${responseCode}")*/
    }

    fun initData() {

        // programs
        programs =
            if (isSignUpActivity!!)
                _signupActivity?.fixedParametersData?.googlePayObject?.programTypes
            else
                _registrationActivity?.fixedParametersData?.googlePayObject?.programTypes

        var programsAdapter: RegistrationGooglePayProgramsAdapter? = null
        programsAdapter = RegistrationGooglePayProgramsAdapter(requireContext(), this, this@RegistrationGooglePayFragment)
        layout.programsList.adapter = programsAdapter
        programsAdapter.setItems(programs)

        // contact us
        if (isSignUpActivity!!) {
            layout.contactUs.visibility = GONE
        }
        else {
            Utilities.setTextViewHtml(layout.contactUs, "signup_contact_us")
        }

        // skip
        if (isSignUpActivity!!) {
            Utilities.setTextViewHtml(layout.skip, "signup_code_skip")
            layout.skip.visibility = VISIBLE
        }
        else {
            layout.skip.visibility = GONE
        }

        // send / next
        Utilities.setButtonDisable(
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.next
            else
                _registrationActivity?.layout?.buttons?.send
        )
    }

    fun initEvents() {

        // google pay
        if (isSignUpActivity!!) {
            _signupActivity?.layout?.buttons?.googlePayButton?.root?.setOnClickListener {
                if (selectedPosition != null) {
                    requestPayment(programs?.get(selectedPosition!!))
                }
            }
        }
        else {
            _registrationActivity?.layout?.buttons?.googlePayButton?.root?.setOnClickListener {
                if (selectedPosition != null) {
                    requestPayment(programs?.get(selectedPosition!!))
                }
            }
        }



        // add to google wallet
        //layout.addToGoogleWalletButton = layout.addToGoogleWalletButton.root
        //layout.addToGoogleWalletButton.setOnClickListener { requestSavePass() }

        // register with coupon
        val isGooglePayAvailable =
            if (isSignUpActivity!!)
                _signupActivity?.fixedParametersData?.googlePayObject?.isAvailable ?: false
            else
                _registrationActivity?.fixedParametersData?.googlePayObject?.isAvailable ?: false

        val supportCoupons =
            if (isSignUpActivity!!)
                _signupActivity?.fixedParametersData?.appVersionArray?.find { it.key == "support_coupons" }?.value?.toBoolean() ?: false
            else
                _registrationActivity?.fixedParametersData?.appVersionArray?.find { it.key == "support_coupons" }?.value?.toBoolean() ?: false

        if (isGooglePayAvailable && supportCoupons) {
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
    private fun handlePurchaseUpdates(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val responseCode = billingResult.responseCode
        when (responseCode) {
            BillingClient.BillingResponseCode.OK ->             // Purchase was successful, process it
                if (purchases != null) {
                    for (purchase in purchases!!) {
                        // Handle the purchase, possibly by acknowledging it
                        //handlePurchase(purchase)
                    }
                }

            BillingClient.BillingResponseCode.USER_CANCELED -> {}
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {}
            else -> {}
        }
    }

    //endregion == in-app products ====

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setTextViewString(layout.title, "signup_google_pay_label")
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
                    _signupActivity?.viewModel?.skipRegistration(userData)
                }
                else {
                    //_signupActivity?.viewModel?.couponRegistration(requireContext(), userData, String(_code))
                }
            }
            else {
                //_registrationActivity?.viewModel?.couponRegistration(requireContext(), userData, String(_code))
            }
        }
        else {
            /*var registrationCodeValidation = couponRegistrationModel()
            registrationCodeValidation.isValidCode = false
            registrationCodeValidation.errorCode = errorCode
            afterCheckCouponCode(registrationCodeValidation)*/
        }
    }

    /*fun afterCheckCouponCode(registrationCodeValidation: CouponRegistrationModel?) {

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        var isValid = registrationCodeValidation?.isValidCode ?: false
        var errorCode = registrationCodeValidation?.errorCode ?: Enums.CodeError.SERVER_ERROR.errorCode

        if (isValid) {
            entities["registration_code_verified"] = true
            entities["registration_expired_time"] = registrationCodeValidation?.registrationExpireDate
            entities["subscriber_type"] = registrationCodeValidation?.subscriberType
        }

        map["isValid"] = isValid
        map["entities"] = entities

        runBlocking {
            Utilities.hideKeyboard(context)
        }

        if (isValid) {
            runBlocking {
                codeError?.visibility = View.INVISIBLE
                if (isSignUpActivity!!) {
                    _signupActivity?.updateUser(map)
                }
                else {
                    Utilities.setButtonDisable(_registrationActivity?.send)
                    _registrationActivity?.updateUser(map)
                }
            }
        }
        else {
            codeError?.visibility = View.VISIBLE
            codeError?.text = String.format(
                resources.getString(Utilities.findStringByName("signup_code_error_${errorCode}")),
                CODE_SIZE
            )

            if (isSignUpActivity!!) {
                Utilities.setButtonEnable(_signupActivity?.next)
            }
            else {
                Utilities.setButtonEnable(_registrationActivity?.send)
            }

            Utilities.log(Enums.LogType.Warning, TAG, "afterCheckCouponCode(): errorCode = ${errorCode} ; errorDesc = ${codeError?.text}", userData)
        }
    }*/

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            activity?.runOnUiThread {
                if (isSignUpActivity!!) {
                    _signupActivity?.layout?.buttons?.googlePayButton?.root?.visibility = VISIBLE
                    _signupActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = false
                }
                else {
                    _registrationActivity?.layout?.buttons?.googlePayButton?.root?.visibility = VISIBLE
                    _registrationActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = false
                }

            }
        }
        else {
            activity?.runOnUiThread {
                if (isSignUpActivity!!)
                    _signupActivity?.layout?.buttons?.googlePayButton?.root?.visibility = GONE
                else
                    _registrationActivity?.layout?.buttons?.googlePayButton?.root?.visibility = GONE

                layout.googlePayMessage.text = Utilities.getRoomString("google_pay_status_unavailable")
                layout.googlePayMessage.style(R.style.formError)
                layout.googlePayMessage.visibility = VISIBLE
            }

            Utilities.log(Enums.LogType.Error, TAG, "setGooglePayAvailable(): Error = ${Utilities.getRoomString("google_pay_status_unavailable")}", userData)
        }
    }

    /**
     * If the Google Wallet API is available, show the button to Add to Google Wallet. Please adjust to fit
     * in with your current user flow.
     *
     * @param available
     */
    /*private fun setAddToGoogleWalletAvailable(available: Boolean) {
        if (available) {
            layout.passContainer.visibility = View.VISIBLE
        } else {
            Toast.makeText(
                this,
                R.string.google_wallet_status_unavailable,
                Toast.LENGTH_LONG).show()
        }
    }*/

    private fun requestPayment(program: GooglePayProgramTypeEntity?) {

        Utilities.log(Enums.LogType.Notify, TAG, "GooglePay requestPayment(): period = ${program?.durationValue}${program?.durationUnit} ; price = ${program?.price}", userData)

        // Disables the button to prevent multiple clicks.
        if (isSignUpActivity!!)
            _signupActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = false
        else
            _registrationActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val priceCents = (program?.price?.times(100))?.toLong() ?: 0
        val shippingCostCents = 0L
        val task =
            if (isSignUpActivity!!)
                _signupActivity?.googlePayViewModel?.getLoadPaymentDataTask(priceCents + shippingCostCents)
            else
                _registrationActivity?.googlePayViewModel?.getLoadPaymentDataTask(priceCents + shippingCostCents)

        task?.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                completedTask.result.let(::handlePaymentSuccess)
            } else {
                when (val exception = completedTask.exception) {
                    is ResolvableApiException -> {
                        Utilities.log(Enums.LogType.Notify, TAG, "GooglePay requestPayment(): ResolvableApiException. ${exception.resolution}", userData)
                        resolvePaymentForResult.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    }
                    is ApiException -> {
                        Utilities.log(Enums.LogType.Error, TAG, "GooglePay requestPayment(): ApiException. Error = ${exception.message}", userData)
                        handleError(exception.statusCode, exception.message)
                    }
                    else -> {
                        Utilities.log(Enums.LogType.Error, TAG, "GooglePay requestPayment(): UnexpectedException. Error = Unexpected non API. exception when trying to deliver the task result to an activity!", userData)
                        handleError(
                            CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                    " exception when trying to deliver the task result to an activity!"
                        )
                    }
                }
            }

            // Re-enables the Google Pay payment button.
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = true
            else
                _registrationActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = true

        }
    }

    // Handle potential conflict from calling loadPaymentData
    private val resolvePaymentForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            result: ActivityResult ->
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK ->
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                }

            AppCompatActivity.RESULT_CANCELED -> {
                // The user cancelled the payment attempt
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson()

        Utilities.log(Enums.LogType.Notify, TAG, "GooglePay handlePaymentSuccess(): paymentInformation = ${paymentInformation}.\n\nprograms = ${programs?.get(selectedPosition!!)?.durationValue}${programs?.get(selectedPosition!!)?.durationUnit}, ${programs?.get(selectedPosition!!)?.price} NIS", userData)

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")

            activity?.runOnUiThread {
                layout.googlePayMessage.text = String.format(
                    Utilities.getRoomString("google_pay_payments_show_name"),
                    billingName)
                layout.googlePayMessage.style(R.style.formSuccess)
                layout.googlePayMessage.visibility = VISIBLE
            }

            if (isSignUpActivity!!)
                _signupActivity?.viewModel?.googlePayRegistration(requireContext(), userData, programs?.get(selectedPosition!!)?.uuid)
            else
                _registrationActivity?.viewModel?.googlePayRegistration(requireContext(), userData, programs?.get(selectedPosition!!)?.uuid)

            val token = paymentMethodData
                .getJSONObject("tokenizationData")
                .getString("token")

            Utilities.log(Enums.LogType.Debug, TAG, "handlePaymentSuccess(): ${String.format(
                Utilities.getRoomString("google_pay_payments_show_name"),
                billingName)}. Google Pay token = $token", userData)

        } catch (error: JSONException) {
            Utilities.log(Enums.LogType.Error, TAG, "handlePaymentSuccess(): Error = $error", userData)
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int, message: String?) {
        Utilities.log(Enums.LogType.Error, TAG, "handleError(): Google Pay API error. Error code: ${statusCode}, Message: $message", userData)
    }

    /*private fun requestSavePass() {

        // Disables the button to prevent multiple clicks.
        //layout.addToGoogleWalletButton.root.isClickable = false

        if (isSignUpActivity!!) {
            if (_signupActivity?.googlePayViewModel?.savePassesJwt != null) {
                _signupActivity!!.googlePayViewModel.savePassesJwt(
                    _signupActivity!!.googlePayViewModel.genericObjectJwt,
                    requireActivity(),
                    addToGoogleWalletRequestCode
                )
            }
        }
        else {
            if (_registrationActivity?.googlePayViewModel?.savePassesJwt != null) {
                _registrationActivity!!.googlePayViewModel.savePassesJwt(
                    _registrationActivity!!.googlePayViewModel.genericObjectJwt,
                    requireActivity(),
                    addToGoogleWalletRequestCode
                )
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == addToGoogleWalletRequestCode) {
            when (resultCode) {
                RESULT_OK -> Toast
                    .makeText(this, getString(R.string.google_wallet_add_success), Toast.LENGTH_LONG)
                    .show()

                RESULT_CANCELED -> {
                    // Save canceled
                }

                PayClient.SavePassesResult.SAVE_ERROR -> data?.let { intentData ->
                    val apiErrorMessage = intentData.getStringExtra(PayClient.EXTRA_API_ERROR_MESSAGE)
                    handleError(resultCode, apiErrorMessage)
                }

                else -> handleError(
                    CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                            " exception when trying to deliver the task result to an activity!"
                )
            }

            // Re-enables the Google Pay payment button.
            addToGoogleWalletButton.isClickable = true

        }
    }*/

    // selected holder
    var selectedPosition: Int? = null
    override fun onProgramClicked(property: GooglePayProgramTypeEntity?, position: Int) {
        if (isSignUpActivity!!)
            _signupActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = true
        else
            _registrationActivity?.layout?.buttons?.googlePayButton?.root?.isClickable = true

        selectedPosition = position
    }

    // server callback
    fun googlePayCallback(registrationModel: RegistrationModel?) {

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        val isValid = registrationModel?.success ?: false
        val errorCode = registrationModel?.error?.errorCode ?: Enums.CodeError.SERVER_ERROR.errorCode

        if (isValid) {
            entities["registration_expired_time"] = registrationModel?.data?.registration?.registrationExpireDate
            entities["subscriber_type"] = registrationModel?.data?.registration?.subscriberType
        }

        map["isValid"] = isValid
        map["entities"] = entities

        runBlocking {
            Utilities.hideKeyboard(context)
        }

        if (isValid) {
            runBlocking {
                layout.googlePayMessage.visibility = View.INVISIBLE
                if (isSignUpActivity!!) {
                    _signupActivity?.insertUpdateUser(map)
                }
                else {
                    _registrationActivity?.updateUser(map)
                }
            }
        }
        else {

            layout.googlePayMessage.visibility = VISIBLE
            layout.googlePayMessage.text = Utilities.getRoomString("signup_code_error_${errorCode}")
            layout.googlePayMessage.style(R.style.formError)

            if (isSignUpActivity!!) {
                Utilities.setButtonEnable(_signupActivity?.layout?.buttons?.next)
            }
            else {
                Utilities.setButtonEnable(_registrationActivity?.layout?.buttons?.send)
            }

            Utilities.log(Enums.LogType.Warning,
                TAG, "afterCheckCouponCode(): errorCode = $errorCode ; errorDesc = ${layout.googlePayMessage.text}", userData)
        }
    }
}