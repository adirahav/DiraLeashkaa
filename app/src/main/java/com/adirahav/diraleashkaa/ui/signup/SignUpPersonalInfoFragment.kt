package com.adirahav.diraleashkaa.ui.signup

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.setInputDisable
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.databinding.FragmentSignupPersonalInfoBinding
import com.adirahav.diraleashkaa.ui.login.LoginActivity
import com.adirahav.diraleashkaa.ui.user.UserActivity


class SignUpPersonalInfoFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "SignUpPersonalInfoFragment"
        private const val SMS_CODE_SIZE = 4
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _signupActivity: SignUpActivity? = null
    var _userActivity: UserActivity? = null
    private var isSignUpActivity: Boolean? = null

    // user data
    var userData: UserResponse? = null

    // layout
    internal var layout: FragmentSignupPersonalInfoBinding? = null

    // sms
    var smsDialog: Dialog? = null

    //endregion == variables ==========

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout? {

        layout = FragmentSignupPersonalInfoBinding.inflate(layoutInflater)

        initGlobal()
        initData()
        initEvents()

        return layout?.root
    }

    fun initGlobal() {

        // activity
        isSignUpActivity = activity?.javaClass?.simpleName.equals("SignUpActivity")

        _signupActivity = if (isSignUpActivity!!) activity as SignUpActivity else null
        _userActivity = if (!isSignUpActivity!!) activity as UserActivity else null

        // hide keyboard
        Utilities.hideKeyboard(requireContext())

        // adjust container padding
        if (isSignUpActivity == true) {
            layout?.container?.setPadding(
                resources.getDimension(R.dimen.padding).toInt(),
                0,
                resources.getDimension(R.dimen.padding).toInt(),
                0
            )
        }

        // phrases
        setPhrases()
    }

    fun initData() {

        // fullname
        layout?.fullname?.setText(
                if (isSignUpActivity!!)
                    _signupActivity?.loggingUser?.fullname ?: ""
                else
                    _userActivity?.loggedinUser?.fullname ?: ""
        )
        layout?.fullname?.contentDescription = Utilities.getLocalPhrase("signup_name_label")

        // email
        layout?.email?.setText(
                if (isSignUpActivity!!)
                    _signupActivity?.loggingUser?.email ?: ""
                else
                    _userActivity?.loggedinUser?.email ?: ""
        )
        layout?.email?.contentDescription = Utilities.getLocalPhrase("signup_email_label")

        if (!isSignUpActivity!!) {
            setInputDisable(layout?.email)
        }

        // password
        if (isSignUpActivity!!) {
            layout?.password?.setText("")
            layout?.password?.contentDescription = Utilities.getLocalPhrase("signup_password_label")
        }
        else {
            layout?.passwordLabel?.visibility = View.GONE
            layout?.password?.visibility = View.GONE
            layout?.passwordError?.visibility = View.GONE
        }

        // year of birth
        layout?.yearOfBirth?.setText(
                if (isSignUpActivity!!)
                    (_signupActivity?.loggingUser?.yearOfBirth ?: "").toString()
                else
                    (_userActivity?.loggedinUser?.yearOfBirth ?: "").toString()
        )
        layout?.yearOfBirth?.contentDescription = Utilities.getLocalPhrase("signup_year_of_birth_hint")

        if (!isSignUpActivity!!) {
            layout?.login?.visibility = View.GONE
        }
        // TO DELETE
        /*layout?.fullname?.setText("עדי 20")
        layout?.password?.setText("Aa123456!")
        layout?.email?.setText("adi20@gmail.com")
        layout?.yearOfBirth?.setText("1976")*/
        // TO DELETE
    }

    fun initEvents() {

        // name
        layout?.fullname?.requestFocus()

        // password
        if (isSignUpActivity!!) {
            var isShowPasswordIcon = true

            val paddingExtra = resources.getDimensionPixelOffset(R.dimen.padding)

            val showPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_show_password)
            showPasswordIcon!!.setBounds(0, 0, showPasswordIcon.intrinsicWidth + paddingExtra, showPasswordIcon.intrinsicHeight)

            val hidePasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_hide_password)
            hidePasswordIcon!!.setBounds(0, 0, hidePasswordIcon.intrinsicWidth + paddingExtra, hidePasswordIcon.intrinsicHeight)

            layout?.password?.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordIcon, null)
            layout?.password?.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP && event.rawX >= requireView().width - requireView().paddingRight - layout?.password!!.compoundDrawables[2]!!.intrinsicWidth) {

                    if (isShowPasswordIcon) {
                        layout?.password?.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordIcon, null)
                        layout?.password?.inputType = InputType.TYPE_CLASS_TEXT
                    }
                    else {
                        layout?.password?.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordIcon, null)
                        layout?.password?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }

                    isShowPasswordIcon = !isShowPasswordIcon

                    true
                } else {
                    false
                }

            }


        }

        // sign up
        if (isSignUpActivity!!) {
            layout?.login?.setOnClickListener {
                goToLogin()
            }
        }
    }

    //region == phrases ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setLabelViewString(layout?.fullnameLabel, "signup_fullname_label")
        Utilities.setTextViewPhrase(layout?.fullnameError, "signup_fullname_error")

        Utilities.setLabelViewString(layout?.emailLabel, "signup_email_label")
        Utilities.setTextViewPhrase(layout?.emailError, "signup_email_error")

        Utilities.setLabelViewString(layout?.passwordLabel, "signup_password_label")
        Utilities.setTextViewPhrase(layout?.passwordError, "signup_password_error")

        Utilities.setLabelViewString(layout?.yearOfBirthLabel, "signup_year_of_birth_hint")
        Utilities.setTextViewPhrase(layout?.yearOfBirthError, "signup_year_of_birth_error")

        layout?.login?.text = HtmlCompat.fromHtml(
                Utilities.getLocalPhrase("signup_goto_login"),
                HtmlCompat.FROM_HTML_MODE_LEGACY)

    }

    //endregion == phrases ============

    fun submitForm(): Map<String, Any?> {
        var isValid = true

        // fullname
        if (layout?.fullname?.text.toString().trim().isEmpty()) {
            layout?.fullnameError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.fullname?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.fullnameError?.visibility = View.GONE
        }

        // password
        if (isSignUpActivity!!) {
            if (!Utilities.isPasswordValid(layout?.password?.text.toString().trim())) {
                layout?.passwordError?.visibility = View.VISIBLE

                if (isValid) {
                    layout?.password?.requestFocus()
                    isValid = false
                }
            } else {
                layout?.passwordError?.visibility = View.GONE
            }
        }

        // email
        if (!Utilities.isEmailValid(layout?.email?.text.toString().trim())) {
            layout?.emailError?.visibility = View.VISIBLE
            Utilities.setTextViewPhrase(layout?.emailError, "signup_email_error")

            if (isValid) {
                layout?.email?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.emailError?.visibility = View.GONE
        }

        // year of birth
        if (!Utilities.isYearOfBirthValid(layout?.yearOfBirth?.text.toString())) {
            layout?.yearOfBirthError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.yearOfBirth?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.yearOfBirthError?.visibility = View.GONE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["fullname"] = layout?.fullname?.text.toString().trim()
            entities["email"] = layout?.email?.text.toString().trim()
            entities["password"] = layout?.password?.text.toString().replace("-", "")
            entities["yearOfBirth"] = layout?.yearOfBirth?.text.toString().trim()
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

    fun goToLogin() {
        LoginActivity.start(requireContext(), null)
    }
}