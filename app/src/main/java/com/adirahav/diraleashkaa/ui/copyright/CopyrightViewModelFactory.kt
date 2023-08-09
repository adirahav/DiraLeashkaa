package com.adirahav.diraleashkaa.ui.copyright

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CopyrightViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CopyrightViewModel::class.java)) {
            return CopyrightViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}