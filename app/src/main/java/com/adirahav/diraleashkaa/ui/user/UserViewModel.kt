package com.adirahav.diraleashkaa.ui.user

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.requests.SignUpRequest
import com.adirahav.diraleashkaa.data.network.response.UserResponse
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

class UserViewModel internal constructor(
        private val activity: UserActivity,
        private val userService: UserService,
        private val phraseService: PhraseService,
) : BaseViewModel() {

    private val TAG = "UserViewModel"

    // fixed parameters
    private val fixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val setUserCallback: MutableLiveData<UserEntity> = MutableLiveData()
    val getLocalUserCallback: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserUpdateRoom: MutableLiveData<UserEntity> = MutableLiveData()
    val roomUserUpdateServer: MutableLiveData<UserEntity> = MutableLiveData()

    // terms of use
    val termsOfUse: MutableLiveData<PhraseEntity> = MutableLiveData()

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
        this.fixedParametersCallback.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    // == SERER =====
    fun insertServerUser(userData: UserEntity?, password: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser()")

        CoroutineScope(Dispatchers.IO).launch {

            /*val call: Call<UserModel?>? = userService.userAPI.signup(
                userData?.platform,
                userData?.email,
                password,
                userData?.fullname,
                userData?.yearOfBirth,
                userData?.equity,
                userData?.incomes,
                userData?.commitments,
                userData?.termsOfUseAccept,
                userData?.appDeviceType,
                userData?.appVersion
            )

            call?.enqueue(object : Callback<UserModel?> {
                override fun onResponse(call: Call<UserModel?>, response: Response<UserModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "insertServerUser(): response = $response")

                    val result: UserModel? = response.body()

                    if (response.code() == 200) {
                        setServerUser(result)
                    }
                    else {
                        setServerUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}", userData)
                    }
                }

                override fun onFailure(call: Call<UserModel?>, t: Throwable) {
                    setServerUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "insertServerUser(): onFailure = $t", userData)
                    call.cancel()
                }
            })*/
        }
    }

    fun updateUser(userData: Map<String, Any?>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateServerUser()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<UserResponse?>? = userService.userAPI.update("Bearer ${activity.userToken}", buildUserRequest(userData))

            call?.enqueue(object : Callback<UserResponse?> {
                override fun onResponse(call: Call<UserResponse?>, response: Response<UserResponse?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "updateUser(): response = $response")

                    val result: UserResponse? = response.body()

                    if (response.code() == 200) {
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

                        setUser(localUser)
                    }
                    else {
                        setUser(null)
                        Utilities.log(Enums.LogType.Error, TAG, "updateUser(): Error = $response ; errorCode = ${response.code()} ; errorMessage = ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<UserResponse?>, t: Throwable) {
                    setUser(null)
                    Utilities.log(Enums.LogType.Error, TAG, "updateUser(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    fun buildUserRequest(userData: Map<String, Any?>?) : SignUpRequest {

        return SignUpRequest(
                platform = "android",
                email = null,
                password = null,
                fullname = Utilities.getMapStringValue(userData, "fullname"),
                yearOfBirth = Utilities.getMapIntValue(userData, "yearOfBirth"),
                equity = Utilities.getMapIntValue(userData, "equity"),
                incomes = Utilities.getMapIntValue(userData, "incomes"),
                commitments = Utilities.getMapIntValue(userData, "commitments"),
                termsOfUseAccept = null,
                appDeviceId = Settings.Secure.getString(AppApplication.context.contentResolver, Utilities.getDeviceID(AppApplication.context)),
                appDeviceType = Utilities.getDeviceType(),
                appVersion = BuildConfig.VERSION_NAME
        )
    }

    private fun setUser(response: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setUser()", showToast = false)
        this.setUserCallback.postValue(response)
    }

    // == ROOM =====

    // update room user
    fun updateLocalUser(applicationContext: Context, userData: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "updateRoomUser()")
        DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.update(userData!!)!!
        setRoomUserUpdate(userData, caller)
    }

    private fun setRoomUserUpdate(user: UserEntity?, caller: Enums.DBCaller) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserUpdate()", showToast = false)
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserUpdate(): caller = ${caller}", showToast = false)
        if (caller == Enums.DBCaller.LOCAL) {
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
        this.getLocalUserCallback.postValue(user)
    }

    //endregion == user ==========================

    //region == terms of use ==================

    fun getTermsOfUse() {
        Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<PhraseEntity?>? = phraseService.phraseAPI.getPhrase("user_terms_of_use_text")

            call?.enqueue(object : Callback<PhraseEntity?> {
                override fun onResponse(call: Call<PhraseEntity?>, response: Response<PhraseEntity?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "getTermsOfUse(): response = $response")
                    val result: PhraseEntity? = response.body()

                    if (response.code() == 200) {
                        setTermsOfUse(result)
                    }
                    else {
                        setTermsOfUse(result)
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

    private fun setTermsOfUse(phrase: PhraseEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setTermsOfUse()", showToast = false)
        this.termsOfUse.postValue(phrase)
    }

    //endregion == terms of use ===================

}