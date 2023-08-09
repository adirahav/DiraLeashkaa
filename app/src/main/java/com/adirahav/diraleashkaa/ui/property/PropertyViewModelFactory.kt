package com.adirahav.diraleashkaa.ui.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.PropertyService

class PropertyViewModelFactory(private val service: PropertyService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
            return PropertyViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}