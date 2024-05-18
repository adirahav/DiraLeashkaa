package com.adirahav.diraleashkaa.ui.contactus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.getMapStringValue
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.ActivityContactusBinding
import kotlinx.coroutines.*
import java.util.*

class ContactUsActivity : BaseActivity<ContactUsViewModel?, ActivityContactusBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "ContactUsActivity"
        const val EXTRA_PAGE_TYPE = "EXTRA_PAGE_TYPE"

        fun start(context: Context, pageType: Enums.ContactUsPageType) {
            val intent = Intent(context, ContactUsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_PAGE_TYPE, pageType.toString())
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // shared preferences
    var preferences: AppPreferences? = null

    // user id
    var roomUID: Long? = 0L

    // user data
    var userData: UserEntity? = null

    // page type
    var pageType : Enums.ContactUsPageType? = null

    // fragments
    private val contactUsMailFormFragment = ContactUsMailFormFragment()

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // local/server data loaded
    var isLocalFixedParametersLoaded: Boolean = false
    var isLocalUserLoaded: Boolean = false
    var isDataInit: Boolean = false

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // layout
    internal lateinit var layout: ActivityContactusBinding

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityContactusBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }

    public override fun onResume() {
        super.onResume()
        log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        initGlobal()
        initData()

        lifecycleOwner = this
        initObserver()

        setCustomActionBar(layout.drawer)
        setDrawer(layout.drawer, layout.menu)

        // title text
        titleText?.text = Utilities.getLocalPhrase("actionbar_title_contact_us")

        // track user
        //Log.d("ADITEST5", "GET ${preferences?.getBoolean("isTrackUser", false).toString()} [contact us]")
        /*trackUser?.visibility =
            if (preferences?.getBoolean("isTrackUser", false) == true)
                VISIBLE
            else
                GONE*/

    }

    override fun createViewModel(): ContactUsViewModel {
        val factory = ContactUsViewModelFactory(this@ContactUsActivity, DataManager.instance!!.contactUsService)
        return ViewModelProvider(this, factory)[ContactUsViewModel::class.java]
    }

    //endregion == lifecycle methods ==

    //region == initialize =========

    fun initObserver() {
        log(Enums.LogType.Debug, TAG, "initObserver()", showToast = false)
        if (!viewModel!!.getLocalFixedParametersCallback.hasObservers()) viewModel!!.getLocalFixedParametersCallback.observe(this@ContactUsActivity, LocalFixedParametersObserver())
        if (!viewModel!!.getLocalUserCallback.hasObservers()) viewModel!!.getLocalUserCallback.observe(this@ContactUsActivity, LocalUserObserver())
        if (!viewModel!!.sendMessageCallback.hasObservers()) viewModel!!.sendMessageCallback.observe(this@ContactUsActivity, ServerMessageObserver())

        if (!isLocalFixedParametersLoaded && !isLocalUserLoaded && !isDataInit) {
            viewModel!!.getLocalFixedParameters(applicationContext)
            viewModel!!.getLocalUser(applicationContext, roomUID)
        }
    }

    private fun initGlobal() {

        // shared preferences
        preferences = AppPreferences.instance

        // user id
        roomUID = preferences?.getLong("roomUID", 0L)

        // local/server data loaded
        isLocalFixedParametersLoaded = false
        isLocalUserLoaded = false

        isDataInit = false

        // buttons
        layout.buttons.back.visibility = GONE
        layout.buttons.next.visibility = GONE
        layout.buttons.save.visibility = GONE
        layout.buttons.pay.visibility = GONE
        layout.buttons.send.visibility = VISIBLE

        // page type
        pageType =
            if (intent.getStringExtra(EXTRA_PAGE_TYPE) != null)
                enumValueOf<Enums.ContactUsPageType>(intent.getStringExtra(EXTRA_PAGE_TYPE)!!)
            else
                Enums.ContactUsPageType.MAIL_FORM
    }

    private fun initData() {

    }

    //endregion == initialize =========

    //region == phrases ============

    override fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setTextViewPhrase(layout.sendMessageSuccess, "contactus_message_send_success")
        Utilities.setTextViewPhrase(layout.sendMessageError, "contactus_message_send_error")

        layout.buttons.back.text = Utilities.getLocalPhrase("button_back")
        layout.buttons.next.text = Utilities.getLocalPhrase("button_next")
        layout.buttons.save.text = Utilities.getLocalPhrase("button_save")
        layout.buttons.send.text = Utilities.getLocalPhrase("button_send")

        super.setPhrases()
    }

    //endregion == phrases ============

    //region == fragments ==========

    private fun expiredRegistration() {
        val nowUTC = Calendar.getInstance()
        nowUTC.timeZone = TimeZone.getTimeZone("UTC")

        val isExpired = nowUTC.timeInMillis > userData?.registrationExpiredTime ?: 0L

        // FIX LOCK DRAWER SWIPE ERROR - START
        if (isExpired) {
            layout.drawer.removeView(layout.container)
            layout.drawer.removeView(layout.navigation)
            layout.root.removeView(layout.drawer)

            layout.root.addView(layout.container, 0)
            setContentView(layout.root)
        }
        // FIX LOCK DRAWER SWIPE ERROR - END
    }

    private fun loadFragment() {
        log(Enums.LogType.Debug, TAG, "loadFragment()")
        when (pageType) {
            Enums.ContactUsPageType.MAIL_FORM ->
                supportFragmentManager.beginTransaction()
                    .replace(R.id.formFragment, contactUsMailFormFragment)
                    .commitAllowingStateLoss()

            else -> {}
        }
    }

    //endregion == fragments ==========

    //region == submit =============

    fun submitSend(view: View?) {

        val result = when (pageType) {
            Enums.ContactUsPageType.MAIL_FORM -> contactUsMailFormFragment.submitForm()
            else -> null
        }

        log(Enums.LogType.Debug, TAG, "initData(): result = $result")
        val isValid = if (result?.containsKey("isValid") == true) { result["isValid"] as Boolean } else { false }

        if (isValid) {
            Utilities.hideKeyboard(this@ContactUsActivity)

            runBlocking {
                sendMessage(result)
            }
        }
    }

    //endregion == submit =============

    //region == send message ========

    private fun sendMessage(result: Map<String, Any?>?) {
        viewModel!!.sendMessage(getMapStringValue(result, "messageType").toString(), getMapStringValue(result, "message"))
    }

    private fun sendMessageSuccessCallback() {
        runOnUiThread {
            contactUsMailFormFragment.layout.messageType.selectItemByIndex(0)
            contactUsMailFormFragment.layout.message.text?.clear()
            //layout.sendMessageSuccess.visibility = VISIBLE
            layout.sendMessageError.visibility = GONE

            Utilities.displayActionSnackbar(this@ContactUsActivity, Utilities.getLocalPhrase("contactus_message_send_success"))
        }
    }

    fun sendMessageFailCallback() {
        runOnUiThread {
            layout.sendMessageError.visibility = VISIBLE
            layout.sendMessageSuccess.visibility = GONE
        }
    }

    //endregion == send message ========

    //region == observers ==========

    private inner class LocalFixedParametersObserver : Observer<FixedParametersEntity?> {

        override fun onChanged(fixedParameters: FixedParametersEntity?) {
            log(Enums.LogType.Debug, TAG, "LocalFixedParametersObserver()")
            isLocalFixedParametersLoaded = true

            if (fixedParameters == null) {
                return
            }

            fixedParametersData = FixedParameters.init(fixedParameters)

            if (isLocalFixedParametersLoaded && isLocalUserLoaded) {
                expiredRegistration()
                loadFragment()
            }
        }
    }

    private inner class LocalUserObserver : Observer<UserEntity?> {

        override fun onChanged(user: UserEntity?) {
            log(Enums.LogType.Debug, TAG, "LocalUserObserver(): user = $user")

            isLocalUserLoaded = true
            userData = user

            if (isLocalFixedParametersLoaded && isLocalUserLoaded) {
                expiredRegistration()
                loadFragment()
            }
        }
    }

    private inner class ServerMessageObserver : Observer<Boolean?> {
        override fun onChanged(success: Boolean?) {
            if (success == true) {
                log(Enums.LogType.Debug, TAG, "ServerEmailObserver(): success = $success")
                sendMessageSuccessCallback()
            }
            else {
                log(Enums.LogType.Error, TAG, "ServerEmailObserver(): success = $success")
                sendMessageFailCallback()
            }
        }
    }

    //endregion == observers ==========

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityContactusBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityContactusBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}