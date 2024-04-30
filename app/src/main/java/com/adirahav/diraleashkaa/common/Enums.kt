package com.adirahav.diraleashkaa.common

object Enums {

    enum class CodeError(val errorCode: Int) {
        WRONG_FORMAT(1),
        INVALID_USE(2),
        NOT_FOUND(3),
        INVALID_PARAMETER(4),
        SERVER_ERROR(5)
    }

    enum class ObserverAction {
        GET,
        GET_LOCAL,
        INSERT_LOCAL,apartment_type,
        CREATE,
        UPDATE_LOCAL,
        UPDATE,
        INSERT_UPDATE_ROOM,
        SAVE_SERVER,
        RESTORE_ROOM,
        RESTORE_PROPERTIES
    }

    enum class DBCaller {
        LOCAL,
        SERVER
    }

    enum class NetworkStatus {
        WIFI,
        MOBILE,
        ETHERNET,
        NOT_CONNECTED
    }

    enum class DialogType {
        NO_INTERNET,
        BETA_VERSION,
        EXPIRED_REGISTRATION,
        NEW_VERSION_AVAILABLE_NOT_REQUIRED,
        NEW_VERSION_AVAILABLE_REQUIRED,
        DATA_ERROR,
        SERVER_DOWN,
        RESTORE,
        BLOCKED,
        ANNOUNCEMENT,
        UNSUBSCRIBE
    }

    enum class LogType {
        Debug,
        Notify,
        Warning,
        Error,
        Crash
    }

    enum class UserPageType {
        PERSONAL_DETAILS,
        FINANCIAL_DETAILS,
        TERMS_OF_USE,
        COUPON_CODE
    }

    enum class SnackType {
        EXPIRED_PAID,
        EXPIRED_PAID_COUPON,
        EXPIRED_PAID_PAID_PROGRAM,
        EXPIRED_TRIAL,
        UNKNOWN
    }

    enum class RegistrationPageType {
        PAY_PROGRAM,
        COUPON_CODE
    }

    enum class ContactUsPageType {
        MAIL_FORM
    }

    enum class SubscriberType {
        TRIAL,
        COUPON_PAID,
        PAY_PROGRAM_PAID,
        BETA_TESTER,
        BLOCKED
    }
}