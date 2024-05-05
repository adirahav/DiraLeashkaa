package com.adirahav.diraleashkaa.ui.contactus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.ContactUsService

class ContactUsViewModelFactory(private val activity: ContactUsActivity,
                             private val contactUsService: ContactUsService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactUsViewModel::class.java)) {
            return ContactUsViewModel(activity, contactUsService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}