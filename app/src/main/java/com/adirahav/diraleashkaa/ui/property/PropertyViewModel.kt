package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.PropertyDataClass
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.data.network.services.PropertyService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.schedule

class PropertyViewModel internal constructor(private val propertyService: PropertyService) : BaseViewModel() {

	private val TAG = "PropertyViewModel"

	// fixed parameters
	val roomFixedParametersGet: MutableLiveData<FixedParametersEntity> = MutableLiveData()

	// user
	val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()

	// property
	val serverPropertyGet: MutableLiveData<PropertyEntity> = MutableLiveData()
	val serverPropertyInsert: MutableLiveData<PropertyEntity> = MutableLiveData()
	val serverPropertyUpdate: MutableLiveData<PropertyEntity> = MutableLiveData()
	val roomPropertyGet: MutableLiveData<PropertyEntity> = MutableLiveData()
	val roomPropertyInsertRoom: MutableLiveData<PropertyEntity> = MutableLiveData()
	val roomPropertyUpdateRoom: MutableLiveData<PropertyEntity> = MutableLiveData()
	val roomPropertyUpdateServer: MutableLiveData<PropertyEntity> = MutableLiveData()

	// yield forecast
	val yieldForecastList: MutableLiveData<List<YieldForecastEntity>> = MutableLiveData()

	// yield forecast
	val amortizationScheduleList: MutableLiveData<List<AmortizationScheduleEntity>> = MutableLiveData()

	// chart
	val chart: MutableLiveData<List<YieldForecastEntity>> = MutableLiveData()

	//region == fixed parameters ==============
	fun getRoomFixedParameters(applicationContext: Context) {
		CoroutineScope(Dispatchers.IO).launch {
			val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
			Timer("FixedParameters", false).schedule(Configuration.ROOM_AWAIT_MILLISEC) {
				setFixedParameters(fixedParameters?.first())
			}
		}
	}

	private fun setFixedParameters(fixedParameters: FixedParametersEntity?) {
		this.roomFixedParametersGet.postValue(fixedParameters)
	}
	//endregion == fixed parameters ==============

	//region == user ==========================

	fun getRoomUser(applicationContext: Context, userID: Long?) {
		Utilities.log(Enums.LogType.Debug, TAG, "getUser()")

		if (userID != null && userID > 0L) {
			CoroutineScope(Dispatchers.IO).launch {
				val resultUser = DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()?.findById(userID)
				setUser(resultUser)
				/*DatabaseClient.getInstance(applicationContext)?.appDatabase?.userDao()
					?.findById(userID!!)?.observe(lifecycleOwner,
					Observer { it ->
						setUser(it)
					})*/
			}
		}
		else {
			setUser(null)
		}

	}

	private fun setUser(user: UserEntity?) {
		this.roomUserGet.postValue(user)
	}


	//endregion == user ==========================

	//region == property ======================

	// == SERER =====

	fun actionServerProperty(propertyUUID: String?, userUUID: String?, fieldName: String?, fieldValue: String?) {
		Utilities.log(Enums.LogType.Debug, TAG, "actionServerProperty(): propertyUUID = ${propertyUUID} ; userUUID = ${userUUID} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")

		CoroutineScope(Dispatchers.IO).launch {

			val action = if (!propertyUUID.isNullOrEmpty() && fieldName == null && fieldValue == null)
							Enums.ObserverAction.GET_SERVER
						 else if (propertyUUID.isNullOrEmpty())
						 	Enums.ObserverAction.INSERT_SERVER
						 else
						 	Enums.ObserverAction.UPDATE_SERVER

			val call: Call<PropertyModel?>? =
				if (action == Enums.ObserverAction.GET_SERVER)
					propertyService.propertyAPI.getProperty(
						UUID = propertyUUID,
						userUUID = userUUID
					)
				else if (action == Enums.ObserverAction.INSERT_SERVER)
					propertyService.propertyAPI.insertProperty(
						userUUID = userUUID,
						fieldName = fieldName,
						fieldValue = fieldValue
					)
				else
					propertyService.propertyAPI.updateProperty(
						UUID = propertyUUID,
						userUUID = userUUID,
						fieldName = fieldName,
						fieldValue = fieldValue
					)

			call?.enqueue(object : Callback<PropertyModel?> {
				override fun onResponse(call: Call<PropertyModel?>, response: Response<PropertyModel?>) {
					Utilities.log(Enums.LogType.Debug, TAG, "actionServerProperty(): action = $action ; response = $response ; body = ${response.body()}")
					val result: PropertyModel? = response.body()

					if (response.code() == 200 && response.body()?.success == true) {
						try {
							setServerPropertyInsertUpdate(action, result?.data)
						}
						catch (e: Exception) {
							Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; e = ${e.message} ; result.data = ${result?.data.toString()}")
							setServerPropertyInsertUpdate(action, null)
						}
					}
					else {
						Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; response = $response")
						setServerPropertyInsertUpdate(action, null)
					}
				}

				override fun onFailure(call: Call<PropertyModel?>, t: Throwable) {
					setServerPropertyInsertUpdate(action, null)
					Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; onFailure = $t ; uuid = ${propertyUUID} ; userUUID = ${userUUID} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")
					call.cancel()
				}
			})
		}
	}

	private fun setServerPropertyInsertUpdate(action: Enums.ObserverAction, response: PropertyDataClass?) {
		Utilities.log(Enums.LogType.Debug, TAG, "setServerPropertyInsertUpdate(): action = $action", showToast = false)
		when (action) {
			Enums.ObserverAction.GET_SERVER ->
				this.serverPropertyGet.postValue(response?.property)
			Enums.ObserverAction.INSERT_SERVER ->
				this.serverPropertyInsert.postValue(response?.property)
			Enums.ObserverAction.UPDATE_SERVER ->
				this.serverPropertyUpdate.postValue(response?.property)
		}
	}

	// == ROOM =====

	fun getRoomProperty(applicationContext: Context, propertyUUID: String?) {
		CoroutineScope(Dispatchers.IO).launch {
			val resultProperty =
				DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()
					?.findByUUID(
						if (!(propertyUUID.isNullOrEmpty()))
							propertyUUID
						else
							""
					)
			setRoomProperty(resultProperty)
		}
	}

	private fun setRoomProperty(property: PropertyEntity?) {
		this.roomPropertyGet.postValue(property)
	}

	fun actionRoomProperty(applicationContext: Context, propertyData: PropertyEntity?) {
		Utilities.log(Enums.LogType.Debug, TAG, "actionRoomProperty(): propertyData = $propertyData")

		val action = if (propertyData?.roomID == null || propertyData.roomID == 0L)
			Enums.ObserverAction.INSERT_ROOM
		else
			Enums.ObserverAction.UPDATE_ROOM

		CoroutineScope(Dispatchers.IO).launch {
			if (action == Enums.ObserverAction.INSERT_ROOM) {
				propertyData?.roomID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(propertyData!!)
				Utilities.log(Enums.LogType.Debug, TAG, "actionRoomProperty(): action = $action ; roomID = ${propertyData?.roomID}")
			}
			else {
				DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.update(propertyData!!)
				Utilities.log(Enums.LogType.Debug, TAG, "actionRoomProperty(): action = $action ; roomID = ${propertyData?.roomID}")
			}
			setRoomPropertyInsertUpdate(action, propertyData!!)
		}
	}

	private fun setRoomPropertyInsertUpdate(action: Enums.ObserverAction, property: PropertyEntity) {
		Utilities.log(Enums.LogType.Debug, TAG, "setRoomPropertyInsertUpdate(): action = $action", showToast = false)
		if (action == Enums.ObserverAction.INSERT_ROOM) {
			this.roomPropertyInsertRoom.postValue(property)
		}
		else {
			this.roomPropertyUpdateRoom.postValue(property)
		}
	}

	//endregion == property ======================

	//region == yield forecast ================

	fun getYieldForecast(_activity: PropertyActivity) {
		val yieldForecastList = _activity.yieldForecastList
		setYieldForecast(yieldForecastList)
	}

	private fun setYieldForecast(yieldForecastList: List<YieldForecastEntity>) {

		// yield forecast months
		this.yieldForecastList.postValue(yieldForecastList)
	}

	//endregion == yield forecast ================

	//region == amortization schedule =========

	fun getAmortizationSchedule(_activity: PropertyActivity) {
		val amortizationScheduleList = _activity.amortizationScheduleList
		setAmortizationSchedule(amortizationScheduleList)
	}

	private fun setAmortizationSchedule(amortizationScheduleList: List<AmortizationScheduleEntity>) {

		// yield forecast months
		this.amortizationScheduleList.postValue(amortizationScheduleList)
	}

	//endregion == amortization schedule =========

	//region == chart =========================

	fun getChart(_activity: PropertyActivity) {
		val chartList = _activity.yieldForecastList
		setChart(chartList)
	}

	private fun setChart(chartList: List<YieldForecastEntity>) {

		// yield forecast months
		this.chart.postValue(chartList)
	}

	//endregion == chart =========================
}