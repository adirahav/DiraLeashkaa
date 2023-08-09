package com.adirahav.diraleashkaa.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.lang.Exception

class PhoneNumber(var editText: EditText?) : TextWatcher {
    private var isBackspaceAndLastHyphen = false
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        isBackspaceAndLastHyphen = (after == 0) && (s.endsWith("-"))
    }
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }
    override fun afterTextChanged(s: Editable) {
        try {
            editText?.removeTextChangedListener(this)
            val value = editText?.text.toString()

            if (value != "") {
                if (isBackspaceAndLastHyphen) {
                    editText?.setText(value.substring(0, value.length-1))
                }

                if (!value.startsWith("0")) {
                    editText?.setText("0" + value)
                }

                val str = editText?.text.toString().replace("-".toRegex(), "")
                if (value != "") {
                    editText?.setText(
                        if (str.length >= 3)
                            "${str.substring(0, 3)}-${str.substring(3)}"
                        else
                            str
                    )
                }
                editText?.setSelection(editText?.text.toString().length)
            }
            editText?.addTextChangedListener(this)
            return
        } catch (ex: Exception) {
            ex.printStackTrace()
            editText?.addTextChangedListener(this)
        }
    }
}