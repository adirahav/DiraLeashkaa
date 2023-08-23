package com.adirahav.diraleashkaa.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.NumberWithComma
import com.adirahav.diraleashkaa.common.Utilities.getDecimalNumber
import com.adirahav.diraleashkaa.common.Utilities.toNumber
import androidx.recyclerview.widget.GridLayoutManager
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.skydoves.powerspinner.PowerSpinnerView


class PropertyInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :

    LinearLayout(context, attrs) {

    private val TAG = "PropertyInput"

    // activity
    var _activity: PropertyActivity? = null

    // type
    var type: InputType? = null

    // views
    var labelView: TextView? = null
    var iconView: ImageView? = null
    var warningView: ImageView? = null
    var rollbackView: ImageView? = null
    var inputView: EditText? = null
    var spinnerView: PowerSpinnerView? = null
    private var numberPickerContainerView: LinearLayout? = null
    var numberPickerView: com.adirahav.diraleashkaa.numberpickerlibrary.NumberPicker? = null
    var numberPickerAcceptView: ImageView? = null
    var numberPickerCancelView: ImageView? = null

    var numberPickerActualValue: Float? = null

    var inputDefaultValue: Int? = null
    var labelText: String? = null
    var labelTextWithoutValue: String? = null
    var numberPickerExist: Boolean? = null
    var numberPickerDefaultValue: Float? = null
    var numberPickerMinValue: Float? = null
    var numberPickerMaxValue: Float? = null
    var numberPickerStepsSize: Float? = null
    var warningExist: Boolean? = null
    var warningShow: Boolean? = null
    var warningTooltipText: String? = null

    init {
        // activity
        _activity = ((context as? PropertyActivity))

        // attributes
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PropertyInput, 0, 0)
        val inputType = attributes.getString(R.styleable.PropertyInput_inputType)
        inputDefaultValue = attributes.getInt(R.styleable.PropertyInput_inputDefaultValue, -1)
        labelText = attributes.getString(R.styleable.PropertyInput_labelText)
        labelTextWithoutValue = attributes.getString(R.styleable.PropertyInput_labelTextWithoutValue)
        numberPickerExist = attributes.getBoolean(R.styleable.PropertyInput_numberPickerExist, false)
        numberPickerDefaultValue = attributes.getFloat(R.styleable.PropertyInput_numberPickerDefaultValue, -1f)
        numberPickerMinValue = attributes.getFloat(R.styleable.PropertyInput_numberPickerMinValue, 0f)
        numberPickerMaxValue = attributes.getFloat(R.styleable.PropertyInput_numberPickerMaxValue, 0f)
        numberPickerStepsSize = attributes.getFloat(R.styleable.PropertyInput_numberPickerStepsSize, 0f)
        val dropDownOptions = attributes.getString(R.styleable.PropertyInput_dropDownOptions)
        val iconSrc = attributes.getDrawable(R.styleable.PropertyInput_iconSrc)
        val iconExist = attributes.getBoolean(R.styleable.PropertyInput_iconExist, false)
        val iconTooltipText = attributes.getString(R.styleable.PropertyInput_iconTooltipText)
        warningExist = attributes.getBoolean(R.styleable.PropertyInput_warningExist, false)
        warningShow = attributes.getBoolean(R.styleable.PropertyInput_warningShow, false)
        warningTooltipText = attributes.getString(R.styleable.PropertyInput_warningTooltipText)
        attributes.recycle()

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        // layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_property_input, this, true)

        initViews()

        // type
        type = InputType.valueOf((inputType ?: "default").uppercase())

        // label
        setLabel()

        // number picker
        if (numberPickerExist != null && numberPickerExist == true) {
            numberPickerView?.minValue = numberPickerMinValue!!
            numberPickerView?.maxValue = numberPickerMaxValue!!
            numberPickerView?.stepSize = numberPickerStepsSize!!
            numberPickerView?.progress = numberPickerDefaultValue!!
        }

        // icon
        if (iconExist) {
            iconView?.visibility = VISIBLE
            iconView?.setImageDrawable(iconSrc)

            iconView?.setOnClickListener {
                openTooltipDialog(iconTooltipText)
            }
        }
        else {
            iconView?.visibility = GONE
        }

        // warning
        if (warningExist == true) {
            warningView?.visibility = if (warningShow == true) VISIBLE else GONE

            warningView?.setOnClickListener {
                openTooltipDialog(warningTooltipText)
            }
        }
        else {
            warningView?.visibility = GONE
        }

        // input
        inputView?.setText(getDecimalNumber(inputDefaultValue))

        // rollback
        rollbackView?.visibility = GONE

        when (type) {
            InputType.DEFAULT -> {
                setDefaultInput(inputView)

                inputView?.addTextChangedListener(NumberWithComma(inputView))

                inputView?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        setDefaultInput(inputView)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            InputType.AUTO_FILL -> {

                setAutoFillInput(inputView, inputDefaultValue!!, rollbackView)

                inputView?.addTextChangedListener(NumberWithComma(inputView))

                inputView?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        setAutoFillInput(inputView, inputDefaultValue!!, rollbackView)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            InputType.CALC -> {
                setCalcInput(inputView, false)
            }

            InputType.CALC_BOLD -> {
                setCalcInput(inputView, true)
            }

            InputType.CALC_EDITABLE -> {

                setCalcEditableInput(inputView, inputDefaultValue!!, rollbackView)

                inputView?.addTextChangedListener(NumberWithComma(inputView))

                inputView?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        setCalcEditableInput(inputView, inputDefaultValue!!, rollbackView)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            InputType.CALC_TOTAL -> {
                setCalcTotalInput(inputView)
            }

            InputType.DROP_DOWN -> {
                inputView?.visibility = GONE
                spinnerView?.visibility = VISIBLE

                var spinnerItems: List<IconSpinnerItem?>? = null

                when (dropDownOptions) {
                    "cities" -> {
                        spinnerItems = _activity?.fixedParametersData?.citiesArray?.map { IconSpinnerItem(text = it.value.toString()) }
                    }
                    "apartment_type" -> {
                        spinnerItems = _activity?.fixedParametersData?.apartmentTypesArray?.map { IconSpinnerItem(text = it.value.toString()) }
                    }
                    "mortgage_period" -> {
                        spinnerItems = _activity?.fixedParametersData?.mortgagePeriodArray?.map { IconSpinnerItem(text = it.value.toString()) }
                    }
                }

                spinnerItems?.forEachIndexed { i, _ -> spinnerItems[i] }

                val spinnerAdapter = IconSpinnerAdapter(spinnerView!!)
                spinnerAdapter.setItems(spinnerItems!! as List<IconSpinnerItem>)
                spinnerView!!.setSpinnerAdapter(spinnerAdapter)
                spinnerView!!.getSpinnerRecyclerView().layoutManager = GridLayoutManager(context, 1)

                spinnerView?.setOnSpinnerItemSelectedListener<IconSpinnerItem?> {
                        _, _, _, _ -> setDropDown(spinnerView)
                }
            }

            InputType.STRING -> {
                setStringInput(inputView)

                inputView?.textDirection = View.TEXT_DIRECTION_RTL

                inputView?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        setStringInput(inputView)
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }

            else -> {}
        }

        rollbackView?.setOnClickListener {
            onRollbackClick()
        }


    }

    enum class InputType {
        DEFAULT,
        CALC,
        CALC_BOLD,
        CALC_EDITABLE,
        CALC_TOTAL,
        AUTO_FILL,
        DROP_DOWN,
        STRING
    }

    fun initViews() {
        labelView = findViewById(R.id.label)
        iconView = findViewById(R.id.icon)
        warningView = findViewById(R.id.warning)
        inputView = findViewById(R.id.input)
        rollbackView = findViewById(R.id.rollback)
        spinnerView = findViewById(R.id.spinner)
        //powerSpinnerView = findViewById(R.id.powerSpinner)
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

    fun setInputLabelWithoutValueText(text: String) {
        labelTextWithoutValue = text
        setLabel()
    }

    fun setInputWarningText(text: String) {
        warningTooltipText = text
        //setWarning()
    }

    // label
    private fun setLabel() {
        if (numberPickerDefaultValue == -1f) {
            labelView?.text =
                HtmlCompat.fromHtml(labelText.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        else {
            if (numberPickerActualValue != null) {
                labelView?.text = HtmlCompat.fromHtml(String.format(
                    labelText.toString(),
                    numberPickerActualValue
                ), HtmlCompat.FROM_HTML_MODE_LEGACY)
            }
            else {
                labelView?.text = HtmlCompat.fromHtml(
                    labelTextWithoutValue.toString(),
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

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
    }

    // inputs
    fun setInputDefaultValue(value: Int) {
        inputDefaultValue = value
    }

    private fun setDefaultInput(view: EditText?) {
        if (view?.text?.isEmpty() == true) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundEmpty))
        }
        else {
            view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
        }
    }

    private fun setAutoFillInput(view: EditText?, defaultValue: Int, rollback: ImageView?) {
        if (view?.text?.isEmpty() == true) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundEmpty))
            rollback?.visibility = VISIBLE
        }
        else {
            if (view?.text?.toNumber() == defaultValue) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundAutoFill))
                rollback?.visibility = GONE
            }
            else {
                view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
                rollback?.visibility = VISIBLE
            }
        }
    }

    private fun setCalcInput(view: EditText?, isBold: Boolean) {
        view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundDisable))
        view?.isEnabled = false

        if (isBold) {
            view?.setTypeface(null, Typeface.BOLD)
        }
    }

    fun setCalcEditableInput(view: EditText?, defaultValue: Int, rollback: ImageView?) {
        if (view?.text?.isEmpty() == true) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundEmpty))
            rollback?.visibility =  if (defaultValue == -1)
                                        GONE
                                    else
                                        VISIBLE
        }
        else {
            if (view?.text?.toNumber() == defaultValue) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
                rollback?.visibility = GONE
            }
            else {
                view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
                rollback?.visibility = VISIBLE
            }
        }
    }

    private fun setCalcTotalInput(view: EditText?) {
        view?.background = ContextCompat.getDrawable(context, R.drawable.background_field_total)
        view?.isEnabled = false
        view?.setTypeface(null, Typeface.BOLD)
    }

    private fun setStringInput(view: EditText?) {
        if (view?.text?.isEmpty() == true) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundEmpty))
        }
        else {
            view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
        }

        view?.inputType = android.text.InputType.TYPE_CLASS_TEXT
    }

    fun setWarning(view: EditText?, hasWarning: Boolean) {
        if (hasWarning) {

            (context as? Activity)?.runOnUiThread {
                view?.setTextColor(ContextCompat.getColor(context, R.color.textWarning))
            }

            if (warningExist == true) {
                warningShow = true
                (context as? Activity)?.runOnUiThread {
                    warningView?.visibility = VISIBLE
                }
            }

        }
        else {
            (context as? Activity)?.runOnUiThread {
                view?.setTextColor(ContextCompat.getColor(context, R.color.form_text))
            }

            if (warningExist == true) {
                warningShow = false
                (context as? Activity)?.runOnUiThread {
                    warningView?.visibility = GONE
                }
            }
        }
    }

    fun setWarning(view: PowerSpinnerView?, hasWarning: Boolean) {
        if (hasWarning) {

            (context as? Activity)?.runOnUiThread {
                view?.setTextColor(ContextCompat.getColor(context, R.color.textWarning))
            }

            if (warningExist == true) {
                warningShow = true
                (context as? Activity)?.runOnUiThread {
                    warningView?.visibility = VISIBLE
                }
            }

        }
        else {
            (context as? Activity)?.runOnUiThread {
                view?.setTextColor(ContextCompat.getColor(context, R.color.formText))
            }

            if (warningExist == true) {
                warningShow = false
                (context as? Activity)?.runOnUiThread {
                    warningView?.visibility = GONE
                }
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
            if (numberPickerActualValue == numberPickerDefaultValue)
                GONE
            else
                VISIBLE
    }

    fun setNumberPickerWithoutValue() {
        numberPickerActualValue = null
        setLabel()

        rollbackView?.visibility = VISIBLE
    }

    // events
    fun onRollbackClick() {
        rollbackView?.visibility = GONE

        if (numberPickerDefaultValue == -1f) {
            inputView?.setText(getDecimalNumber(inputDefaultValue))
        }
        else {
            labelView?.text = HtmlCompat.fromHtml(String.format(
                labelText.toString(),
                numberPickerDefaultValue
            ), HtmlCompat.FROM_HTML_MODE_LEGACY)

            numberPickerView?.progress = numberPickerDefaultValue!!

            if (type!!.equals(InputType.CALC_EDITABLE)) {
                inputView?.setText(getDecimalNumber(inputDefaultValue))
            }

            onNumberPickerAccept()
        }

        inputView?.contentDescription = labelText

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

    // drop down
    fun setDropDown(view: PowerSpinnerView?) {
        if (view?.selectedIndex == 0) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackgroundEmpty))
        }
        else {
            view?.setBackgroundColor(ContextCompat.getColor(context, R.color.formBackground))
        }
    }

    // tooltip
    private fun openTooltipDialog(text: String?) {
        val dialog = Dialog(context)

        // layout
        dialog.setContentView(R.layout.dialog_tooltip)

        // close
        val dialogClose= dialog.findViewById<ImageView>(R.id.close)
        dialogClose?.setOnClickListener {
            closeTooltipDialog(dialog)
        }

        // text
        val dialogText = dialog.findViewById<TextView>(R.id.text)
        dialogText.text = HtmlCompat.fromHtml(text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        // ok
        val dialogOk= dialog.findViewById<Button>(R.id.ok)
        dialogOk?.setOnClickListener {
            closeTooltipDialog(dialog)
        }

        // open dialog
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        val windowAlDl: Window = dialog.window!!

        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        windowAlDl.attributes = layoutParams

        dialog.show();
    }

    private fun closeTooltipDialog(dialog: Dialog) {
        dialog.hide()
    }
}