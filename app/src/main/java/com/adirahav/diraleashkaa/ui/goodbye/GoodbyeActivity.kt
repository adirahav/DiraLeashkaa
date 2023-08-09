package com.adirahav.diraleashkaa.ui.goodbye

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.await
import com.adirahav.diraleashkaa.data.DataManager
import com.adirahav.diraleashkaa.databinding.ActivityGoodbyeBinding
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import com.adirahav.diraleashkaa.ui.home.HomeViewModel
import com.adirahav.diraleashkaa.ui.home.HomeViewModelFactory
import java.util.*

class GoodbyeActivity : BaseActivity<GoodbyeViewModel?, ActivityGoodbyeBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "GoodbyeActivity"
        private const val AWAIT_SECONDS = 3

        fun start(context: Context) {
            val intent = Intent(context, GoodbyeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // activity
    val activity = this@GoodbyeActivity

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // start time
    var startTime: Date? = null

    // layout
    internal lateinit var layout: ActivityGoodbyeBinding

    //endregion == variables ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = ActivityGoodbyeBinding.inflate(layoutInflater)
        setContentView(layout.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    public override fun onResume() {
        super.onResume()

        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        setCustomActionBar()

        initEvents()
    }

    private fun initEvents() {
        startTime = Date()
        await(Date(), AWAIT_SECONDS, ::responseAfterAwait)
    }

    override fun createViewModel(): GoodbyeViewModel {
        val factory = GoodbyeViewModelFactory()
        return ViewModelProvider(this, factory)[GoodbyeViewModel::class.java]
    }

    private fun responseAfterAwait() {
        this.finishAffinity()
    }

    override fun onBackPressed() {
        this.finishAffinity()
    }

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityGoodbyeBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityGoodbyeBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}