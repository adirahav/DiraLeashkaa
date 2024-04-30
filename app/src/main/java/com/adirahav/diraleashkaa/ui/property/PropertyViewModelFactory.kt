package com.adirahav.diraleashkaa.ui.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.PropertyService
import com.adirahav.diraleashkaa.ui.home.HomeActivity

class PropertyViewModelFactory(
        private val activity: PropertyActivity,
        private val service: PropertyService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
            return PropertyViewModel(activity, service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}