package com.adirahav.diraleashkaa.ui.signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.databinding.ActivitySigninBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

class SignInActivity : BaseActivity<SignInViewModel?, ActivitySigninBinding>() {

	companion object {
		private const val TAG = "SignInActivity"
		private const val EXTRA_USER_ID = "EXTRA_USER_ID"

		fun start(context: Context, userID: Long) {
			val intent = Intent(context, SignInActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			intent.putExtra(EXTRA_USER_ID, userID)
			context.startActivity(intent)
		}
	}

	// user id
	var userID: Long = 0

	// layout
	internal lateinit var layout: ActivitySigninBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// remove notification bar
		this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

		// remove action bar
		supportActionBar?.hide()

		layout = ActivitySigninBinding.inflate(layoutInflater)
		setContentView(layout.root)

		initGlobal()
		//initData()
		//initEvents()


	}

	public override fun onResume() {
		super.onResume()

		setCustomActionBar()
		setDrawer()
	}

	private fun initGlobal() {

	}

	private fun initData() {

	}

	private fun initEvents() {
		// submit
		layout.submit.setOnClickListener {
			submitForm()
		}

		// sign up
		layout.signUp.setOnClickListener {
			goToSignUp()
		}
	}

	override fun createViewModel(): SignInViewModel {
		userID = intent.getLongExtra(EXTRA_USER_ID, 0L)

		val factory = SignInViewModelFactory(this@SignInActivity)
		return ViewModelProvider(this, factory)[SignInViewModel::class.java]
	}

	//strings
	override fun setRoomStrings() {
		Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

		layout.header.text = Utilities.getRoomString("signin_header")
		layout.email.hint = Utilities.getRoomString("signin_email_hint")
		layout.emailError.text = Utilities.getRoomString("signin_email_error")
		layout.password.hint = Utilities.getRoomString("signin_password_hint")
		layout.passwordError.text = Utilities.getRoomString("signin_password_error")
		layout.submit.text = Utilities.getRoomString("signin_submit")
		layout.signUp.text = HtmlCompat.fromHtml(
			Utilities.getRoomString("signin_signup"),
			HtmlCompat.FROM_HTML_MODE_LEGACY)

		super.setRoomStrings()
	}
	//strings

	// events
	fun submitForm() {
		var isValid = true

		// email
		if (!Utilities.isEmailValid(layout.email.text.toString())) {
			layout.emailError.visibility = View.VISIBLE
			isValid = false
		}
		else {
			layout.emailError.visibility = View.INVISIBLE
		}

		// password
		if (layout.password.text.toString().isEmpty()) {
			layout.passwordError.text = Utilities.getRoomString("signin_password_error")
			layout.passwordError.visibility = View.VISIBLE
			isValid = false
		}
		else {
			layout.passwordError.visibility = View.INVISIBLE
		}

		if (isValid) {
			savedUserObserver()
		}
	}

	fun goToSignUp() {
		SignUpActivity.start(context)
	}

	// observers
	fun savedUserObserver() = runBlocking {
		getSavedUser()
	}

	private suspend fun getSavedUser() =
		coroutineScope {
			/*val deferredOne = async {
				DatabaseClient.getInstance(getApplicationContext())?.appDatabase?.userDao()?.findById(uid = userID)?.observe(lifecycleOwner,
					Observer { it ->
						if (it.email.equals(email?.text.toString())/* && it.password.equals(password?.text.toString())*/) {
							viewModel!!.submitLogin(email?.text.toString(), password?.text.toString())
						}
						else {
							passwordError?.text = getString(R.string.signin_login_error)
							passwordError?.visibility = View.VISIBLE
						}
					})
			}

			deferredOne.await()*/
		}

	/*private inner class SignInObserver : Observer<APIResponseModel?> {
		override fun onChanged(signInData: APIResponseModel?) {
			if (signInData == null) {
				return
			}
		}
	}*/

	//region == base abstract ======

	override fun attachBinding(list: MutableList<ActivitySigninBinding>, layoutInflater: LayoutInflater) {
		list.add(ActivitySigninBinding.inflate(layoutInflater))
	}

	//endregion == base abstract ======
}