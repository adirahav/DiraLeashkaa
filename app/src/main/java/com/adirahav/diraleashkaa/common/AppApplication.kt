package com.adirahav.diraleashkaa.common

import android.app.Application
import android.content.Context

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        var application: Application? = null
            private set

        @JvmStatic
        val context: Context
            get() = application!!.applicationContext
    }
}