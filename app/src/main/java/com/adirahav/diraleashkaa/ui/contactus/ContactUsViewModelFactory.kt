package com.adirahav.diraleashkaa.ui.contactus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ContactUsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactUsViewModel::class.java)) {
            return ContactUsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}