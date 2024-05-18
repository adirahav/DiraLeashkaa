package com.adirahav.diraleashkaa.ui.calculators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.CalculatorsService
import com.adirahav.diraleashkaa.ui.calculators.CalculatorsActivity
import com.adirahav.diraleashkaa.ui.calculators.CalculatorsViewModel
import com.adirahav.diraleashkaa.ui.property.PropertyActivity

class CalculatorsViewModelFactory(
        private val activity: CalculatorsActivity,
        private val calculatorsService: CalculatorsService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorsViewModel::class.java)) {
            return CalculatorsViewModel(activity, calculatorsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}