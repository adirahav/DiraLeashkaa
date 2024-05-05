package com.adirahav.diraleashkaa.ui.base

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.PhraseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

abstract class BaseViewModel : ViewModel() {
    private val TAG = "BaseViewModel"

    // strings
    val roomBaseStrings: MutableLiveData<ArrayList<PhraseEntity>?> = MutableLiveData()

    //region strings
    fun getRoomPhrases(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomPhrases()")

        CoroutineScope(Dispatchers.IO).launch {
            val strings = DatabaseClient.getInstance(applicationContext)?.appDatabase?.stringsDao()?.getAll()
            Timer("Strings", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setStrings(strings as ArrayList<PhraseEntity>)
            }
        }
    }

    private fun setStrings(strings: ArrayList<PhraseEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setStrings()", showToast = false)
        this.roomBaseStrings.postValue(strings)
    }
    //endregion strings
}