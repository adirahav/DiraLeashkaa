package com.adirahav.diraleashkaa.ui.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.hideKeyboard
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import com.adirahav.diraleashkaa.databinding.FragmentRegistrationBetaCodeBinding
import com.adirahav.diraleashkaa.ui.contactus.ContactUsActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import kotlinx.coroutines.runBlocking

class RegistrationBetaCodeFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "RegistrationBetaCodeFragment"
        private const val CODE_SIZE = 6

        fun newInstance() = RegistrationBetaCodeFragment()
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _signupActivity: SignUpActivity? = null
    var _registrationActivity: RegistrationActivity? = null
    var isSignUpActivity: Boolean? = null

    // layout
    private var layout: FragmentRegistrationBetaCodeBinding? = null

    // user data
    var userData: UserEntity? = null

    // code
    private var codeAdapter: RegistrationBetaCodeAdapter? = null
    var code = CharArray(CODE_SIZE)

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = FragmentRegistrationBetaCodeBinding.inflate(layoutInflater)

        initGlobal()
        initData()
        initEvents()

        return layout?.root
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
        hideKeyboard(requireContext())

        // adjust container padding
        if (isSignUpActivity == true) {
            layout?.container?.setPadding(
                resources.getDimension(R.dimen.padding).toInt(),
                0,
                resources.getDimension(R.dimen.padding).toInt(),
                0
            )
        }

        // strings
        setRoomStrings()
    }

    fun initData() {

        // code
        setCode()

        // error code

        // contact us
        if (isSignUpActivity!!) {
            layout?.contactUs?.visibility = GONE
        }
        else {
            Utilities.setTextViewHtml(layout?.contactUs, "signup_contact_us")
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

        // contact us
        layout?.contactUs?.setOnClickListener {
            ContactUsActivity.start(AppApplication.context, Enums.ContactUsPageType.MAIL_FORM)
        }

    }

    //endregion == initialize =========

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setTextViewString(layout?.title, "registration_beta_code_label")
        Utilities.setTextViewString(layout?.contactUs, "signup_contact_us")
    }

    //endregion == strings ============

    fun setCode() {

        // code
        codeAdapter = RegistrationBetaCodeAdapter(this@RegistrationBetaCodeFragment)
        layout?.codeList?.adapter = codeAdapter
        codeAdapter!!.setItems(code)
    }

    fun submitForm() {
        var isValid = true
        var errorCode: Enums.CodeError? = null
        val _code = CharArray(CODE_SIZE)

        for (i in 0..CODE_SIZE.minus(1)) {
            val _char = ((layout?.codeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text

            if (_char.isNullOrEmpty()) {
                isValid = false
                errorCode = Enums.CodeError.WRONG_FORMAT
                layout?.codeList?.getChildAt(i)?.requestFocus()
                break
            }
            else {
                _code[i] = _char.toString().first()
            }
        }

        if (isValid) {
            if (isSignUpActivity!!) {
                _signupActivity?.viewModel?.betaRegistration(userData, String(_code))
            }
            else {
                _registrationActivity?.viewModel?.betaRegistration(userData, String(_code))
            }
        }
        else {
            val registrationCodeValidation = RegistrationModel(
                success = false,
                data = null,
                error = APIResponseErrorEntity(errorCode = errorCode.toString(), errorMessage = "")
            )
            betaCodeCallback(registrationCodeValidation)
        }
    }

    fun betaCodeCallback(registrationCodeValidation: RegistrationModel?) {

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        val isValid = registrationCodeValidation?.success ?: false
        val errorCode = if (registrationCodeValidation?.error?.errorCode != null)
                            Enums.CodeError.valueOf(registrationCodeValidation.error!!.errorCode).errorCode
                        else
                            Enums.CodeError.SERVER_ERROR.errorCode

        if (isValid) {
            entities["registration_expired_time"] = registrationCodeValidation?.data?.registration?.registrationExpireDate
            entities["subscriber_type"] = registrationCodeValidation?.data?.registration?.subscriberType
        }

        map["isValid"] = isValid
        map["entities"] = entities

        hideKeyboard(requireContext())

        if (isValid) {
            runBlocking {
                layout?.codeError?.visibility = View.INVISIBLE
                if (isSignUpActivity!!) {
                    _signupActivity?.insertUpdateUser(map)
                }
                else {
                    Utilities.setButtonDisable(_registrationActivity?.layout?.buttons?.send)
                    _registrationActivity?.updateUser(map)
                }
            }
        }
        else {
            Utilities.log(
                Enums.LogType.Debug,
                TAG,
                "registration_beta_code_error_${errorCode}"
            )

            layout?.codeError?.visibility = VISIBLE
            layout?.codeError?.text =
                if (Utilities.getRoomString("registration_beta_code_error_${errorCode}").isNotEmpty())
                    String.format(Utilities.getRoomString("registration_beta_code_error_${errorCode}"), CODE_SIZE)
                else
                    ""

            if (isSignUpActivity!!) {
                Utilities.setButtonEnable(_signupActivity?.layout?.buttons?.next)
            }
            else {
                Utilities.setButtonEnable(_registrationActivity?.layout?.buttons?.send)
            }

            Utilities.log(Enums.LogType.Warning, TAG, "betaCodeCallback(): errorCode = $errorCode ; errorDesc = ${layout?.codeError?.text}", userData)
        }
    }
}