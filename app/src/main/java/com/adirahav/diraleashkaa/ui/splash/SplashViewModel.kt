package com.adirahav.diraleashkaa.ui.splash

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.common.AppApplication.Companion.context
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.DeviceDataClass
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import com.adirahav.diraleashkaa.data.network.dataClass.SplashDataClass
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.AnnouncementModel
import com.adirahav.diraleashkaa.data.network.services.AnnouncementService
import com.adirahav.diraleashkaa.data.network.services.SplashService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashViewModel internal constructor(private val activity: SplashActivity,
                                           private val splashService: SplashService,
                                           private val announcementService: AnnouncementService) : BaseViewModel() {

    companion object {
        private const val TAG = "SplashViewModel"
    }

    // splash
    val serverSplash: MutableLiveData<SplashDataClass> = MutableLiveData()
    val roomSplash: MutableLiveData<MutableList<Any?>> = MutableLiveData()

    // user
    val roomUser: MutableLiveData<UserEntity> = MutableLiveData()

    // restore
    val roomRestoreData: MutableLiveData<UserEntity> = MutableLiveData()

    // announcement
    val serverAnnouncement: MutableLiveData<AnnouncementEntity> = MutableLiveData()

    //region == splash =============

    fun getServerSplash(userUUID: String?, deviceID: String, trackUser: Boolean? = false) {

        CoroutineScope(Dispatchers.IO).launch {

            //Log.d("ADITEST10","splashAPI.get userUUID = ${userUUID} ; deviceID ${deviceID} ; VERSION_NAME ${BuildConfig.VERSION_NAME} ; trackUser ${trackUser ?: false}")
            val call: Call<SplashModel?>? = splashService.splashAPI.get(userUUID, deviceID, BuildConfig.VERSION_NAME, trackUser ?: false)

            call?.enqueue(object : Callback<SplashModel?> {
                override fun onResponse(call: Call<SplashModel?>, response: Response<SplashModel?>) {

                    val result: SplashModel? = response.body()

                    Utilities.log(Enums.LogType.Debug, TAG, "getServerSplash(): response = $response")

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            setServerSplash(result?.data)
                            //Log.d("ADITEST11","user.userName ${result?.data?.user?.userName} ; restore.userName ${result?.data?.restore?.user?.userName}")
                        }
                        catch (e: Exception) {
                            setServerSplash(null)
                            Utilities.log(Enums.LogType.Error, TAG, "getServerSplash(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                        }
                    }
                    else {
                        setServerSplash(null)
                        Utilities.log(Enums.LogType.Error, TAG, "getServerSplash(): response = $response")
                    }
                }

                override fun onFailure(call: Call<SplashModel?>, t: Throwable) {
                    setServerSplash(null)

                    Utilities.log(Enums.LogType.Error, TAG, "getServerSplash(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setServerSplash(splash: SplashDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerSplash()", showToast = false)
        this.serverSplash.postValue(splash)
    }

    fun saveRoomSplash(applicationContext: Context, lifecycleOwner: LifecycleOwner, splash: SplashDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveRoomSplash(): splash = ${splash.toString()}")
        CoroutineScope(Dispatchers.IO).launch {

            // ----------------
            // fixed parameters
            // ----------------
            val roomFixedParametersList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()

            if (roomFixedParametersList != null) {
                if (roomFixedParametersList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.deleteAll()!!
                }
            }

            val roomFixedParameters = splash?.fixedParameters
            if (roomFixedParameters != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.insert(roomFixedParameters)!!
            }

            // ----------------
            // strings
            // ----------------
            val stringsList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.getAll()

            if (stringsList != null) {
                if (stringsList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.deleteAll()!!
                }
            }

            val roomStrings = splash?.strings
            if (roomStrings != null) {
                for (item in roomStrings) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.insert(item as StringEntity)!!
                }
            }

            // ----------------
            // user
            // ----------------
            val roomUser = splash?.user
            if (roomUser != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
                val roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(roomUser)!!
                activity.preferences?.setLong("roomUID", roomUID, false)
            }

            setRoomSplash(roomUser, roomFixedParameters)
        }
    }

    private fun setRoomSplash(user: UserEntity?, fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomSplash()", showToast = false)
        val roomData = mutableListOf(user, fixedParameters)
        this.roomSplash.postValue(roomData)
    }

    //endregion == splash =============

    //region == user ===============

    fun getRoomUser(applicationContext: Context, lifecycleOwner: LifecycleOwner) {
        //appVersion = "[{key:'subscriber_type',value:'trial'},{key:'expired_time',value:1649673662915},{key:'is_code_expired',value:false}]",
        //registerTime = 1649673662915,

        Utilities.log(Enums.LogType.Debug, TAG, "getRoomUser()")
        GlobalScope.launch {

            val resultUsers =
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.getAll()

            // TO DELETE
            /*if (resultUsers?.isNotEmpty() == true) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.delete(resultUsers.first())
                setRoomUser(null)
                return@launch
            }*/
            // TO DELETE

            if (resultUsers?.isNotEmpty() == true) {
                setRoomUser(resultUsers.first())
            } else {
                setRoomUser(null)
            }
        }
    }

    private fun setRoomUser(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setUser()", showToast = false)
        this.roomUser.postValue(user)
    }

    //endregion == user ===============

    //region == restore ============

    fun restoreRoomData(applicationContext: Context, userEntity: UserEntity?, propertyEntities: List<PropertyEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "restoreRoomData(): userModel = $userEntity, propertiesModel = $propertyEntities")

        GlobalScope.launch {
            if (userEntity != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!

                val roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(userEntity)!!
                userEntity.roomUID = roomUID

                activity.preferences?.setLong("roomUID", roomUID, false)

            }

            if (propertyEntities != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.deleteAll()!!

                for (propertyEntity in propertyEntities) {
                    val roomID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(propertyEntity)
                    propertyEntity.roomID = roomID
                }
            }

            setRoomUserRestore(userEntity)

        }
    }

    private fun setRoomUserRestore(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomUserRestore(): roomUID = ${user?.roomUID}", showToast = false)
        this.roomRestoreData.postValue(user)
    }

    //endregion == restore ============

    //region == announcement =======

    fun confirmAnnouncement(announcementUUID: String, userUUID: String?) {
        CoroutineScope(Dispatchers.IO).launch {


            val call: Call<AnnouncementModel?>? = announcementService.announcementAPI.confirm(announcementUUID, userUUID)
            call?.enqueue(object : Callback<AnnouncementModel?> {
                override fun onResponse(call: Call<AnnouncementModel?>, response: Response<AnnouncementModel?>) {

                    if (response.code() == 200 && response.body()?.success == true) {
                        Utilities.log(Enums.LogType.Debug, TAG, "confirmAnnouncement(): response = $response")
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "confirmAnnouncement(): response = $response")
                    }
                }
                override fun onFailure(call: Call<AnnouncementModel?>, t: Throwable) {
                    Utilities.log(Enums.LogType.Error, TAG, "confirmAnnouncement(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    //endregion == announcement =======
}