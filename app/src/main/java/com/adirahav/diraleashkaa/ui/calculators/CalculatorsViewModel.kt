package com.adirahav.diraleashkaa.ui.calculators

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.PropertyDataClass
import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.PropertyModel
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import com.adirahav.diraleashkaa.data.network.services.CalculatorsService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalculatorsViewModel internal constructor(
        private val activity: CalculatorsActivity,
        private val calculatorsService: CalculatorsService) : BaseViewModel() {

    companion object {
        private const val TAG = "CalculatorsViewModel"
    }

    // calculators
    val localCalculatorsCallback: MutableLiveData<List<CalculatorEntity>?> = MutableLiveData()

    // fixed parameters
    val localFixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val localUserCallback: MutableLiveData<UserEntity> = MutableLiveData()

    //region == calculators ===================

    fun getLocalCalculators(applicationContext: Context, _id: String?) {

        CoroutineScope(Dispatchers.IO).launch {
            val resultsCalculators = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.getAll()
            setLocalCalculators(resultsCalculators)
        }
    }

    private fun setLocalCalculators(calculators: List<CalculatorEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalCalculators()", showToast = false)
        this.localCalculatorsCallback.postValue(calculators)
    }

    //endregion == calculators ===================

    //region == fixed parameters ==============
    fun getLocalFixedParameters(applicationContext: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            setFixedParameters(fixedParameters?.first())
        }
    }

    private fun setFixedParameters(fixedParameters: FixedParametersEntity?) {
        this.localFixedParametersCallback.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    fun getLocalUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalUser()")

        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setLocalUser(resultUser)
            }
        }
        else {
            setLocalUser(null)
        }

    }

    private fun setLocalUser(user: UserEntity?) {
        this.localUserCallback.postValue(user)
    }

    //endregion == user ==========================
}