package com.adirahav.diraleashkaa.ui.forgotPassword

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.databinding.FragmentForgotPasswordGenerateCodeBinding
import com.adirahav.diraleashkaa.ui.login.LoginActivity


class ForgotPasswordGenerateCodeFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "ForgotPasswordGenerateCodeFragment"
        internal const val CODE_SIZE = 4
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    var _activity: ForgotPasswordActivity? = null

    // user data
    var userData: UserResponse? = null

    // layout
    internal var layout: FragmentForgotPasswordGenerateCodeBinding? = null

    // sms
    var codeDialog: Dialog? = null

    //endregion == variables ==========

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): LinearLayout? {

        layout = FragmentForgotPasswordGenerateCodeBinding.inflate(layoutInflater)

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
        layout?.container?.setPadding(resources.getDimension(R.dimen.padding).toInt(), 0, resources.getDimension(R.dimen.padding).toInt(), 0)

        // phrases
        setPhrases()


    }

    fun initData() {

        // email
        if (!_activity?.userEmail.isNullOrEmpty()) {
            layout?.email?.setText(_activity?.userEmail)
        }

        // send
        //val send = codeDialog?.findViewById<Button>(R.id.send)
        //Utilities.setButtonEnable(send)
    }

    fun initEvents() {
        // login
        layout?.login?.setOnClickListener {
            goToLogin()
        }
    }

    //region == phrases ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        // email
        Utilities.setLabelViewString(layout?.emailLabel, "forgot_password_email_label")

        // send button
        layout?.submit?.text = Utilities.getLocalPhrase("button_send")

        // login
        layout?.login?.text = HtmlCompat.fromHtml(
                Utilities.getLocalPhrase("forgot_password_goto_login"),
                HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    //endregion == phrases ============

    fun submitForm(): Map<String, Any?> {
        var isValid = true

        // email
        if (!Utilities.isEmailValid(layout?.email?.text.toString().trim())) {
            layout?.emailError?.visibility = View.VISIBLE
            Utilities.setTextViewPhrase(layout?.emailError, "forgot_password_email_error")

            if (isValid) {
                layout?.email?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.emailError?.visibility = View.INVISIBLE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["email"] = layout?.email?.text.toString().trim()
            Utilities.setButtonDisable(layout?.submit)
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

    //region == code ===============

    fun onDialogClosing() {

    }
    internal fun openCodeDialog() {
        codeDialog = Dialog(requireContext())

        codeDialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                _activity?.submitBack(null)
                dialog.dismiss()
                Utilities.setButtonEnable(layout?.submit)
            }
            false
        }

        codeDialog?.setOnCancelListener({
                _activity?.submitBack(null)
                Utilities.setButtonEnable(layout?.submit)
            }
        )

        // layout
        codeDialog?.setContentView(R.layout.dialog_forgot_password_code)

        // title
        val formLabel = codeDialog?.findViewById<TextView>(R.id.formLabel)
        formLabel?.text = Utilities.getLocalPhrase("forgot_password_code_title")

        // code list
        val codeAdapter = ForgotPasswordCodeAdapter(this@ForgotPasswordGenerateCodeFragment)
        val code = CharArray(CODE_SIZE)

        val codeList = codeDialog?.findViewById<RecyclerView>(R.id.codeList)

        codeList!!.adapter = codeAdapter
        codeAdapter.setItems(code)

        // send
        val send = codeDialog?.findViewById<Button>(R.id.send)
        send?.text = Utilities.getLocalPhrase("button_send")
        Utilities.setButtonDisable(send)

        val windowParams = WindowManager.LayoutParams()
        windowParams.copyFrom(codeDialog?.window?.attributes)
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        codeDialog?.show()
        codeDialog?.window?.attributes = windowParams
    }

    internal fun closeCodeDialog() {
        codeDialog?.hide()
    }

    fun submitCode(): Map<String, Any?> {
        var isValid = true

        // code verification
        val codeList = codeDialog?.findViewById<RecyclerView>(R.id.codeList)

        val code = CharArray(CODE_SIZE)

        for (i in 0..CODE_SIZE.minus(1)) {
            val char = ((codeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text

            if (char.isNullOrEmpty()) {
                isValid = false
                codeList.getChildAt(i)?.requestFocus()
                break
            }
            else {
                code[i] = char.toString().first()
            }
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["code"] = String(code)
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

    //endregion == code ===============

    fun goToLogin() {
        LoginActivity.start(requireContext(), _activity?.userEmail)
    }
}