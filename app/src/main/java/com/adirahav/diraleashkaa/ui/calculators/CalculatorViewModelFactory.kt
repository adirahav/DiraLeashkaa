package com.adirahav.diraleashkaa.ui.calculators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.CalculatorsService

class CalculatorViewModelFactory(
        private val activity: CalculatorActivity,
        private val calculatorsService: CalculatorsService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            return CalculatorViewModel(activity, calculatorsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}