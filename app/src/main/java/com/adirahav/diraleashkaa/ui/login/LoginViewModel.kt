package com.adirahav.diraleashkaa.ui.login

import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.requests.LoginRequest
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.data.network.services.AuthService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel internal constructor(
        private val activity: LoginActivity,
        private val authService: AuthService,
) : BaseViewModel() {

    private val TAG = "LoginViewModel"

    // shared preferences
    var preferences: AppPreferences? = null

    // logging user
    val loginCallback: MutableLiveData<UserEntity> = MutableLiveData()

    init {
        // shared preferences
        preferences = AppPreferences.instance
    }

    fun submitLogin(loginData: MutableMap<String, Any?>) {
        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserResponse?>? = authService.authAPI.login(buildSignInRequest(loginData))

            call?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "signupUser(): response = $response")

                    val result: UserResponse? = response.body()

                    if (response.code() == 200) {
                        try {
                            if (!result?.token.isNullOrEmpty()) {
                                activity.userToken = result?.token
                                activity.preferences!!.setString("token", activity.userToken, false)

                                val localUser = UserEntity(
                                        email = result?.email,
                                        fullname = result?.fullname,
                                        yearOfBirth = result?.yearOfBirth,
                                        equity = result?.equity,
                                        incomes = result?.incomes,
                                        commitments = result?.commitments,
                                        termsOfUseAccept = result?.termsOfUseAccept,
                                        registrationExpiredTime = result?.registrationExpiredTime,
                                        subscriberType = result?.subscriberType,
                                        calcCanTakeMortgage = result?.calcCanTakeMortgage,
                                        calcAge = result?.calcAge
                                )
                                setLogin(localUser)
                            }
                            else {
                                setLogin(null)
                            }

                        } catch (e: Exception) {
                            setLogin(null)
                            Utilities.log(Enums.LogType.Error, TAG, "setLogin(): e = ${e.message} ; result.data = ${result?.toString()}")
                        }
                    }
                    else {
                        setLogin(null)
                        Utilities.log(Enums.LogType.Error, TAG, "setLogin(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    setLogin(null)
                    Utilities.log(Enums.LogType.Error, TAG, "setLogin(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    fun buildSignInRequest(userData: Map<String, Any?>?) : LoginRequest {
        return LoginRequest(
            platform = "android",
            email = Utilities.getMapStringValue(userData, "email"),
            password = Utilities.getMapStringValue(userData, "password"),
            appDeviceType = Utilities.getDeviceType(),
            appVersion = BuildConfig.VERSION_NAME
        )
    }
    private fun setLogin(response: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLogin()", showToast = false)
        this.loginCallback.postValue(response)
    }

}