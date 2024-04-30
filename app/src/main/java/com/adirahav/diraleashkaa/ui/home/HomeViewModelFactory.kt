package com.adirahav.diraleashkaa.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.PropertyService
import com.adirahav.diraleashkaa.data.network.services.UserService

class HomeViewModelFactory(
    private val activity: HomeActivity,
    private val propertyService: PropertyService,
    private val userService: UserService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(activity, propertyService, userService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}