package com.adirahav.diraleashkaa.ui.signin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SignInViewModelFactory(
	private val activityContext: Context
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
			return SignInViewModel(activityContext) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}

}