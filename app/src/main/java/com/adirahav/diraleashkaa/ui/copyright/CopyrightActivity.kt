package com.adirahav.diraleashkaa.ui.copyright

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import com.adirahav.diraleashkaa.ui.base.BaseActivity
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.log
import com.adirahav.diraleashkaa.databinding.ActivityCopyrightBinding

class CopyrightActivity : BaseActivity<CopyrightViewModel?, ActivityCopyrightBinding>() {

    //region == companion ==========

    companion object {
        private const val TAG = "CopyrightActivity"
        const val EXTRA_PAGE_TYPE = "EXTRA_PAGE_TYPE"

        fun start(context: Context) {
            val intent = Intent(context, CopyrightActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    //endregion == companion ==========

    //region == variables ==========

    // layout
    internal lateinit var layout: ActivityCopyrightBinding

    //endregion == variables ==========

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityCopyrightBinding.inflate(layoutInflater)
        setContentView(layout.root)
    }

    public override fun onResume() {
        super.onResume()
        log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)

        setCustomActionBar(layout.drawer)
        setDrawer(layout.drawer, layout.menu)

        initData()

        // title text
        titleText?.text = Utilities.getRoomString("actionbar_title_copyright")
    }

    private fun initData() {
        layout.content.text =
            HtmlCompat.fromHtml(
                Utilities.getRoomString("copyright_text"), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun createViewModel(): CopyrightViewModel {
        val factory = CopyrightViewModelFactory()
        return ViewModelProvider(this, factory)[CopyrightViewModel::class.java]
    }

    //endregion == lifecycle methods ==

    //region == base abstract ======

    override fun attachBinding(list: MutableList<ActivityCopyrightBinding>, layoutInflater: LayoutInflater) {
        list.add(ActivityCopyrightBinding.inflate(layoutInflater))
    }

    //endregion == base abstract ======
}