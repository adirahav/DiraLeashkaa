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
        GET_SERVER,
        GET_ROOM,
        INSERT_ROOM,apartment_type,
        INSERT_SERVER,
        UPDATE_ROOM,
        UPDATE_SERVER,
        INSERT_UPDATE_ROOM,
        INSERT_UPDATE_SERVER,
        RESTORE_ROOM,
        RESTORE_PROPERTIES
    }

    enum class DBCaller {
        ROOM,
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
        COUPON_CODE,
        GOOGLE_PAY
    }

    enum class SnackType {
        EXPIRED_PAID,
        EXPIRED_PAID_COUPON,
        EXPIRED_PAID_GOOGLE_PAY,
        EXPIRED_TRIAL,
        UNKNOWN
    }

    enum class RegistrationPageType {
        PAY_PROGRAM,
        COUPON_CODE,
        BETA_CODE
    }

    enum class ContactUsPageType {
        MAIL_FORM
    }

    enum class SubscriberType {
        TRIAL,
        COUPON_PAID,
        GOOGLE_PAY_PAID,
        BETA_TESTER,
        BLOCKED
    }
}