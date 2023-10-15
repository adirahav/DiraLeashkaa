package com.adirahav.diraleashkaa.ui.property

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.adirahav.diraleashkaa.R
import com.jakewharton.rxbinding.widget.RxTextView
import com.skydoves.powerspinner.IconSpinnerItem
import com.skydoves.powerspinner.PowerSpinnerView
import java.util.concurrent.TimeUnit
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adirahav.diraleashkaa.common.*
import com.adirahav.diraleashkaa.common.Utilities.toFormatNumber
import com.adirahav.diraleashkaa.common.Utilities.toNumber
import com.adirahav.diraleashkaa.data.network.dataClass.PropertyDataClass
import com.adirahav.diraleashkaa.data.network.dataClass.SplashDataClass
import com.adirahav.diraleashkaa.data.network.entities.AmortizationScheduleEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.entities.UserEntity
import com.adirahav.diraleashkaa.databinding.FragmentCalculatorMaxPriceBinding
import com.adirahav.diraleashkaa.ui.calculators.CalculatorActivity
import com.adirahav.diraleashkaa.ui.calculators.CalculatorsActivity
import com.adirahav.diraleashkaa.ui.goodbye.GoodbyeActivity
import com.adirahav.diraleashkaa.ui.home.HomeActivity
import com.adirahav.diraleashkaa.ui.splash.SplashActivity
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class CalculatorMaxPriceFragment : Fragment() {

    //region == companion ==========

    companion object {
        private const val TAG = "CalculatorMaxPriceFragment"
        private const val CALL_SERVER_AWAIT_SECONDS = 2
    }

    //endregion == companion ==========

    //region == variables ==========

    // lifecycle owner
    var lifecycleOwner: LifecycleOwner? = null

    // activity
    var _activity: CalculatorActivity? = null

    // context
    private var _context: Context? = null

    // form
    val form = mutableMapOf<String, Any?>()

    // layout
    private var layout: FragmentCalculatorMaxPriceBinding? = null

    // changed field
    private var whichFieldChangedByUser: String? = null
    internal var isFieldFieldChangedByUser: Boolean? = null

    // max price
    var maxPriceData: PropertyEntity? = null

    // room/server data loaded
    var isRoomDataInit: Boolean = false
    var isServerDataInit: Boolean = false

    // start time
    var callServerStartTime: Date? = null

    //endregion == variables ==========

    //region == views variables ====

    // apartment type
    var apartmentTypeSpinnerView: PowerSpinnerView? = null

    // price
    var propertyPriceLabelInputView: TextView? = null
    var propertyPriceInputView: TextView? = null

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

    // observers
    var roomMaxPriceObserver : Observer<in PropertyEntity>? = null
    var serverMaxPriceObserver : Observer<in PropertyEntity>? = null

    //endregion == views variables ====

    //region == lifecycle methods ==

    override fun onCreate(savedInstanceState: Bundle?) {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreate()", showToast = false)
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            lifecycleOwner = this
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        Utilities.log(Enums.LogType.Debug, TAG, "onCreateView()", showToast = false)

        // activity
        _activity = activity as CalculatorActivity

        layout = FragmentCalculatorMaxPriceBinding.inflate(layoutInflater)

        initGlobal()
        initViews()
        initEvents()
        initObserver()

        return layout?.root
    }

    override fun onResume() {
        Utilities.log(Enums.LogType.Debug, TAG, "onResume()", showToast = false)
        super.onResume()
    }

    //endregion == lifecycle methods ==

    //region == initialize =========
    private fun initGlobal() {
        Utilities.log(Enums.LogType.Debug, TAG, "initGlobal()", showToast = false)

        // context
        _context = context

        // strings
        setRoomStrings()

        // room/server data loaded
        isRoomDataInit = false
        isServerDataInit = false
    }

    private fun initViews() {
        Utilities.log(Enums.LogType.Debug, TAG, "initViews()", showToast = false)

        // apartment type
        apartmentTypeSpinnerView = layout?.apartmentType?.findViewById(R.id.spinner)

        // price
        propertyPriceLabelInputView = layout?.propertyPriceLabel
        propertyPriceInputView = layout?.propertyPrice

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
    }

    fun initData() {

        Utilities.log(Enums.LogType.Debug, TAG, "initData():uuid = ${maxPriceData?.uuid} ; maxPriceData = ${maxPriceData}")

        isFieldFieldChangedByUser = false

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

        _activity?.unfreezeScreen()

        isFieldFieldChangedByUser = true

        if (isServerDataInit == false) {
            callServerStartTime = Date()
            _activity?.viewModel!!.calcServerMaxPrice(_activity!!.userUUID, null, null)
        }
    }

    private fun initEvents() {
        Utilities.log(Enums.LogType.Debug, TAG, "initEvents()", showToast = false)

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
            maxPriceData?.possibleMonthlyRepaymentPercent = layout?.possibleMonthlyRepayment?.numberPickerActualValue
            calcMaxPrice(Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT, maxPriceData?.possibleMonthlyRepaymentPercent)

            layout?.possibleMonthlyRepayment?.onNumberPickerAccept()
        }

        possibleMonthlyRepaymentRollbackView?.setOnClickListener {
            layout?.possibleMonthlyRepayment?.onRollbackClick()
            updatePossibleMonthlyRepayment()
            onPossibleMonthlyRepaymentChanged()

            maxPriceData?.possibleMonthlyRepaymentPercent = ((_activity?.fixedParametersData?.propertyInputsArray?.find { it.name == Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT })?.default ?: 0F)

            calcMaxPrice(Const.POSSIBLE_MONTHLY_REPAYMENT_PERCENT, maxPriceData?.possibleMonthlyRepaymentPercent)
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
            maxPriceData?.calcLawyerPercent = layout?.lawyer?.numberPickerActualValue
            calcMaxPrice(Const.LAWYER_PERCENT, maxPriceData?.calcLawyerPercent)
            layout?.lawyer?.onNumberPickerAccept()
        }

        lawyerRollbackView?.setOnClickListener {
            layout?.lawyer?.onRollbackClick()

            maxPriceData?.calcLawyerPercent = null
            maxPriceData?.calcLawyer = null
            lawyerInputView?.clearFocus()

            calcMaxPrice(Const.LAWYER_PERCENT, "")
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
            maxPriceData?.calcRealEstateAgentPercent = layout?.realEstateAgent?.numberPickerActualValue
            calcMaxPrice(Const.REAL_ESTATE_AGENT_PERCENT, maxPriceData?.calcRealEstateAgentPercent)
            layout?.realEstateAgent?.onNumberPickerAccept()
        }

        realEstateAgentRollbackView?.setOnClickListener {
            layout?.realEstateAgent?.onRollbackClick()

            maxPriceData?.calcRealEstateAgentPercent = null
            maxPriceData?.calcRealEstateAgent = null
            realEstateAgentInputView?.clearFocus()

            calcMaxPrice(Const.REAL_ESTATE_AGENT_PERCENT, "")
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
    }

    //endregion == initialize =========

    //region == strings ============

    private fun initObserver() {
        Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): roomMaxPrice.hasObservers = ${_activity?.viewModel!!.roomMaxPrice.hasObservers()}")
        Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): serverMaxPrice.hasObservers = ${_activity?.viewModel!!.serverMaxPrice.hasObservers()}")

        if (!_activity?.viewModel!!.roomMaxPrice.hasObservers()) _activity?.viewModel!!.roomMaxPrice.observe(viewLifecycleOwner, RoomMaxPriceObserver())
        if (!_activity?.viewModel!!.serverMaxPrice.hasObservers()) _activity?.viewModel!!.serverMaxPrice.observe(viewLifecycleOwner, ServerMaxPriceObserver())

        if (!isRoomDataInit) {
            Utilities.log(Enums.LogType.Debug, TAG, "initObserver(): getRoomMaxPrice")
            _activity?.viewModel!!.getRoomMaxPrice(_activity!!.applicationContext)
        }
    }

    private fun setRoomStrings() {
        Utilities.log(Enums.LogType.Debug, TAG, "setRoomStrings()")

        Utilities.setPropertyInputString(layout?.apartmentType, "property_apartment_type_label")
        Utilities.setTextViewString(layout?.propertyPriceLabel, "calculator_maxprice_price_label")
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
    }

    //endregion == strings ============

    //region == change events ======

    private fun onApartmentTypeChanged(position: Int) {
        val apartmentType =  if (position == 0)
            null
        else
        _activity?.fixedParametersData?.apartmentTypesArray?.get(position)

        maxPriceData?.apartmentType = apartmentType?.key
        calcMaxPrice(Const.APARTMENT_TYPE, maxPriceData?.apartmentType)
    }

    private fun onEquityChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcEquity =
                     if (equityInputView?.text != null)
                        equityInputView?.text?.toNumber()
                    else
                        null

            if (!equityInputView?.text.isNullOrEmpty()) {
                calcMaxPrice(Const.EQUITY, maxPriceData?.calcEquity)
            }
        }
    }

    private fun onEquityCleaningExpensesChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcEquityCleaningExpenses =
                if (equityCleaningExpensesInputView?.text != null)
                    equityCleaningExpensesInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onMortgageRequiredChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcMortgageRequired =
                if (mortgageRequiredInputView?.text != null)
                    mortgageRequiredInputView?.text?.toNumber()
                else
                    null
        }
    }

    private fun onIncomesChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcIncomes =
                if (incomesInputView?.text != null)
                    incomesInputView?.text?.toNumber()
                else
                    null

            if (!incomesInputView?.text.isNullOrEmpty()) {
                calcMaxPrice(Const.INCOMES, maxPriceData?.calcIncomes)
            }
        }
    }

    private fun onCommitmentsChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcCommitments =
                    if (commitmentsInputView?.text != null)
                        commitmentsInputView?.text?.toNumber()
                    else
                        null

            if (!commitmentsInputView?.text.isNullOrEmpty()) {
                calcMaxPrice(Const.COMMITMENTS, maxPriceData?.calcCommitments)
            }
        }
    }

    private fun onDisposableIncomeChanged() {

    }

    private fun onPossibleMonthlyRepaymentChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcPossibleMonthlyRepayment =
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
            maxPriceData?.calcActualPercentOfFinancing =
                    if (actualPercentOfFinancingInputView?.text != null)
                        actualPercentOfFinancingInputView?.text?.toNumber()
                    else
                        null
        }
    }

    private fun onTransferTaxChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcTransferTax =
                    if (transferTaxInputView?.text != null)
                        transferTaxInputView?.text?.toNumber()
                    else
                        null
        }
    }

    private fun onLawyerChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcLawyer = lawyerInputView?.text?.toNumber()
            maxPriceData?.calcLawyerPercent = null

            if (maxPriceData?.calcLawyer == null) {
                _activity?.setNumberPickerAttributes(layout?.lawyer, Const.LAWYER_PERCENT, maxPriceData?.calcLawyerPercent)
            }
            else {
                layout?.lawyer?.setNumberPickerWithoutValue()
            }

            calcMaxPrice(Const.LAWYER_CUSTOM_VALUE, maxPriceData?.calcLawyer)
        }
    }

    private fun onRealEstateAgentChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcRealEstateAgent = realEstateAgentInputView?.text?.toNumber()
            maxPriceData?.calcRealEstateAgentPercent = null

            if (maxPriceData?.calcRealEstateAgent == null) {
                _activity?.setNumberPickerAttributes(layout?.realEstateAgent, Const.REAL_ESTATE_AGENT_PERCENT, maxPriceData?.calcRealEstateAgentPercent)
            }
            else {
                layout?.realEstateAgent?.setNumberPickerWithoutValue()
            }

            calcMaxPrice(Const.REAL_ESTATE_AGENT_CUSTOM_VALUE, maxPriceData?.calcRealEstateAgent)
        }
    }

    private fun onBrokerMortgageChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcBrokerMortgage =
                    if (brokerMortgageInputView?.text != null)
                         brokerMortgageInputView?.text?.toNumber()
                     else
                        null

            calcMaxPrice(Const.BROKER_MORTGAGE, maxPriceData?.calcBrokerMortgage)
        }
    }

    private fun onRepairingChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcRepairing =
                    if (repairingInputView?.text != null)
                        repairingInputView?.text?.toNumber()
                    else
                        null

            calcMaxPrice(Const.REPAIRING, maxPriceData?.calcRepairing)
        }
    }

    private fun onIncidentalsTotalChanged() {
        activity?.runOnUiThread {
            maxPriceData?.calcIncidentalsTotal =
                    if (incidentalsTotalInputView?.text != null)
                        incidentalsTotalInputView?.text?.toNumber()
                    else
                        null
        }
    }

    //endregion == change events ======

    //region == update data ========

    private fun updateApartmentType() {
        val apartmentTypeObject = _activity?.fixedParametersData?.apartmentTypesArray?.find { it.key == maxPriceData?.apartmentType ?: "choose" }
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
            if (maxPriceData?.price != null) {
                propertyPriceInputView?.setText(
                        String.format(
                                Utilities.getRoomString("calculator_maxprice_price_nis"),
                                maxPriceData?.price.toFormatNumber())
                )
            }
        }
    }

    private fun updateEquity() {
        activity?.runOnUiThread {
            /*if (_activity?.roomPID == 0L) {
                maxPriceData?.calcEquity = _activity?.userData?.equity
            }*/

            layout?.equity?.setInputDefaultValue(_activity?.userData?.equity ?: 0)

            activity?.runOnUiThread {
                equityInputView?.setText(maxPriceData?.calcEquity.toFormatNumber())
            }
        }
    }

    private fun updateEquityCleaningExpenses() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcEquityCleaningExpenses == null) {
               equityCleaningExpensesInputView?.text?.clear()
            }
            else {
                equityCleaningExpensesInputView?.setText(
                        Utilities.getDecimalNumber(maxPriceData?.calcEquityCleaningExpenses)
                )

                var hasWarning = maxPriceData?.calcEquityCleaningExpenses!! < 0
                layout?.equityCleaningExpenses?.setWarning(equityCleaningExpensesInputView, hasWarning)
            }


        }
    }

    private fun updateMortgageRequired() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcMortgageRequired == null) {
                mortgageRequiredInputView?.text?.clear()
            }
            else {
                mortgageRequiredInputView?.setText(Utilities.getDecimalNumber(maxPriceData!!.calcMortgageRequired))

                layout?.mortgageRequired?.setWarning(mortgageRequiredInputView, false)
            }
        }
    }

    // --------------------------------------

    private fun updateIncomes() {
        /*if (_activity?.roomPID == 0L) {
            maxPriceData?.calcIncomes = _activity?.userData?.incomes
        }*/

        layout?.incomes?.setInputDefaultValue(_activity?.userData?.incomes ?: 0)

        activity?.runOnUiThread {
            incomesInputView?.setText(maxPriceData?.calcIncomes.toFormatNumber())
        }
    }

    private fun updateCommitments() {
        /*if (_activity?.roomPID == 0L) {
            maxPriceData?.calcCommitments = _activity?.userData?.commitments
        }*/

        layout?.commitments?.setInputDefaultValue(_activity?.userData?.commitments ?: 0)
        activity?.runOnUiThread {
            commitmentsInputView?.setText(maxPriceData?.calcCommitments.toFormatNumber())
        }
    }

    private fun updateDisposableIncome() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcDisposableIncome == null) {
                disposableIncomeInputView?.text?.clear()
             }
             else {
                disposableIncomeInputView?.setText(maxPriceData?.calcDisposableIncome.toFormatNumber())
            }
        }
    }

    private fun updatePossibleMonthlyRepayment() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcPossibleMonthlyRepayment == null) {
                possibleMonthlyRepaymentInputView?.text?.clear()
            }
            else {
                possibleMonthlyRepaymentInputView?.setText(maxPriceData?.calcPossibleMonthlyRepayment.toFormatNumber())
                layout?.possibleMonthlyRepayment?.setInputDefaultValue(maxPriceData?.calcPossibleMonthlyRepayment ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.possibleMonthlyRepayment, "possible_monthly_repayment_percent", maxPriceData?.calcPossibleMonthlyRepaymentPercent)
        }
    }

    // --------------------------------------

    private fun updateMaxPercentOfFinancing() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcMaxPercentOfFinancing == null) {
                maxPercentOfFinancingInputView?.text?.clear()
            }
            else {
                maxPercentOfFinancingInputView?.setText(maxPriceData?.calcMaxPercentOfFinancing.toString() + "%")
            }
        }
    }

    private fun updateActualPercentOfFinancing() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcActualPercentOfFinancing == null) {
                actualPercentOfFinancingInputView?.text?.clear()
            }
            else {
                actualPercentOfFinancingInputView?.setText(maxPriceData?.calcActualPercentOfFinancing.toString() + "%")
            }

            var hasWarning =
                   if (maxPriceData?.calcActualPercentOfFinancing == null || maxPriceData?.calcMaxPercentOfFinancing == null)
                       false
                   else
                       maxPriceData!!.calcActualPercentOfFinancing!! > maxPriceData!!.calcMaxPercentOfFinancing!!

            layout?.actualPercentOfFinancing?.setWarning(actualPercentOfFinancingInputView, hasWarning)
        }
    }

    // --------------------------------------

    private fun updateTransferTax() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcTransferTax == null) {
                 transferTaxInputView?.text?.clear()
            }
            else {
                transferTaxInputView?.setText(Utilities.getDecimalNumber(maxPriceData?.calcTransferTax))
           }
        }
    }

    private fun updateLayer() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcLawyer == null) {
                lawyerInputView?.text?.clear()
            }
            else {
               lawyerInputView?.setText(maxPriceData?.calcLawyer.toFormatNumber())
               layout?.lawyer?.setInputDefaultValue(maxPriceData?.defaultLawyer ?: 0)
           }

            _activity?.setNumberPickerAttributes(layout?.lawyer, Const.LAWYER_PERCENT, maxPriceData?.calcLawyerPercent)
        }
    }

    private fun updateRealEstateAgent() {
        activity?.runOnUiThread {
            if (maxPriceData?.calcRealEstateAgent == null) {
                  realEstateAgentInputView?.text?.clear()
            }
            else {
                 realEstateAgentInputView?.setText(maxPriceData?.calcRealEstateAgent.toFormatNumber())
                 layout?.realEstateAgent?.setInputDefaultValue(maxPriceData?.calcRealEstateAgent ?: 0)
            }

            _activity?.setNumberPickerAttributes(layout?.realEstateAgent, Const.REAL_ESTATE_AGENT_PERCENT, maxPriceData?.calcRealEstateAgentPercent)
        }
    }

    private fun updateBrokerMortgage() {
        /*if (_activity?.roomPID == 0L) {
            val brokerMortgageDefaultExcludingVAT = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.BROKER_MORTGAGE })?.value ?: 0)
            val brokerMortgageDefaultIncludingVAT = brokerMortgageDefaultExcludingVAT.times(_activity?.fixedParametersData?.vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()

            maxPriceData?.calcBrokerMortgage = brokerMortgageDefaultIncludingVAT
        }*/

        activity?.runOnUiThread {
            brokerMortgageInputView?.setText(maxPriceData?.calcBrokerMortgage.toFormatNumber())
        }
    }

    private fun updateRepairing() {
        /*if (_activity?.roomPID == 0L) {
            val repairingDefaultExcludingVAT = ((_activity?.fixedParametersData?.propertyValuesArray?.find { it.key == Const.REPAIRING })?.value ?: 0)
            val repairingDefaultIncludingVAT = repairingDefaultExcludingVAT.times(_activity?.fixedParametersData?.vatPercent?.percentToMultipleFraction() ?: 1.0f).roundToInt()

            maxPriceData?.calcRepairing = repairingDefaultIncludingVAT
        }*/

        activity?.runOnUiThread {
            repairingInputView?.setText(maxPriceData?.calcRepairing.toFormatNumber())
        }
    }

    private fun updateIncidentalsTotal() {
        /*if (_activity?.roomPID == 0L) {
            maxPriceData?.calcIncidentalsTotal = maxPriceData?.calcBrokerMortgage!!.plus(maxPriceData?.calcRepairing!!)
        }*/

        activity?.runOnUiThread {
            incidentalsTotalInputView?.setText(Utilities.getDecimalNumber(maxPriceData?.calcIncidentalsTotal))
        }
    }

    //endregion == update data ========

    //region == observers =============

    private inner class RoomMaxPriceObserver : Observer<PropertyEntity?> {
        override fun onChanged(maxPrice: PropertyEntity?) {
            Log.d(TAG, "RoomMaxPriceObserver()")

            if (maxPrice == null) {
                Log.d(TAG, "RoomMaxPriceObserver(): splashData == null)")
                _activity?.unfreezeScreen()
                return
            }

            isRoomDataInit = true
            maxPriceData = maxPrice
            initData()

            roomMaxPriceObserver = this
        }
    }

    private inner class ServerMaxPriceObserver : Observer<PropertyEntity?> {
        override fun onChanged(maxPrice: PropertyEntity?) {
            Log.d(TAG, "ServerMaxPriceObserver()")

            if (maxPrice == null) {
                Log.d(TAG, "ServerMaxPriceObserver(): splashData == null)")
                _activity?.unfreezeScreen()
                return
            }

            isServerDataInit = true

            val formattedMaxPrice = maxPrice
            val formattedMaxPriceData = maxPriceData

            formattedMaxPrice.roomID = formattedMaxPriceData?.roomID

            Log.d(TAG, "ServerMaxPriceObserver(): formattedMaxPrice     == ${formattedMaxPrice})")
            Log.d(TAG, "ServerMaxPriceObserver(): formattedMaxPriceData == ${formattedMaxPriceData})")

            val equals = formattedMaxPrice.equals(formattedMaxPriceData)
            Log.d(TAG, "ServerMaxPriceObserver(): equals == ${equals})")

            if (!formattedMaxPrice.equals(formattedMaxPriceData)) {
                maxPriceData = maxPrice
                _activity?.viewModel!!.saveRoomMaxPrice(_activity!!.applicationContext, maxPriceData!!)
                Utilities.await(callServerStartTime, CALL_SERVER_AWAIT_SECONDS, ::initData)
            }

            serverMaxPriceObserver = this
        }
    }

    fun removeObservers() {
        if (roomMaxPriceObserver != null) _activity?.viewModel!!.roomMaxPrice.removeObserver(roomMaxPriceObserver!!)
        if (serverMaxPriceObserver != null) _activity?.viewModel!!.serverMaxPrice.removeObserver(serverMaxPriceObserver!!)
    }

    //endregion == observers =============


    //region == general ==================
    fun calcMaxPrice(fieldName: String? = "", fieldValue: Boolean? =  null) {
        calcMaxPrice(fieldName, fieldValue.toString())
    }

    fun calcMaxPrice(fieldName: String? = "", fieldValue: Int? =  null) {
        calcMaxPrice(fieldName, fieldValue.toString())
    }

    fun calcMaxPrice(fieldName: String? = "", fieldValue: Float? =  null) {
        calcMaxPrice(fieldName, fieldValue.toString())
    }

    fun calcMaxPrice(fieldName: String? = "", fieldValue: String? =  null) {
        _activity?.freezeScreen()
        _activity?.viewModel!!.calcServerMaxPrice(_activity?.userUUID, fieldName, if (fieldValue.isNullOrEmpty() || fieldValue.equals("null")) null else fieldValue)
    }

    //endregion == general ==================

}