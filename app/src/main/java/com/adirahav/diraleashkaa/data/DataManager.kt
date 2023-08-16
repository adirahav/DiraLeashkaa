package com.adirahav.diraleashkaa.data

import com.adirahav.diraleashkaa.data.network.services.*

class DataManager private constructor() {

    val fixedParametersService: FixedParametersService
        get() = FixedParametersService.instance!!

    val userService: UserService
        get() = UserService.instance!!

    val registrationService: RegistrationService
        get() = RegistrationService.instance!!

    val homeService: HomeService
        get() = HomeService.instance!!

    val propertyService: PropertyService
        get() = PropertyService.instance!!

    val splashService: SplashService
        get() = SplashService.instance!!

    val stringsService: StringsService
        get() = StringsService.instance!!

    val announcementService: AnnouncementService
        get() = AnnouncementService.instance!!

    val emailService: EmailService
        get() = EmailService.instance!!

    companion object {
        @JvmStatic
        @get:Synchronized
        var instance: DataManager? = null
            get() {
                if (field == null) {
                    field = DataManager()
                }
                return field
            }
            private set
    }
}