package com.adirahav.diraleashkaa.ui.splash

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.AppApplication
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.common.Utilities.getVersionName
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.AnnouncementModel
import com.adirahav.diraleashkaa.data.network.services.AnnouncementService
import com.adirahav.diraleashkaa.data.network.services.UserService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.login.LoginActivity
import com.adirahav.diraleashkaa.ui.signup.SignUpWelcomeFragment
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class SplashViewModel internal constructor(private val activity: SplashActivity,
                                           private val userService: UserService,
                                           private val announcementService: AnnouncementService) : BaseViewModel() {

    companion object {
        private const val TAG = "SplashViewModel"
    }

    // splash
    val splashCallback: MutableLiveData<SplashModel> = MutableLiveData()
    val localSplashCallback: MutableLiveData<MutableList<Any?>> = MutableLiveData()

    // user
    val localUserCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // restore
    val localRestoreData: MutableLiveData<UserEntity> = MutableLiveData()

    // announcement
    val announcement: MutableLiveData<AnnouncementEntity> = MutableLiveData()

val MAX_CONNECTING_TRIES = 20
    val AWAIT_SECONDS = 5
    var tryConnecting = 1
    var connectionSuccess = false

    //region == splash =============

    fun getSplash() {

        CoroutineScope(Dispatchers.IO).launch {

            var gotoLogin = false
            val existLocalUser = DatabaseClient.getInstance(activity.applicationContext)?.appDatabase?.userDao()?.getFirst()
            val existLocalFixedParameters = DatabaseClient.getInstance(activity.applicationContext)?.appDatabase?.fixedParametersDao()?.getFirst()
            val existLocalStrings = DatabaseClient.getInstance(activity.applicationContext)?.appDatabase?.stringsDao()?.getFirst()
            if (existLocalUser != null && existLocalFixedParameters != null && existLocalStrings != null) {
                if (activity.preferences!!.getString("token", "").isNullOrEmpty()) {
                    gotoLogin = true
                    LoginActivity.start(AppApplication.context, existLocalUser.email)
                }
            }

            if (!gotoLogin) {
                tryCallSplash("android", getVersionName(), if (existLocalUser != null) existLocalUser.email else null)
            }
        }
    }

    private fun tryCallSplash(platform: String, versionName: String, localUser: String?) {
        val call: Call<SplashModel?> = userService.userAPI.getSplashData(
                platform,
                versionName,
                localUser
        )

        call.enqueue(object : Callback<SplashModel?> {

            override fun onResponse(call: Call<SplashModel?>, response: Response<SplashModel?>) {

                connectionSuccess = true

                val result: SplashModel? = response.body()

                Utilities.log(Enums.LogType.Debug, TAG, "getSplash(): response = $response")

                if (response.code() == 200) {
                    try {
                        setSplash(result)
                    } catch (e: Exception) {
                        if (tryConnecting > MAX_CONNECTING_TRIES) {
                            Utilities.log(Enums.LogType.Error, TAG, "getSplash(): e = ${e.message} ; result.data = ${result?.toString()}")
                            setSplash(null)
                        }
                        else {
                            Utilities.log(Enums.LogType.Error, TAG, "getSplash(): tryConnecting = $tryConnecting")

                            Utilities.await(Date(), AWAIT_SECONDS) {
                                if (activity.layout?.waiting?.isVisible == false) {
                                    activity.layout!!.waiting.setAnimation(R.raw.lottie_waiting9)
                                    activity.layout!!.waiting.visibility = View.VISIBLE
                                }
                                tryConnecting++
                                tryCallSplash(platform, versionName, localUser)
                            }
                        }
                    }
                } else {
                    if (tryConnecting > MAX_CONNECTING_TRIES) {
                        Utilities.log(Enums.LogType.Error, TAG, "getSplash(): response = $response")
                        setSplash(null)
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "getSplash(): tryConnecting = $tryConnecting")

                        Utilities.await(Date(), AWAIT_SECONDS) {
                            if (activity.layout?.waiting?.isVisible == false) {
                                activity.layout!!.waiting.setAnimation(R.raw.lottie_waiting2)
                                activity.layout!!.waiting.visibility = View.VISIBLE
                            }
                            tryConnecting++
                            tryCallSplash(platform, versionName, localUser)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<SplashModel?>, t: Throwable) {

                if (tryConnecting > MAX_CONNECTING_TRIES) {
                    Utilities.log(Enums.LogType.Error, TAG, "getSplash(): onFailure = $t")
                    setSplash(null)
                    call.cancel()
                }
                else {
                    Utilities.log(Enums.LogType.Error, TAG, "getSplash(): tryConnecting = $tryConnecting")

                    Utilities.await(Date(), AWAIT_SECONDS) {
                        if (activity.layout?.waiting?.isVisible == false) {
                            activity.layout?.waiting?.visibility = View.VISIBLE
                        }
                        tryConnecting++
                        call.cancel()
                        tryCallSplash(platform, versionName, localUser)
                    }
                }
            }
        })
    }

    private fun setSplash(splash: SplashModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSplash()", showToast = false)
        this.splashCallback.postValue(splash)
    }

    fun saveLocalSplash(applicationContext: Context, lifecycleOwner: LifecycleOwner, splash: SplashModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveLocalSplash(): splash = ${splash.toString()}")
        CoroutineScope(Dispatchers.IO).launch {

            // ----------------
            // fixed parameters
            // ----------------
            val localFixedParametersList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()

            if (localFixedParametersList != null) {
                if (localFixedParametersList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.deleteAll()!!
                }
            }

            val localFixedParameters = splash?.fixedParameters
            if (localFixedParameters != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.insert(localFixedParameters)!!
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

            val localPhrases = splash?.strings
            if (localPhrases != null) {
                for (item in localPhrases) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.insert(item as PhraseEntity)!!
                }
            }

            // ----------------
            // user
            // ----------------
            val localUser = splash?.user
            if (localUser != null) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.deleteAll()!!
                val roomUID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.insert(localUser)!!
                activity.preferences?.setLong("roomUID", roomUID, false)
            }

            // ----------------
            // calculators
            // ----------------
            val calculatorsList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.getAll()

            if (calculatorsList != null) {
                if (calculatorsList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.deleteAll()!!
                }
            }

            val localCalculators = splash?.calculators
            if (localCalculators != null) {
                for (item in localCalculators) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.insert(item as CalculatorEntity)!!
                }
            }

            setLocalSplash(localUser, localFixedParameters)
        }
    }

    private fun setLocalSplash(user: UserEntity?, fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveLocalSplash()", showToast = false)
        val localData = mutableListOf(user, fixedParameters)
        this.localSplashCallback.postValue(localData)
    }

    //endregion == splash =============

    //region == user ===============

    fun getLocalUser(applicationContext: Context, lifecycleOwner: LifecycleOwner) {
        //appVersion = "[{key:'subscriber_type',value:'trial'},{key:'expired_time',value:1649673662915},{key:'is_code_expired',value:false}]",
        //registerTime = 1649673662915,

        Utilities.log(Enums.LogType.Debug, TAG, "getLocalUser()")
        GlobalScope.launch {

            val resultUsers =
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.getAll()

            // TO DELETE
            /*if (resultUsers?.isNotEmpty() == true) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.delete(resultUsers.first())
                setLocalUser(null)
                return@launch
            }*/
            // TO DELETE

            if (resultUsers?.isNotEmpty() == true) {
                setLocalUser(resultUsers.first())
            } else {
                setLocalUser(null)
            }
        }
    }

    private fun setLocalUser(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalUser()", showToast = false)
        this.localUserCallback.postValue(user)
    }

    //endregion == user ===============

    //region == restore ============

    fun restoreLocalData(applicationContext: Context, userEntity: UserEntity?, propertyEntities: List<PropertyEntity>?, calculatorEntities: List<CalculatorEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "restoreLocalData(): userEntity = $userEntity, propertyEntities = $propertyEntities, calculatorEntities = $calculatorEntities")

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

            setLocalUserRestore(userEntity)

        }
    }

    private fun setLocalUserRestore(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalUserRestore(): roomUID = ${user?.roomUID}", showToast = false)
        this.localRestoreData.postValue(user)
    }

    //endregion == restore ============

    //region == announcement =======

    fun confirmAnnouncement(announcementId: String) {
        CoroutineScope(Dispatchers.IO).launch {


            val call: Call<AnnouncementModel?>? = announcementService.announcementAPI.confirm(announcementId)
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