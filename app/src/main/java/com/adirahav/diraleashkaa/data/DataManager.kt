package com.adirahav.diraleashkaa.data

import com.adirahav.diraleashkaa.data.network.services.*

class DataManager private constructor() {

    val authService: AuthService
        get() = AuthService.instance!!

    val userService: UserService
        get() = UserService.instance!!

    val registrationService: RegistrationService
        get() = RegistrationService.instance!!

    val propertyService: PropertyService
        get() = PropertyService.instance!!

    val phraseService: PhraseService
        get() = PhraseService.instance!!

    val announcementService: AnnouncementService
        get() = AnnouncementService.instance!!

    val emailService: EmailService
        get() = EmailService.instance!!

    val calculatorsService: CalculatorsService
        get() = CalculatorsService.instance!!

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