package com.adirahav.diraleashkaa.common

import java.text.SimpleDateFormat
import java.util.Locale.*
import java.util.regex.Pattern

object Configuration {
    const val DB_NAME = "database-diraleashkaa"


    const val TYPING_RESPONSE_DELAY = 800L   // millisecond

    const val ROOM_AWAIT_MILLISEC = 10L   // millisecond

    ///

    private const val EXPIRED_REGISTRATION_DATE_FORMAT = "dd/MM/yyyy"
    private const val EXPIRED_REGISTRATION_TIME_FORMAT = "HH:mm"
    private const val DATETIME_LOG_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    val dateFormatter = SimpleDateFormat(EXPIRED_REGISTRATION_DATE_FORMAT, getDefault())
    val timeFormatter = SimpleDateFormat(EXPIRED_REGISTRATION_TIME_FORMAT, getDefault())
    val dateTimeLogFormatter = SimpleDateFormat(DATETIME_LOG_FORMAT, getDefault())

    /*const val DATETIME_ROOM_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    const val DATETIME_DISPLAY_PATTERN_ST = "MMMM d'st', yyyy"
    const val DATETIME_DISPLAY_PATTERN_ND = "MMMM d'nd', yyyy"
    const val DATETIME_DISPLAY_PATTERN_RD = "MMMM d'rd', yyyy"
    const val DATETIME_DISPLAY_PATTERN_OTHER = "MMMM d'th', yyyy"*/

    const val DECIMAL_PATTERN = "#,###,###"

    val PHONE_PATTERN = Pattern.compile(
        "^[0][5]\\d{1}(\\-)\\d{7}$"
    )

    val EMAIL_PATTERN = Pattern.compile(
        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+\$"
    )

    val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"      // Minimum 8 characters ; at least one letter ; and one number
    )

    val AGE_PATTERN = Pattern.compile(
        "^(?:1[01][0-9]|120|1[7-9]|[2-9][0-9])$"        // 17-120: 1[7-9] covers numbers between 17 and 19 ; [2-9][0-9] covers numbers between 20 and 99 ; 1[01][0-9] covers numbers between 100 and 119 ; and 120 covers the number 120
    )

}