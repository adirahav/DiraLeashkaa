package com.adirahav.diraleashkaa.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.AuthService

class LoginViewModelFactory(
		private val activity: LoginActivity,
		private val authService: AuthService
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
			return LoginViewModel(activity, authService) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}

}