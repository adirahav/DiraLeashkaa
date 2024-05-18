package com.adirahav.diraleashkaa.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.ActivityLoginBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.forgotPassword.ForgotPasswordActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import com.adirahav.diraleashkaa.ui.splash.SplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<LoginViewModel?, ActivityLoginBinding>() {

	companion object {
		private const val TAG = "LoginActivity"
		private const val EXTRA_USER_EMAIL = "EXTRA_USER_EMAIL"

		fun start(context: Context, email: String?) {
			val intent = Intent(context, LoginActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			intent.putExtra(EXTRA_USER_EMAIL, email)
			context.startActivity(intent)
		}
	}

	// activity
	val activity = this@LoginActivity

	// shared preferences
	var preferences: AppPreferences? = null


	// logging user
	var userEmail: String? = null
	var userToken: String? = null

	// layout
	internal lateinit var layout: ActivityLoginBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// remove notification bar
		this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

		// remove action bar
		supportActionBar?.hide()

		layout = ActivityLoginBinding.inflate(layoutInflater)
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

	fun initObserver() {
		Utilities.log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
		if (!viewModel!!.loginCallback.hasObservers()) viewModel!!.loginCallback.observe(this@LoginActivity, LoginObserver())
	}
	private fun initGlobal() {
		// shared preferences
		preferences = AppPreferences.instance
	}

	private fun initData() {
		if (!userEmail.isNullOrEmpty()) {
			layout.email.setText(userEmail)
		}
	}

	private fun initEvents() {
		// submit
		layout.submit.setOnClickListener {
			submitForm()
		}

		// password
		var isShowPasswordIcon = true

		val paddingExtra = resources.getDimensionPixelOffset(R.dimen.padding)

		val showPasswordIcon = ContextCompat.getDrawable(context, R.drawable.icon_show_password)
		showPasswordIcon!!.setBounds(0, 0, showPasswordIcon.intrinsicWidth + paddingExtra, showPasswordIcon.intrinsicHeight)

		val hidePasswordIcon = ContextCompat.getDrawable(context, R.drawable.icon_hide_password)
		hidePasswordIcon!!.setBounds(0, 0, hidePasswordIcon.intrinsicWidth + paddingExtra, hidePasswordIcon.intrinsicHeight)

		layout.password.setCompoundDrawablesWithIntrinsicBounds(null, null, showPasswordIcon, null)
		layout.password?.setOnTouchListener { view, event ->
			if (event.action == MotionEvent.ACTION_UP && event.rawX >= view.width - view.paddingRight - layout?.password!!.compoundDrawables[2]!!.intrinsicWidth) {

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

		// sign up
		layout.signUp.setOnClickListener {
			goToSignUp()
		}

		// sign up
		layout.forgotPassword.setOnClickListener {
			goToForgotPassword()
		}
	}

	override fun createViewModel(): LoginViewModel {
		userEmail = intent.getStringExtra(EXTRA_USER_EMAIL)

		val factory = LoginViewModelFactory(
				this@LoginActivity,
				DataManager.instance!!.authService)
		return ViewModelProvider(this, factory)[LoginViewModel::class.java]
	}

	//phrases
	override fun setPhrases() {
		Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

		layout.header.text = Utilities.getLocalPhrase("login_header")

		Utilities.setLabelViewString(layout.emailLabel, "login_email_label")
		Utilities.setLabelViewString(layout.passwordLabel, "login_password_label")
		Utilities.setTextViewPhrase(layout.loginError, "login_credentials_error")

		layout.submit.text = Utilities.getLocalPhrase("login_submit")
		layout.signUp.text = HtmlCompat.fromHtml(
			Utilities.getLocalPhrase("login_goto_signup"),
			HtmlCompat.FROM_HTML_MODE_LEGACY)
		layout.forgotPassword.text = HtmlCompat.fromHtml(
				Utilities.getLocalPhrase("login_goto_forgot_password"),
				HtmlCompat.FROM_HTML_MODE_LEGACY)

		super.setPhrases()
	}
	//phrases

	// events
	fun submitForm() {
		var isValid = true

		// email
		if (!Utilities.isEmailValid(layout.email.text.toString())) {
			isValid = false
		}

		// password
		if (layout.password.text.toString().isEmpty()) {
			isValid = false
		}

		if (isValid) {
			layout.loginError.visibility = View.INVISIBLE
			runOnUiThread {

				val map = mutableMapOf<String, Any?>()
				val entities = mutableMapOf<String, Any?>()

				entities["email"] = layout.email.text.toString()
				entities["password"] = layout.password.text.toString()

				map["isValid"] = true
				map["entities"] = entities

				Utilities.hideKeyboard(this@LoginActivity)

				viewModel!!.submitLogin(map)
			}
		}
		else {
			layout.loginError.visibility = View.VISIBLE
		}
	}

	fun goToSignUp() {
		CoroutineScope(Dispatchers.IO).launch {
			DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
		}
		SignUpActivity.start(context)
	}
	fun goToForgotPassword() {
		CoroutineScope(Dispatchers.IO).launch {
			DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
		}
		ForgotPasswordActivity.start(context, layout.email.text.toString())
	}
	//region == base abstract ======

	private inner class LoginObserver : Observer<UserEntity?> {
		override fun onChanged(user: UserEntity?) {
			Utilities.log(Enums.LogType.Debug, TAG, "LoginObserver() user = {${user}}")

			GlobalScope.launch {
				if (user != null) {
					DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
					val roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(user)!!
					activity.preferences?.setLong("roomUID", roomUID, false)

					SplashActivity.start(activity)
				}
				else {
					activity.runOnUiThread {
						layout.loginError.visibility = View.VISIBLE
					}
				}
			}
		}
	}

	override fun attachBinding(list: MutableList<ActivityLoginBinding>, layoutInflater: LayoutInflater) {
		list.add(ActivityLoginBinding.inflate(layoutInflater))
	}

	//endregion == base abstract ======
}