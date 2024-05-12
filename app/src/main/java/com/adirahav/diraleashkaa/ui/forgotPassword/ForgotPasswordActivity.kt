package com.adirahav.diraleashkaa.ui.forgotPassword

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.FixedParameters
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.databinding.ActivityForgotPasswordBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.login.LoginActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpWelcomeFragment
import com.airbnb.paris.extensions.style
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.Date

class ForgotPasswordActivity : BaseActivity<ForgotPasswordViewModel?, ActivityForgotPasswordBinding>() {

	companion object {
		private const val TAG = "ForgotPasswordActivity"
		private const val AWAIT_SECONDS = 3
		private const val EXTRA_USER_EMAIL = "EXTRA_USER_EMAIL"

		fun start(context: Context, email: String?) {
			val intent = Intent(context, ForgotPasswordActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			intent.putExtra(EXTRA_USER_EMAIL, email)
			context.startActivity(intent)
		}
	}

	// activity
	val activity = this@ForgotPasswordActivity

	// shared preferences
	var preferences: AppPreferences? = null

	// logging user
	var userEmail: String? = null
	var userToken: String? = null

	// fixed parameters data
	var fixedParametersData: FixedParameters? = null

	// steps
	var currentStep: Int = 1

	// fragments
	val generateCodeFragment = ForgotPasswordGenerateCodeFragment()
	val changePasswordFragment = ForgotPasswordChangePasswordFragment()

	// layout
	internal lateinit var layout: ActivityForgotPasswordBinding

	//region == lifecycle methods ==

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// remove notification bar
		this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

		// remove action bar
		supportActionBar?.hide()

		layout = ActivityForgotPasswordBinding.inflate(layoutInflater)
		setContentView(layout.root)
	}

	public override fun onResume() {
		super.onResume()

		initGlobal()
		initData()
		initEvents()
		initObserver()

		setCustomActionBar()
		setDrawer()
	}

	override fun createViewModel(): ForgotPasswordViewModel {
		userEmail = intent.getStringExtra(EXTRA_USER_EMAIL)

		val factory = ForgotPasswordViewModelFactory(
				this@ForgotPasswordActivity,
				DataManager.instance!!.forgotPasswordService)

		return ViewModelProvider(this, factory)[ForgotPasswordViewModel::class.java]
	}

	override fun onBackPressed() {
		if (currentStep == 3) {
			currentStep = 1
			updateStep()
		}
		else {
			super.onBackPressed()
		}
	}
	//endregion == lifecycle methods ==

	//region == initialize =========

	fun initObserver() {
		Utilities.log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
		if (!viewModel!!.fixedParametersCallback.hasObservers()) viewModel!!.fixedParametersCallback.observe(this@ForgotPasswordActivity, LocalFixedParametersObserver())
		if (!viewModel!!.generateCodeCallback.hasObservers()) viewModel!!.generateCodeCallback.observe(this@ForgotPasswordActivity, GenerateCodeObserver())
		if (!viewModel!!.validateCodeCallback.hasObservers()) viewModel!!.validateCodeCallback.observe(this@ForgotPasswordActivity, ValidateCodeObserver())
		if (!viewModel!!.changePasswordCallback.hasObservers()) viewModel!!.changePasswordCallback.observe(this@ForgotPasswordActivity, ChangePasswordObserver())

		if (fixedParametersData == null) {
			viewModel!!.getLocalFixedParameters(applicationContext)
		}
	}

	private fun initGlobal() {
		// shared preferences
		preferences = AppPreferences.instance
	}

	private fun initData() {

	}

	private fun initEvents() {

	}

	//endregion == initialize ========

	//region == phrases ============

	override fun setPhrases() {
		Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

		layout.header.text = Utilities.getLocalPhrase("forgot_password_header")

		super.setPhrases()
	}

	//endregion == phrases ============

	private fun loadFragment() {
		Utilities.log(Enums.LogType.Debug, TAG, "loadFragment()")

		currentStep = 1
		updateStep()
	}

	//region == steps ==============

	fun submitNext(view: View?) {
		val result = when (currentStep) {
			1 -> generateCodeFragment.submitForm()
			2 -> generateCodeFragment.submitCode()
			3 -> changePasswordFragment.submitForm()
			else -> null
		}

		val isValid = if (result?.containsKey("isValid") == true) { result["isValid"] as Boolean } else { false }

		if (isValid) {
			when (currentStep) {
				1 -> runBlocking {
					viewModel!!.generateCode(result)
				}
				2 -> runBlocking {
					viewModel!!.codeValidation(result)
				}
				3 -> runBlocking {
					viewModel!!.changePassword(result)
				}
			}
		}
	}

	fun submitBack(view: View?) {

		if (currentStep > 1) {
			currentStep--
			updateStep()
		}
	}

	fun updateStep() {
		when (currentStep) {
			1, 2 -> {
				supportFragmentManager.beginTransaction()
						.replace(R.id.formFragment, generateCodeFragment)
						.commitAllowingStateLoss()
			}
			3 -> {
				supportFragmentManager.beginTransaction()
						.replace(R.id.formFragment, changePasswordFragment)
						.commitAllowingStateLoss()
			}
		}
	}
	//endregion == steps ==============

	//region == observers ==========

	private inner class LocalFixedParametersObserver : Observer<FixedParametersEntity?> {
		override fun onChanged(fixedParameters: FixedParametersEntity?) {
			Utilities.log(Enums.LogType.Debug, TAG, "LocalFixedParametersObserver()")

			if (fixedParameters == null) {
				return
			}

			fixedParametersData = FixedParameters.init(fixedParameters)

			loadFragment()
		}
	}

	private inner class GenerateCodeObserver : Observer<JsonObject?> {
		override fun onChanged(result: JsonObject?) {
			Utilities.log(Enums.LogType.Debug, TAG, "GenerateCodeObserver()")

			if (result != null) {
				currentStep++
				generateCodeFragment.openCodeDialog()
			}
			else {
				generateCodeFragment.layout?.emailError?.visibility = View.VISIBLE
				Utilities.setTextViewString(generateCodeFragment.layout?.emailError, "forgot_password_credentials_error")
				generateCodeFragment.layout?.email?.requestFocus()
				Utilities.setButtonEnable(generateCodeFragment.layout?.submit)
			}
		}
	}

	private inner class ValidateCodeObserver : Observer<JsonObject?> {
		override fun onChanged(result: JsonObject?) {
			Utilities.log(Enums.LogType.Debug, TAG, "ValidateCodeObserver()")

			GlobalScope.launch {
				activity.runOnUiThread {
					if (result != null) {
						generateCodeFragment.closeCodeDialog()
						currentStep++
						updateStep()
					}
					else {
						val codeError = generateCodeFragment.codeDialog?.findViewById<TextView>(R.id.codeError)
						codeError?.visibility = View.VISIBLE
						Utilities.setTextViewString(codeError, "forgot_password_code_error")

						val codeList = generateCodeFragment.codeDialog?.findViewById<RecyclerView>(R.id.codeList)

						for (i in 0..ForgotPasswordGenerateCodeFragment.CODE_SIZE.minus(1)) {
							((codeList?.getChildAt(i) as LinearLayout).getChildAt(0) as EditText).text.clear()
						}

						codeList?.getChildAt(0)?.requestFocus()
					}
				}
			}
		}
	}

	private inner class ChangePasswordObserver : Observer<JsonObject?> {
		override fun onChanged(result: JsonObject?) {
			Utilities.log(Enums.LogType.Debug, TAG, "ChangePasswordObserver()")

			GlobalScope.launch {
				activity.runOnUiThread {
					if (result != null) {
						changePasswordFragment.layout?.newPasswordMessage?.style(R.style.formSuccess)
						changePasswordFragment.layout?.newPasswordMessage?.text = Utilities.getLocalPhrase("forgot_password_new_password_success")
						changePasswordFragment.layout?.newPasswordMessage?.visibility = View.VISIBLE
						Utilities.await(Date(), AWAIT_SECONDS, ::responseAfterAwait)
					}
					else {
						changePasswordFragment.layout?.newPasswordMessage?.style(R.style.formError)
						changePasswordFragment.layout?.newPasswordMessage?.text = Utilities.getLocalPhrase("forgot_password_new_password_error")
						changePasswordFragment.layout?.newPasswordMessage?.visibility = View.VISIBLE
						Utilities.setButtonEnable(changePasswordFragment.layout?.submit)
					}

				}
			}
		}
	}

	private fun responseAfterAwait() {
		LoginActivity.start(context, userEmail)
	}

	//region == base abstract ======


	override fun attachBinding(list: MutableList<ActivityForgotPasswordBinding>, layoutInflater: LayoutInflater) {
		list.add(ActivityForgotPasswordBinding.inflate(layoutInflater))
	}

	//endregion == base abstract ======
}
