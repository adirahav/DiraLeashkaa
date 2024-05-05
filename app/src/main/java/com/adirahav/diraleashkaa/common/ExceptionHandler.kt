package com.adirahav.diraleashkaa.common

import android.app.Activity
import java.io.IOException
import java.lang.Thread.*

class ExceptionHandler(app: Activity?) : UncaughtExceptionHandler {
    private val defaultUEH: UncaughtExceptionHandler = getDefaultUncaughtExceptionHandler()
    private var app: Activity? = null
    override fun uncaughtException(t: Thread, e: Throwable) {
        var arr = e.stackTrace
        var report = "${e}.trimIndent()"
        report += "--------- Stack trace ---------\n\n"
        for (i in arr.indices) {
            report += "${arr[i]}"
        }
        report += "-------------------------------\n\n"

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report += "--------- Cause ---------------\n\n"
        val cause = e.cause
        if (cause != null) {
            report += "${cause}.trimIndent()"
            arr = cause.stackTrace
            for (i in arr.indices) {
                report += "${arr[i]}"
            }
        }
        report += "-------------------------------\n\n"
        try {
            Utilities.log(
                logType = Enums.LogType.Crash,
                tag = this::class.java.simpleName,
                message = report,
                showToast = false,
            )

            /*val trace: FileOutputStream = app!!.openFileOutput(
                "stack.trace",
                Context.MODE_PRIVATE
            )
            trace.write(report.toByteArray())
            trace.close()*/
        } catch (ioe: IOException) {
            // ...
        }
        defaultUEH.uncaughtException(t, e)
    }

    init {
        this.app = app
    }
}