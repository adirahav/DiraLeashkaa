package com.adirahav.diraleashkaa.ui.registration

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration.ROOM_AWAIT_MILLISEC
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.GooglePayProgramDataClass
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.APIResponseModel
import com.adirahav.diraleashkaa.data.network.models.RegistrationModel
import com.adirahav.diraleashkaa.data.network.models.UnsubscribeModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
import com.adirahav.diraleashkaa.data.network.services.RegistrationService
import com.adirahav.diraleashkaa.data.network.services.UserService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    val roomFixedParameters_Get: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // coupon registration
    val couponRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // google pay registration
    val googlePayRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // trial registration
    val trialRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // beta registration
    val betaRegistration: MutableLiveData<RegistrationModel> = MutableLiveData()

    // user
    val serverUser_InsertUpdateServer: MutableLiveData<UserModel> = MutableLiveData()
    val roomUser_Get: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUser_UpdateRoom: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUser_UpdateServer: MutableLiveData<UserEntity> = MutableLiveData()

    // unsubscribe
    val unsubscribe: MutableLiveData<UnsubscribeModel> = MutableLiveData()

    //region == fixed parameters ==============
    fun getRoomFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(ROOM_AWAIT_MILLISEC) {
                setRoomFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setRoomFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomFixedParameters()", showToast = false)
        this.roomFixedParameters_Get.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    // == SERER =====
    fun insertServerUser(applicationContext: Context, lifecycleOwner: LifecycleOwner, userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser()")

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
                //userData?.insertTime,
                //userData?.updateTime,
                userData?.subscriberType,
                //userData?.registrationStartTime,
                userData?.registrationExpiredTime,
                userData?.appVersion,
                userData?.isFirstLogin)

            call?.enqueue(object : Callback<UserModel?> {
                override fun onResponse(call: Call<UserModel?>, response: Response<UserModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser(): response = ${response}")

                    val result: UserModel? = response.body()

                    if (response.code() == 200) {
                        setServerUser(result)
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): Error = ${response}", userData)
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): onFailure = ${t}", userData)
                    call.cancel()
                }
            })
        }
    }

    fun updateServerUser(applicationContext: Context, lifecycleOwner: LifecycleOwner, userData: UserEntity?) {
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
                    Utilities.log(Enums.LogType.Debug, TAG, "updateServerNewUser(): response = ${response}")

                    val result: UserModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setServerUser(result)
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "updateServerNewUser(): Error = ${response}", userData)
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "updateServerNewUser(): onFailure = ${t}", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setServerUser(response: UserModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerUser()", showToast = false)
        this.serverUser_InsertUpdateServer.postValue(response)
    }

    private fun setRoomUser_Insert(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUser_Insert()", showToast = false)
        this.roomUser_UpdateRoom.postValue(user)
    }

    // update room user
    fun updateRoomUser(applicationContext: Context, lifecycleOwner: LifecycleOwner, userData: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateRoomUser()")
        DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.update(userData!!)!!
        setRoomUser_Update(userData, caller)
    }

    private fun setRoomUser_Update(user: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUser_Update()", showToast = false)
        if (caller == Enums.DBCaller.ROOM) {
            this.roomUser_UpdateRoom.postValue(user)
        }
        else {
            this.roomUser_UpdateServer.postValue(user)
        }
    }

    // get room user
    fun getRoomUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomUser(): userID = $userID")
        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setRoomUser_Get(resultUser)
            }
        }
        else {
            setRoomUser_Get(null)
        }
    }

    private fun setRoomUser_Get(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUser_Get()", showToast = false)
        this.roomUser_Get.postValue(user)
    }

    /*private inner class UserServerCallback : Callback<UserEntity?> {
        override fun onResponse(call: Call<UserEntity?>, response: Response<UserEntity?>) {
            val user = response.body()
            if (user != null) {
                setRoomUser_Get(user)
            }
        }

        override fun onFailure(call: Call<UserEntity?>, t: Throwable) {
            Utilities.log(Enums.LogType.Error, TAG, "UserCallback(): onFailure = ${t.message}", userData)
        }
    }*/

    //endregion == user ==========================

    //region == coupon registration ===========

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

    private fun setCouponRegistration(registrationModel: RegistrationModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setCouponRegistration()", showToast = false)
        this.couponRegistration.postValue(registrationModel)
    }

    //endregion == coupon registration ===========

    //region == beta registration =============

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
        Utilities.log(Enums.LogType.Debug, TAG, "setBetaRegistration()", showToast = false)
        this.betaRegistration.postValue(registrationModel)
    }

    //endregion == beta registration =============

    //region == google pay registration =======

    fun googlePayRegistration(applicationContext: Context, userData: UserEntity?, googlePayUUID: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "googlePayRegistration()")

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
    //endregion == google pay registration ========

    //region == unsubscribe ===================
    fun getUnsubscribe(applicationContext: Context, userData: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getUnsubscribe()")

        CoroutineScope(Dispatchers.IO).launch {
            Utilities.log(Enums.LogType.Debug, TAG,
                    "getUnsubscribe(): uuid = ${userData?.uuid} ; userName = ${userData?.userName} ; email = ${userData?.email} ;  phoneNumber = ${userData?.phoneNumber} ;  deviceID = ${userData?.deviceID}")

            // room
            DatabaseClient.getInstance(applicationContext)?.appDatabase?.clearAllTables()

            // server
            val call: Call<UnsubscribeModel?>? = userService.userAPI.deleteUser(
                    userData?.uuid,
                    userData?.userName,
                    userData?.email,
                    userData?.phoneNumber,
                    userData?.deviceID)

            call?.enqueue(object : Callback<UnsubscribeModel?> {
                override fun onResponse(call: Call<UnsubscribeModel?>, response: Response<UnsubscribeModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getUnsubscribe(): response = ${response}")

                    val result: UnsubscribeModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setUnsubscribe(result)
                    }
                    else {
                        setUnsubscribe(null)
                        Utilities.log(Enums.LogType.Error, TAG, "getUnsubscribe(): Error = ${response}", userData)
                    }
                }

                override fun onFailure(call: Call<UnsubscribeModel?>, t: Throwable) {
                    setUnsubscribe(null)
                    Utilities.log(Enums.LogType.Error, TAG, "getUnsubscribe(): onFailure = ${t}", userData)
                    call.cancel()
                }
            })
        }
    }

    private fun setUnsubscribe(response: UnsubscribeModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setUnsubscribe()", showToast = false)
        this.unsubscribe.postValue(response)
    }
    //endregion == unsubscribe ===================
}