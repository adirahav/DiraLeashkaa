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
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.FragmentRegistrationCouponCodeBinding
import com.adirahav.diraleashkaa.ui.contactus.ContactUsActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import kotlinx.coroutines.runBlocking

class RegistrationCouponCodeFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "RegistrationCouponCodeFragment"
        private const val CODE_SIZE = 6

        fun newInstance() = RegistrationCouponCodeFragment()
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _signupActivity: SignUpActivity? = null
    var _registrationActivity: RegistrationActivity? = null
    var isSignUpActivity: Boolean? = null

    // layout
    internal var layout: FragmentRegistrationCouponCodeBinding? = null

    // user data
    var registerUser: UserEntity? = null

    // code
    private var codeAdapter: RegistrationCouponCodeAdapter? = null
    var code = CharArray(CODE_SIZE)

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = FragmentRegistrationCouponCodeBinding.inflate(layoutInflater)



        return layout?.root
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
        setPhrases()
    }

    fun initData() {

        // code
        setCode()

        // error code

        // register with google pay
        Utilities.setTextViewHtml(layout?.registerWithPayProgram, "signup_register_with_pay_program")

        // contact us
        if (isSignUpActivity!!) {
            layout?.contactUs?.visibility = GONE
        }
        else {
            Utilities.setTextViewHtml(layout?.contactUs, "signup_contact_us")
        }


        // skip
        if (isSignUpActivity!!) {
            Utilities.setTextViewHtml(layout?.skip, "signup_code_skip")
            layout?.skip?.visibility = VISIBLE
        }
        else {
            layout?.skip?.visibility = GONE
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

        // register with pay
        layout?.registerWithPayProgram?.setOnClickListener {
            if (isSignUpActivity!!) {
                _signupActivity?.forceLoadFragment(Enums.RegistrationPageType.PAY_PROGRAM)
            } else {
                _registrationActivity?.forceLoadFragment(Enums.RegistrationPageType.PAY_PROGRAM)
            }
        }

        // contact us
        layout?.contactUs?.setOnClickListener {
            ContactUsActivity.start(AppApplication.context, Enums.ContactUsPageType.MAIL_FORM)
        }

        // skip
        layout?.skip?.setOnClickListener {
            //SKIP-1
            _signupActivity?.submitNext(null)
        }

    }

    //endregion == initialize =========

    //region == strings ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setTextViewString(layout?.title, "signup_code_label")
        Utilities.setTextViewString(layout?.registerWithPayProgram, "signup_register_with_pay_program")
        Utilities.setTextViewString(layout?.contactUs, "signup_contact_us")
        Utilities.setTextViewString(layout?.skip, "signup_code_skip")
    }

    //endregion == strings ============

    fun setCode() {

        // code
        codeAdapter = RegistrationCouponCodeAdapter(this@RegistrationCouponCodeFragment)
        layout?.codeList?.adapter = codeAdapter
        codeAdapter!!.setItems(code)
    }

    fun submitForm(skip: Boolean) {
        var isValid = true
        val _code = CharArray(CODE_SIZE)

        if (!skip) {
            for (i in 0..CODE_SIZE.minus(1)) {
                val _char = ((layout?.codeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text

                if (_char.isNullOrEmpty()) {
                    isValid = false
                    layout?.codeList?.getChildAt(i)?.requestFocus()
                    break
                }
                else {
                    _code[i] = _char.toString().first()
                }
            }
        }

        if (isValid) {
            if (isSignUpActivity!!) {
                if (skip) {
                    _signupActivity?.viewModel?.skipRegistration(registerUser)
                }
                else {
                    _signupActivity?.viewModel?.couponRegistration(String(_code))
                }
            }
            else {
                _registrationActivity?.viewModel?.couponRegistration(String(_code))
            }
        }
        else {
            couponCodeAfterResponse(null)
        }
    }

    fun couponCodeAfterResponse(userData: UserEntity?) {
        hideKeyboard(requireContext())

        if (userData != null) {

            runBlocking {
                layout?.codeError?.visibility = View.INVISIBLE
                if (isSignUpActivity!!) {
                    userData.roomUID = _signupActivity?.roomUID
                    //_signupActivity?.setPayProgramRegistration(userData)
                }
                else {
                    Utilities.setButtonDisable(_registrationActivity?.layout?.buttons?.send)
                    userData.roomUID = _registrationActivity?.roomUID
                    _registrationActivity?.updateLocalUser(userData)
                }
            }
        }
        else {
            layout?.codeError?.visibility = View.VISIBLE
            layout?.codeError?.text = Utilities.getLocalPhrase("signup_code_error")
            Utilities.setButtonEnable(_registrationActivity?.layout?.buttons?.send)

            if (isSignUpActivity!!) {
                Utilities.setButtonEnable(_signupActivity?.layout?.buttons?.next)
            }
            else {
                Utilities.setButtonEnable(_registrationActivity?.layout?.buttons?.send)
            }

            Utilities.log(Enums.LogType.Warning, TAG, "couponCodeAfterResponse(): ${layout?.codeError?.text}")
        }
    }
}