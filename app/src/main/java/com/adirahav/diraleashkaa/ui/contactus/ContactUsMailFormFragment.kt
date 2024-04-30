package com.adirahav.diraleashkaa.ui.contactus

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.databinding.FragmentContactusMailFormBinding
import com.jakewharton.rxbinding.widget.RxTextView
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import java.util.concurrent.TimeUnit


class ContactUsMailFormFragment : Fragment() {

    companion object {
        private const val TAG = "ContactUsMailFormFragment"
    }

    // activity
    var _activity: ContactUsActivity? = null

    // layout
    internal lateinit var layout: FragmentContactusMailFormBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        layout = FragmentContactusMailFormBinding.inflate(layoutInflater)

        initGlobal()
        initData()
        initEvents()

        return layout.root
    }

    fun initGlobal() {
        // activity
        _activity = activity as ContactUsActivity

        // hide keyboard
        Utilities.hideKeyboard(requireContext())

        // strings
        setPhrases()
    }

    fun initData() {
        // message type
        var spinnerItems: List<IconSpinnerItem?>? = null

        spinnerItems = _activity?.fixedParametersData?.contactUsObject?.messageTypes?.map { IconSpinnerItem(text = it.value.toString()) }


        spinnerItems?.forEachIndexed { i, _ -> spinnerItems[i] }

        val spinnerAdapter = IconSpinnerAdapter(layout.messageType)
        spinnerAdapter.setItems(spinnerItems!!)
        layout.messageType.setSpinnerAdapter(spinnerAdapter)
        layout.messageType.getSpinnerRecyclerView().layoutManager = GridLayoutManager(context, 1)

        if (spinnerItems.isNotEmpty()) {
            layout.messageType.selectItemByIndex(0)
        }

        /*messageType?.setOnSpinnerItemSelectedListener<IconSpinnerItem?> {
            oldIndex, oldItem, newIndex, newItem ->
            messageType?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.formBackground))
        }*/

        Utilities.setButtonDisable(_activity?.layout?.buttons?.send)
    }

    fun initEvents() {

        // message type
        layout.messageType.setOnSpinnerItemSelectedListener<IconSpinnerItem> {
            _, _, newIndex, _ ->
            if (layout.messageType.text.isNotEmpty() && layout.message.text.isNotEmpty()) {
                Utilities.setButtonEnable(_activity?.layout?.buttons?.send)
            }
            else {
                Utilities.setButtonDisable(_activity?.layout?.buttons?.send)
            }
        }


        // message
        layout?.message?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (layout.messageType.text.isNotEmpty() && layout.message.text.isNotEmpty()) {
                    Utilities.setButtonEnable(_activity?.layout?.buttons?.send)
                }
                else {
                    Utilities.setButtonDisable(_activity?.layout?.buttons?.send)
                }
            }
        })

        layout.message.requestFocus()
    }

    //region == strings ============

    private fun setPhrases() {
        Utilities.log(Enums.LogType.Debug, TAG, "setPhrases()")

        Utilities.setLabelViewString(layout.messageTypeTitle, "contactus_message_type_hint")
        Utilities.setTextViewString(layout.messageTypeError, "contactus_message_type_error")
        Utilities.setLabelViewString(layout.messageTitle, "contactus_message_hint")
        Utilities.setTextViewString(layout.messageError, "contactus_message_error")
    }

    //endregion == strings ============

    fun submitForm(): Map<String, Any?> {
        var isValid = true

        // message type
        if (layout.messageType.selectedIndex == 0) {
            layout.messageTypeError.visibility = View.VISIBLE

            if (isValid) {
                layout.messageType.requestFocus()
                isValid = false
            }
        }
        else {
            layout.messageTypeError.visibility = View.GONE
        }

        // message
        if (layout.message.text.toString().trim().isEmpty()) {
            layout.messageError.visibility = View.VISIBLE

            if (isValid) {
                layout.message.requestFocus()
                isValid = false
            }
        }
        else {
            layout.messageError.visibility = View.GONE
        }

        // results
        val map = mutableMapOf<String, Any?>()
        val entities = mutableMapOf<String, Any?>()

        if (isValid) {
            entities["messageType"] =
                if (layout.messageType.selectedIndex == 0)
                    "undefined"
                else
                    _activity?.fixedParametersData?.contactUsObject?.messageTypes?.get(layout.messageType.selectedIndex)?.value

            entities["message"] = layout.message.text.toString().trim()

            Utilities.setButtonDisable(_activity?.layout?.buttons?.send)
        }

        map["isValid"] = isValid
        map["entities"] = entities

        return map
    }
}