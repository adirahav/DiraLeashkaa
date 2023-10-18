package com.adirahav.diraleashkaa.ui.signup

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.findStringByName
import com.adirahav.diraleashkaa.data.network.entities.APIResponseErrorEntity
import com.adirahav.diraleashkaa.data.network.entities.SMSCodeValidationEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.SMSCodeValidationModel
import com.adirahav.diraleashkaa.databinding.FragmentSignupPersonalInfoBinding
import com.adirahav.diraleashkaa.ui.user.UserActivity
import kotlinx.coroutines.runBlocking
import java.util.Calendar


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
    var userData: UserEntity? = null

    // layout
    private var layout: FragmentSignupPersonalInfoBinding? = null

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

        // user data
        userData = if (isSignUpActivity!!) _signupActivity?.userData else _userActivity?.userData

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

        // strings
        setRoomStrings()
    }

    fun initData() {

        // name
        layout?.name?.setText(userData?.userName)
        layout?.name?.contentDescription = Utilities.getRoomString("signup_name_label")

        // phone number
        layout?.phone?.setText(
            if (userData?.phoneNumber?.length ?: 0 > 3)
                "${userData?.phoneNumber?.substring(0,3)}-${userData?.phoneNumber?.substring(3)}"
            else
                userData?.phoneNumber
        )
        layout?.phone?.contentDescription = Utilities.getRoomString("signup_phone_label")

        if (isSignUpActivity == false) {
            layout?.phone?.isEnabled = false
        }

        // email
        layout?.email?.setText(userData?.email)
        layout?.email?.contentDescription = Utilities.getRoomString("signup_email_label")

        // year of birth
        layout?.yearOfBirth?.setText((userData?.yearOfBirth ?: "").toString())
        layout?.yearOfBirth?.contentDescription = Utilities.getRoomString("signup_year_of_birth_hint")

        // TO DELETE
        /*layout.name.setText("adi")
        layout.phone.setText("054-6789966")
        layout.email.setText("adi_rahav@yahoo.com")
        layout.yearOfBirth.setText("1976")*/
        // TO DELETE
    }

    fun initEvents() {

        // name
        layout?.name?.requestFocus()

        // phone
        layout?.phone?.addTextChangedListener(PhoneNumber(layout?.phone))

    }

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setLabelViewString(layout?.nameLabel, "signup_name_label")
        Utilities.setTextViewString(layout?.nameError, "signup_name_error")

        Utilities.setLabelViewString(layout?.phoneLabel, "signup_phone_label")
        Utilities.setTextViewString(layout?.phoneError, "signup_phone_error")

        Utilities.setLabelViewString(layout?.emailLabel, "signup_email_label")
        Utilities.setTextViewString(layout?.emailError, "signup_email_error")

        Utilities.setLabelViewString(layout?.yearOfBirthLabel, "signup_year_of_birth_hint")
        Utilities.setTextViewString(layout?.yearOfBirthError, "signup_year_of_birth_error")

    }

    //endregion == strings ============

    fun submitForm(): Map<String, Any?> {
        var isValid = true

        // name
        if (layout?.name?.text.toString().trim().isEmpty()) {
            layout?.nameError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.name?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.nameError?.visibility = View.GONE
        }

        // phone
        if (!Utilities.isPhoneValid(layout?.phone?.text.toString().trim())) {
            layout?.phoneError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.phone?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.phoneError?.visibility = View.GONE
        }

        // email
        if (!Utilities.isEmailValid(layout?.email?.text.toString().trim())) {
            layout?.emailError?.visibility = View.VISIBLE

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
            entities["name"] = layout?.name?.text.toString().trim()
            entities["email"] = layout?.email?.text.toString().trim()
            entities["phone_number"] = layout?.phone?.text.toString().replace("-", "")
            entities["year_of_birth"] = layout?.yearOfBirth?.text.toString().trim()

            if (isSignUpActivity!! && _signupActivity?.showSMSNotification == false) {
                entities["phone_number_sms_verified"] = false
            }
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

    //region == sms ================

    internal fun openSMSDialog() {
        smsDialog = Dialog(requireContext())

        // layout
        smsDialog?.setContentView(R.layout.dialog_sms_code)

        // title
        val formLabel = smsDialog?.findViewById<TextView>(R.id.formLabel)
        formLabel?.text = Utilities.getRoomString("sms_title")

        // code list
        val codeAdapter = SignUpSMSCodeAdapter(this@SignUpPersonalInfoFragment)
        val code = CharArray(SMS_CODE_SIZE)

        val codeList = smsDialog?.findViewById<RecyclerView>(R.id.smsCodeList)

        codeList!!.adapter = codeAdapter
        codeAdapter.setItems(code)

        // code error

        // send again
        val sendAgain = smsDialog?.findViewById<TextView>(R.id.sendAgain)
        sendAgain?.text = Utilities.getRoomString("sms_link_send_again")
        sendAgain?.setOnClickListener {
            sendSMSCodeAgain()
        }

        // send
        val send = smsDialog?.findViewById<Button>(R.id.send)
        send?.text = Utilities.getRoomString("sms_button_send")
        Utilities.setButtonDisable(send)

        send?.setOnClickListener {
            checkSMSCode()
        }

        val windowParams = WindowManager.LayoutParams()
        windowParams.copyFrom(smsDialog?.window?.attributes)
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        smsDialog?.show()
        smsDialog?.window?.attributes = windowParams
    }

    private fun closeSMSDialog() {
        smsDialog?.hide()
    }

    private fun sendSMSCodeAgain() {
        Utilities.log(Enums.LogType.Warning, TAG, "TODO: sendSMSCodeAgain()", userData)
    }

    private fun checkSMSCode() {
        var isValid = true
        var errorCode: Enums.CodeError? = null

        // sms verification
        val smsCodeList = smsDialog?.findViewById<RecyclerView>(R.id.smsCodeList)

        val code = CharArray(SMS_CODE_SIZE)

        for (i in 0..SMS_CODE_SIZE.minus(1)) {
            val char = ((smsCodeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text

            if (char.isNullOrEmpty()) {
                isValid = false
                errorCode = Enums.CodeError.WRONG_FORMAT
                smsCodeList.getChildAt(i)?.requestFocus()
                break
            }
            else {
                code[i] = char.toString().first()
            }
        }

        if (isValid) {
            runBlocking {
                val send = smsDialog?.findViewById<Button>(R.id.send)
                send?.text = Utilities.getRoomString("sms_button_send")
                Utilities.setButtonDisable(send)
                _signupActivity?.viewModel?.smsCodeValidation(
                    _signupActivity?.userData,
                    String(code)
                )
            }
        }
        else {
            val smsCodeValidationAPIResponse = SMSCodeValidationModel(
                success = false,
                SMSCodeValidationEntity(isValidCode = false),
                APIResponseErrorEntity(errorCode = errorCode.toString(), errorMessage = "")
            )
            afterCheckSMSCode(smsCodeValidationAPIResponse)
        }
    }

    fun afterCheckSMSCode(smsCodeValidation: SMSCodeValidationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "afterCheckSMSCode()")

        val codeError = smsDialog?.findViewById<TextView>(R.id.codeError)

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        val isValid = smsCodeValidation?.data?.isValidCode ?: false
        //val errorCode = smsCodeValidation?.error?.errorCode ?: Enums.CodeError.SERVER_ERROR.errorCode
        val errorCode = smsCodeValidation?.error?.errorCode

        entities["phone_number_sms_verified"] = isValid

        map["isValid"] = isValid
        map["entities"] = entities

        if (isValid) {
            runBlocking {
                codeError?.visibility = View.INVISIBLE
                closeSMSDialog()
                _signupActivity?.insertUpdateUser(map)
            }
        }
        else {
            val send= smsDialog?.findViewById<Button>(R.id.send)
            Utilities.setButtonEnable(send)
            codeError?.visibility = View.VISIBLE
            codeError?.text = String.format(
                resources.getString(findStringByName("signup_code_error_${errorCode}")),
                SMS_CODE_SIZE
            )
        }
    }

    fun skipSMSCode() {
        Utilities.log(Enums.LogType.Debug, TAG, "skipSMSCode()")

        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        entities["phone_number_sms_verified"] = false

        map["isValid"] = true
        map["entities"] = entities

        runBlocking {
            _signupActivity?.insertUpdateUser(map)
        }
    }

    //endregion == sms ================

}