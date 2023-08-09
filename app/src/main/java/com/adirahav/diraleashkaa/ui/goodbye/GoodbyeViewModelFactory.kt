package com.adirahav.diraleashkaa.ui.goodbye

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GoodbyeViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoodbyeViewModel::class.java)) {
            return GoodbyeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}