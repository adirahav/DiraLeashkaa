
package com.adirahav.diraleashkaa.ui.registration

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.models.StringModel
import com.adirahav.diraleashkaa.databinding.FragmentSignupTermsOfUseBinding
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import com.adirahav.diraleashkaa.ui.user.UserActivity

class RegistrationTermsOfUseFragment : Fragment() {

    companion object {
        private const val TAG = "RegistrationTermsOfUseFragment"
        fun newInstance() = RegistrationTermsOfUseFragment()
    }

    // activity
    var _signupActivity: SignUpActivity? = null
    var _userActivity: UserActivity? = null
    private var isSignUpActivity: Boolean? = null

    // layout
    private var layout: FragmentSignupTermsOfUseBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = FragmentSignupTermsOfUseBinding.inflate(layoutInflater)

        initGlobal()
        initViews()
        initEvents()
        initData()

        return layout?.root
    }

    fun initGlobal() {
        // activity
        isSignUpActivity = activity?.javaClass?.simpleName.equals("SignUpActivity")

        _signupActivity = if (isSignUpActivity!!) activity as SignUpActivity else null
        _userActivity = if (!isSignUpActivity!!) activity as UserActivity else null

        // hide keyboard
        if (isSignUpActivity!!)
            _signupActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        else
            _userActivity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // strings
        setRoomStrings()
    }

    fun initViews() {
        layout?.agreeContainer?.visibility = GONE

        Utilities.setButtonDisable(
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.back
            else
                _userActivity?.layout?.buttons?.back
        )

        Utilities.setButtonDisable(
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.next
            else
                _userActivity?.layout?.buttons?.next
        )
    }

    fun initEvents() {
        layout?.agree?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Utilities.setButtonEnable(
                    if (isSignUpActivity!!)
                        _signupActivity?.layout?.buttons?.next
                    else
                        _userActivity?.layout?.buttons?.next
                )
            }
            else {
                Utilities.setButtonDisable(
                    if (isSignUpActivity!!)
                        _signupActivity?.layout?.buttons?.next
                    else
                        _userActivity?.layout?.buttons?.next
                )
            }
        }
    }

    fun initData() {
        if (isSignUpActivity!!) {
            _signupActivity?.viewModel?.getTermsOfUse()
        }
        else {
            _userActivity?.viewModel?.getTermsOfUse()
        }

        layout?.agree?.contentDescription = Utilities.getRoomString("signup_terms_of_use_agree")
    }

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setTextViewString(layout?.agreeLabel, "signup_terms_of_use_agree")
        Utilities.setTextViewString(layout?.agreeError, "signup_terms_of_use_agree_error")

    }

    //endregion == strings ============

    fun submitForm() : Map<String, Any?> {
        var isValid = true

        // agree
        if (layout?.agree?.isChecked == false) {
            layout?.agreeError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.agree?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.agreeError?.visibility = View.INVISIBLE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["terms_of_use_accept_time"] = System.currentTimeMillis()
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

    fun termsOfUseCallback(result: StringModel?) {

        if (result == null || !result.success) {
            Utilities.openFancyDialog(requireContext(),
                Enums.DialogType.DATA_ERROR, ::responseAfterDataErrorPositivePress, null, emptyArray())
            return
        }

        if (isSignUpActivity!!)
            layout?.agreeContainer?.visibility = VISIBLE
        else
            layout?.agreeContainer?.visibility = GONE

        Utilities.setButtonEnable(
            if (isSignUpActivity!!)
                _signupActivity?.layout?.buttons?.back
            else
                _userActivity?.layout?.buttons?.back
        )

        val isValid = result.success ?: false
        val errorCode = if (result.error?.errorCode != null)
            Enums.CodeError.valueOf(result.error!!.errorCode).errorCode
        else
            Enums.CodeError.SERVER_ERROR.errorCode

        if (isValid) {
            layout?.text?.text = Html.fromHtml(result.data?.strings?.value)
        }
        else {
            Utilities.log(
                Enums.LogType.Error,
                TAG,
                "termsOfUseCallback(): errorCode = ${errorCode}"
            )
        }

        Utilities.hideKeyboard(requireContext())
    }

    private fun responseAfterDataErrorPositivePress() {
        if (isSignUpActivity!!) {
            _signupActivity?.finishAffinity()
        }
        else {
            _userActivity?.finishAffinity()
        }
    }
}