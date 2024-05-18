package com.adirahav.diraleashkaa.ui.calculators

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
import com.adirahav.diraleashkaa.data.network.services.CalculatorsService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalculatorViewModel internal constructor(
        private val activity: CalculatorActivity,
        private val calculatorsService: CalculatorsService) : BaseViewModel() {

    companion object {
        private const val TAG = "CalculatorViewModel"
    }

    // fixed parameters
    val localFixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val localUserCallback: MutableLiveData<UserEntity> = MutableLiveData()

    // max price
    val localMaxPriceCallback: MutableLiveData<PropertyEntity> = MutableLiveData()
    val serverMaxPriceCallback: MutableLiveData<PropertyEntity> = MutableLiveData()

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

    //region == max price =====================

    // == LOCAL  =====

    fun getLocalMaxPrice(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalMaxPrice(): showToast = false")

        CoroutineScope(Dispatchers.IO).launch {
            val result = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.getMaxPrice()
            setLocalMaxPrice(result)
        }
    }

    private fun setLocalMaxPrice(response: PropertyEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalMaxPrice(): showToast = false")
        this.localMaxPriceCallback.postValue(response)
    }

    fun saveLocalMaxPrice(applicationContext: Context, property: PropertyEntity) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveLocalMaxPrice(): showToast = false")

        CoroutineScope(Dispatchers.IO).launch {
            val result = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.updateMaxPrice(property)
        }
    }

    // == SERER =====
    fun calcServerMaxPrice(fieldName: String?, fieldValue: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "calcServerMaxPrice(): fieldName = ${fieldName} ; fieldValue = ${fieldValue}")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<PropertyEntity?>? =
                    calculatorsService.calculatorsAPI.maxPrice(
                        token = "Bearer ${activity.userToken}",
                        PropertyRequest(propertyId = "", fieldName = fieldName, fieldValue = fieldValue)
                    )

            call?.enqueue(object : Callback<PropertyEntity?> {
                override fun onResponse(call: Call<PropertyEntity?>, response: Response<PropertyEntity?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "calcServerMaxPrice(): response = $response ; body = ${response.body()}")
                    val result: PropertyEntity? = response.body()

                    if (response.code() == 200) {
                        try {
                            setServerMaxPrice(result)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): e = ${e.message} ; result.data = ${result}")
                            setServerMaxPrice(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): response = $response")
                        setServerMaxPrice(null)
                    }
                }

                override fun onFailure(call: Call<PropertyEntity?>, t: Throwable) {
                    setServerMaxPrice(null)
                    Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): onFailure = $t ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")
                    call.cancel()
                }
            })
        }
    }

    private fun setServerMaxPrice(response: PropertyEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerMaxPrice(): showToast = false")
        this.serverMaxPriceCallback.postValue(response)
    }

    //endregion == max price =====================
}