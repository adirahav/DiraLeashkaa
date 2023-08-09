package com.adirahav.diraleashkaa.ui.signin

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.AppPreferences
import com.adirahav.diraleashkaa.data.network.models.APIResponseModel
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.home.HomeActivity

class SignInViewModel internal constructor(private val activityContext: Context) : BaseViewModel() {

    private var _email = ""    // save it just for the dummy content

    // shared preferences
    var preferences: AppPreferences? = null

    // sign in data
    val signInData: MutableLiveData<APIResponseModel>

    init {
        // shared preferences
        preferences = AppPreferences.instance

        // sign in data
        signInData = MutableLiveData()
    }

    fun submitLogin(email: String) {
        _email = email

        // login
        /*service.signInAPI
                .setLogin(email, password)
                ?.enqueue(SignInCallback())*/

        // TO DELETE - DUMMY CONTENT
        //val userData = DummyData.instance!!.createDummyUserName(email)
        //setSignInData(userData)
        // TO DELETE - DUMMY CONTENT
    }

    private fun setSignInData(signInData: APIResponseModel) {

        // save user details
        /*signInData?.userID?.let { preferences!!.setLong("userID", it, true) }
        preferences!!.setString("userAvatar", signInData.avatar, false)
        preferences!!.setString("userName", signInData.userName, false)*/

        // sign in data
        this.signInData.postValue(signInData)

        // goto HomeActivity
        HomeActivity.start(activityContext)
    }

    /*private inner class SignInCallback : Callback<APIResponseModel?> {
        override fun onResponse(call: Call<APIResponseModel?>, response: Response<APIResponseModel?>) {
            setSignInData(response.body()!!)
        }

        override fun onFailure(call: Call<APIResponseModel?>, t: Throwable) {

        }
    }*/
}