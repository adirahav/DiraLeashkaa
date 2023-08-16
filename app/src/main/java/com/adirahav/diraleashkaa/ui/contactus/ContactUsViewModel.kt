package com.adirahav.diraleashkaa.ui.contactus

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.BuildConfig
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.EmailDataClass
import com.adirahav.diraleashkaa.data.network.dataClass.SplashDataClass
import com.adirahav.diraleashkaa.data.network.entities.EmailEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.EmailModel
import com.adirahav.diraleashkaa.data.network.models.SplashModel
import com.adirahav.diraleashkaa.data.network.services.EmailService
import com.adirahav.diraleashkaa.data.network.services.SplashService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.splash.SplashActivity
import com.adirahav.diraleashkaa.ui.splash.SplashViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class ContactUsViewModel internal constructor(private val activity: ContactUsActivity,
                                              private val emailService: EmailService,) : BaseViewModel() {

    private val TAG = "ContactUsViewModel"

    // fixed parameters
    val roomFixedParametersGet: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()

    // email
    val serverEmail: MutableLiveData<EmailDataClass> = MutableLiveData()

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

    //region == email =========================
    fun serverSendEmail(userUUID: String?, subject: String, message: String?) {

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<EmailModel?>? = emailService.emailAPI.sendEmail(userUUID, "CONTACT_US", subject, message)

            call?.enqueue(object : Callback<EmailModel?> {
                override fun onResponse(call: Call<EmailModel?>, response: Response<EmailModel?>) {

                    val result: EmailModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            setSendEmail(result?.data)
                        }
                        catch (e: Exception) {
                            setSendEmail(null)
                            Utilities.log(Enums.LogType.Error, TAG, "serverSendEmail(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                        }
                    }
                    else {
                        setSendEmail(null)
                        Utilities.log(Enums.LogType.Error, TAG, "serverSendEmail(): response = $response")
                    }
                }

                override fun onFailure(call: Call<EmailModel?>, t: Throwable) {
                    var result : EmailDataClass? = null
                    if (t.message.equals("timeout")) {
                        result = EmailDataClass(
                                email = null
                        )
                    }
                    setSendEmail(result)

                    Utilities.log(Enums.LogType.Error, TAG, "serverSendEmail(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setSendEmail(email: EmailDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSendEmail()", showToast = false)
        this.serverEmail.postValue(email)
    }

    //endregion == email =========================
}