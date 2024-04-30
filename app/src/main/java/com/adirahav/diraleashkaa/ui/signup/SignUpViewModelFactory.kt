package com.adirahav.diraleashkaa.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.AuthService
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.PhraseService
import com.adirahav.diraleashkaa.data.network.services.UserService

class SignUpViewModelFactory(
    private val activity: SignUpActivity,
    private val authService: AuthService,
    private val userService: UserService,
    private val registrationService: RegistrationService,
    private val phraseService: PhraseService
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(activity, authService, userService, registrationService, phraseService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}