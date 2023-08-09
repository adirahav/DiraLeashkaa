package com.adirahav.diraleashkaa.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.StringsService
import com.adirahav.diraleashkaa.data.network.services.UserService

class UserViewModelFactory(private val userService: UserService, private val stringsService: StringsService): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userService, stringsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}