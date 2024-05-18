package com.adirahav.diraleashkaa.ui.signup

import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.requests.SignUpRequest
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.data.network.services.AuthService
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.PhraseService
import com.adirahav.diraleashkaa.data.network.services.UserService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class SignUpViewModel internal constructor(
        private val activity: SignUpActivity,
        private val authService: AuthService,
        private val userService: UserService,
        private val registrationService: RegistrationService,
        private val phraseService: PhraseService,
) : BaseViewModel() {

    private val TAG = "SignUpViewModel"

    // fixed parameters
    val fixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // coupon registration
    val couponRegistration: MutableLiveData<UserEntity> = MutableLiveData()

    // skip registration
    val skipRegistrationCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // pay registration
    val payProgramRegistration: MutableLiveData<UserEntity> = MutableLiveData()

    // logging user
    val setSignupCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // terms of use
    val termsOfUse: MutableLiveData<PhraseEntity> = MutableLiveData()


    //region == fixed parameters ==============
    fun getLocalFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setLocalFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setLocalFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalFixedParameters()", showToast = false)
        this.fixedParametersCallback.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    fun buildSignUpRequest(userData: Map<String, Any?>?) : SignUpRequest {

        return SignUpRequest(
            platform = "android",
            email = Utilities.getMapStringValue(userData, "email"),
            password = Utilities.getMapStringValue(userData, "password"),
            fullname = Utilities.getMapStringValue(userData, "fullname"),
            yearOfBirth = Utilities.getMapIntValue(userData, "yearOfBirth"),
            equity = Utilities.getMapIntValue(userData, "equity"),
            incomes = Utilities.getMapIntValue(userData, "incomes"),
            commitments = Utilities.getMapIntValue(userData, "commitments"),
            termsOfUseAccept = Utilities.getMapBooleanValue(userData, "termsOfUseAccept"),
            appDeviceId = Settings.Secure.getString(AppApplication.context.contentResolver, Utilities.getDeviceID(context)),
            appDeviceType = Utilities.getDeviceType(),
            appVersion = BuildConfig.VERSION_NAME
        )
    }

    fun signupUser(userData: Map<String, Any?>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "signupUser()")

        Utilities.log(Enums.LogType.Notify, TAG, "NEW USER SIGN UP: ${Utilities.getMapStringValue(userData, "email")}")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserResponse?>? =
                    if (activity.loggingUser?.email.isNullOrEmpty() || activity.userToken.isNullOrEmpty()) {
                        authService.authAPI.signup(buildSignUpRequest(userData))
                    }
                    else {
                        userService.userAPI.update("Bearer ${activity.userToken}", buildSignUpRequest(userData))
                    }
            call?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "signupUser(): response = $response")

                    val result: UserResponse? = response.body()

                    if (response.code() == 200) {
                        try {
                            if (!result?.token.isNullOrEmpty()) {
                                activity.userToken = result?.token
                                activity.preferences!!.setString("token", activity.userToken, false)
                            }

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

                            setSignup(localUser)
                        } catch (e: Exception) {
                            setSignup(null)
                            Utilities.log(Enums.LogType.Error, TAG, "signupUser(): e = ${e.message} ; result.data = ${result?.toString()}")
                        }
                    }
                    else if (response.code() == 400 && activity.currentStepProgressBar == 1) {
                        activity.personalInfoFragment.layout?.emailError?.visibility = View.VISIBLE
                        Utilities.setTextViewPhrase(activity.personalInfoFragment.layout?.emailError, "signup_email_taken_error")
                        setSignup(null)
                    }
                    else {
                        setSignup(null)
                        Utilities.log(Enums.LogType.Error, TAG, "signupUser(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    setSignup(null)
                    Utilities.log(Enums.LogType.Error, TAG, "signupUser(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setSignup(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSignup()", showToast = false)
        this.setSignupCallback.postValue(userData)
    }

    //endregion == user ==========================

    //region == registration ==================

    fun couponRegistration(code: String) {
        Utilities.log(Enums.LogType.Debug, TAG, "couponRegistration()")

        CoroutineScope(Dispatchers.IO).launch {
            val call: Call<UserResponse?>? = registrationService.registrationAPI.coupon(
                    "Bearer ${activity.userToken}", code)

            call?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "couponRegistration(): response = $response")
                    val result: UserResponse? = response.body()

                    if (response.code() == 200) {
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

                        setCouponRegistration(localUser)
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "couponRegistration(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                        activity.couponCodeFragment.layout?.codeError?.visibility = View.VISIBLE
                        activity.couponCodeFragment.layout?.codeError?.text = Utilities.getLocalPhrase("signup_code_error")

                        setCouponRegistration(null)
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    setCouponRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "couponRegistration(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setCouponRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setCouponRegistration()", showToast = false)
        this.couponRegistration.postValue(userData)
    }

    fun skipRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "skipRegistration()")
        //SKIP-5
        setSkipRegistration(userData)
    }

    private fun setSkipRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSkipRegistration(): userData = ${userData.toString()}", showToast = false)
        //SKIP-6
        this.skipRegistrationCallback.postValue(userData)
    }

    //endregion == registration ==================

    //region == pay program registration ===
    fun payProgramRegistration(applicationContext: Context, userData: UserEntity?, programPayId: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "payProgramRegistration()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserResponse?>? = registrationService.registrationAPI.payProgram(
                    "Bearer ${activity.userToken}", programPayId!!)


            call?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "payProgramRegistration(): response = ${response}")
                    val result: UserResponse? = response.body()

                    if (response.code() == 200) {
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

                        setPayProgramRegistration(localUser)
                        Utilities.log(Enums.LogType.Notify, TAG, "payProgramRegistration(): Success")
                    }
                    else {
                        setPayProgramRegistration(null)
                        Utilities.log(Enums.LogType.Error, TAG, "payProgramRegistration(): Error = ${response}")
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    setPayProgramRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "payProgramRegistration(): onFailure = ${t}")
                    call.cancel()
                }
            })
        }
    }

    private fun setPayProgramRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setPayProgramRegistration()", showToast = false)
        this.payProgramRegistration.postValue(userData)
    }
    //endregion == pay program registration ======

    //region == terms of use ==================

    fun getTermsOfUse() {
        Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse()")

        CoroutineScope(Dispatchers.IO).launch {
            val call: Call<PhraseEntity?>? = phraseService.phraseAPI.getPhrase("signup_terms_of_use_text")

            call?.enqueue(object : Callback<PhraseEntity?> {
                override fun onResponse(call: Call<PhraseEntity?>, response: Response<PhraseEntity?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse(): response = $response")
                    val result: PhraseEntity? = response.body()

                    if (response.code() == 200) {
                        setTermsOfUse(result)
                    }
                    else {
                        setTermsOfUse(null)
                        Utilities.log(Enums.LogType.Error, TAG, "getTermsOfUse(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<PhraseEntity?>, t: Throwable) {
                    setTermsOfUse(null)
                    Utilities.log(Enums.LogType.Error, TAG, "getTermsOfUse(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setTermsOfUse(content: PhraseEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setTermsOfUse()", showToast = false)
        this.termsOfUse.postValue(content)
    }

    //endregion == terms of use ===================

}