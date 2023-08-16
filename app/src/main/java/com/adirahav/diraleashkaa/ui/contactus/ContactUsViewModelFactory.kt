package com.adirahav.diraleashkaa.ui.contactus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.data.network.services.AnnouncementService
import com.adirahav.diraleashkaa.data.network.services.EmailService
import com.adirahav.diraleashkaa.data.network.services.SplashService
import com.adirahav.diraleashkaa.ui.splash.SplashActivity
import com.adirahav.diraleashkaa.ui.splash.SplashViewModel

class ContactUsViewModelFactory(private val activity: ContactUsActivity,
                             private val emailService: EmailService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactUsViewModel::class.java)) {
            return ContactUsViewModel(activity, emailService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}