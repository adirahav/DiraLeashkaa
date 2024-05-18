package com.adirahav.diraleashkaa.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.FixedParameters
import com.adirahav.diraleashkaa.common.NumberWithComma
import com.adirahav.diraleashkaa.common.Utilities.getDecimalNumber
import com.adirahav.diraleashkaa.common.Utilities.toNumber
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.ui.calculators.CalculatorActivity
import com.adirahav.diraleashkaa.ui.property.PropertyActivity
import com.airbnb.paris.extensions.style
import com.google.gson.annotations.SerializedName
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PropertyInput @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :

    LinearLayout(context, attrs) {

    private val TAG = "PropertyInput"

    // activity
    var _activityProperty: PropertyActivity? = null
    var _activityCalculator: CalculatorActivity? = null
    internal var isPropertyActivity: Boolean? = null

    // fixed parameters data
    var fixedParametersData: FixedParameters? = null

    // type
    var type: InputType? = null

    // views
    var labelView: TextView? = null
    var iconView: ImageView? = null
    var warningView: ImageView? = null
    var rollbackView: ImageView? = null
    var inputView: EditText? = null
    var spinnerView: PowerSpinnerView? = null
    var searchableSpinnerView: TextView? = null
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
        isPropertyActivity = context.javaClass.simpleName.equals("PropertyActivity")

        // activity
        if (isPropertyActivity!!)
            _activityProperty = ((context as? PropertyActivity))
        else
            _activityCalculator = ((context as? CalculatorActivity))

        // fixed parameters data
        fixedParametersData =
                if (isPropertyActivity!!)
                    _activityProperty?.fixedParametersData
                else
                    _activityCalculator?.fixedParametersData

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
                searchableSpinnerView?.visibility = GONE

                var spinnerItems: List<IconSpinnerItem?>? = null

                when (dropDownOptions) {
                    "apartment_type" -> {
                        spinnerItems = fixedParametersData?.apartmentTypesArray?.map { IconSpinnerItem(text = it.value.toString()) }
                    }
                    "mortgage_period" -> {
                        spinnerItems = fixedParametersData?.mortgagePeriodArray?.map { IconSpinnerItem(text = it.value.toString()) }
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

            InputType.SEARCHABLE_DROP_DOWN -> {
                inputView?.visibility = GONE
                spinnerView?.visibility = GONE
                searchableSpinnerView?.visibility = VISIBLE

                var searchableSpinnerSuggestionItems: List<String?>? = null
                var searchableSpinnerItems: List<String?>? = null

                when (dropDownOptions) {
                    "cities" -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            val localPropertiesList = DatabaseClient.getInstance(context)?.appDatabase?.propertyDao()?.getAll()

                            searchableSpinnerSuggestionItems = fixedParametersData?.citiesArray?.filter { cityEntity ->
                                localPropertiesList!!.any { propertyEntity ->
                                    propertyEntity.city == cityEntity.key
                                }
                            }?.mapNotNull { it.value }

                            searchableSpinnerItems = fixedParametersData?.citiesArray?.filter {
                                it.key != "choose" && it.value !in searchableSpinnerSuggestionItems!!
                            }?.map { it.value.toString() }
                        }
                    }
                }

                searchableSpinnerView?.setOnClickListener(OnClickListener {
                    val dialog = Dialog(context)
                    dialog.setContentView(R.layout.view_searchable_spinner_dialog)
                    dialog.getWindow()?.setLayout(650, 800)
                    dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val editText: EditText = dialog.findViewById(R.id.edit_text)
                    val listView: ListView = dialog.findViewById(R.id.list_view)

                    val itemsData: MutableList<ItemDataClass> = ArrayList()

                    for (i in 0 until searchableSpinnerSuggestionItems!!.size) {
                        itemsData.add(itemsData.size, ItemDataClass(searchableSpinnerSuggestionItems!![i] .toString(), R.layout.item_searchable_spinner_suggestion_dialog))
                    }

                    for (i in 0 until searchableSpinnerItems!!.size) {
                        itemsData.add(itemsData.size, ItemDataClass(searchableSpinnerItems!![i].toString(), R.layout.item_searchable_spinner_dialog))
                    }

                    val mergedAdapter = SuggestionArrayAdapter(
                            if (isPropertyActivity!!)
                                _activityProperty!!.applicationContext
                            else
                                _activityCalculator!!.applicationContext
                            ,
                            itemsData)

                    listView.adapter = mergedAdapter

                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            mergedAdapter.filter.filter(s)
                        }

                        override fun afterTextChanged(s: Editable) {}
                    })

                    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        searchableSpinnerView?.setText((view as TextView).text)
                        dialog.dismiss()
                    }
                })
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
        SEARCHABLE_DROP_DOWN,
        STRING
    }

    fun initViews() {
        labelView = findViewById(R.id.label)
        iconView = findViewById(R.id.icon)
        warningView = findViewById(R.id.warning)
        inputView = findViewById(R.id.input)
        rollbackView = findViewById(R.id.rollback)
        spinnerView = findViewById(R.id.spinner)
        searchableSpinnerView = findViewById(R.id.searchableSpinner)
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

data class ItemDataClass(
        @SerializedName("name") val name: String,
        @SerializedName("resource") val resource: Int,
)
class SuggestionArrayAdapter(
        context: Context,
        private val allItems: List<ItemDataClass>,
) : ArrayAdapter<ItemDataClass>(context, 0, ArrayList(allItems)) {

    private var items: List<ItemDataClass> = allItems
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)

        val itemView: View = convertView ?: inflater.inflate(items[position].resource, parent, false)
        val textView: TextView = itemView.findViewById<TextView>(R.id.text)
        textView.text = items[position].name

        textView.setTypeface(null,
                if (items[position].resource == R.layout.item_searchable_spinner_suggestion_dialog)
                    Typeface.BOLD
                else
                    Typeface.NORMAL
        )

        itemView.style(items[position].resource)

        return itemView
    }

    override fun getCount(): Int {
        return items.size
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults) {
                items = filterResults.values as List<ItemDataClass>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()

                val filterResults = Filter.FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    allItems
                else
                    allItems.filter {
                        it.name.contains(queryString)
                    }
                return filterResults
            }
        }
    }
}

