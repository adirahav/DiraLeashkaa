package com.adirahav.diraleashkaa.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.StringsService
import com.adirahav.diraleashkaa.data.network.services.UserService

class SignUpViewModelFactory(
    private val activity: SignUpActivity,
    private val userService: UserService,
    private val registrationService: RegistrationService,
    private val stringsService: StringsService
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(activity, userService, registrationService, stringsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}