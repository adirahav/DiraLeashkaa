package com.adirahav.diraleashkaa.ui.contactus

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

class ContactUsViewModel internal constructor() : BaseViewModel() {

    private val TAG = "ContactUsViewModel"

    // fixed parameters
    val roomFixedParametersGet: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()

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

}