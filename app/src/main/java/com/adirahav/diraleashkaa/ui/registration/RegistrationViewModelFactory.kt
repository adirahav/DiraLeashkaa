package com.adirahav.diraleashkaa.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.UserService

class RegistrationViewModelFactory(private val activity: RegistrationActivity,
                                   private val userService: UserService,
                                   private val registrationService: RegistrationService): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel(activity, userService, registrationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}