package com.adirahav.diraleashkaa.ui.signup

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.StringsService
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
    private val userService: UserService,
    private val registrationService: RegistrationService,
    private val stringsService: StringsService,
) : BaseViewModel() {

    private val TAG = "SignUpViewModel"

    // fixed parameters
    val roomFixedParametersGet: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // coupon registration
    val couponRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // skip registration
    val skipRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // beta registration
    val betaRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // sms code validation
    val smsCodeValidation: MutableLiveData<SMSCodeValidationModel> = MutableLiveData()

    // google pay registration
    val googlePayRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // trial registration
    val trialRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // user
    val serverUserInsertUpdateServer: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserInsertUpdateRoom: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserInsertUpdateServer: MutableLiveData<UserEntity> = MutableLiveData()

    // terms of use
    val termsOfUse: MutableLiveData<StringModel> = MutableLiveData()


    //region == fixed parameters ==============
    fun getRoomFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.ROOM_AWAIT_MILLISEC) {
                setRoomFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setRoomFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomFixedParameters()", showToast = false)
        this.roomFixedParametersGet.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    // == SERVER =====
    fun insertServerUser(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser()")

        Utilities.log(Enums.LogType.Notify, TAG, "NEW USER SIGN UP: ${userData?.userName}")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserModel?>? = userService.userAPI.insertUser(
                userData?.userName,
                userData?.email,
                userData?.age,
                userData?.phoneNumber,
                userData?.phoneNumberSMSVerified,
                userData?.deviceID,
                userData?.deviceType,
                userData?.equity,
                userData?.incomes,
                userData?.commitments,
                userData?.termsOfUseAcceptTime,
                userData?.subscriberType,
                userData?.registrationExpiredTime,
                userData?.appVersion,
                userData?.isFirstLogin)

            call?.enqueue(object : Callback<UserModel?> {
                override fun onResponse(call: Call<UserModel?>, response: Response<UserModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser(): response = $response")

                    val result: UserModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser(): result = $result")

                            CoroutineScope(Dispatchers.IO).launch {
                                userData?.uuid = result?.data?.user?.uuid
                                deleteOldRoomUsers(AppApplication.application!!.applicationContext)
                                insertRoomUser(AppApplication.application!!.applicationContext, userData)
                                setServerUser(result?.data?.user)
                            }
                        } catch (e: Exception) {
                            setServerUser(null)
                            Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                        }
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    fun updateServerUser(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateServerUser()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserModel?>? = userService.userAPI.updateUser(
                userData?.uuid,
                userData?.userName,
                userData?.email,
                userData?.age,
                userData?.phoneNumber,
                userData?.phoneNumberSMSVerified,
                userData?.deviceID,
                userData?.deviceType,
                userData?.equity,
                userData?.incomes,
                userData?.commitments,
                userData?.termsOfUseAcceptTime,
                //userData?.insertTime,
                //userData?.updateTime,
                userData?.subscriberType,
                //userData?.registrationStartTime,
                userData?.registrationExpiredTime,
                userData?.appVersion,
                userData?.isFirstLogin)

            call?.enqueue(object : Callback<UserModel?> {
                override fun onResponse(call: Call<UserModel?>, response: Response<UserModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "updateServerUser(): response = $response")

                    val result: UserModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            //setServerUser(response.body()?.data)
                            setServerUser(userData)
                            Utilities.log(Enums.LogType.Debug, TAG, "updateServerUser(): result = $result")
                        } catch (e: Exception) {
                            setServerUser(null)
                            Utilities.log(Enums.LogType.Error, TAG, "updateServerUser(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                        }
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "updateServerUser(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "updateServerUser(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setServerUser(response: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerUser()", showToast = false)
        this.serverUserInsertUpdateServer.postValue(response)
    }

    // == ROOM =====

    // delete room user
    fun deleteOldRoomUsers(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "deleteOldRoomUsers()")

        DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
    }

    // insert room user
    fun insertRoomUser(applicationContext: Context, userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "insertRoomUser()")

        DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!

        val roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(userData!!)!!

        setRoomUserInsert(UserEntity(
            roomUID,
            userData?.uuid,
            userName = userData?.userName,
            email = userData?.email,
            age = userData?.age,
            phoneNumber = userData?.phoneNumber,
            phoneNumberSMSVerified = userData?.phoneNumberSMSVerified,
            deviceID = userData?.deviceID,
            deviceType = userData?.deviceType,
            equity = userData?.equity,
            incomes = userData?.incomes,
            commitments = userData?.commitments,
            termsOfUseAcceptTime = userData?.termsOfUseAcceptTime,
            //insertTime = userData?.insertTime,
            //updateTime = userData?.updateTime,
            //serverUpdateTime = userData?.serverUpdateTime,
            subscriberType = userData?.subscriberType,
            //registrationStartTime = userData?.registrationStartTime,
            registrationExpiredTime = userData?.registrationExpiredTime,
            appVersion = userData?.appVersion,
            isFirstLogin = userData?.isFirstLogin,
            canTakeMortgage = userData?.canTakeMortgage
        ))
    }

    private fun setRoomUserInsert(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserInsert()", showToast = false)
        this.roomUserInsertUpdateRoom.postValue(user)
    }

    // update room user
    fun updateRoomUser(applicationContext: Context, userData: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateRoomUser()")
        DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.update(userData!!)!!
        setRoomUserUpdate(userData, caller)
    }

    private fun setRoomUserUpdate(user: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserUpdate()", showToast = false)
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserUpdate(): caller = ${caller}", showToast = false)
        if (caller == Enums.DBCaller.ROOM) {
            this.roomUserInsertUpdateRoom.postValue(user)
        }
        else {
            this.roomUserInsertUpdateServer.postValue(user)
        }
    }

    // get room user
    fun getRoomUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomUser()")
        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setRoomUserGet(resultUser)
            }
        }
        else {
            setRoomUserGet(null)
        }
    }

    private fun setRoomUserGet(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserGet()", showToast = false)
        this.roomUserGet.postValue(user)
    }

    //endregion == user ==========================

    //region == registration ==================

    fun couponRegistration(userData: UserEntity?, code: String) {
        Utilities.log(Enums.LogType.Debug, TAG, "couponRegistration()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<RegistrationModel?>? = registrationService.registrationAPI.coupon(code, userData?.uuid)

            call?.enqueue(object : Callback<RegistrationModel?> {
                override fun onResponse(call: Call<RegistrationModel?>, response: Response<RegistrationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "couponRegistration(): response = $response")
                    val result: RegistrationModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setCouponRegistration(result)
                    }
                    else {
                        setCouponRegistration(result)
                        Utilities.log(Enums.LogType.Error, TAG, "couponRegistration(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<RegistrationModel?>, t: Throwable) {
                    setCouponRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "couponRegistration(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setCouponRegistration(couponCode: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setCouponRegistration()", showToast = false)
        this.couponRegistration.postValue(couponCode)
    }

    fun skipRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "skipRegistration()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<RegistrationModel?>? = registrationService.registrationAPI.trial(userData?.uuid)

            call?.enqueue(object : Callback<RegistrationModel?> {
                override fun onResponse(call: Call<RegistrationModel?>, response: Response<RegistrationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "skipRegistration(): response = $response")
                    val result: RegistrationModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setSkipRegistration(result)
                    }
                    else {
                        setSkipRegistration(result)
                        Utilities.log(Enums.LogType.Error, TAG, "skipRegistration(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<RegistrationModel?>, t: Throwable) {
                    setSkipRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "skipRegistration(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setSkipRegistration(registrationModel: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSkipRegistration(): registrationModel = ${registrationModel.toString()}", showToast = false)
        this.skipRegistration.postValue(registrationModel)
    }

    fun betaRegistration(userData: UserEntity?, code: String) {
        Utilities.log(Enums.LogType.Debug, TAG, "betaRegistration()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<RegistrationModel?>? = registrationService.registrationAPI.beta(code, userData?.uuid)

            call?.enqueue(object : Callback<RegistrationModel?> {
                override fun onResponse(call: Call<RegistrationModel?>, response: Response<RegistrationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "betaRegistration(): response = $response")
                    val result: RegistrationModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setBetaRegistration(result)
                    }
                    else {
                        setBetaRegistration(result)
                        Utilities.log(Enums.LogType.Error, TAG, "betaRegistration(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<RegistrationModel?>, t: Throwable) {
                    setBetaRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "betaRegistration(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setBetaRegistration(registrationModel: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setBetaRegistration(): registrationModel = ${registrationModel.toString()}", showToast = false)
        this.betaRegistration.postValue(registrationModel)
    }

    //endregion == registration ==================

    //region == sms code validation ===========
    fun smsCodeValidation(userData: UserEntity?, code: String) {
        Utilities.log(Enums.LogType.Debug, TAG, "smsCodeValidation()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<SMSCodeValidationModel?>? = userService.userAPI.smsCodeValidation(userData?.uuid, code, userData?.phoneNumber)

            call?.enqueue(object : Callback<SMSCodeValidationModel?> {
                override fun onResponse(call: Call<SMSCodeValidationModel?>, response: Response<SMSCodeValidationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "smsCodeValidation(): response = $response")
                    val result: SMSCodeValidationModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            setSMSCodeValidation(result)
                            Utilities.log(Enums.LogType.Debug, TAG, "smsCodeValidation(): result = $result")
                        } catch (e: Exception) {
                            setSMSCodeValidation(null)
                            Utilities.log(Enums.LogType.Error, TAG, "smsCodeValidation(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                        }
                    }
                }

                override fun onFailure(call: Call<SMSCodeValidationModel?>, t: Throwable) {
                    setSMSCodeValidation(null)
                    Utilities.log(Enums.LogType.Error, TAG, "smsCodeValidation(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setSMSCodeValidation(smsCode: SMSCodeValidationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSMSCodeValidation()", showToast = false)
        this.smsCodeValidation.postValue(smsCode)
    }

    //endregion == sms code validation ===========

    //region == google pay registration =======

    fun googlePayRegistration(applicationContext: Context, userData: UserEntity?, googlePayUUID: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "googlePayRegistration()")
        Utilities.log(Enums.LogType.Debug, TAG, "googlePayRegistration(): googlePayUUID = ${googlePayUUID}, userData.uuid = ${userData?.uuid}")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<RegistrationModel?>? = registrationService.registrationAPI.googlePay(googlePayUUID, userData?.uuid)

            call?.enqueue(object : Callback<RegistrationModel?> {
                override fun onResponse(call: Call<RegistrationModel?>, response: Response<RegistrationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "googlePayRegistration(): response = ${response}")
                    val result: RegistrationModel? = response.body()

                    if (response.code() == 200) {
                        setGooglePayRegistration(result)
                        Utilities.log(Enums.LogType.Notify, TAG, "googlePayRegistration(): Success", userData)
                    }
                    else {
                        setGooglePayRegistration(null)
                        Utilities.log(Enums.LogType.Error, TAG, "googlePayRegistration(): Error = ${response}", userData)
                    }
                }

                override fun onFailure(call: Call<RegistrationModel?>, t: Throwable) {
                    setGooglePayRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "googlePayRegistration(): onFailure = ${t}", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setGooglePayRegistration(registrationModel: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setGooglePayRegistration()", showToast = false)
        this.googlePayRegistration.postValue(registrationModel)
    }
    //endregion == google pay registration =======

    //region == trial registration ============

    fun trialRegistration(userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "trialRegistration()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<RegistrationModel?>? = registrationService.registrationAPI.trial(userData?.uuid)

            call?.enqueue(object : Callback<RegistrationModel?> {
                override fun onResponse(call: Call<RegistrationModel?>, response: Response<RegistrationModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "trialRegistration(): response = $response")
                    val result: RegistrationModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setTrialRegistration(result)
                    }
                    else {
                        setTrialRegistration(result)
                        Utilities.log(Enums.LogType.Error, TAG, "trialRegistration(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", userData)
                    }
                }

                override fun onFailure(call: Call<RegistrationModel?>, t: Throwable) {
                    setTrialRegistration(null)
                    Utilities.log(Enums.LogType.Error, TAG, "trialRegistration(): onFailure = $t", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setTrialRegistration(registrationModel: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setTrialRegistration()", showToast = false)
        this.trialRegistration.postValue(registrationModel)
    }

    //endregion == trial registration ============

    //region == terms of use ==================

    fun getTermsOfUse() {
        Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse()")

        CoroutineScope(Dispatchers.IO).launch {
            val call: Call<StringModel?>? = stringsService.stringsAPI.getString("signup_terms_of_use_text")

            call?.enqueue(object : Callback<StringModel?> {
                override fun onResponse(call: Call<StringModel?>, response: Response<StringModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse(): response = $response")
                    val result: StringModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setTermsOfUse(result)
                    }
                    else {
                        setTermsOfUse(null)
                        Utilities.log(Enums.LogType.Error, TAG, "getTermsOfUse(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}")
                    }
                }

                override fun onFailure(call: Call<StringModel?>, t: Throwable) {
                    setTermsOfUse(null)
                    Utilities.log(Enums.LogType.Error, TAG, "getTermsOfUse(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setTermsOfUse(content: StringModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setTermsOfUse()", showToast = false)
        this.termsOfUse.postValue(content)
    }

    //endregion == terms of use ===================

}