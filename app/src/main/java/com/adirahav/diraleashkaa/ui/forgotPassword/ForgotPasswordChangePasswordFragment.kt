package com.adirahav.diraleashkaa.ui.forgotPassword

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.setButtonDisable
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.databinding.FragmentForgotPasswordChangePasswordBinding


class ForgotPasswordChangePasswordFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "ForgotPasswordChangePasswordFragment"
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _activity: ForgotPasswordActivity? = null

    // user data
    var userData: UserResponse? = null

    // layout
    internal var layout: FragmentForgotPasswordChangePasswordBinding? = null

    //endregion == variables ==========

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout? {

        layout = FragmentForgotPasswordChangePasswordBinding.inflate(layoutInflater)

        initGlobal()
        initData()
        initEvents()

        return layout?.root
    }

    fun initGlobal() {

        // activity
        _activity = activity as ForgotPasswordActivity

        // hide keyboard
        Utilities.hideKeyboard(requireContext())

        // adjust container padding
        layout?.container?.setPadding(
            resources.getDimension(R.dimen.padding).toInt(),
            0,
            resources.getDimension(R.dimen.padding).toInt(),
            0
        )

        // phrases
        setPhrases()
    }

    fun initData() {

    }

    fun initEvents() {

        // password
        var isShowPasswordIcon = true

        val paddingExtra = resources.getDimensionPixelOffset(R.dimen.padding)

        val showPasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_show_password)
        showPasswordIcon!!.setBounds(0, 0, showPasswordIcon.intrinsicWidth + paddingExtra, showPasswordIcon.intrinsicHeight)

        val hidePasswordIcon = ContextCompat.getDrawable(requireContext(), R.drawable.icon_hide_password)
        hidePasswordIcon!!.setBounds(0, 0, hidePasswordIcon.intrinsicWidth + paddingExtra, hidePasswordIcon.intrinsicHeight)

        layout?.newPassword?.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordIcon, null)
        layout?.newPassword?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= requireView().width - requireView().paddingRight - layout?.newPassword!!.compoundDrawables[2]!!.intrinsicWidth) {

                if (isShowPasswordIcon) {
                    layout?.newPassword?.setCompoundDrawablesWithIntrinsicBounds(null, null, hidePasswordIcon, null)
                    layout?.newPassword?.inputType = InputType.TYPE_CLASS_TEXT
                }
                else {
                    layout?.newPassword?.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordIcon, null)
                    layout?.newPassword?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

                isShowPasswordIcon = !isShowPasswordIcon

                true
            } else {
                false
            }

        }
    }

    //region == phrases ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        // new password
        Utilities.setLabelViewString(layout?.newPasswordLabel, "forgot_password_new_password_label")

        // change button
        layout?.submit?.text = Utilities.getLocalPhrase("button_change")
    }

    //endregion == phrases ============

    fun submitForm(): Map<String, Any?> {
        var isValid = true

        // password
        if (!Utilities.isPasswordValid(layout?.newPassword?.text.toString().trim())) {
            layout?.newPasswordMessage?.text = Utilities.getLocalPhrase("forgot_password_new_password_error")
            layout?.newPasswordMessage?.visibility = View.VISIBLE
            Utilities.setTextViewPhrase(layout?.newPasswordMessage, "forgot_password_new_password_error")

            if (isValid) {
                layout?.newPassword?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.newPasswordMessage?.visibility = View.INVISIBLE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["newPassword"] = layout?.newPassword?.text.toString().trim()
            setButtonDisable(layout?.submit)
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

}