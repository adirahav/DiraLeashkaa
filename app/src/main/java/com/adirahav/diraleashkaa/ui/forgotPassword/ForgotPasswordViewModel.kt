package com.adirahav.diraleashkaa.ui.forgotPassword

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.services.ForgotPasswordService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import kotlin.concurrent.schedule

class ForgotPasswordViewModel internal constructor(
        private val activity: ForgotPasswordActivity,
        private val forgotPasswordService: ForgotPasswordService,
) : BaseViewModel() {

    private val TAG = "ForgotPasswordViewModel"

    // shared preferences
    var preferences: AppPreferences? = null

    // fixed parameters
    val fixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // generate code
    val generateCodeCallback: MutableLiveData<JsonObject> = MutableLiveData()

    // validate code
    val validateCodeCallback: MutableLiveData<JsonObject> = MutableLiveData()

    // change password
    val changePasswordCallback: MutableLiveData<JsonObject> = MutableLiveData()

    init {
        // shared preferences
        preferences = AppPreferences.instance
    }

    fun generateCode(forgotPasswordData: Map<String, Any?>?) {
        CoroutineScope(Dispatchers.IO).launch {

            val email = Utilities.getMapStringValue(forgotPasswordData, "email")
            val parameters = JSONObject().apply {
                put("email", email)
            }

            val call: Call<JsonObject?>? = forgotPasswordService.forgotPasswordAPI.generateCode(parameters)

            call?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "generateCode(): response = $response")

                    val result: JsonObject? = response.body()

                    if (response.code() == 200) {
                        try {
                            if (!result?.get("token")?.asString.isNullOrEmpty()) {
                                activity.userToken = result?.get("token")?.asString
                                activity.userEmail = email
                                setGenerateCodeCallback(result)
                            }
                            else {
                                setGenerateCodeCallback(null)
                            }

                        } catch (e: Exception) {
                            setGenerateCodeCallback(null)
                            Utilities.log(Enums.LogType.Error, TAG, "generateCode(): e = ${e.message} ; result.data = ${result?.toString()}")
                        }
                    }
                    else {
                        setGenerateCodeCallback(null)
                        Utilities.log(Enums.LogType.Error, TAG, "generateCode(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    setGenerateCodeCallback(null)
                    Utilities.log(Enums.LogType.Error, TAG, "setForgotPasswordCallback(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    fun codeValidation(codeData: Map<String, Any?>?) {
        CoroutineScope(Dispatchers.IO).launch {

            val parameters = JSONObject().apply {
                put("code", Utilities.getMapStringValue(codeData, "code"))
            }

            val call: Call<JsonObject?>? = forgotPasswordService.forgotPasswordAPI.validateCode("Bearer ${activity.userToken}", parameters)

            call?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "codeValidation(): response = $response")

                    val result: JsonObject? = response.body()

                    if (response.code() == 200) {
                        try {
                            if (result?.get("verified") != null) {
                                activity.userToken = result.get("token")?.asString
                                setCodeValidationCallback(result)
                            }
                            else {
                                setCodeValidationCallback(null)
                            }

                        } catch (e: Exception) {
                            setCodeValidationCallback(null)
                            Utilities.log(Enums.LogType.Error, TAG, "codeValidation(): e = ${e.message} ; result.data = ${result?.toString()}")
                        }
                    }
                    else {
                        setCodeValidationCallback(null)
                        Utilities.log(Enums.LogType.Error, TAG, "codeValidation(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    setCodeValidationCallback(null)
                    Utilities.log(Enums.LogType.Error, TAG, "codeValidation(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    fun changePassword(forgotPasswordData: Map<String, Any?>?) {
        CoroutineScope(Dispatchers.IO).launch {

            val parameters = JSONObject().apply {
                put("newPassword", Utilities.getMapStringValue(forgotPasswordData, "newPassword"))
            }

            val call: Call<JsonObject?>? = forgotPasswordService.forgotPasswordAPI.changePassword("Bearer ${activity.userToken}", parameters)

            call?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "changePassword(): response = $response")

                    val result: JsonObject? = response.body()

                    if (response.code() == 200) {
                        try {
                            if (result?.get("passwordHasChanged") != null) {
                                setChangePasswordCallback(result)
                            }
                            else {
                                setChangePasswordCallback(null)
                            }

                        } catch (e: Exception) {
                            setChangePasswordCallback(null)
                            Utilities.log(Enums.LogType.Error, TAG, "changePassword(): e = ${e.message} ; result.data = ${result?.toString()}")
                        }
                    }
                    else {
                        setChangePasswordCallback(null)
                        Utilities.log(Enums.LogType.Error, TAG, "changePassword(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    setGenerateCodeCallback(null)
                    Utilities.log(Enums.LogType.Error, TAG, "changePassword(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    //region == fixed parameters ==============
    fun getLocalFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setRoomFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setRoomFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomFixedParameters()", showToast = false)
        this.fixedParametersCallback.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    private fun setGenerateCodeCallback(response: JsonObject?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setGenerateCodeCallback()", showToast = false)
        this.generateCodeCallback.postValue(response)
    }

    private fun setCodeValidationCallback(response: JsonObject?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setCodeValidationCallback()", showToast = false)
        this.validateCodeCallback.postValue(response)
    }

    private fun setChangePasswordCallback(response: JsonObject?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setChangePasswordCallback()", showToast = false)
        this.changePasswordCallback.postValue(response)
    }

}