package com.adirahav.diraleashkaa.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.HomeService
import com.adirahav.diraleashkaa.data.network.services.PropertyService

class HomeViewModelFactory(
    private val propertyService: PropertyService,
    private val homeService: HomeService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(propertyService, homeService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}