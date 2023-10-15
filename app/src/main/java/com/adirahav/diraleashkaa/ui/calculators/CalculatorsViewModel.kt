package com.adirahav.diraleashkaa.ui.calculators

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.CalculatorDataClass
import com.adirahav.diraleashkaa.data.network.dataClass.PropertyDataClass
import com.adirahav.diraleashkaa.data.network.entities.CalculatorEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.data.network.models.PropertyModel
import com.adirahav.diraleashkaa.data.network.services.CalculatorsService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CalculatorsViewModel internal constructor(private val calculatorsService: CalculatorsService) : BaseViewModel() {

    companion object {
        private const val TAG = "CalculatorsViewModel"
    }

    // calculators
    val roomCalculators: MutableLiveData<List<CalculatorEntity>?> = MutableLiveData()

    // fixed parameters
    val roomFixedParameters: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // user
    val roomUser: MutableLiveData<UserEntity> = MutableLiveData()

    // max price
    val roomMaxPrice: MutableLiveData<PropertyEntity> = MutableLiveData()
    val serverMaxPrice: MutableLiveData<PropertyEntity> = MutableLiveData()

    //region == calculators =============

    fun getRoomCalculators(applicationContext: Context, userUUID: String?) {

        CoroutineScope(Dispatchers.IO).launch {
            val resultsCalculators = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.getAll()
            setRoomCalculators(resultsCalculators)
        }
    }

    private fun setRoomCalculators(calculators: List<CalculatorEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomCalculators()", showToast = false)
        this.roomCalculators.postValue(calculators)
    }

    //endregion == calculators =============

    //region == fixed parameters ==============
    fun getRoomFixedParameters(applicationContext: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            setFixedParameters(fixedParameters?.first())
        }
    }

    private fun setFixedParameters(fixedParameters: FixedParametersEntity?) {
        this.roomFixedParameters.postValue(fixedParameters)
    }
    //endregion == fixed parameters ==============

    //region == user ==========================

    fun getRoomUser(applicationContext: Context, userID: Long?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getUser()")

        if (userID != null && userID > 0L) {
            CoroutineScope(Dispatchers.IO).launch {
                val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
                setUser(resultUser)
            }
        }
        else {
            setUser(null)
        }

    }

    private fun setUser(user: UserEntity?) {
        this.roomUser.postValue(user)
    }

    //endregion == user ==========================

    //region == max price =====================

    // == ROOM  ======

    fun getRoomMaxPrice(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "calcRoomMaxPrice(): showToast = false")

        CoroutineScope(Dispatchers.IO).launch {
            val result = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.getMaxPrice()
            setRoomMaxPrice(result)
        }
    }

    private fun setRoomMaxPrice(response: PropertyEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomMaxPrice(): showToast = false")
        this.roomMaxPrice.postValue(response)
    }

    fun saveRoomMaxPrice(applicationContext: Context, property: PropertyEntity) {
        Utilities.log(Enums.LogType.Debug, TAG, "calcRoomMaxPrice(): showToast = false")

        CoroutineScope(Dispatchers.IO).launch {
            val result = DatabaseClient.getInstance(applicationContext)?.appDatabase?.calculatorDao()?.updateMaxPrice(property)
        }
    }

    // == SERER =====
    fun calcServerMaxPrice(userUUID: String?, fieldName: String?, fieldValue: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "calcServerMaxPrice(): userUUID = ${userUUID} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<PropertyModel?>? =
                    calculatorsService.calculatorsAPI.maxPrice(
                            userUUID = userUUID,
                            fieldName = fieldName,
                            fieldValue = fieldValue,
                    )

            call?.enqueue(object : Callback<PropertyModel?> {
                override fun onResponse(call: Call<PropertyModel?>, response: Response<PropertyModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "calcServerMaxPrice(): response = $response ; body = ${response.body()}")
                    val result: PropertyModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            setServerMaxPrice(result?.data)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                            setServerMaxPrice(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): response = $response")
                        setServerMaxPrice(null)
                    }
                }

                override fun onFailure(call: Call<PropertyModel?>, t: Throwable) {
                    setServerMaxPrice(null)
                    Utilities.log(Enums.LogType.Error, TAG, "calcServerMaxPrice(): onFailure = $t ; userUUID = ${userUUID} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")
                    call.cancel()
                }
            })
        }
    }

    private fun setServerMaxPrice(response: PropertyDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerMaxPrice(): showToast = false")
        this.serverMaxPrice.postValue(response?.property)
    }

    //endregion == max price =====================
}