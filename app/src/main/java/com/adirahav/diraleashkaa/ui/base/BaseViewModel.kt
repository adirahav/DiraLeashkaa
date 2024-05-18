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

    // phrases
    val localBasePhrasesCallback: MutableLiveData<ArrayList<PhraseEntity>?> = MutableLiveData()

    //region phrases
    fun getLocalPhrases(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalPhrases()")

        CoroutineScope(Dispatchers.IO).launch {
            val phrases = DatabaseClient.getInstance(applicationContext)?.appDatabase?.phrasesDao()?.getAll()
            Timer("Phrases", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setPhrases(phrases as ArrayList<PhraseEntity>)
            }
        }
    }

    private fun setPhrases(phrases: ArrayList<PhraseEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()", showToast = false)
        this.localBasePhrasesCallback.postValue(phrases)
    }
    //endregion phrases
}