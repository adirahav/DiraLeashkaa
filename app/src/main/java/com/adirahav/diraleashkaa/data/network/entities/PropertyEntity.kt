package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class PropertyEntity (
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,

	@SerializedName("uuid")											var uuid: String? = null,

	// תמונות
	@SerializedName("pictures")										var pictures: String? = null,

	// עיר
	@SerializedName("city") 										var city: String? = null,
	@SerializedName("city_else")									var cityElse: String? = null,

	// כתובת
	@SerializedName("address") 										var address: String? = null,

	// סוג דירה
	@SerializedName("apartment_type") 								var apartmentType: String? = null,

	// מחיר הנכס
	@SerializedName("price") 										var price: Int? = null,

	// מס רכישה
	@SerializedName("calc_transfer_tax") 							var calcTransferTax: Int? = null,

	// הון עצמי
	@SerializedName("calc_equity") 									var calcEquity: Int? = null,
	@SerializedName("default_equity") 								var defaultEquity: Int? = null,

	// הכנסות
	@SerializedName("calc_incomes") 								var calcIncomes: Int? = null,
	@SerializedName("default_incomes") 								var defaultIncomes: Int? = null,

	// הלוואות והתחייבויות
	@SerializedName("calc_commitments") 							var calcCommitments: Int? = null,
	@SerializedName("default_commitments") 							var defaultCommitments: Int? = null,

	// הכנסה פנויה
	@SerializedName("calc_disposable_income") 						var calcDisposableIncome: Int? = null,

	// אחוז החזר חודשי אפשרי
	@SerializedName("possible_monthly_repayment_percent") 			var possibleMonthlyRepaymentPercent: Float? = null,
	@SerializedName("calc_possible_monthly_repayment_percent") 		var calcPossibleMonthlyRepaymentPercent: Float? = null,
	@SerializedName("calc_possible_monthly_repayment") 				var calcPossibleMonthlyRepayment: Int? = null,
	@SerializedName("default_possible_monthly_repayment_percent") 	var defaultPossibleMonthlyRepaymentPercent: Float? = null,

	// אחוז עלות עורך דין
	@SerializedName("lawyer_percent") 								var lawyerPercent: Float? = null,
	@SerializedName("lawyer_custom_value") 							var lawyerCustom: Int? = null,
	@SerializedName("calc_lawyer_percent") 							var calcLawyerPercent: Float? = null,
	@SerializedName("calc_lawyer") 									var calcLawyer: Int? = null,
	@SerializedName("default_lawyer_percent") 						var defaultLawyerPercent: Float? = null,
	@SerializedName("default_lawyer") 								var defaultLawyer: Int? = null,

	// אחוז עלות מתווך
	@SerializedName("real_estate_agent_percent") 					var realEstateAgentPercent: Float? = null,
	@SerializedName("real_estate_agent_custom_value") 				var realEstateAgentCustom: Int? = null,
	@SerializedName("calc_real_estate_agent_percent") 				var calcRealEstateAgentPercent: Float? = null,
	@SerializedName("calc_real_estate_agent") 						var calcRealEstateAgent: Int? = null,
	@SerializedName("default_real_estate_agent_percent") 			var defaultRealEstateAgentPercent: Float? = null,
	@SerializedName("default_real_estate_agent") 					var defaultRealEstateAgent: Int? = null,

	// יועץ משכנתא
	@SerializedName("calc_broker_mortgage") 						var calcBrokerMortgage: Int? = null,
	@SerializedName("default_broker_mortgage") 						var defaultBrokerMortgage: Int? = null,

	// שיפוץ
	@SerializedName("calc_repairing") 								var calcRepairing: Int? = null,
	@SerializedName("default_repairing") 							var defaultRepairing: Int? = null,

	// צפי לאחוז שכר דירה
	@SerializedName("rent_percent") 								var rentPercent: Float? = null,
	@SerializedName("rent_custom_value") 							var rentCustom: Int? = null,
	@SerializedName("calc_rent_percent") 							var calcRentPercent: Float? = null,
	@SerializedName("calc_rent") 									var calcRent: Int? = null,
	@SerializedName("default_rent_percent") 						var defaultRentPercent: Float? = null,
	@SerializedName("default_rent") 								var defaultRent: Int? = null,

	// שכר דירה בניכוי הוצאות נלוות
	@SerializedName("calc_rent_cleaning_expenses") 					var calcRentCleaningExpenses: Int? = null,

	// ביטוח חיים
	@SerializedName("calc_life_insurance") 							var calcLifeInsurance: Int? = null,
	@SerializedName("default_life_insurance") 						var defaultLifeInsurance: Int? = null,	// [in FIXED_PARAMETERS]

	// ביטוח מבנה
	@SerializedName("calc_structure_insurance") 					var calcStructureInsurance: Int? = null,
	@SerializedName("default_structure_insurance") 					var defaultStructureInsurance: Int? = null,	// [in FIXED_PARAMETERS]

	// הוצאות נלוות
	@SerializedName("calc_incidentals_total") 						var calcIncidentalsTotal: Int? = null,

	// הון עצמי בניכוי הוצאות נלוונת
	@SerializedName("calc_equity_cleaning_expenses") 				var calcEquityCleaningExpenses: Int? = null,

	// משכנתא נדרשת
	@SerializedName("calc_mortgage_required") 						var calcMortgageRequired: Int? = null,

	// החזר חודשי
	@SerializedName("calc_mortgage_monthly_repayment") 				var calcMortgageMonthlyRepayment: Int? = null,

	// תשואה חודשית
	@SerializedName("calc_mortgage_monthly_yield") 					var calcMortgageMonthlyYield: Int? = null,

	// תקופת משכנתא
	@SerializedName("calc_mortgage_period") 						var calcMortgagePeriod: Int? = null,

	// אחוז מימון מקסימלי
	@SerializedName("calc_max_percent_of_financing") 				var calcMaxPercentOfFinancing: Int? = null,

	// אחוז מימון בפועל
	@SerializedName("calc_actual_percent_of_financing") 			var calcActualPercentOfFinancing: Int? = null,

	// הצג ריביות
	@SerializedName("show_interests_container") 					var showInterestsContainer: Boolean? = null,

	// מדד
	@SerializedName("calc_index_percent") 							var calcIndexPercent: Float? = null,
	@SerializedName("default_index_percent") 						var defaultIndexPercent: Float? = null,

	// ריבית
	@SerializedName("calc_interest_percent") 						var calcInterestPercent: Float? = null,
	@SerializedName("default_interest_percent") 					var defaultInterestPercent: Float? = null,

	// ריבית בעוד 5 שנים
	@SerializedName("calc_interest_in_5_years_percent") 			var calcInterestIn5YearsPercent: Float? = null,
	@SerializedName("default_interest_in_5_years_percent") 			var defaultInterestIn5YearsPercent: Float? = null,

	// ריבית בעוד 10 שנים
	@SerializedName("calc_interest_in_10_years_percent") 			var calcInterestIn10YearsPercent: Float? = null,
	@SerializedName("default_interest_in_10_years_percent") 		var defaultInterestIn10YearsPercent: Float? = null,

	// ריבית ממוצעת בזמן לקיחה
	@SerializedName("calc_average_interest_at_taking_percent") 		var calcAverageInterestAtTakingPercent: Float? = null,
	@SerializedName("default_average_interest_at_taking_percent") 	var defaultAverageInterestAtTakingPercent: Float? = null,

	// ריבית ממוצעת בעת פירעון
	@SerializedName("calc_average_interest_at_maturity_percent") 	var calcAverageInterestAtMaturityPercent: Float? = null,
	@SerializedName("default_average_interest_at_maturity_percent") var defaultAverageInterestAtMaturityPercent: Float? = null,

	// ריבית להיוון
	@SerializedName("calc_interest_to_capitalize_percent") 			var calcInterestToCapitalizePercent: Float? = null,
	@SerializedName("default_interest_to_capitalize_percent") 		var defaultInterestToCapitalizePercent: Float? = null,

	// צפי תשואה שנתית של הנכס
	@SerializedName("calc_forecast_annual_price_increase_percent") 	var calcForecastAnnualPriceIncreasePercent: Float? = null,
	@SerializedName("default_forecast_annual_price_increase_percent") var defaultForecastAnnualPriceIncreasePercent: Float? = null,

	// עלויות מכירה
	@SerializedName("calc_sales_costs_percent")						var calcSalesCostsPercent: Float? = null,
	@SerializedName("default_sales_costs_percent")					var defaultSalesCostsPercent: Float? = null,

	// פחת לצורך מס
	@SerializedName("calc_depreciation_for_tax_purposes_percent") 	var calcDepreciationForTaxPurposesPercent: Float? = null,
	@SerializedName("default_depreciation_for_tax_purposes_percent") var defaultDepreciationForTaxPurposesPercent: Float? = null,

	// מס במכירה
	@SerializedName("calc_tax_on_sale") 							var calcTaxOnSale: Int? = null,
	@SerializedName("default_tax_on_sale") 							var defaultTaxOnSale: Int? = null,

	// תקופת שנים למכירה
	@SerializedName("calc_sale_years_period") 						var calcSaleYearsPeriod: Int? = null,
	@SerializedName("default_sale_years_period") 					var defaultSaleYearsPeriod: Int? = null,

	@SerializedName("archive")										var archive: Boolean? = null,

	@SerializedName("show_mortgage_prepayment")						var showMortgagePrepayment: Boolean? = null,

	@SerializedName("calc_amortization_schedule")					var calcAmortizationScheduleList: String? = null,
	@SerializedName("calc_yield_forecast")							var calcYieldForecastList: String? = null

)