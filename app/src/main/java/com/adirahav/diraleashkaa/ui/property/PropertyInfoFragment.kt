package com.adirahav.diraleashkaa.ui.property

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Utilities.toFormatNumber
import com.adirahav.diraleashkaa.common.Utilities.toNumber
import com.jakewharton.rxbinding.widget.RxTextView
import com.skydoves.powerspinner.IconSpinnerItem
import com.skydoves.powerspinner.PowerSpinnerView
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import com.adirahav.diraleashkaa.common.Utilities.getImageUri
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.dpToPx
import com.adirahav.diraleashkaa.common.Utilities.percentToMultipleFraction
import com.adirahav.diraleashkaa.data.network.entities.SpinnerEntity
import com.adirahav.diraleashkaa.databinding.FragmentPropertyInfoBinding
import com.adirahav.diraleashkaa.ui.signup.SignUpFinancialInfoFragment
import com.adirahav.diraleashkaa.views.LabelWithIcon
import com.adirahav.diraleashkaa.views.PropertyInput

class PropertyInfoFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "PropertyInfoFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val PICTURE_SIZE_RATIO = 0.5F
    }

    //endregion == companion ==========

    //region == variables ==========

    // pictures
    var pictureWidth: Int? = null
    var pictureHeight: Int? = null

    //lateinit var currentPhotoPath: String

    // activity
    var _activity: PropertyActivity? = null

    // context
    private var _context: Context? = null

    // form
    val form = mutableMapOf<String, Any?>()

    // layout
    private var layout: FragmentPropertyInfoBinding? = null

    // changed field
    private var whichFieldChangedByUser: String? = null
    internal var isFieldFieldChangedByUser: Boolean? = null

    //endregion == variables ==========

    //region == views variables ====

    // city
    private var citySearchableSpinnerView: TextView? = null

    // city else
    var cityElseInputView: EditText? = null

    // address
    var addressInputView: EditText? = null

    // apartment type
    var apartmentTypeSpinnerView: PowerSpinnerView? = null

    // price
    var propertyPriceInputView: EditText? = null

    // equity
    private var equityInputView: EditText? = null

    // equity cleaning expenses
    var equityCleaningExpensesInputView: EditText? = null

    // mortgage required
    var mortgageRequiredInputView: EditText? = null

    // incomes
    var incomesInputView: EditText? = null

    // commitments
    var commitmentsInputView: EditText? = null

    // disposable income
    var disposableIncomeInputView: EditText? = null

    // possible monthly payment
    var possibleMonthlyRepaymentInputView: EditText? = null
    private var possibleMonthlyRepaymentLabelView: TextView? = null
    private var possibleMonthlyRepaymentNumberPickerAcceptView: ImageView? = null
    var possibleMonthlyRepaymentRollbackView: ImageView? = null

    // max percent of financing
    private var maxPercentOfFinancingInputView: EditText? = null

    // actual percent of financing
    var actualPercentOfFinancingInputView: EditText? = null

    // transfer tax
    private var transferTaxInputView: EditText? = null

    // lawyer
    var lawyerInputView: EditText? = null
    var lawyerLabelView: TextView? = null
    private var lawyerNumberPickerAcceptView: ImageView? = null
    var lawyerRollbackView: ImageView? = null

    // real estate agent
    var realEstateAgentInputView: EditText? = null
    var realEstateAgentLabelView: TextView? = null
    private var realEstateAgentNumberPickerAcceptView: ImageView? = null
    var realEstateAgentRollbackView: ImageView? = null

    // broker mortgage
    var brokerMortgageInputView: EditText? = null

    // repairing
    var repairingInputView: EditText? = null

    // incidentals
    var incidentalsTotalInputView: EditText? = null

    // rent
    var rentInputView: EditText? = null
    var rentLabelView: TextView? = null
    private var rentNumberPickerAcceptView: ImageView? = null
    var rentRollbackView: ImageView? = null

    // life insurance
    private var lifeInsuranceInputView: EditText? = null

    // structure insurance
    var structureInsuranceInputView: EditText? = null

    // rent cleaning expenses
    var rentCleaningExpensesInputView: EditText? = null

    // mortgage period
    var mortgagePeriodSpinnerView: PowerSpinnerView? = null

    // mortgage monthly payment
    var mortgageMonthlyRepaymentInputView: EditText? = null

    // mortgage monthly yield
    var mortgageMonthlyYieldInputView: EditText? = null

    //endregion == views variables ====

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreateView()", showToast = false)

        // activity
        _activity = activity as PropertyActivity

        // orientation
        _activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        layout = FragmentPropertyInfoBinding.inflate(layoutInflater)


        initGlobal()
        initViews()
        initData()
        initEvents()

        return layout?.root
    }

    override fun onResume() {
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)
        super.onResume()
        //initData()
    }

    override fun onAttach(context: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "onAttach()", showToast = false)
        super.onAttach(context)
    }

    override fun onDetach() {
        Utilities.log(Enums.LogType.Debug, TAG, "onDetach()", showToast = false)
        super.onDetach()
    }

    //endregion == lifecycle methods ==

    //region == initialize =========
    private fun initGlobal() {
        Utilities.log(Enums.LogType.Debug, TAG, "initGlobal()", showToast = false)

        // context
        _context = context

        // interests
        _activity?.layout?.interestsContainer?.visibility = GONE

        // actions menu
        _activity?.layout?.actionsMenuContainerBottom?.visibility = VISIBLE
        _activity?.layout?.actionsMenuContainerSide?.visibility = GONE

        setActionsMenuEnable(false)

        // strings
        setRoomStrings()
    }

    private fun initViews() {
        Utilities.log(Enums.LogType.Debug, TAG, "initViews()", showToast = false)

        // camera
        val showCamera = _activity?.fixedParametersData?.pictureArray?.find { it.key == "allow_upload" }?.value?.toBoolean() ?: true
        layout?.camera?.visibility = if (showCamera) VISIBLE else GONE

        // picture
        pictureWidth = Resources.getSystem().displayMetrics.widthPixels
        pictureHeight = pictureWidth!!.times(PICTURE_SIZE_RATIO).toInt()

        layout?.picture?.layoutParams?.height = pictureHeight
        layout?.pictureGradientBackground?.layoutParams?.height = pictureHeight
        layout?.formContainer?.layoutParams = (layout?.formContainer?.layoutParams as ViewGroup.MarginLayoutParams).apply {
            setMargins(0, pictureHeight!!, 0, 0)
        }
        layout?.cityIcon?.layoutParams = (layout?.cityIcon?.layoutParams as ViewGroup.MarginLayoutParams).apply {
            setMargins(0, pictureHeight!!.minus(dpToPx(requireContext(), 35F)), 0, 0)
        }

        // city
        citySearchableSpinnerView = layout?.city?.findViewById(R.id.searchableSpinner)

        // city else
        cityElseInputView = layout?.cityElse?.findViewById(R.id.input)

        // address
        addressInputView = layout?.address?.findViewById(R.id.input)

        // apartment type
        apartmentTypeSpinnerView = layout?.apartmentType?.findViewById(R.id.spinner)

        // price
        propertyPriceInputView = layout?.propertyPrice?.findViewById(R.id.input)

        // equity
        equityInputView = layout?.equity?.findViewById(R.id.input)

        // equity cleaning expenses
        equityCleaningExpensesInputView = layout?.equityCleaningExpenses?.findViewById(R.id.input)

        // mortgage required
        mortgageRequiredInputView = layout?.mortgageRequired?.findViewById(R.id.input)

        // incomes
        incomesInputView = layout?.incomes?.findViewById(R.id.input)

        // commitments
        commitmentsInputView = layout?.commitments?.findViewById(R.id.input)

        // disposable income
        disposableIncomeInputView = layout?.disposableIncome?.findViewById(R.id.input)

        // possible monthly payment
        possibleMonthlyRepaymentInputView = layout?.possibleMonthlyRepayment?.findViewById(R.id.input)
        possibleMonthlyRepaymentLabelView = layout?.possibleMonthlyRepayment?.findViewById(R.id.label)
        possibleMonthlyRepaymentNumberPickerAcceptView = layout?.possibleMonthlyRepayment?.findViewById(R.id.numberPickerAccept)
        possibleMonthlyRepaymentRollbackView = layout?.possibleMonthlyRepayment?.findViewById(R.id.rollback)

        // max percent of financing
        maxPercentOfFinancingInputView = layout?.maxPercentOfFinancing?.findViewById(R.id.input)

        // actual percent of financing
        actualPercentOfFinancingInputView = layout?.actualPercentOfFinancing?.findViewById(R.id.input)

        // transfer tax
        transferTaxInputView = layout?.transferTax?.findViewById(R.id.input)

        // lawyer
        lawyerInputView = layout?.lawyer?.findViewById(R.id.input)
        lawyerLabelView = layout?.lawyer?.findViewById(R.id.label)
        lawyerNumberPickerAcceptView = layout?.lawyer?.findViewById(R.id.numberPickerAccept)
        lawyerRollbackView = layout?.lawyer?.findViewById(R.id.rollback)

        // real estate agent
        realEstateAgentInputView = layout?.realEstateAgent?.findViewById(R.id.input)
        realEstateAgentLabelView = layout?.realEstateAgent?.findViewById(R.id.label)
        realEstateAgentNumberPickerAcceptView = layout?.realEstateAgent?.findViewById(R.id.numberPickerAccept)
        realEstateAgentRollbackView = layout?.realEstateAgent?.findViewById(R.id.rollback)

        // broker mortgage
        brokerMortgageInputView = layout?.brokerMortgage?.findViewById(R.id.input)

        // incidentals
        incidentalsTotalInputView = layout?.incidentalsTotal?.findViewById(R.id.input)

        // repairing
        repairingInputView = layout?.repairing?.findViewById(R.id.input)

        // rent
        rentInputView = layout?.rent?.findViewById(R.id.input)
        rentLabelView = layout?.rent?.findViewById(R.id.label)
        rentNumberPickerAcceptView = layout?.rent?.findViewById(R.id.numberPickerAccept)
        rentRollbackView = layout?.rent?.findViewById(R.id.rollback)

        // life insurance
        lifeInsuranceInputView = layout?.lifeInsurance?.findViewById(R.id.input)

        // structure insurance
        structureInsuranceInputView = layout?.structureInsurance?.findViewById(R.id.input)

        // rent cleaning expenses
        rentCleaningExpensesInputView = layout?.rentCleaningExpenses?.findViewById(R.id.input)

        // mortgage period
        mortgagePeriodSpinnerView = layout?.mortgagePeriod?.findViewById(R.id.spinner)

        // mortgage monthly payment
        mortgageMonthlyRepaymentInputView = layout?.mortgageMonthlyRepayment?.findViewById(R.id.input)

        // mortgage monthly yield
        mortgageMonthlyYieldInputView = layout?.mortgageMonthlyYield?.findViewById(R.id.input)

        // yield forecast
        //yieldForecastContainerView = findViewById(R.id.yieldForecastContainer)

    }

    fun initData() {

        Utilities.log(Enums.LogType.Debug, TAG, "initData(): roomID = ${_activity?.propertyData?.roomID} ; uuid = ${_activity?.propertyData?.uuid} ; propertyData = ${_activity?.propertyData}")

        isFieldFieldChangedByUser = false

        // pictures
        if (_activity?.propertyData?.pictures != null) {
            updatePicture()
        }
        else {
            layout?.picture?.background = ContextCompat.getDrawable(_context!!, R.drawable.property)
        }

        // city
        if (!_activity?.argPropertyCity.isNullOrEmpty() && _activity?.roomPID == 0L && _activity?.isCityUpdatedFromArgs != true) {
            val cityObject = _activity?.fixedParametersData?.citiesArray?.find { it.key == _activity?.argPropertyCity }

            _activity?.isCityUpdatedFromArgs = true

            onCityChanged(cityObject?.key)
        }
        else {
            updateCity()
        }

        // city else
        updateCityElse()

        // address
        updateAddress()

        // apartment type
        updateApartmentType()

        // price
        updatePrice()

        // equity
        updateEquity()

        // equity cleaning expenses
        updateEquityCleaningExpenses()

        // mortgage required
        updateMortgageRequired()

        // --------------------------------------

        // incomes
        updateIncomes()

        // commitments
        updateCommitments()

        // disposable income
        updateDisposableIncome()

        // possible monthly payment
        updatePossibleMonthlyRepayment()

        // --------------------------------------

        // max percent of financing
        updateMaxPercentOfFinancing()

        // actual percent of financing
        updateActualPercentOfFinancing()

        // --------------------------------------

        // transfer tax
        updateTransferTax()

        // lawyer
        updateLayer()

        // real estate agent
        updateRealEstateAgent()

        // broker mortgage
        updateBrokerMortgage()

        // repairing
        updateRepairing()

        // incidentals total
        updateIncidentalsTotal()

        // --------------------------------------

        // rent
        updateRent()

        // life insurance
        updateLifeInsurance()

        // structure insurance
        updateStructureInsurance()

        // rent cleaning expenses
        updateRentCleaningExpenses()

        // --------------------------------------

        // Show mortgage prepayment
        takeMortgageVisibility()

        // mortgage period
        updateMortgagePeriod()

        // mortgage monthly repayment
        updateMortgageMonthlyRepayment()

        // mortgage monthly yield
        updateMortgageMonthlyYield()

        // --------------------------------------

        // actions menu
        activateActionsMenuIfNeeded()

        isFieldFieldChangedByUser = true
    }

    private fun initEvents() {
        Utilities.log(Enums.LogType.Debug, TAG, "initEvents()", showToast = false)

        // camera
        layout?.camera?.setOnClickListener {
            dispatchTakePictureIntent()
        }

        // city
        RxTextView.textChanges(citySearchableSpinnerView!!)
                .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
                .subscribe {
                    if (whichFieldChangedByUser?.equals(Const.CITY) == true) {
                        var cityObject =
                                if (_activity?.fixedParametersData?.citiesArray?.find { it.value == citySearchableSpinnerView?.text.toString() } == null)
                                    _activity?.fixedParametersData?.citiesArray?.find { it.key == "choose" }
                                else
                                    _activity?.fixedParametersData?.citiesArray?.find { it.value == citySearchableSpinnerView?.text.toString() }

                        onCityChanged(cityObject?.key)
                        whichFieldChangedByUser = null
                    }
                }

        citySearchableSpinnerView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    var cityObject = _activity?.fixedParametersData?.citiesArray?.find { it.value == citySearchableSpinnerView?.text.toString() }

                    if(!cityObject?.key.equals(_activity?.propertyData?.city)) {
                        whichFieldChangedByUser = Const.CITY
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // city else
        RxTextView.textChanges(cityElseInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (layout?.cityElse?.visibility == VISIBLE && whichFieldChangedByUser?.equals(Const.CITY_ELSE) == true) {
                    onCityElseChanged()
                    whichFieldChangedByUser = null
                }
            }

        cityElseInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.CITY_ELSE
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // address
        RxTextView.textChanges(addressInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.ADDRESS) == true) {
                    onAddressChanged()
                    whichFieldChangedByUser = null
                }
            }

        addressInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.ADDRESS
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // apartment type
        apartmentTypeSpinnerView?.setOnSpinnerItemSelectedListener<IconSpinnerItem> {
                _, _, newIndex, _ ->
            if (whichFieldChangedByUser?.equals("apartmentType") == true) {
                onApartmentTypeChanged(newIndex)
                whichFieldChangedByUser = null
            }
        }

        apartmentTypeSpinnerView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "apartmentType"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // price
        RxTextView.textChanges(propertyPriceInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("propertyPrice") == true) {
                    onPriceChanged()
                    whichFieldChangedByUser = null
                }
            }

        propertyPriceInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "propertyPrice"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // equity
        RxTextView.textChanges(equityInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.EQUITY) == true) {
                    onEquityChanged()
                    whichFieldChangedByUser = null
                }
            }

        equityInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.EQUITY
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // equity cleaning Expenses
        RxTextView.textChanges(equityCleaningExpensesInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("equityCleaningExpenses") == true) {
                    onEquityCleaningExpensesChanged()
                    whichFieldChangedByUser = null
                }
            }

        equityCleaningExpensesInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "equityCleaningExpenses"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // mortgage required
        RxTextView.textChanges(mortgageRequiredInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("mortgageRequired") == true) {
                    onMortgageRequiredChanged()
                    whichFieldChangedByUser = null
                }
            }

        mortgageRequiredInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "mortgageRequired"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // incomes
        RxTextView.textChanges(incomesInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.INCOMES) == true) {
                    onIncomesChanged()
                    whichFieldChangedByUser = null
                }
            }

        incomesInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.INCOMES
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // commitments
        RxTextView.textChanges(commitmentsInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.COMMITMENTS) == true) {
                    onCommitmentsChanged()
                    whichFieldChangedByUser = null
                }
            }

        commitmentsInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.COMMITMENTS
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // disposable income
        RxTextView.textChanges(disposableIncomeInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("disposableIncome") == true) {
                    onDisposableIncomeChanged()
                    whichFieldChangedByUser = null
                }
            }

        disposableIncomeInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "disposableIncome"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // possible monthly payment
        RxTextView.textChanges(possibleMonthlyRepaymentInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("possibleMonthlyRepayment") == true) {
                    onPossibleMonthlyRepaymentChanged()
                    whichFieldChangedByUser = null
                }
            }

        layout?.possibleMonthlyRepayment?.numberPickerAcceptView?.setOnClickListener {
            layout?.possibleMonthlyRepayment?.numberPickerActualValue = layout?.possibleMonthlyRepayment?.numberPickerView?.progress
            _activity?.propertyData?.possibleMonthlyRepaymentPercent = layout?.possibleMonthlyRepayment?.numberPickerActualValue
            _activity?.insertUpdateProperty(Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT, _activity?.propertyData?.possibleMonthlyRepaymentPercent)

            layout?.possibleMonthlyRepayment?.onNumberPickerAccept()
        }

        possibleMonthlyRepaymentRollbackView?.setOnClickListener {
            layout?.possibleMonthlyRepayment?.onRollbackClick()
            updatePossibleMonthlyRepayment()
            onPossibleMonthlyRepaymentChanged()

            _activity?.propertyData?.possibleMonthlyRepaymentPercent = ((_activity?.fixedParametersData?.propertyInputsArray?.find { it.name == Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT })?.default ?: 0F)

            _activity?.insertUpdateProperty(Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT, _activity?.propertyData?.possibleMonthlyRepaymentPercent)
        }

        possibleMonthlyRepaymentInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "possibleMonthlyRepayment"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // max percent of financing
        RxTextView.textChanges(maxPercentOfFinancingInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("maxPercentOfFinancing") == true) {
                    onMaxPercentOfFinancingChanged()
                    whichFieldChangedByUser = null
                }
            }

        maxPercentOfFinancingInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "maxPercentOfFinancing"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // actual percent of financing
        RxTextView.textChanges(actualPercentOfFinancingInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("actualPercentOfFinancing") == true) {
                    onActualPercentOfFinancingChanged()
                    whichFieldChangedByUser = null
                }
            }

        actualPercentOfFinancingInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "actualPercentOfFinancing"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // transfer tax
        RxTextView.textChanges(transferTaxInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("transferTax") == true) {
                    onTransferTaxChanged()
                    whichFieldChangedByUser = null
                }
            }

        transferTaxInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "transferTax"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // lawyer
        RxTextView.textChanges(lawyerInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (lawyerInputView?.isFocused == true && whichFieldChangedByUser?.equals(Const.LAWYER_CUSTOM_VALUE) == true) {
                    onLawyerChanged()
                    whichFieldChangedByUser = null
                }
            }

        lawyerNumberPickerAcceptView?.setOnClickListener {
            layout?.lawyer?.numberPickerActualValue = layout?.lawyer?.numberPickerView?.progress
            _activity?.propertyData?.calcLawyerPercent = layout?.lawyer?.numberPickerActualValue
            _activity?.insertUpdateProperty(Const.LAWYER_PERCENT, _activity?.propertyData?.calcLawyerPercent)
            layout?.lawyer?.onNumberPickerAccept()
        }

        lawyerRollbackView?.setOnClickListener {
            layout?.lawyer?.onRollbackClick()

            _activity?.propertyData?.calcLawyerPercent = null
            _activity?.propertyData?.calcLawyer = null
            lawyerInputView?.clearFocus()

            _activity?.insertUpdateProperty(Const.LAWYER_PERCENT, "")
        }

        lawyerInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.LAWYER_CUSTOM_VALUE
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // real estate agent
        RxTextView.textChanges(realEstateAgentInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (realEstateAgentInputView?.isFocused == true && whichFieldChangedByUser?.equals(Const.REAL_ESTATE_AGENT_CUSTOM_VALUE) == true) {
                    onRealEstateAgentChanged()
                    whichFieldChangedByUser = null
                }
            }

        realEstateAgentNumberPickerAcceptView?.setOnClickListener {
            layout?.realEstateAgent?.numberPickerActualValue = layout?.realEstateAgent?.numberPickerView?.progress
            _activity?.propertyData?.calcRealEstateAgentPercent = layout?.realEstateAgent?.numberPickerActualValue
            _activity?.insertUpdateProperty(Const.REAL_ESTATE_AGENT_PERCENT, _activity?.propertyData?.calcRealEstateAgentPercent)
            layout?.realEstateAgent?.onNumberPickerAccept()
        }

        realEstateAgentRollbackView?.setOnClickListener {
            layout?.realEstateAgent?.onRollbackClick()

            _activity?.propertyData?.calcRealEstateAgentPercent = null
            _activity?.propertyData?.calcRealEstateAgent = null
            realEstateAgentInputView?.clearFocus()

            _activity?.insertUpdateProperty(Const.REAL_ESTATE_AGENT_PERCENT, "")
        }

        realEstateAgentInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.REAL_ESTATE_AGENT_CUSTOM_VALUE
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // broker mortgage
        RxTextView.textChanges(brokerMortgageInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.BROKER_MORTGAGE) == true) {
                    onBrokerMortgageChanged()
                    whichFieldChangedByUser = null
                }
            }

        brokerMortgageInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.BROKER_MORTGAGE
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // repairing
        RxTextView.textChanges(repairingInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals(Const.REPAIRING) == true) {
                    onRepairingChanged()
                    whichFieldChangedByUser = null
                }
            }

        repairingInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = Const.REPAIRING
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // incidentals
        RxTextView.textChanges(incidentalsTotalInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("incidentalsTotal") == true) {
                    onIncidentalsTotalChanged()
                    whichFieldChangedByUser = null
                }
            }

        incidentalsTotalInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "incidentalsTotal"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // rent
        RxTextView.textChanges(rentInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (rentInputView?.isFocused == true && whichFieldChangedByUser?.equals("rent") == true) {
                    onRentChanged()
                    whichFieldChangedByUser = null
                }
            }

        rentNumberPickerAcceptView?.setOnClickListener {
            layout?.rent?.numberPickerActualValue = layout?.rent?.numberPickerView?.progress
            _activity?.propertyData?.rentPercent = layout?.rent?.numberPickerActualValue
            _activity?.insertUpdateProperty(Const.RENT_PERCENT, _activity?.propertyData?.rentPercent)
            layout?.rent?.onNumberPickerAccept()
        }

        rentRollbackView?.setOnClickListener {
            layout?.rent?.onRollbackClick()

            _activity?.propertyData?.rentPercent = null
            _activity?.propertyData?.calcRent = null
            rentInputView?.clearFocus()

            _activity?.insertUpdateProperty(Const.RENT_PERCENT, "")
        }

        rentInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "rent"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // life insurance
        RxTextView.textChanges(lifeInsuranceInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("lifeInsurance") == true) {
                    onLifeInsuranceChanged()
                    whichFieldChangedByUser = null
                }
            }

        lifeInsuranceInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "lifeInsurance"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // structure insurance
        RxTextView.textChanges(structureInsuranceInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("structureInsurance") == true) {
                    onStructureInsuranceChanged()
                    whichFieldChangedByUser = null
                }
            }

        structureInsuranceInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "structureInsurance"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // rent cleaning expenses
        RxTextView.textChanges(rentCleaningExpensesInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("rentCleaningExpenses") == true) {
                    onRentCleaningExpensesChanged()
                    whichFieldChangedByUser = null
                }
            }

        rentCleaningExpensesInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "rentCleaningExpenses"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // mortgage period
        mortgagePeriodSpinnerView?.setOnSpinnerItemSelectedListener<IconSpinnerItem> {
                _, _, newIndex, _ ->
            if (whichFieldChangedByUser?.equals("mortgagePeriod") == true) {
                onMortgagePeriodChanged(newIndex)
                whichFieldChangedByUser = null
            }
        }

        mortgagePeriodSpinnerView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "mortgagePeriod"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // mortgage monthly repayment
        RxTextView.textChanges(mortgageMonthlyRepaymentInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("mortgageMonthlyRepayment") == true) {
                    onMortgageMonthlyRepaymentChanged()
                    whichFieldChangedByUser = null
                }
            }

        mortgageMonthlyRepaymentInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "mortgageMonthlyRepayment"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

        // mortgage monthly yield
        RxTextView.textChanges(mortgageMonthlyYieldInputView!!)
            .debounce(Configuration.TYPING_RESPONSE_DELAY, TimeUnit.MILLISECONDS)
            .subscribe {
                if (whichFieldChangedByUser?.equals("mortgageMonthlyYield") == true) {
                    onMortgageMonthlyYieldChanged()
                    whichFieldChangedByUser = null
                }
            }

        /*layout.mortgageMonthlyYield.viewTreeObserver?.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() { updateMortgageMonthlyYield() }
        })*/

        mortgageMonthlyYieldInputView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isFieldFieldChangedByUser == true) {
                    whichFieldChangedByUser = "mortgageMonthlyYield"
                }
            }

            override fun afterTextChanged(editable: Editable) { }
        })

    }

    //endregion == initialize =========

    //region == strings ============

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setPropertyInputString(layout?.city, "property_city_label")
        Utilities.setPropertyInputString(layout?.cityElse, "property_city_else_label")
        Utilities.setPropertyInputString(layout?.address, "property_address_label")
        Utilities.setPropertyInputString(layout?.apartmentType, "property_apartment_type_label")
        Utilities.setPropertyInputString(layout?.propertyPrice, "property_price_label")
        Utilities.setPropertyInputString(layout?.equity, "property_equity_label")
        Utilities.setPropertyInputString(layout?.equityCleaningExpenses, "property_equity_cleaning_expenses_label", null, "property_equity_cleaning_expenses_warning")
        Utilities.setPropertyInputString(layout?.mortgageRequired, "property_mortgage_required_label", null, "property_mortgage_required_warning")
        Utilities.setPropertyInputString(layout?.incomes, "property_incomes_label")
        Utilities.setPropertyInputString(layout?.commitments, "property_commitments_label")
        Utilities.setPropertyInputString(layout?.disposableIncome, "property_disposable_income_label")
        Utilities.setPropertyInputString(layout?.possibleMonthlyRepayment, "property_possible_monthly_payment_label")
        Utilities.setPropertyInputString(layout?.maxPercentOfFinancing, "property_max_percent_of_financing_label")
        Utilities.setPropertyInputString(layout?.actualPercentOfFinancing, "property_actual_percent_of_financing_label", null, "property_actual_percent_of_financing_warning")
        Utilities.setTextViewString(layout?.incidentalsTitle, "property_incidentals_title")
        Utilities.setPropertyInputString(layout?.transferTax, "property_transfer_tax_label")
        Utilities.setInputViewString(layout?.lawyer, "property_lawyer_label", "property_lawyer_label_without_value")
        Utilities.setPropertyInputString(layout?.realEstateAgent, "property_real_estate_agent_label", "property_real_estate_agent_label_without_value")
        Utilities.setPropertyInputString(layout?.brokerMortgage, "property_broker_mortgage_label")
        Utilities.setPropertyInputString(layout?.repairing, "property_repairing_label")
        Utilities.setPropertyInputString(layout?.incidentalsTotal, "property_incidentals_total_label")
        Utilities.setInputViewString(layout?.rent, "property_rent_label", "property_rent_label_without_value")
        Utilities.setPropertyInputString(layout?.lifeInsurance, "property_life_insurance_label")
        Utilities.setPropertyInputString(layout?.structureInsurance, "property_structure_insurance_label")
        Utilities.setPropertyInputString(layout?.rentCleaningExpenses, "property_rent_cleaning_expenses_label")
        Utilities.setTextViewString(layout?.propertyMortgagePrepaymentLabel, "property_mortgage_repayment_title")
        Utilities.setInputViewString(layout?.mortgagePeriod, "property_mortgage_period_label", null, "property_mortgage_period_warning")
        Utilities.setPropertyInputString(layout?.mortgageMonthlyRepayment, "property_mortgage_monthly_repayment_label", null, "property_mortgage_monthly_repayment_warning")
        Utilities.setPropertyInputString(layout?.mortgageMonthlyYield, "property_mortgage_monthly_yield_label", null, "property_mortgage_monthly_yield_warning")

    }

    //endregion == strings ============

    //region == change events ======
    private fun onCityChanged(key: String?) {
        Utilities.log(Enums.LogType.Debug, TAG, "onCityChanged()")

        val city =  if (key == "choose")
                        null
                    else
                        key

        activity?.runOnUiThread {
            _activity?.propertyData?.city = city
            _activity?.insertUpdateProperty(Const.CITY, _activity?.propertyData?.city)
            updateCity()
        }
    }

    private fun onCityElseChanged() {
        Utilities.log(Enums.LogType.Debug, TAG, "onCityElseChanged()", showToast = false)

        activity?.runOnUiThread {
            _activity?.propertyData?.cityElse = cityElseInputView?.text.toString()
            _activity?.insertUpdateProperty(Const.CITY_ELSE, _activity?.propertyData?.cityElse)
        }
    }

    private fun onAddressChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.address = addressInputView?.text.toString()
            _activity?.insertUpdateProperty(Const.ADDRESS, _activity?.propertyData?.address)
        }
    }

    private fun onApartmentTypeChanged(position: Int) {
        val apartmentType =  if (position == 0)
            null
        else
            _activity?.fixedParametersData?.apartmentTypesArray?.get(position)

        _activity?.propertyData?.apartmentType = apartmentType?.key
        _activity?.insertUpdateProperty(Const.APARTMENT_TYPE, _activity?.propertyData?.apartmentType)
    }

    private fun onPriceChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.price =
                if (propertyPriceInputView?.text != null)
                    propertyPriceInputView?.text?.toNumber()
                else
                    null

            _activity?.insertUpdateProperty(Const.PRICE, _activity?.propertyData?.price)
        }
    }

    private fun onEquityChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcEquity =
                if (equityInputView?.text != null)
                    equityInputView?.text?.toNumber()
                else
                    null

            if (!equityInputView?.text.isNullOrEmpty()) {
                _activity?.insertUpdateProperty(Const.EQUITY, _activity?.propertyData?.calcEquity)
            }
        }
    }

    private fun onEquityCleaningExpensesChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcEquityCleaningExpenses =
                if (equityCleaningExpensesInputView?.text != null)
                    equityCleaningExpensesInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onMortgageRequiredChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcMortgageRequired =
                if (mortgageRequiredInputView?.text != null)
                    mortgageRequiredInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onIncomesChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcIncomes =
                if (incomesInputView?.text != null)
                    incomesInputView?.text?.toNumber()
                else
                    null

            if (!incomesInputView?.text.isNullOrEmpty()) {
                _activity?.insertUpdateProperty(Const.INCOMES, _activity?.propertyData?.calcIncomes)
            }
        }
    }

    private fun onCommitmentsChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcCommitments =
                if (commitmentsInputView?.text != null)
                    commitmentsInputView?.text?.toNumber()
                else
                    null

            if (!commitmentsInputView?.text.isNullOrEmpty()) {
                _activity?.insertUpdateProperty(Const.COMMITMENTS, _activity?.propertyData?.calcCommitments)
            }
        }
    }

    private fun onDisposableIncomeChanged() {

    }

    private fun onPossibleMonthlyRepaymentChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcPossibleMonthlyRepayment =
                if (possibleMonthlyRepaymentInputView?.text != null)
                    possibleMonthlyRepaymentInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onMaxPercentOfFinancingChanged() {

    }

    private fun onActualPercentOfFinancingChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcActualPercentOfFinancing =
                if (actualPercentOfFinancingInputView?.text != null)
                    actualPercentOfFinancingInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onTransferTaxChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcTransferTax =
                if (transferTaxInputView?.text != null)
                    transferTaxInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onLawyerChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcLawyer = lawyerInputView?.text?.toNumber()
            _activity?.propertyData?.calcLawyerPercent = null

            if (_activity?.propertyData?.calcLawyer == null) {
                _activity?.setNumberPickerAttributes(layout?.lawyer, Const.LAWYER_PERCENT, _activity?.propertyData?.calcLawyerPercent)
            }
            else {
                layout?.lawyer?.setNumberPickerWithoutValue()
            }

            _activity?.insertUpdateProperty(Const.LAWYER_CUSTOM_VALUE, _activity?.propertyData?.calcLawyer)
        }
    }

    private fun onRealEstateAgentChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcRealEstateAgent = realEstateAgentInputView?.text?.toNumber()
            _activity?.propertyData?.calcRealEstateAgentPercent = null

            if (_activity?.propertyData?.calcRealEstateAgent == null) {
                _activity?.setNumberPickerAttributes(layout?.realEstateAgent, Const.REAL_ESTATE_AGENT_PERCENT, _activity?.propertyData?.calcRealEstateAgentPercent)
            }
            else {
                layout?.realEstateAgent?.setNumberPickerWithoutValue()
            }

            _activity?.insertUpdateProperty(Const.REAL_ESTATE_AGENT_CUSTOM_VALUE, _activity?.propertyData?.calcRealEstateAgent)
        }
    }

    private fun onBrokerMortgageChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcBrokerMortgage =
                if (brokerMortgageInputView?.text != null)
                    brokerMortgageInputView?.text?.toNumber()
                else
                    null

            _activity?.insertUpdateProperty(Const.BROKER_MORTGAGE, _activity?.propertyData?.calcBrokerMortgage)
        }
    }

    private fun onRepairingChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcRepairing =
                if (repairingInputView?.text != null)
                    repairingInputView?.text?.toNumber()
                else
                    null

            _activity?.insertUpdateProperty(Const.REPAIRING, _activity?.propertyData?.calcRepairing)
        }
    }

    private fun onIncidentalsTotalChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcIncidentalsTotal =
                if (incidentalsTotalInputView?.text != null)
                    incidentalsTotalInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onRentChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcRent = rentInputView?.text?.toNumber()
            _activity?.propertyData?.rentPercent = null

            if (_activity?.propertyData?.calcRent == null) {
                _activity?.setNumberPickerAttributes(layout?.rent, Const.RENT_PERCENT, _activity?.propertyData?.rentPercent)
            }
            else {
                layout?.rent?.setNumberPickerWithoutValue()
            }

            _activity?.insertUpdateProperty(Const.RENT_CUSTOM_VALUE, _activity?.propertyData?.calcRent)
        }
    }

    private fun onLifeInsuranceChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcLifeInsurance =
                if (lifeInsuranceInputView?.text != null)
                    lifeInsuranceInputView?.text?.toNumber()
                else
                    null

            _activity?.insertUpdateProperty(Const.LIFE_INSURANCE, _activity?.propertyData?.calcLifeInsurance)
        }
    }

    private fun onStructureInsuranceChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcStructureInsurance =
                if (structureInsuranceInputView?.text != null)
                    structureInsuranceInputView?.text?.toNumber()
                else
                    null

            _activity?.insertUpdateProperty(Const.STRUCTURE_INSURANCE, _activity?.propertyData?.calcStructureInsurance)
        }
    }

    private fun onRentCleaningExpensesChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcRentCleaningExpenses =
                if (rentCleaningExpensesInputView?.text != null)
                    rentCleaningExpensesInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onMortgagePeriodChanged(position: Int) {
        Utilities.log(Enums.LogType.Debug, TAG, "onMortgagePeriodChanged()")

        val period =  _activity?.fixedParametersData?.mortgagePeriodArray?.get(position)

        activity?.runOnUiThread {
            _activity?.propertyData?.calcMortgagePeriod = period?.key?.toInt()
            _activity?.insertUpdateProperty(Const.MORTGAGE_PERIOD, _activity?.propertyData?.calcMortgagePeriod)
        }
    }

    private fun onMortgageMonthlyRepaymentChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcMortgageMonthlyRepayment =
                if (mortgageMonthlyRepaymentInputView?.text != null)
                    mortgageMonthlyRepaymentInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onMortgageMonthlyYieldChanged() {
        activity?.runOnUiThread {
            _activity?.propertyData?.calcMortgageMonthlyYield =
                if (mortgageMonthlyYieldInputView?.text != null)
                    mortgageMonthlyYieldInputView?.text?.toNumber()
                else
                    null
        }
    }

    //endregion == change events ======

    //region == update data ========
    private fun updatePicture() {
        Utilities.log(Enums.LogType.Debug, TAG, "updatePicture()")

        Utilities.setPropertyPicture(
            layout?.picture,
            _activity?.propertyData?.pictures!!,
            PICTURE_SIZE_RATIO,
            pictureWidth!!,
            pictureHeight!!,
            R.drawable.property
        )

        activateActionsMenuIfNeeded()
    }

    private fun updateCity() {
        Utilities.log(Enums.LogType.Debug, TAG, "updateCity()")

        activity?.runOnUiThread {
            val drawableID = Utilities.findDrawableByName("icon_city_" + _activity?.propertyData?.city)
            if (drawableID > 0) {
                Picasso.with(AppApplication.context)
                    .load(drawableID)
                    .error(R.drawable.icon_city_missing)
                    .into(layout?.cityIcon)
            }
            else {
                Picasso.with(AppApplication.context)
                    .load(R.drawable.icon_city_missing)
                    .error(R.drawable.icon_city_missing)
                    .into(layout?.cityIcon)
            }

            val cityObject = _activity?.fixedParametersData?.citiesArray?.find { it.key == _activity?.propertyData?.city ?: "choose" }

            activity?.runOnUiThread {
                citySearchableSpinnerView?.text = cityObject?.value
            }
        }
    }

    private fun updateCityElse() {

        activity?.runOnUiThread {
            val cityObject = _activity?.fixedParametersData?.citiesArray?.find { it.key == _activity?.propertyData?.city }

            if (_activity?.propertyData?.city.equals("else")) {
                cityElseInputView?.setText(_activity?.propertyData?.cityElse)

                layout?.cityElse?.visibility = VISIBLE

                if (cityElseInputView?.hasFocus() == false) {
                    cityElseInputView?.setText(_activity?.propertyData?.cityElse ?: "")
                }
                else {
                    cityElseInputView?.setSelection(cityElseInputView?.text?.length ?: 0)
                }
            }
            else {
                layout?.cityElse?.visibility = GONE
            }
        }
    }

    private fun updateAddress() {
        activity?.runOnUiThread {
            if (addressInputView?.hasFocus() == false) {
                addressInputView?.setText(_activity?.propertyData?.address ?: "")
            }
            else {
                addressInputView?.setSelection(addressInputView?.text?.length ?: 0)
            }
        }
    }

    private fun updateApartmentType() {
        val apartmentTypeObject = _activity?.fixedParametersData?.apartmentTypesArray?.find { it.key == _activity?.propertyData?.apartmentType ?: "choose" }
        val apartmentTypeIndex = _activity?.fixedParametersData?.apartmentTypesArray?.indexOf(apartmentTypeObject)

        activity?.runOnUiThread {
            if (apartmentTypeSpinnerView?.selectedIndex != (apartmentTypeIndex ?: 0)) {
                apartmentTypeSpinnerView?.selectItemByIndex(apartmentTypeIndex ?: 0)
            }
        }

        layout?.apartmentType?.setDropDown(apartmentTypeSpinnerView)
    }

    private fun updatePrice() {
        activity?.runOnUiThread {
            propertyPriceInputView?.setText(_activity?.propertyData?.price.toFormatNumber())
        }
    }

    private fun updateEquity() {
        activity?.runOnUiThread {
            if (_activity?.roomPID == 0L) {
                _activity?.propertyData?.calcEquity = _activity?.userData?.equity
            }

            layout?.equity?.setInputDefaultValue(_activity?.userData?.equity ?: 0)

            activity?.runOnUiThread {
                equityInputView?.setText(_activity?.propertyData?.calcEquity.toFormatNumber())
            }
        }
    }

    private fun updateEquityCleaningExpenses() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcEquityCleaningExpenses == null) {
                equityCleaningExpensesInputView?.text?.clear()
            }
            else {
                equityCleaningExpensesInputView?.setText(
                    Utilities.getDecimalNumber(_activity?.propertyData?.calcEquityCleaningExpenses)
                )

                var hasWarning = _activity?.propertyData?.calcEquityCleaningExpenses!! < 0

                layout?.equityCleaningExpenses?.setWarning(equityCleaningExpensesInputView, hasWarning)
            }


        }
    }

    private fun updateMortgageRequired() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcMortgageRequired == null) {
                mortgageRequiredInputView?.text?.clear()
            }
            else {
                mortgageRequiredInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData!!.calcMortgageRequired))

                var hasWarning = _activity?.userData?.canTakeMortgage == false && _activity?.propertyData!!.calcMortgageRequired!! > 0

                layout?.mortgageRequired?.setWarning(mortgageRequiredInputView, hasWarning)
            }
        }
    }

    // --------------------------------------

    private fun updateIncomes() {
        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcIncomes = _activity?.userData?.incomes
        }

        layout?.incomes?.setInputDefaultValue(_activity?.userData?.incomes ?: 0)

        activity?.runOnUiThread {
            incomesInputView?.setText(_activity?.propertyData?.calcIncomes.toFormatNumber())
        }
    }

    private fun updateCommitments() {
        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcCommitments = _activity?.userData?.commitments
        }

        layout?.commitments?.setInputDefaultValue(_activity?.userData?.commitments ?: 0)
        activity?.runOnUiThread {
            commitmentsInputView?.setText(_activity?.propertyData?.calcCommitments.toFormatNumber())
        }
    }

    private fun updateDisposableIncome() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcDisposableIncome == null) {
                disposableIncomeInputView?.text?.clear()
            }
            else {
                disposableIncomeInputView?.setText(_activity?.propertyData?.calcDisposableIncome.toFormatNumber())
            }
        }
    }

    private fun updatePossibleMonthlyRepayment() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcPossibleMonthlyRepayment == null) {
                possibleMonthlyRepaymentInputView?.text?.clear()
            }
            else {
                possibleMonthlyRepaymentInputView?.setText(_activity?.propertyData?.calcPossibleMonthlyRepayment.toFormatNumber())
                layout?.possibleMonthlyRepayment?.setInputDefaultValue(_activity?.propertyData?.calcPossibleMonthlyRepayment ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.possibleMonthlyRepayment, "possible_monthly_repayment_percent", _activity?.propertyData?.calcPossibleMonthlyRepaymentPercent)
        }
    }

    // --------------------------------------

    private fun updateMaxPercentOfFinancing() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcMaxPercentOfFinancing == null) {
                maxPercentOfFinancingInputView?.text?.clear()
            }
            else {
                maxPercentOfFinancingInputView?.setText(_activity?.propertyData?.calcMaxPercentOfFinancing.toString() + "%")
            }
        }
    }

    private fun updateActualPercentOfFinancing() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcActualPercentOfFinancing == null) {
                actualPercentOfFinancingInputView?.text?.clear()
            }
            else {
                actualPercentOfFinancingInputView?.setText(_activity?.propertyData?.calcActualPercentOfFinancing.toString() + "%")
            }

            var hasWarning =
                if ( _activity?.propertyData?.calcActualPercentOfFinancing == null || _activity?.propertyData?.calcMaxPercentOfFinancing == null)
                    false
                else
                    _activity!!.propertyData!!.calcActualPercentOfFinancing!! > _activity!!.propertyData!!.calcMaxPercentOfFinancing!!

            layout?.actualPercentOfFinancing?.setWarning(actualPercentOfFinancingInputView, hasWarning)
        }
    }

    // --------------------------------------

    private fun updateTransferTax() {
        activity?.runOnUiThread {
            if ( _activity?.propertyData?.calcTransferTax == null) {
                transferTaxInputView?.text?.clear()
            }
            else {
                transferTaxInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData?.calcTransferTax))
            }
        }
    }

    private fun updateLayer() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcLawyer == null) {
                lawyerInputView?.text?.clear()
            }
            else {
                lawyerInputView?.setText(_activity?.propertyData?.calcLawyer.toFormatNumber())
                layout?.lawyer?.setInputDefaultValue(_activity?.propertyData?.defaultLawyer ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.lawyer, Const.LAWYER_PERCENT, _activity?.propertyData?.calcLawyerPercent)
        }
    }

    private fun updateRealEstateAgent() {
        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcRealEstateAgent == null) {
                realEstateAgentInputView?.text?.clear()
            }
            else {
                realEstateAgentInputView?.setText(_activity?.propertyData?.calcRealEstateAgent.toFormatNumber())
                layout?.realEstateAgent?.setInputDefaultValue(_activity?.propertyData?.calcRealEstateAgent ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.realEstateAgent, Const.REAL_ESTATE_AGENT_PERCENT, _activity?.propertyData?.calcRealEstateAgentPercent)
        }
    }

    private fun updateBrokerMortgage() {
        if (_activity?.roomPID == 0L) {
            val brokerMortgageDefaultExcludingVAT = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.BROKER_MORTGAGE })?.value ?: 0)
            val brokerMortgageDefaultIncludingVAT = brokerMortgageDefaultExcludingVAT.times(_activity?.fixedParametersData?.vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()

            _activity?.propertyData?.calcBrokerMortgage = brokerMortgageDefaultIncludingVAT
        }

        activity?.runOnUiThread {
            brokerMortgageInputView?.setText(_activity?.propertyData?.calcBrokerMortgage.toFormatNumber())
        }
    }

    private fun updateRepairing() {
        if (_activity?.roomPID == 0L) {
            val repairingDefaultExcludingVAT = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.REPAIRING })?.value ?: 0)
            val repairingDefaultIncludingVAT = repairingDefaultExcludingVAT.times(_activity?.fixedParametersData?.vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()

            _activity?.propertyData?.calcRepairing = repairingDefaultIncludingVAT
        }

        activity?.runOnUiThread {
            repairingInputView?.setText(_activity?.propertyData?.calcRepairing.toFormatNumber())
        }
    }

    private fun updateIncidentalsTotal() {
        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcIncidentalsTotal = _activity?.propertyData?.calcBrokerMortgage!!.plus(_activity?.propertyData?.calcRepairing!!)
        }

        activity?.runOnUiThread {
            incidentalsTotalInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData?.calcIncidentalsTotal))
        }
    }

    // --------------------------------------

    private fun updateRent() {

        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcRent == null) {
                rentInputView?.text?.clear()
            }
            else {
                rentInputView?.setText(_activity?.propertyData?.calcRent.toFormatNumber())
                layout?.rent?.setInputDefaultValue(_activity?.propertyData?.calcRent ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.rent, "rent_percent", _activity?.propertyData?.calcRentPercent)
        }
    }

    private fun updateLifeInsurance() {
        val lifeInsuranceDefault = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.LIFE_INSURANCE })?.value ?: 0)
        layout?.lifeInsurance?.setInputDefaultValue(lifeInsuranceDefault)

        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcLifeInsurance = lifeInsuranceDefault
        }

        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcLifeInsurance == null) {
                lifeInsuranceInputView?.text?.clear()
            }
            else {
                lifeInsuranceInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData?.calcLifeInsurance))
            }
        }
    }

    private fun updateStructureInsurance() {
        val structureInsuranceDefault = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.STRUCTURE_INSURANCE })?.value ?: 0)
        layout?.structureInsurance?.setInputDefaultValue(structureInsuranceDefault)

        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcStructureInsurance = structureInsuranceDefault
        }

        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcStructureInsurance == null) {
                structureInsuranceInputView?.text?.clear()
            }
            else {
                structureInsuranceInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData?.calcStructureInsurance))
            }
        }
    }

    private fun updateRentCleaningExpenses() {
        if (_activity?.roomPID == 0L) {
            _activity?.propertyData?.calcRentCleaningExpenses = _activity?.propertyData?.calcLifeInsurance!!.plus(_activity?.propertyData?.calcStructureInsurance!!)
        }

        activity?.runOnUiThread {
            rentCleaningExpensesInputView?.setText(Utilities.getDecimalNumber(_activity?.propertyData?.calcRentCleaningExpenses))
        }
    }

    // --------------------------------------

    private fun takeMortgageVisibility() {
        val visible = if (_activity?.propertyData?.showMortgagePrepayment == false || _activity?.userData?.canTakeMortgage == false) GONE else VISIBLE
        layout?.possibleMonthlyRepayment?.visibility = visible
        layout?.brokerMortgage?.visibility = visible

        layout?.percentOfFinancingDivider?.visibility = visible
        layout?.maxPercentOfFinancing?.visibility = visible
        layout?.actualPercentOfFinancing?.visibility = visible

        layout?.mortgageDivider?.visibility = visible
        layout?.propertyMortgagePrepaymentLabel?.visibility = visible
        layout?.mortgagePeriod?.visibility = visible
        layout?.mortgageMonthlyRepayment?.visibility = visible
        layout?.mortgageMonthlyYield?.visibility = visible
    }

    private fun updateMortgagePeriod() {

        if (_activity?.propertyData?.showMortgagePrepayment == false || _activity?.userData?.canTakeMortgage == false) {
            return
        }

        var mortgagePeriodObject = _activity?.fixedParametersData?.mortgagePeriodArray?.find { it.key.equals(_activity?.propertyData?.calcMortgagePeriod.toString()) }

        if (mortgagePeriodObject == null) {
            mortgagePeriodObject = _activity?.fixedParametersData?.mortgagePeriodArray?.find { it.default == true }
        }

        activity?.runOnUiThread {
            if (mortgagePeriodObject != null) {
                _activity?.propertyData?.calcMortgagePeriod = mortgagePeriodObject.key?.toInt()

                val mortgagePeriodIndex = _activity?.fixedParametersData?.mortgagePeriodArray?.indexOf(mortgagePeriodObject)

                activity?.runOnUiThread {
                    if (mortgagePeriodSpinnerView?.selectedIndex != (mortgagePeriodIndex ?: 0)) {
                        mortgagePeriodSpinnerView?.selectItemByIndex(mortgagePeriodIndex ?: 0)
                    }
                }

                layout?.mortgagePeriod?.setDropDown(mortgagePeriodSpinnerView)
                //updateMortgageMonthlyRepayment()

                var hasWarning = false

                if (_activity?.propertyData?.calcMortgagePeriod != null && _activity?.userData?.calcAge != null && _activity?.fixedParametersData?.mortgageMaxAge != null) {
                    if (_activity!!.propertyData!!.calcMortgagePeriod!!.plus(_activity!!.userData!!.calcAge!!) > _activity!!.fixedParametersData!!.mortgageMaxAge!!) {
                        hasWarning = true
                    }
                }

                layout?.mortgagePeriod?.setWarning(mortgagePeriodSpinnerView, hasWarning)
            }
        }
    }

    private fun updateMortgageMonthlyRepayment() {
        if (_activity?.propertyData?.showMortgagePrepayment == false || _activity?.userData?.canTakeMortgage == false) {
            return
        }

        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcMortgageMonthlyRepayment != null) {
                mortgageMonthlyRepaymentInputView?.setText(
                    Utilities.getDecimalNumber(_activity?.propertyData?.calcMortgageMonthlyRepayment)
                )

                layout?.mortgageMonthlyRepayment?.setWarning(mortgageMonthlyRepaymentInputView,
                    _activity?.propertyData?.calcMortgageMonthlyRepayment ?: 0 > _activity?.propertyData?.calcPossibleMonthlyRepayment ?: 0
                )
            }
            else {
                mortgageMonthlyRepaymentInputView?.text?.clear()
            }
        }
    }

    private fun updateMortgageMonthlyYield() {
        if (_activity?.propertyData?.showMortgagePrepayment == false || _activity?.userData?.canTakeMortgage == false) {
            return
        }

        var hasWarning = false

        activity?.runOnUiThread {
            if (_activity?.propertyData?.calcMortgageMonthlyYield != null) {
                mortgageMonthlyYieldInputView?.setText(
                    Utilities.getDecimalNumber(
                        _activity?.propertyData?.calcMortgageMonthlyYield
                    )
                )
            }
            else {
                mortgageMonthlyYieldInputView?.text?.clear()
            }
        }

        if (_activity?.propertyData?.calcMortgageMonthlyYield != null && _activity?.propertyData?.calcMortgageMonthlyYield!! < 0) {
            hasWarning = true
        }

        layout?.mortgageMonthlyYield?.setWarning(mortgageMonthlyYieldInputView, hasWarning)
    }

    //endregion == update data ========

    //region == actions menu =======

    internal fun activateActionsMenuIfNeeded() {
        val isEnable = !_activity?.propertyData?.calcYieldForecastList.isNullOrEmpty()
        setActionsMenuEnable(isEnable)
    }

    private fun setActionsMenuEnable(isEnable: Boolean) {
        _activity?.layout?.actionsMenuLockBottomIcon?.visibility = if (isEnable) GONE else VISIBLE

        _activity?.layout?.actionsMenuYieldForecastBottomIcon?.isEnabled = isEnable
        _activity?.layout?.actionsMenuYieldForecastBottomIcon?.setImageDrawable(ContextCompat.getDrawable(_context!!, if (isEnable) R.drawable.icon_yield_forecast_off else R.drawable.icon_yield_forecast_disable))
        _activity?.layout?.actionsMenuYieldForecastBottomText?.setTextColor(ContextCompat.getColor(_context!!, if (isEnable) R.color.textActionsMenu else R.color.textActionsMenuDisable))

        if (_activity?.propertyData?.showMortgagePrepayment == false || _activity?.userData?.canTakeMortgage == false) {
            _activity?.layout?.actionsMenuAmortizationScheduleBottom?.visibility = GONE
        }
        else {
            _activity?.layout?.actionsMenuAmortizationScheduleBottom?.visibility = VISIBLE
            _activity?.layout?.actionsMenuAmortizationScheduleBottomIcon?.isEnabled = isEnable
            _activity?.layout?.actionsMenuAmortizationScheduleBottomIcon?.setImageDrawable(ContextCompat.getDrawable(_context!!, if (isEnable) R.drawable.icon_amortization_schedule_off else R.drawable.icon_amortization_schedule_disable))
            _activity?.layout?.actionsMenuAmortizationScheduleBottomText?.setTextColor(ContextCompat.getColor(_context!!, if (isEnable) R.color.textActionsMenu else R.color.textActionsMenuDisable))
        }

        _activity?.layout?.actionsMenuChartBottomIcon?.isEnabled = isEnable
        _activity?.layout?.actionsMenuChartBottomIcon?.setImageDrawable(ContextCompat.getDrawable(_context!!, if (isEnable) R.drawable.icon_chart_off else R.drawable.icon_graph_disable))
        _activity?.layout?.actionsMenuChartBottomText?.setTextColor(ContextCompat.getColor(_context!!, if (isEnable) R.color.textActionsMenu else R.color.textActionsMenuDisable))
    }

    //endregion == actions menu =======

    //region == picture ============

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //takePictureIntent.putExtra("crop", "true")

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    /*private fun dispatchTakePictureIntent1() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ...
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            _activity?.propertyData?.pictures = "[]"
            //_activity?.insertUpdateProperty()
            updatePicture()
        }

        /*if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(_activity as Activity)
            CropImage.activity(getImageUri(requireContext(), data?.extras?.get("data") as Bitmap)).start(_activity as Activity)
            CropImage.activity().start(_activity!!, this@PropertyInfoFragment)
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)

            _activity?.propertyData?.pictures = "[\"" + result?.getUri().toString()   + "\"]"
            _activity?.insertUpdateProperty()
            updatePicture()
        }*/
    }

    /*@Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }*/

    //endregion == picture ============
}