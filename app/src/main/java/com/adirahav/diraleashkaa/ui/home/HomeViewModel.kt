package com.adirahav.diraleashkaa.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.dataClass.HomeDataClass
import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.data.network.services.HomeService
import com.adirahav.diraleashkaa.data.network.services.PropertyService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HomeViewModel internal constructor(
        private val propertyService: PropertyService,
        private val homeService: HomeService

        ) : BaseViewModel() {

    private val TAG = "HomeViewModel"

    // fixed parameters
    val roomFixedParameters: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // my cities
    val roomMyCities: MutableLiveData<List<PropertyEntity>> = MutableLiveData()

    // city's properties
    val roomCityProperties: MutableLiveData<ArrayList<PropertyEntity>>

    // delete property
    val roomDeleteProperty: MutableLiveData<Int>
    val serverDeleteProperty: MutableLiveData<Int>

    // best yield
    val roomBestYield: MutableLiveData<List<BestYieldEntity>>

    // home
    val serverHome: MutableLiveData<HomeDataClass>

    init {
        // fixed parameters

        // my cities

        // city's properties
        roomCityProperties = MutableLiveData()

        // delete property
        roomDeleteProperty = MutableLiveData()
        serverDeleteProperty = MutableLiveData()

        // best yield
        roomBestYield = MutableLiveData()

        // home
        serverHome = MutableLiveData()
    }

    //region fixed parameters
    fun getRoomFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.ROOM_AWAIT_MILLISEC) {
                setRoomFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setRoomFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setFixedParameters()", showToast = false)
        this.roomFixedParameters.postValue(fixedParameters)
    }
    //endregion fixed parameters

    //region my cities
    fun getRoomMyCities(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getRoomMyCities()")
        CoroutineScope(Dispatchers.IO).launch {
            //getMyCitiesSuspend(applicationContext, lifecycleOwner)
            val resultsProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getMyCities()
            setMyCities(resultsProperties)
        }
    }

    private fun setMyCities(cities: List<PropertyEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setMyCities()", showToast = false)
        this.roomMyCities.postValue(cities)
    }

    //endregion my cities

    //region city's properties
    fun getCityProperties(applicationContext: Context, city: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val resultProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getCityProperties(city)
            setCityProperties(resultProperties as ArrayList<PropertyEntity>)
        }
    }

    private fun setCityProperties(properties: ArrayList<PropertyEntity>?) {
        this.roomCityProperties.postValue(properties)
    }

    //endregion city's properties

    //region delete property
    // == SERER =====
    fun deleteServerProperty(applicationContext: Context, propertyData: PropertyEntity?, userUUID: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty()")

        CoroutineScope(Dispatchers.IO).launch {

            /**/
            val call: Call<HomeModel?>? = propertyService.propertyAPI.deleteFromHome(propertyData?.uuid!!, userUUID)
            call?.enqueue(object : Callback<HomeModel?> {
                override fun onResponse(call: Call<HomeModel?>, response: Response<HomeModel?>) {

                    Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                    val result: HomeModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                            saveServerHomeToRoom(applicationContext, result?.data)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                            setServerHome(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): response = $response")
                        setServerHome(null)
                    }
                }

                override fun onFailure(call: Call<HomeModel?>, t: Throwable) {
                    Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): onFailure = $t")
                    setServerHome(null)
                    call.cancel()
                }
            })
            /**/

            /*

            call?.enqueue(object : Callback<PropertyModel?> {
                override fun onResponse(call: Call<PropertyModel?>, response: Response<PropertyModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                    val result: PropertyModel? = response.body()

                    if (response.code() == 200) {
                        //setPropertiesAfterServerDelete(result)
                    }
                    else {
                        setPropertiesAfterServerDelete(0)
                        Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", propertyData = propertyData)
                    }
                }

                override fun onFailure(call: Call<PropertyModel?>, t: Throwable) {
                    setPropertiesAfterServerDelete(0)
                    Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): onFailure = $t")
                    call.cancel()
                }
            })*/
        }
    }

    private fun setPropertiesAfterServerDelete(deletedItemsCount: Int) {
        Utilities.log(Enums.LogType.Debug, TAG, "setPropertiesAfterServerDelete()", showToast = false)
        this.serverDeleteProperty.postValue(deletedItemsCount)
    }


    //endregion delete property

    //region best yield
    fun getRoomBestYield(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getBestYield()")

        CoroutineScope(Dispatchers.IO).launch {
            val resultProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.getAll()
            setRoomBestYield(resultProperties)
        }
    }

    private fun setRoomBestYield(bestYield: List<BestYieldEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setBestYield()", showToast = false)
        this.roomBestYield.postValue(bestYield)
    }
    //endregion best yield

    //region home
    fun getServerHome(applicationContext: Context, userUUID: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "getServerHome()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<HomeModel?>? = homeService.homeAPI.get(userUUID)
            call?.enqueue(object : Callback<HomeModel?> {
                override fun onResponse(call: Call<HomeModel?>, response: Response<HomeModel?>) {

                    Utilities.log(Enums.LogType.Debug, TAG, "getServerHome(): response = $response")

                    val result: HomeModel? = response.body()

                    if (response.code() == 200 && response.body()?.success == true) {
                        try {
                            Utilities.log(Enums.LogType.Debug, TAG, "getServerHome(): response = $response")
                            saveServerHomeToRoom(applicationContext, result?.data)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): e = ${e.message} ; result.data = ${result?.data.toString()}")
                            setServerHome(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): response = $response")
                        setServerHome(null)
                    }
                }

                override fun onFailure(call: Call<HomeModel?>, t: Throwable) {
                    Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): onFailure = $t")
                    setServerHome(null)
                    call.cancel()
                }
            })
        }
    }

    private fun setServerHome(home: HomeDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerHome()", showToast = false)
        this.serverHome.postValue(home)
    }

    fun saveServerHomeToRoom(applicationContext: Context, home: HomeDataClass?) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveServerHomeToRoom(): home = ${home.toString()}")
        CoroutineScope(Dispatchers.IO).launch {

            // ----------------
            // properties
            // ----------------
            val roomPropertiesList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getAll()

            var oldProperties = if (roomPropertiesList != null)
                                    ArrayList(roomPropertiesList.map { it.copy() }).filter { item -> item.uuid != "" }
                                else
                                    null

            if (oldProperties != null) {
                for (property in oldProperties) {
                    property.roomID = null
                }
            }

            if (roomPropertiesList != null) {
                if (roomPropertiesList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.deleteAll()!!
                }
            }

            val newProperties = home?.properties
            if (newProperties != null) {
                for (newProperty in newProperties) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(newProperty)!!
                }
            }

            val emptyProperty = home?.emptyProperty
            if (emptyProperty != null) {
                emptyProperty.roomID = 0L
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(emptyProperty)!!
            }

            val isPropertiesNeedToRefresh = oldProperties?.equals(newProperties) != true

            // ----------------
            // best yield
            // ----------------
            val roomBestYieldList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.getAll()

            if (roomBestYieldList != null) {
                if (roomBestYieldList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.deleteAll()!!
                }
            }

            val oldBestYieldList = if (roomBestYieldList != null)
                ArrayList(roomBestYieldList.map { it.copy() })
            else
                null

            if (oldBestYieldList != null) {
                for (property in oldBestYieldList) {
                    property.roomID = null
                }
            }

            val newBestYields = home?.bestYields
            if (newBestYields != null) {
                for (newBestYield in newBestYields) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.insert(newBestYield)!!
                }
            }

            val isBestYieldNeedToRefresh = oldBestYieldList?.equals(newBestYields) != true

            setServerHome(newProperties, newBestYields, isPropertiesNeedToRefresh, isBestYieldNeedToRefresh)
        }
    }

    private fun setServerHome(
        properties: List<PropertyEntity>?,
        bestYields: List<BestYieldEntity>?,
        isPropertiesNeedToRefresh: Boolean,
        isBestYieldNeedToRefresh: Boolean) {
        Utilities.log(Enums.LogType.Debug, TAG, "setServerHome()", showToast = false)
        val roomHomeData = HomeDataClass(
            properties = properties,
            bestYields = bestYields,
            isPropertiesNeedToRefresh = isPropertiesNeedToRefresh,
            isBestYieldNeedToRefresh = isBestYieldNeedToRefresh,
        )
        this.serverHome.postValue(roomHomeData)
    }
    //endregion home
}