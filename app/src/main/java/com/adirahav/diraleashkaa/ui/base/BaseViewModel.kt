package com.adirahav.diraleashkaa.ui.base

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.StringEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

abstract class BaseViewModel : ViewModel() {
    private val TAG = "BaseViewModel"

    // strings
    val roomBaseStrings: MutableLiveData<ArrayList<StringEntity>?> = MutableLiveData()

    //region strings
    fun getRoomStrings(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomStrings()")

        CoroutineScope(Dispatchers.IO).launch {
            val strings = DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.getAll()
            Timer("Strings", false).schedule(Configuration.ROOM_AWAIT_MILLISEC) {
                setStrings(strings as ArrayList<StringEntity>)
            }
        }
    }

    private fun setStrings(strings: ArrayList<StringEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setStrings()", showToast = false)
        this.roomBaseStrings.postValue(strings)
    }
    //endregion strings
}