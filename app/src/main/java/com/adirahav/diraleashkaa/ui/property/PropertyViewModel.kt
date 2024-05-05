package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.*
import com.adirahav.diraleashkaa.data.network.requests.PropertyRequest
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

class PropertyViewModel internal constructor(
		private val activity: PropertyActivity,
		private val propertyService: PropertyService) : BaseViewModel() {

	private val TAG = "PropertyViewModel"

	// fixed parameters
	val fixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

	// user
	val roomUserGet: MutableLiveData<UserEntity> = MutableLiveData()

	// property
	val propertyGet: MutableLiveData<PropertyEntity> = MutableLiveData()
	val propertyInsert: MutableLiveData<PropertyEntity> = MutableLiveData()
	val propertyUpdate: MutableLiveData<PropertyEntity> = MutableLiveData()
	val localPropertyGet: MutableLiveData<PropertyEntity> = MutableLiveData()
	val localPropertyInsert: MutableLiveData<PropertyEntity> = MutableLiveData()
	val localPropertyUpdate: MutableLiveData<PropertyEntity> = MutableLiveData()
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
			Timer("FixedParameters", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
				setFixedParameters(fixedParameters?.first())
			}
		}
	}

	private fun setFixedParameters(fixedParameters: FixedParametersEntity?) {
		this.fixedParametersCallback.postValue(fixedParameters)
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

	fun actionProperty(propertyId: String?, fieldName: String?, fieldValue: String?) {
		Utilities.log(Enums.LogType.Debug, TAG, "actionProperty(): propertyId = ${propertyId} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")

		CoroutineScope(Dispatchers.IO).launch {

			val action = if (!propertyId.isNullOrEmpty() && fieldName == null && fieldValue == null)
							Enums.ObserverAction.GET
						 else if (propertyId.isNullOrEmpty())
						 	Enums.ObserverAction.CREATE
						 else
						 	Enums.ObserverAction.UPDATE

			val call: Call<PropertyEntity?>? =
				if (action == Enums.ObserverAction.GET)
					propertyService.propertyAPI.getProperty(
						token = "Bearer ${activity.userToken}",
						_id = propertyId!!
					)
				else if (action == Enums.ObserverAction.CREATE)
					propertyService.propertyAPI.createProperty(
						token = "Bearer ${activity.userToken}",
						PropertyRequest(propertyId = null, fieldName = fieldName!!, fieldValue = fieldValue!!)
					)
				else
					propertyService.propertyAPI.updateProperty(
						token = "Bearer ${activity.userToken}",
						PropertyRequest(propertyId = propertyId!!, fieldName = fieldName!!, fieldValue = fieldValue)
					)

			call?.enqueue(object : Callback<PropertyEntity?> {
				override fun onResponse(call: Call<PropertyEntity?>, response: Response<PropertyEntity?>) {
					Utilities.log(Enums.LogType.Debug, TAG, "actionServerProperty(): action = $action ; response = $response ; body = ${response.body()}")
					val result: PropertyEntity? = response.body()

					if (response.code() == 200) {
						try {
							setProperty(action, result)
						}
						catch (e: Exception) {
							Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; e = ${e.message} ; result.data = ${result?.toString()}")
							setProperty(action, null)
						}
					}
					else {
						Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; response = $response")
						setProperty(action, null)
					}
				}

				override fun onFailure(call: Call<PropertyEntity?>, t: Throwable) {
					setProperty(action, null)
					Utilities.log(Enums.LogType.Error, TAG, "actionServerProperty(): action = $action ; onFailure = $t ; _id = ${propertyId} ; fieldName = ${fieldName} ; fieldValue = ${fieldValue}")
					call.cancel()
				}
			})
		}
	}

	private fun setProperty(action: Enums.ObserverAction, response: PropertyEntity?) {
		Utilities.log(Enums.LogType.Debug, TAG, "setProperty(): action = $action", showToast = false)
		when (action) {
			Enums.ObserverAction.GET ->
				this.propertyGet.postValue(response)
			Enums.ObserverAction.CREATE ->
				this.propertyInsert.postValue(response)
			Enums.ObserverAction.UPDATE ->
				this.propertyUpdate.postValue(response)

			else -> {}
		}
	}

	// == LOCAL =====

	fun getLocalProperty(applicationContext: Context, propertyId: String?) {
		CoroutineScope(Dispatchers.IO).launch {
			val localProperty =
				DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()
					?.findById(
						if (!(propertyId.isNullOrEmpty()))
							propertyId
						else
							""
					)
			setLocalProperty(localProperty)
		}
	}

	private fun setLocalProperty(property: PropertyEntity?) {
		this.localPropertyGet.postValue(property)
	}

	fun actionLocalProperty(applicationContext: Context, propertyData: PropertyEntity?) {
		Utilities.log(Enums.LogType.Debug, TAG, "actionRoomProperty(): propertyData = $propertyData")

		val action = if (propertyData?.roomID == null || propertyData.roomID == 0L)
			Enums.ObserverAction.INSERT_LOCAL
		else
			Enums.ObserverAction.UPDATE_LOCAL

		CoroutineScope(Dispatchers.IO).launch {
			if (action == Enums.ObserverAction.INSERT_LOCAL) {
				propertyData?.roomID = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(propertyData!!)
				Utilities.log(Enums.LogType.Debug, TAG, "actionLocalProperty(): action = $action ; roomID = ${propertyData?.roomID}")
			}
			else {
				DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.update(propertyData!!)
				Utilities.log(Enums.LogType.Debug, TAG, "actionLocalProperty(): action = $action ; roomID = ${propertyData?.roomID}")
			}
			setLocalPropertySave(action, propertyData!!)
		}
	}

	private fun setLocalPropertySave(action: Enums.ObserverAction, property: PropertyEntity) {
		Utilities.log(Enums.LogType.Debug, TAG, "setLocalPropertySave(): action = $action", showToast = false)
		if (action == Enums.ObserverAction.INSERT_LOCAL) {
			this.localPropertyInsert.postValue(property)
		}
		else {
			this.localPropertyUpdate.postValue(property)
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