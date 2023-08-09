package com.adirahav.diraleashkaa.views

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.R.*
import com.adirahav.diraleashkaa.common.Utilities

class PropertyPercent @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :

    LinearLayout(context, attrs) {

    // views
    var labelView: TextView? = null
    var rollbackView: ImageView? = null
    private var numberPickerContainerView: LinearLayout? = null
    var numberPickerView: com.adirahav.diraleashkaa.numberpickerlibrary.NumberPicker? = null
    var numberPickerAcceptView: ImageView? = null
    var numberPickerCancelView: ImageView? = null

    var numberPickerActualValue: Float? = null

    var inputDefaultValue: Int? = null
    var labelText: String? = null
    var numberPickerExist: Boolean? = null
    var numberPickerDefaultValue: Float? = null
    var numberPickerMinValue: Float? = null
    var numberPickerMaxValue: Float? = null
    var numberPickerStepsSize: Float? = null

    init {
        // attributes
        val attributes = context.obtainStyledAttributes(attrs, styleable.PropertyPercentInput, 0, 0)
        inputDefaultValue = attributes.getInt(styleable.PropertyInput_inputDefaultValue, -1)
        labelText = attributes.getString(styleable.PropertyPercentInput_labelText)
        numberPickerExist = attributes.getBoolean(styleable.PropertyPercentInput_numberPickerExist, false)
        numberPickerDefaultValue = attributes.getFloat(styleable.PropertyPercentInput_numberPickerDefaultValue, -1f)
        numberPickerMinValue = attributes.getFloat(styleable.PropertyPercentInput_numberPickerMinValue, 0f)
        numberPickerMaxValue = attributes.getFloat(styleable.PropertyPercentInput_numberPickerMaxValue, 0f)
        numberPickerStepsSize = attributes.getFloat(styleable.PropertyPercentInput_numberPickerStepsSize, 0f)
        attributes.recycle()

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        // layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(layout.view_property_percent, this, true)

        initViews()

        // label
        setLabel()

        // number picker
        if (numberPickerExist != null && numberPickerExist == true) {
            numberPickerView?.minValue = numberPickerMinValue!!
            numberPickerView?.maxValue = numberPickerMaxValue!!
            numberPickerView?.stepSize = numberPickerStepsSize!!
            numberPickerView?.progress = numberPickerDefaultValue!!
        }

        // rollback
        rollbackView?.visibility = GONE

        rollbackView?.setOnClickListener {
            onRollbackClick()
        }


    }

    fun initViews() {
        labelView = findViewById(R.id.label)
        rollbackView = findViewById(R.id.rollback)
        numberPickerContainerView = findViewById(R.id.numberPickerContainer)
        numberPickerView = findViewById(R.id.numberPicker)
        numberPickerAcceptView = findViewById(R.id.numberPickerAccept)
        numberPickerCancelView = findViewById(R.id.numberPickerCancel)
    }

    // setters
    fun setInputLabelText(text: String) {
        labelText = text
        setLabel()
    }

    // label
    private fun setLabel() {
        labelView?.text = HtmlCompat.fromHtml(String.format(
            labelText.toString(),
            numberPickerActualValue
        ), HtmlCompat.FROM_HTML_MODE_LEGACY)

        if (labelView?.hasOnClickListeners() == false) {
            labelView?.setOnClickListener {
                numberPickerContainerView?.visibility = VISIBLE
            }

            numberPickerCancelView?.setOnClickListener {
                numberPickerContainerView?.visibility = GONE
            }

            numberPickerAcceptView?.setOnClickListener {
                onNumberPickerAccept()
            }
        }
    }

    // number picker
    fun setNumberPickerDefaultValue(value: Float) {
        numberPickerDefaultValue = value
    }

    fun setNumberPickerMinValue(value: Float) {
        numberPickerMinValue = value
        numberPickerView?.minValue = numberPickerMinValue!!
    }

    fun setNumberPickerMaxValue(value: Float) {
        numberPickerMaxValue = value
        numberPickerView?.maxValue = numberPickerMaxValue!!
    }

    fun setNumberPickerStepsSize(value: Float) {
        numberPickerStepsSize = value
        numberPickerView?.stepSize = numberPickerStepsSize!!
    }

    fun setNumberPickerActualValue(value: Float) {
        numberPickerActualValue = value
        numberPickerView?.setProgress(value)
        setLabel()

        rollbackView?.visibility =
            if (numberPickerActualValue == null || numberPickerActualValue == numberPickerDefaultValue)
                GONE
            else
                VISIBLE
    }

    // events
    fun onRollbackClick() {
        rollbackView?.visibility = GONE

        if (numberPickerDefaultValue == -1f) {
            labelView?.text = Utilities.getDecimalNumber(inputDefaultValue)
        }
        else {
            labelView?.text = HtmlCompat.fromHtml(String.format(
                labelText.toString(),
                numberPickerDefaultValue
            ), HtmlCompat.FROM_HTML_MODE_LEGACY)

            numberPickerView?.progress = numberPickerDefaultValue!!

            labelView?.text = Utilities.getDecimalNumber(inputDefaultValue)

            onNumberPickerAccept()
        }
    }

    fun onNumberPickerAccept() {
        numberPickerContainerView?.visibility = GONE
        numberPickerActualValue = numberPickerView?.progress

        labelView?.text = HtmlCompat.fromHtml(String.format(
            labelText.toString(),
            numberPickerActualValue
        ), HtmlCompat.FROM_HTML_MODE_LEGACY)

        rollbackView?.visibility =
            if (numberPickerActualValue != numberPickerDefaultValue)
                VISIBLE
            else
                GONE
    }
}