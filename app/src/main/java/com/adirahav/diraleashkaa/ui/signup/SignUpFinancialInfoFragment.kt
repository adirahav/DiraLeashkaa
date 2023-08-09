package com.adirahav.diraleashkaa.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Const
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.NumberWithComma
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.toFormatNumber
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.FragmentSignupFinancialInfoBinding
import com.adirahav.diraleashkaa.ui.user.UserActivity

class SignUpFinancialInfoFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "SignUpFinancialInfoFragment"
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
    private var layout: FragmentSignupFinancialInfoBinding? = null


    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = FragmentSignupFinancialInfoBinding.inflate(layoutInflater)

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

        // equity
        layout?.equity?.setText((userData?.equity.toFormatNumber()))
        layout?.equity?.contentDescription = Utilities.getRoomString("signup_equity_label")

        // incomes
        layout?.incomes?.setText((userData?.incomes.toFormatNumber()))
        layout?.incomes?.contentDescription = Utilities.getRoomString("signup_incomes_label")

        // commitments
        layout?.commitments?.setText((userData?.commitments.toFormatNumber()))
        layout?.commitments?.contentDescription = Utilities.getRoomString("signup_commitments_label")

        // TO DELETE
        /*layout.equity.setText("500,000")
        layout.incomes.setText("50,000")
        layout.commitments.setText("5,000")*/
        // TO DELETE


    }

    fun initEvents() {

        // equity
        layout?.equity?.requestFocus()
        layout?.equity?.addTextChangedListener(NumberWithComma(layout?.equity))

        // incomes
        layout?.incomes?.addTextChangedListener(NumberWithComma(layout?.incomes))

        // commitments
        layout?.commitments?.addTextChangedListener(NumberWithComma(layout?.commitments))
    }

    //endregion == initialize =========

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setLabelViewString(layout?.equityLabel, "signup_equity_label", "signup_equity_tooltip")
        Utilities.setTextViewString(layout?.equityError, "signup_equity_error")

        Utilities.setLabelViewString(layout?.incomesLabel, "signup_incomes_label", "signup_incomes_tooltip")
        Utilities.setTextViewString(layout?.incomesError, "signup_incomes_error")

        Utilities.setLabelViewString(layout?.commitmentsLabel, "signup_commitments_label", "signup_commitments_tooltip")
        Utilities.setTextViewString(layout?.commitmentsError, "signup_commitments_error")
    }

    //endregion == strings ============

    fun submitForm() : Map<String, Any?> {
        var isValid = true

        // equity
        if (layout?.equity?.text.toString().isEmpty()) {
            layout?.equityError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.equity?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.equityError?.visibility = View.GONE
        }

        // incomes
        if (layout?.incomes?.text.toString().isEmpty()) {
            layout?.incomesError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.incomes?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.incomesError?.visibility = View.GONE
        }

        // commitments
        if (layout?.commitments?.text.toString().isEmpty()) {
            layout?.commitmentsError?.visibility = View.VISIBLE

            if (isValid) {
                layout?.commitments?.requestFocus()
                isValid = false
            }
        }
        else {
            layout?.commitmentsError?.visibility = View.GONE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities[Const.EQUITY] = layout?.equity?.text.toString()
            entities[Const.INCOMES] = layout?.incomes?.text.toString()
            entities[Const.COMMITMENTS] = layout?.commitments?.text.toString()
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }

}