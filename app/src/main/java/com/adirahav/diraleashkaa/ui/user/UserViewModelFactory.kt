package com.adirahav.diraleashkaa.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.PhraseService
import com.adirahav.diraleashkaa.data.network.services.UserService
import com.adirahav.diraleashkaa.ui.signup.SignUpActivity

class UserViewModelFactory(
        private val activity: UserActivity,
        private val userService: UserService,
        private val phraseService: PhraseService): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(activity, userService, phraseService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}