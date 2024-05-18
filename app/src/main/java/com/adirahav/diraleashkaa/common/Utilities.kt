package com.adirahav.diraleashkaa.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.BuildConfig.BASE_URL
import com.adirahav.diraleashkaa.BuildConfig.BUILD_TYPE
import com.adirahav.diraleashkaa.BuildConfig.FLAVOR
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration.AGE_PATTERN
import com.adirahav.diraleashkaa.common.Configuration.DECIMAL_PATTERN
import com.adirahav.diraleashkaa.common.Configuration.EMAIL_PATTERN
import com.adirahav.diraleashkaa.common.Configuration.PASSWORD_PATTERN
import com.adirahav.diraleashkaa.common.Configuration.PHONE_PATTERN
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.requests.ErrorReportRequest
import com.adirahav.diraleashkaa.ui.dialog.FancyDialog
import com.adirahav.diraleashkaa.ui.dialog.FancyDialogListener
import com.adirahav.diraleashkaa.views.LabelWithIcon
import com.adirahav.diraleashkaa.views.PropertyInput
import com.adirahav.diraleashkaa.views.PropertyPercent
import com.airbnb.paris.extensions.style
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Thread.sleep
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


object Utilities {

    const val TYPING_DELAY = 200L
    const val TAG = "Utilities"

    fun log(logType: Enums.LogType, tag: String, message: String, showToast: Boolean = true) {
        when (logType) {
            Enums.LogType.Debug -> {
                Log.d(tag, message)

                /*if (BuildConfig.DEBUG && showToast) {
                    Toast.makeText(context, Html.fromHtml("${"<font color='#00ff00' ><b>" + tag + "</b>: " + message + "</font>"}"), Toast.LENGTH_SHORT).show();
                }*/
            }

            Enums.LogType.Notify -> {
                Log.w(tag, message)

                if (BuildConfig.DEBUG && showToast) {
                    Toast.makeText(context, Html.fromHtml("${"<font color='#0000ff' ><b>" + tag + "</b>: " + message + "</font>"}"), Toast.LENGTH_SHORT).show()
                }
            }

            Enums.LogType.Warning -> {
                Log.w(tag, message)

                if (BuildConfig.DEBUG && showToast) {
                    Toast.makeText(context, Html.fromHtml("${"<font color='#ffcc00' ><b>" + tag + "</b>: " + message + "</font>"}"), Toast.LENGTH_SHORT).show()
                }
            }

            Enums.LogType.Error -> {
                Log.e(tag, message)

                if (BuildConfig.DEBUG && showToast) {
                    Toast.makeText(context, Html.fromHtml("${"<font color='#55eb4a7c' ><b>" + tag + "</b>: " + message + "</font>"}"), Toast.LENGTH_SHORT).show()
                }

                composeEmail(logType,"$tag", message)
            }

            Enums.LogType.Crash -> {
                Log.e(tag, message)

                if (BuildConfig.DEBUG && showToast) {
                    Toast.makeText(context, Html.fromHtml("${"<font color='#ff0000' ><b>" + tag + "</b>: " + message + "</font>"}"), Toast.LENGTH_SHORT).show()
                }

                composeEmail(logType,"$tag", message)
            }
        }
    }

    //region == email ==============

    fun composeEmail(logType: Enums.LogType, subject: String, message: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val call: Call<Void>? = DataManager.instance?.errorReportService?.errorReportAPI?.reportError(
                    ErrorReportRequest(
                            type = logType.toString().uppercase(Locale.getDefault()),
                            subject = subject,
                            message = message,
                            appEnv = BuildConfig.BUILD_TYPE.uppercase() ))

            call?.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {

                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    call.cancel()
                }
            })
        }
    }

    //endregion == email ==============

    //region == resolution =========

    fun dpToPx(context: Context, dp: Float) : Int {
        return dp.times(context.resources.displayMetrics.density).toInt()
    }

    fun pxToDp(context: Context, px: Float) : Int {
        return (px * context.resources.displayMetrics.density).toInt()
    }

    //endregion == resolution =========

    //region == form validation ====

    fun isPhoneValidp(hone: String): Boolean {
        return !TextUtils.isEmpty(hone) && PHONE_PATTERN.matcher(hone).matches()
    }

    fun isEmailValid(email: String): Boolean {
        //return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return !TextUtils.isEmpty(password) && PASSWORD_PATTERN.matcher(password).matches()
    }

    fun isYearOfBirthValid(yearOfBirth: String): Boolean {

        if (TextUtils.isEmpty(yearOfBirth)) {
            return false
        }

        val age = Calendar.getInstance().get(Calendar.YEAR).minus(yearOfBirth.toInt())
        return AGE_PATTERN.matcher(age.toString()).matches()
    }

    //endregion == form validation ====

    /*fun getDateTimeFormat(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)

        if (day == 11 || day == 12 || day == 13) {
            return Configuration.DATETIME_DISPLAY_PATTERN_OTHER
        }

        return when (day % 10) {
            1 -> Configuration.DATETIME_DISPLAY_PATTERN_ST
            2 -> Configuration.DATETIME_DISPLAY_PATTERN_ND
            3 -> Configuration.DATETIME_DISPLAY_PATTERN_RD
            else -> Configuration.DATETIME_DISPLAY_PATTERN_OTHER
        }
    }*/

    //region == find by name =======

    fun findStringByName(name: String): Int {
        val resources: Resources = context.resources
        val resourceID: Int = resources.getIdentifier(
            name, "string",
            context.packageName
        )
        return resourceID
    }

    fun findDrawableByName(name: String): Int {
        val resources: Resources = context.resources
        val resourceID: Int = resources.getIdentifier(
            name, "drawable",
            context.packageName
        )
        return resourceID
    }

    fun findColorByName(name: String): Int {
        return context.resources.getIdentifier(
            name, "color",
            context.packageName
        )
    }

    fun findDimenByName(name: String): Int {
        return com.adirahav.diraleashkaa.common.AppApplication.Companion.context.resources.getIdentifier(
            name, "dimen",
            context.packageName
        )
    }

    //endregion == find by name =======

    //region == environment ========

    fun showEnvironmentSnackMessageIfNeeded(activity: Activity) {
        if (FLAVOR.equals("develop")) {
            var message = AppPreferences.instance?.getString("userName", "") + "\n" + FLAVOR + " " + BUILD_TYPE + "\n" + BASE_URL

            val viewGroup: ViewGroup = (activity.findViewById(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
            val snackbar = Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG).setAction("Action", null)
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.formText))

            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackBackgroundSuccess))

            val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(ContextCompat.getColor(context, R.color.snackText))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            else {
                textView.gravity = Gravity.CENTER_HORIZONTAL
            }
            textView.textSize = 16f

            snackbar.show()
        }

    }

    //endregion == environment ========

    //region == numbers parser =====

    fun Float.percentToFraction(): Float {
        return  if (this != null)
            return this.div(100)
        else
            0f
    }

    fun Float.percentToMultipleFraction(): Float {
        return  if (this != null)
            return this.div(100).plus(1)
        else
            1.0f
    }

    fun Int.percentToFraction(): Double {
        return  if (this != null)
            return this.toDouble().div(100)
        else
            0.0
    }

    fun Double.percentToFraction(): Double {
        return  if (this != null)
            return this.div(100)
        else
            0.0
    }

    fun Float.fractionToPercent(): Int {
        return  if (this != null)
            return this.times(100).roundToInt()
        else
            0
    }

    fun Double.fractionToPercent(): Float {
        return  if (this != null)
            return this.times(100).toFloat()
        else
            0f
    }

    fun Float.percentFormat(digits: Int): String {
        val format = "%." + digits + "f"
        return format.format(this) + "%"
    }

    fun Double.fractionToPercentFormat(digits: Int): String {
        return  if (this != null)
            return this.times(100).toFloat().percentFormat(digits)
        else
            0.toFloat().percentFormat(digits)
    }

    fun Float.fractionToPercentFormat(digits: Int): String {
        return  if (this != null)
            return this.times(100).percentFormat(digits)
        else
            0.toFloat().percentFormat(digits)
    }

    fun Float.floatFormat(digits: Int): Float {
        val format = "%." + digits + "f"
        return format.format(this).toFloat()
    }

    fun Double.fractionToFloatFormat(digits: Int): Float {
        return  if (this != null)
            return this.times(100).toFloat().floatFormat(digits)
        else
            0.toFloat().floatFormat(digits)
    }

    fun Int?.toFormatNumber(): String {
        return  if (this != null)
            return String.format("%,d", this)
        else
            ""
    }
    //

    fun getDecimalNumber(number: Int?): String {
        if (number == null) {
            return ""
        }

        if (number == -1) {
            return ""
        }

        var formatter: DecimalFormat = DecimalFormat(DECIMAL_PATTERN)
        return formatter.format(number)
    }

    fun getPercentNumber(number: Float?): String {
        if (number == null) {
            return ""
        }

        if (number == -1.0f) {
            return ""
        }

       return number.toString()
    }

    fun Editable.toNumber(): Int? {
        return  if (this != null && this.isNotEmpty()) {
            try {
                if (this.equals("0"))
                    return 0
                else
                    this.toString().replace(",", "").replace("%", "").toInt()
            }
            catch (e: Exception) {
                null
            }
        }
        else
            null
    }

    fun String.toNumber(): Int? {
        return  if (this != null && this.isNotEmpty())
            this.toString().replace(",", "").replace("%", "").toInt()
        else
            null
    }

    fun Int.toNIS(): String {
        return getDecimalNumber(this) + " ש\"ח"
    }

    //endregion == numbers parser =====

    //region == buttons ============

    fun setButtonEnable(button: Button?) {
        button?.style(R.style.button)
        button?.isEnabled = true
    }

    fun setButtonDisable(button: Button?) {
        button?.style(R.style.buttonDisable)
        button?.isEnabled = false
    }

    //endregion == buttons ============

    //region == inputs =============

    fun setInputDisable(editText: EditText?) {
        editText?.style(R.style.formFieldDarkDisable)
        editText?.isEnabled = false
    }

    //endregion == inputs =============

    /////

    fun getVersionName() : String {
        var versionName = com.adirahav.diraleashkaa.BuildConfig.VERSION_NAME
        if (versionName.contains("-")) {
            versionName = versionName.split("-").get(0)
        }
        return versionName
    }

    fun typeSimulate(views: MutableMap<EditText?, String>) {
        for (view in views) {
            coroutineExample1(view.key, view.value, 0)
        }
    }

    fun coroutineExample1(view: EditText?, text: String, i: Int) = runBlocking {
        typingSimulate(view, text)
        sleep(TYPING_DELAY * text.length)
    }

    fun typingSimulate(view: EditText?, text: String) {

        view?.requestFocus()

        for (i in 1..view!!.length()) {
            view.setText(text.substring(0, i))
            view.setSelection(view.length())
            sleep(TYPING_DELAY)
        }


    }

    fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val resources: Resources = context.resources
            val resourceID: Int = resources.getIdentifier(
                fileName, "raw",
                context.packageName
            )

            val inputStream: InputStream = resources.openRawResource(resourceID)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    //region == get map value by key ==

    fun getMapStringValue(map: Map<String, Any?>?, key: String) : String? {
        val entities =  (map?.get("entities")) as Map<*, *>
        return if (entities.containsKey(key)) { entities[key].toString() } else { null }
    }

    fun getMapIntValue(map: Map<String, Any?>?, key: String) : Int? {
        val entities =  (map?.get("entities")) as Map<*, *>
        return if (entities.containsKey(key)) { entities[key].toString().toNumber() } else { null }
    }

    fun getMapFloatValue(map: Map<String, Any?>?, key: String) : Float? {
        val entities =  (map?.get("entities")) as Map<*, *>
        return if (entities.containsKey(key)) { entities[key].toString().toFloat() } else { null }
    }

    fun getMapLongValue(map: Map<String, Any?>?, key: String) : Long? {
        val entities =  (map?.get("entities")) as Map<*, *>
        return if (entities.containsKey(key)) { entities[key].toString().toLong()} else { null }
    }

    fun getMapBooleanValue(map: Map<String, Any?>?, key: String) : Boolean? {
        val entities =  (map?.get("entities")) as Map<*, *>
        return if (entities.containsKey(key)) { entities[key].toString().toBoolean()} else { null }
    }

    //endregion == get map value by key ==

    inline fun <reified T> parseArray(json: String?, typeToken: Type): T {
        val gson = GsonBuilder().create()
        return gson.fromJson(json, typeToken)
    }

    inline fun <reified T> parseObject(json: String?, typeToken: Type): T {
        val gson = GsonBuilder().create()
        return gson.fromJson(json, typeToken)
    }

    //////

    fun await(startTime: Date?, awaitSec: Int, response: (() -> Unit)?) {
        val currentTime = Date()

        //val diff: Long = startTime?.time!!.minus(currentTime.time)
        val diff: Long = currentTime.time.minus(startTime?.time!!)
        val seconds = diff / 1000

        GlobalScope.launch(Dispatchers.Main) {
            if (seconds < awaitSec) {
                delay((awaitSec - seconds).times(1000))
            }
            if (response != null) {
                response()
            }
        }
    }

    ///

    fun hideKeyboard(context: Context?) {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (context as Activity).currentFocus ?: return
        try {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
        catch (e: Exception) {

        }
    }

    ///
    private const val PICTURE_DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss_z"

    fun getImageUri(inContext: Context, bitmap: Bitmap): Uri? {
        val dateFormatter = SimpleDateFormat(
            PICTURE_DATE_FORMAT, Locale.getDefault()
        )
        val fileName = "dira_leashkaa_${dateFormatter.format(Date())}.png"

        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(inContext.contentResolver, bitmap, fileName, null)
        return Uri.parse(path)
    }

    fun getPathFromURI(_activity: Activity, contentURI: Uri): String? {
        val cursor: Cursor? = _activity.contentResolver?.query(contentURI, null, null, null, null)
        return if (cursor == null) { // Source is Dropbox or other similar local file path
            contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(idx)
        }
    }

    //region == dialog ================

    fun openFancyDialog(context: Context, dialogType: Enums.DialogType, responsePositive: (() -> Unit)?, responseNegative: (() -> Unit)?, messageArgs: Array<Any>): FancyDialog {

        if (localPhrase.isNullOrEmpty()) {
            return FancyDialog.Builder(context)
                .setTitle(findStringByName("dialog_${dialogType.name.lowercase()}_title"))
                .setMessage(AppApplication.context.getString(findStringByName("dialog_${dialogType.name.lowercase()}_message"), *messageArgs))
                .setPositiveBtnText(findStringByName("dialog_${dialogType.name.lowercase()}_positive"))
                .setNegativeBtnText(findStringByName("dialog_${dialogType.name.lowercase()}_negative"))
                .setGifResource(findDrawableByName("anim_dialog_${dialogType.name.lowercase()}"))
                .setBackgroundColor(findColorByName("dialog_background_${dialogType.name.lowercase()}"))
                .isCancellable(
                    !(dialogType == Enums.DialogType.NO_INTERNET ||
                            dialogType == Enums.DialogType.EXPIRED_REGISTRATION ||
                            dialogType == Enums.DialogType.NEW_VERSION_AVAILABLE_REQUIRED)
                )
                .onPositiveClicked(
                    if (responsePositive != null) {
                        object : FancyDialogListener {
                            override fun onClick() {
                                responsePositive()
                            }
                        }
                    } else null)
                .onNegativeClicked(
                    if (responseNegative != null) {
                        object : FancyDialogListener {
                            override fun onClick() {
                                responseNegative()
                            }
                        }
                    } else null)
                .build()
        }
        else {
            return FancyDialog.Builder(context)
                .setTitle(getLocalPhrase("dialog_${dialogType.name.lowercase()}_title"))
                .setMessage(String.format(getLocalPhrase("dialog_${dialogType.name.lowercase()}_message"), *messageArgs))
                .setPositiveBtnText(getLocalPhrase("dialog_${dialogType.name.lowercase()}_positive"))
                .setNegativeBtnText(getLocalPhrase("dialog_${dialogType.name.lowercase()}_negative"))
                .setGifResource(findDrawableByName("anim_dialog_${dialogType.name.lowercase()}"))
                .setBackgroundColor(findColorByName("dialog_background_${dialogType.name.lowercase()}"))
                .isCancellable(
                    !(dialogType == Enums.DialogType.NO_INTERNET ||
                            dialogType == Enums.DialogType.EXPIRED_REGISTRATION ||
                            dialogType == Enums.DialogType.NEW_VERSION_AVAILABLE_REQUIRED)
                )
                .onPositiveClicked(
                    if (responsePositive != null) {
                        object : FancyDialogListener {
                            override fun onClick() {
                                responsePositive()
                            }
                        }
                    } else null)
                .onNegativeClicked(
                    if (responseNegative != null) {
                        object : FancyDialogListener {
                            override fun onClick() {
                                responseNegative()
                            }
                        }
                    } else null)
                .build()
        }
    }

    fun openTrackUserDialog(context: Context, valueLength: Int?, responsePositive: ((String) -> Unit), responseNegative: (() -> Unit)) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_track_user, null)
        val input = view.findViewById<View>(R.id.input) as EditText

        builder.setView(view)

        builder.setOnDismissListener { _ ->
            responseNegative()
        }

        val dialog: AlertDialog = builder.show()

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == valueLength) {
                    dialog.dismiss()
                    responsePositive(input.text.toString())
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        dialog.show()
    }

    //endregion == dialog ================

    //region == snack message =========

    fun getSnackMessage(snackType: Enums.SnackType, expiredTimeUTC: Long) : String? {

        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        val diffInMs: Long = expiredTimeUTC.minus(nowUTC.timeInMillis)
        val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
        val diffInMinutes: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMs)
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMs)
        val diffInDays: Long = TimeUnit.MILLISECONDS.toDays(diffInMs)

        if (diffInSec <= 0) {
            return "EXPIRED"
        }

        //val count = fixedParametersData?.expirationAlertArray?.find { it.key == "count" }?.value?.toInt() ?: 1
        //val units = fixedParametersData?.expirationAlertArray?.find { it.key == "units" }?.value ?: "h"

        val leftTime =
            if (diffInSec == 1L)
                Utilities.getLocalPhrase("snack_expired_second")
            else if (diffInSec < 60L)
                String.format(Utilities.getLocalPhrase("snack_expired_seconds"), diffInSec)
            else if (diffInMinutes == 1L)
                Utilities.getLocalPhrase("snack_expired_minute")
            else if (diffInMinutes < 60L)
                String.format(Utilities.getLocalPhrase("snack_expired_minutes"), diffInMinutes)
            else if (diffInHours == 1L)
                Utilities.getLocalPhrase("snack_expired_hour")
            else if (diffInHours == 2L)
                Utilities.getLocalPhrase("snack_expired_two_hours")
            else if (diffInHours < 24L)
                String.format(Utilities.getLocalPhrase("snack_expired_hours"), diffInHours)
            /*else if (diffInDays == 1L)
                context.resources.getString(R.string.snack_expired_day)
            else if (diffInDays == 2L)
                context.resources.getString(R.string.snack_expired_two_days)
            else if (diffInDays < 60L)
                String.format(context.resources.getString(R.string.snack_expired_days), diffInDays)*/
            else
                null

        if (leftTime == null) {
            return null
        }

        return String.format(
            getLocalPhrase("snack_${snackType.toString().lowercase()}"),
            leftTime)

    }

    fun showSnackMessageIfNeeded(activity: Activity, snackType: Enums.SnackType, expiredTimeUTC: Long) {

        if (snackType == Enums.SnackType.EXPIRED_PAID_COUPON || snackType == Enums.SnackType.EXPIRED_TRIAL) {
            val snackMessage = getSnackMessage(snackType, expiredTimeUTC)

            /*if (snackMessage.equals("EXPIRED")) {
                SplashActivity.start(activity)
            }*/

            if (snackMessage.isNullOrEmpty()) {
                return
            }

            val preferences = AppPreferences.instance
            val expiredDialogHasShown = preferences?.getBoolean("expiredDialogHasShown", false)
            if (expiredDialogHasShown!!) {
                return
            }
            preferences.setBoolean("expiredDialogHasShown", true, isAsync = false)

            //////

            val viewGroup: ViewGroup = (activity.findViewById(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup

            val snackbar = Snackbar.make(viewGroup, snackMessage, Snackbar.LENGTH_LONG).setAction("Action", null)
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.formText))

            val params: FrameLayout.LayoutParams = viewGroup.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            viewGroup.layoutParams = params

            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackBackgroundWarnning))

            val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            textView.setTextColor(ContextCompat.getColor(context, R.color.snackText))
            textView.textSize = 16f

            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_error, 0)
            textView.compoundDrawablePadding = activity.resources.getDimensionPixelOffset(R.dimen.padding)

            snackbar.show()
        }
    }

    fun showOfflineIndicationIfNeeded(activity: Activity) {


    }

    //endregion == snack message =========

    //region == device type ===========

    fun getDeviceType(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(str: String?): String {
        if (str == null || str.isEmpty()) {
            return ""
        }
        val first = str[0]
        return  if (Character.isUpperCase(first)) {
                    str
                }
                else {
                    Character.toUpperCase(first).toString() + str.substring(1)
                }
    }

    //endregion == device type ===========

    //region == internet connection ===

    fun getNetworkStatus() : Enums.NetworkStatus {

        var networkStatus : Enums.NetworkStatus = Enums.NetworkStatus.NOT_CONNECTED
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return Enums.NetworkStatus.NOT_CONNECTED
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return Enums.NetworkStatus.NOT_CONNECTED
            networkStatus = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Enums.NetworkStatus.WIFI
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Enums.NetworkStatus.MOBILE
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> Enums.NetworkStatus.ETHERNET
                else -> Enums.NetworkStatus.NOT_CONNECTED
            }
        } else {
            connectivityManager.run {
                activeNetworkInfo?.run {
                    networkStatus = when (type) {
                        ConnectivityManager.TYPE_WIFI -> Enums.NetworkStatus.WIFI
                        ConnectivityManager.TYPE_MOBILE -> Enums.NetworkStatus.MOBILE
                        ConnectivityManager.TYPE_ETHERNET -> Enums.NetworkStatus.ETHERNET
                        else -> Enums.NetworkStatus.NOT_CONNECTED
                    }
                }
            }
        }

        return networkStatus
    }

    //endregion == internet connection ===

    //region == phrases ===============

    var localPhrase: ArrayList<PhraseEntity>? = null

    fun getLocalPhrase(key: String) : String {
        if (localPhrase?.find { it.key == key }?.value != null) {
            return localPhrase?.find { it.key == key }?.value.toString()
        }

        return key
    }

    fun setTextViewPhrase(textView: TextView?, key: String) {
        textView?.text = getLocalPhrase(key)
    }

    fun setTextViewHtml(textView: TextView?, key: String) {
        if (localPhrase?.find { it.key == key }?.value != null) {
            textView?.text =
                HtmlCompat.fromHtml(getLocalPhrase(key), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    fun setLabelViewString(labelView: LabelWithIcon?, textKey: String, tooltipKey: String? = null) {
        labelView?.setLabelText(getLocalPhrase(textKey))

        if (tooltipKey != null && getLocalPhrase(tooltipKey).isNotEmpty()) {
            labelView?.setTooltipText(getLocalPhrase(tooltipKey))
        }
    }

    fun setInputViewPhrase(inputView: PropertyInput?, textKey: String, textWithoutValueKey: String? = null, warningKey: String? = null) {
        inputView?.setInputLabelText(getLocalPhrase(textKey))

        if (textWithoutValueKey != null) {
            inputView?.setInputLabelWithoutValueText(getLocalPhrase(textWithoutValueKey))
        }

        if (warningKey != null) {
            inputView?.setInputWarningText(getLocalPhrase(warningKey))
        }
    }

    fun setPropertyInputPhrase(inputView: PropertyInput?, textKey: String, textWithoutValueKey: String? = null, warningKey: String? = null) {
        inputView?.setInputLabelText(getLocalPhrase(textKey))

        if (textWithoutValueKey != null) {
            inputView?.setInputLabelWithoutValueText(getLocalPhrase(textWithoutValueKey))
        }

        if (warningKey != null) {
            inputView?.setInputWarningText(getLocalPhrase(warningKey))
        }
    }

    fun setPropertyPercentViewString(inputView: PropertyPercent?, textKey: String) {
        inputView?.setInputLabelText(getLocalPhrase(textKey))
    }
    //endregion == phrases ===============

    data class BitmapSize(val width: Int, val height: Int)

    fun setPropertyPicture(view: ImageView?, pictures: String?, pictureSizeRatio: Float, pictureWidth: Int, pictureHeight: Int, defaultPicture: Int? = null) {
        var isPictureExist = !pictures.isNullOrEmpty()

        if (isPictureExist) {
            /*var jsonPictures: JSONArray? = null
            try {
                jsonPictures = JSONArray(pictures)
            }
            catch (e: Exception) {
                jsonPictures = JSONArray()
                jsonPictures.put(pictures)
            }

            if (jsonPictures != null && jsonPictures.length() > 0) {
                val uri =  Uri.parse(jsonPictures.get(0).toString())

                isPictureExist = try {
                    val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
                    inputStream != null
                }
                catch (e: IOException) {
                    log(Enums.LogType.Error, TAG, "setPropertyPicture(): uri = ${uri}. IOException = ${e.message}")
                    //isPictureExist = false
                    false
                }
                catch (e: SecurityException) {
                    log(Enums.LogType.Error, TAG, "setPropertyPicture(): uri = ${uri}. SecurityException = ${e.message}")
                    //isPictureExist = false
                    false
                }
                catch (e: Exception) {
                    log(Enums.LogType.Error, TAG, "setPropertyPicture(): uri = ${uri}. Exception = ${e.message}")
                    //isPictureExist = false
                    false
                }

                if (isPictureExist) {
                    var bitmap =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                        else
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

                    val bitmapSize = resizePicture(
                        pictureSizeRatio,
                        pictureWidth,
                        pictureHeight,
                        bitmap.width,
                        bitmap.height
                    )

                    bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmapSize.width, bitmapSize.height)
                    view.setImageBitmap(bitmap)
                }
            }*/

            if (pictures.isNullOrBlank() == false) {
                Picasso.with(context)
                    .load(pictures)
                    .into(view)
            }
        }

        if (!isPictureExist) {
            if (defaultPicture != null) {
                view?.setImageBitmap(BitmapFactory.decodeResource(context.resources, defaultPicture))
            }
            else {
                view?.visibility = GONE
            }
        }
    }

    private fun resizePicture(ratio: Float, pictureWidth: Int, pictureHeight: Int, bitmapWidth: Int, bitmapHeight: Int) : BitmapSize {

        var bitmapResizeWidth = bitmapWidth
        var bitmapResizeHeight = bitmapHeight

        val oppositeRatio = 1.div(ratio)
        val bitmapRatio: Float = bitmapHeight.div(bitmapWidth.toFloat())

        when {
            bitmapWidth > bitmapHeight ->
                when {
                    bitmapRatio > ratio -> {
                        bitmapResizeWidth = if (pictureWidth > bitmapWidth) bitmapWidth else pictureWidth
                        bitmapResizeHeight = bitmapResizeWidth.times(ratio).toInt()
                    }
                    bitmapRatio < ratio -> {
                        bitmapResizeHeight = if (pictureWidth > bitmapWidth) bitmapHeight else pictureHeight
                        bitmapResizeWidth = bitmapResizeHeight.times(oppositeRatio).toInt()

                    }
                    bitmapRatio == ratio -> {
                        bitmapResizeWidth = if (pictureWidth > bitmapWidth) bitmapWidth else pictureWidth
                        bitmapResizeHeight = if (pictureWidth > bitmapWidth) bitmapHeight else pictureHeight
                    }
                }

            bitmapWidth < bitmapHeight ->
                when {
                    bitmapRatio > oppositeRatio -> {
                        bitmapResizeWidth = bitmapWidth
                        bitmapResizeHeight = bitmapWidth.times(ratio).toInt()
                    }
                    bitmapRatio < oppositeRatio -> {
                        bitmapResizeWidth = if (pictureHeight > bitmapHeight) bitmapWidth else pictureWidth
                        bitmapResizeHeight = bitmapResizeWidth.times(ratio).toInt()
                    }
                    bitmapRatio == oppositeRatio -> {
                        bitmapResizeWidth = if (pictureWidth > bitmapWidth) bitmapWidth else pictureWidth
                        bitmapResizeHeight = if (pictureWidth > bitmapWidth) bitmapResizeWidth.times(ratio).toInt() else pictureHeight
                    }
                }

            bitmapWidth == bitmapHeight -> {
                bitmapResizeWidth = if (pictureWidth < bitmapWidth) pictureWidth else bitmapWidth
                bitmapResizeHeight = bitmapResizeWidth.times(ratio).toInt()
            }
        }

        return BitmapSize(bitmapResizeWidth, bitmapResizeHeight)
    }

    fun displayActionSnackbar(activity: Activity, message: String) {
        val viewGroup: ViewGroup = (activity.findViewById(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        val snackbar = Snackbar.make(viewGroup, message, Snackbar.LENGTH_LONG).setAction("Action", null)
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.formText))

        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackBackgroundSuccess))

        val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(context, R.color.snackText))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
        else {
            textView.gravity = Gravity.CENTER_HORIZONTAL
        }
        textView.textSize = 16f

        snackbar.show()
    }

    ///
    @SuppressLint("HardwareIds")
    fun getDeviceID(context: Context): String {
        val androidID = Settings.Secure.ANDROID_ID

        return androidID
    }

    fun responseDataFixedParametersFormat(data: String) : String {
        return data.replace("=[", "=\"[")
            .replace("],", "]\",")
            .replace("={", "=\"{")
            .replace("}, ", "}\", ")
            .dropLast(1)
            .plus("\"}")
    }

    fun responseDataRestoreFormat(data: String) : String {
        return data.replace("=", "=\"")
            .replace("],", "]\",")
            .replace(", ", "\", ")
            .dropLast(3)
            .plus("\"}]\"")
    }

    fun getAppID(fixedParameters: FixedParameters?) : String {
        val appURL = fixedParameters?.appVersionArray?.find { it.key == "url" }?.value ?: ""
        val appURLArr = appURL.split("?id=")
        val appID = if (appURLArr.size == 2) appURLArr[1] else ""
        return appID
    }

    fun String.camelToSnakeCase(): String {
        val pattern = "(?<=.)[A-Z]".toRegex()
        return this.replace(pattern, "_$0").lowercase()
    }
}