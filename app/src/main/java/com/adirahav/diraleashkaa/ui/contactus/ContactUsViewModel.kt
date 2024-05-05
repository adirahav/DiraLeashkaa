package com.adirahav.diraleashkaa.ui.contactus

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.requests.ContactUsRequest
import com.adirahav.diraleashkaa.data.network.services.ContactUsService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class ContactUsViewModel internal constructor(private val activity: ContactUsActivity,
                                              private val contactUsService: ContactUsService,) : BaseViewModel() {

    private val TAG = "ContactUsViewModel"

    // fixed parameters
    val getLocalFixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val getLocalUserCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // message
    val sendMessageCallback: MutableLiveData<Boolean> = MutableLiveData()

    //region == fixed parameters ==============
    fun getLocalFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setLocalFixedParametersCallback(fixedParameters?.first())
            }
        }
    }

    private fun setLocalFixedParametersCallback(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalFixedParametersCallback()", showToast = false)
        this.getLocalFixedParametersCallback.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    fun getLocalUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalUser()")
        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setLocalUserCallback(resultUser)
            }
        }
        else {
            setLocalUserCallback(null)
        }
    }

    private fun setLocalUserCallback(user: UserEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalUserCallback()", showToast = false)
        this.getLocalUserCallback.postValue(user)
    }

    //endregion == user ==========================

    //region == message =======================
    fun sendMessage(subject: String, message: String?) {

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<Boolean>? = contactUsService.contactUsAPI.sendMessage(ContactUsRequest(subject = subject, message = message))

            call?.enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {

                    val result: Boolean? = response.body()

                    if (response.code() == 200) {
                        try {
                            setSendMessage(result)
                        }
                        catch (e: Exception) {
                            setSendMessage(false)
                            Utilities.log(Enums.LogType.Error, TAG, "serverSendMessage(): e = ${e.message} ; result = ${result?.toString()}")
                        }
                    }
                    else {
                        setSendMessage(false)
                        Utilities.log(Enums.LogType.Error, TAG, "serverSendMessage(): response = $response")
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {

                    setSendMessage(false)

                    Utilities.log(Enums.LogType.Error, TAG, "serverSendMessage(): onFailure = $t")
                    call.cancel()
                }
            })
        }
    }

    private fun setSendMessage(success: Boolean?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setSendMessage()", showToast = false)
        this.sendMessageCallback.postValue(success)
    }

    //endregion == message =======================
}