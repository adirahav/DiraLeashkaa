package com.adirahav.diraleashkaa.ui.user

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.APIResponseModel
import com.adirahav.diraleashkaa.data.network.models.StringModel
import com.adirahav.diraleashkaa.data.network.models.UserModel
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

class UserViewModel internal constructor(
    private val userService: UserService,
    private val stringsService: StringsService,
) : BaseViewModel() {

    private val TAG = "UserViewModel"

    // fixed parameters
    private val roomFixedParametersGet: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val serverUserInsertUpdateServer: MutableLiveData<UserModel> = MutableLiveData()
    val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserUpdateRoom: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserUpdateServer: MutableLiveData<UserEntity> = MutableLiveData()

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

    // == SERER =====
    fun insertServerUser(userData: UserEntity?) {
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

            /*val call: Call<UserAPIResponse?>? = userService.userAPI.insertUser(
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
                userData?.insertTime,
                userData?.updateTime,
                userData?.subscriberType,
                userData?.registrationStartTime,
                userData?.registrationExpiredTime,
                userData?.appVersion,
                userData?.isFirstLogin)*/

            call?.enqueue(object : Callback<UserModel?> {
                override fun onResponse(call: Call<UserModel?>, response: Response<UserModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser(): response = $response")

                    val result: UserModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setServerUser(result)
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
                        setServerUser(result)
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "updateServerUser(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}")
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "updateServerUser(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setServerUser(response: UserModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerUser()", showToast = false)
        this.serverUserInsertUpdateServer.postValue(response)
    }

    // == ROOM =====

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
            this.roomUserUpdateRoom.postValue(user)
        }
        else {
            this.roomUserUpdateServer.postValue(user)
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

    //region == terms of use ==================

    fun getTermsOfUse() {
        Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<StringModel?>? = stringsService.stringsAPI.getString("user_terms_of_use_text")

            call?.enqueue(object : Callback<StringModel?> {
                override fun onResponse(call: Call<StringModel?>, response: Response<StringModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse(): response = $response")
                    val result: StringModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        setTermsOfUse(result)
                    }
                    else {
                        setTermsOfUse(result)
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

    private fun setTermsOfUse(strings: StringModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setTermsOfUse()", showToast = false)
        this.termsOfUse.postValue(strings)
    }

    //endregion == terms of use ===================

}