package com.adirahav.diraleashkaa.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.hideKeyboard
import com.adirahav.diraleashkaa.databinding.FragmentSignupWelcomeBinding
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import java.util.*

class SignUpWelcomeFragment : Fragment() {

    companion object {
        private const val TAG = "SignUpWelcomeFragment"
        private const val AWAIT_SECONDS = 3
    }

    // activity
    var _activity: SignUpActivity? = null

    // layout
    private var layout: FragmentSignupWelcomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = FragmentSignupWelcomeBinding.inflate(layoutInflater)

        initGlobal()
        initData()

        return layout?.root
    }

    fun initGlobal() {
        // activity
        _activity = activity as SignUpActivity

        // hide keyboard
        hideKeyboard(requireContext())

        // phrases
        setPhrases()
    }

    fun initData() {
        Utilities.await(Date(), AWAIT_SECONDS, ::responseAfterAwait)
    }

    //region == phrases ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")
        Utilities.setTextViewPhrase(layout?.welcomeMessage, "signup_welcome_text")
    }

    //endregion == phrases ============

    private fun responseAfterAwait() {
        HomeActivity.start(requireContext())
    }
}