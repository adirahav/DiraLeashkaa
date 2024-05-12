package com.adirahav.diraleashkaa.ui.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.AuthService
import com.adirahav.diraleashkaa.data.network.services.ForgotPasswordService

class ForgotPasswordViewModelFactory(
		private val activity: ForgotPasswordActivity,
		private val forgotPasswordService: ForgotPasswordService
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
			return ForgotPasswordViewModel(activity, forgotPasswordService) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}

}