package com.adirahav.diraleashkaa.ui.registration

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration.ROOM_AWAIT_MILLISEC
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import com.adirahav.diraleashkaa.data.network.models.UnsubscribeModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
import com.adirahav.diraleashkaa.data.network.response.UserResponse
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
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

class RegistrationViewModel internal constructor(
    val activity: RegistrationActivity,
    private val userService: UserService,
    private val registrationService: RegistrationService,
) : BaseViewModel() {

    private val TAG = "RegistrationViewModel"

    // fixed parameters
    val getLocalFixedParameters: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // coupon registration
    val couponRegistrationCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // pay registration
    val payProgramRegistrationCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // trial registration
    val trialRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // beta registration
    val betaRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // user
    val saveUserCallback: MutableLiveData<UserModel> = MutableLiveData()
    val getLocalUserCallback: MutableLiveData<UserEntity> = MutableLiveData()
    val updateLocalUserCallback: MutableLiveData<UserEntity> = MutableLiveData()
    val updateServerUserCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // unsubscribe
    val unsubscribeCallback: MutableLiveData<UnsubscribeModel> = MutableLiveData()

    //region == fixed parameters ==============
    fun getLocalFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(ROOM_AWAIT_MILLISEC) {
                setLocalFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setLocalFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalFixedParameters()", showToast = false)
        this.getLocalFixedParameters.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    // == SERER =====
    fun insertServerUser(applicationContext: Context, lifecycleOwner: LifecycleOwner, userData: UserEntity?, password: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser()")

        CoroutineScope(Dispatchers.IO).launch {

        }
    }

    fun updateServerUser(applicationContext: Context, lifecycleOwner: LifecycleOwner, userData: UserEntity?, password: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateServerUser()")

        CoroutineScope(Dispatchers.IO).launch {

        }
    }

    // update room user
    fun updateLocalUser(applicationContext: Context, userData: UserEntity?, caller: Enums.DBCaller) {
        if (userData != null) {
            Utilities.log(Enums.LogType.Debug, TAG, "updateLocalUser()")

            DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.update(userData)!!
            setUpdateLocalUser(userData, caller)
        }
    }

    private fun setUpdateLocalUser(user: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "setUpdateLocalUser()", showToast = false)
        if (caller == Enums.DBCaller.LOCAL) {
            this.updateLocalUserCallback.postValue(user)
        }
        else {
            this.updateServerUserCallback.postValue(user)
        }
    }

    // get room user
    fun getRoomUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomUser(): userID = $userID")
        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setLocalUser_Get(resultUser)
            }
        }
        else {
            setLocalUser_Get(null)
        }
    }

    private fun setLocalUser_Get(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUser_Get()", showToast = false)
        this.getLocalUserCallback.postValue(user)
    }

    //endregion == user ==========================

    //region == coupon registration ===========

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
                        setCouponRegistration(null)
                        Utilities.log(Enums.LogType.Error, TAG, "couponRegistration(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
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
        this.couponRegistrationCallback.postValue(userData)
    }

    //endregion == coupon registration ===========

    //region == pay program registration ===
    fun payProgramRegistration(applicationContext: Context, userData: UserEntity?, programPayId: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "payRegistration()")

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
        this.payProgramRegistrationCallback.postValue(userData)
    }
    //endregion == pay program registration =======

    //region == unsubscribe ===================
    fun getUnsubscribe(applicationContext: Context, userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getUnsubscribe()")

        CoroutineScope(Dispatchers.IO).launch {
            Utilities.log(Enums.LogType.Debug, TAG,
                    "getUnsubscribe(): email = ${userData?.email}")

            // room
            DatabaseClient.getInstance(applicationContext)?.appDatabase?.clearAllTables()

            // server
            val call: Call<UnsubscribeModel?>? = userService.userAPI.deleteUser()

            call?.enqueue(object : Callback<UnsubscribeModel?> {
                override fun onResponse(call: Call<UnsubscribeModel?>, response: Response<UnsubscribeModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getUnsubscribe(): response = ${response}")

                    val result: UnsubscribeModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setUnsubscribe(result)
                    }
                    else {
                        setUnsubscribe(null)
                        Utilities.log(Enums.LogType.Error, TAG, "getUnsubscribe(): Error = ${response}")
                    }
                }

                override fun onFailure(call: Call<UnsubscribeModel?>, t: Throwable) {
                    setUnsubscribe(null)
                    Utilities.log(Enums.LogType.Error, TAG, "getUnsubscribe(): onFailure = ${t}")
                    call.cancel()
                }
            })
        }
    }

    private fun setUnsubscribe(response: UnsubscribeModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setUnsubscribe()", showToast = false)
        this.unsubscribeCallback.postValue(response)
    }
    //endregion == unsubscribe ===================
}