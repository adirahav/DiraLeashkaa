package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adirahav.diraleashkaa.common.Const
import com.google.gson.annotations.SerializedName

@Entity
data class PropertyEntity (
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,

	@SerializedName("_id")											var _id: String? = null,

	// סוג פריט (דירה פוטנציאלית, דירה במחשבון)
	@SerializedName("type")											var type: String? = null,

	// תמונות
	@SerializedName("pictures")										var pictures: String? = null,

	// עיר
	@SerializedName("city") 										var city: String? = null,
	@SerializedName("cityElse")										var cityElse: String? = null,

	// כתובת
	@SerializedName("address") 										var address: String? = null,

	// סוג דירה
	@SerializedName("apartmentType") 								var apartmentType: String? = null,

	// מחיר הנכס
	@SerializedName("price") 										var price: Int? = null,

	// מס רכישה
	@SerializedName("calcTransferTax") 								var calcTransferTax: Int? = null,

	// הון עצמי
	@SerializedName("calcEquity") 									var calcEquity: Int? = null,
	@SerializedName("defaultEquity") 								var defaultEquity: Int? = null,

	// הכנסות
	@SerializedName("calcIncomes") 									var calcIncomes: Int? = null,
	@SerializedName("defaultIncomes") 								var defaultIncomes: Int? = null,

	// הלוואות והתחייבויות
	@SerializedName("calcCommitments") 								var calcCommitments: Int? = null,
	@SerializedName("defaultCommitments") 							var defaultCommitments: Int? = null,

	// הכנסה פנויה
	@SerializedName("calcDisposableIncome") 						var calcDisposableIncome: Int? = null,

	// אחוז החזר חודשי אפשרי
	@SerializedName("possibleMonthlyRepaymentPercent") 				var possibleMonthlyRepaymentPercent: Float? = null,
	@SerializedName("calcPossibleMonthlyRepaymentPercent") 			var calcPossibleMonthlyRepaymentPercent: Float? = null,
	@SerializedName("calcPossibleMonthlyRepayment") 				var calcPossibleMonthlyRepayment: Int? = null,
	@SerializedName("defaultPossibleMonthlyRepaymentPercent") 		var defaultPossibleMonthlyRepaymentPercent: Float? = null,

	// אחוז עלות עורך דין
	@SerializedName("lawyerPercent") 								var lawyerPercent: Float? = null,
	@SerializedName("lawyerCustomValue") 							var lawyerCustomValue: Int? = null,
	@SerializedName("calcLawyerPercent") 							var calcLawyerPercent: Float? = null,
	@SerializedName("calcLawyer") 									var calcLawyer: Int? = null,
	@SerializedName("defaultLawyerPercent") 						var defaultLawyerPercent: Float? = null,
	@SerializedName("defaultLawyer") 								var defaultLawyer: Int? = null,

	// אחוז עלות מתווך
	@SerializedName("realEstateAgentPercent") 						var realEstateAgentPercent: Float? = null,
	@SerializedName("realEstateAgentCustomValue") 					var realEstateAgentCustomValue: Int? = null,
	@SerializedName("calcRealEstateAgentPercent") 					var calcRealEstateAgentPercent: Float? = null,
	@SerializedName("calcRealEstateAgent") 							var calcRealEstateAgent: Int? = null,
	@SerializedName("defaultRealEstateAgentPercent") 				var defaultRealEstateAgentPercent: Float? = null,
	@SerializedName("defaultRealEstateAgent") 						var defaultRealEstateAgent: Int? = null,

	// יועץ משכנתא
	@SerializedName("calcBrokerMortgage") 							var calcBrokerMortgage: Int? = null,
	@SerializedName("defaultBrokerMortgage") 						var defaultBrokerMortgage: Int? = null,

	// שיפוץ
	@SerializedName("calcRepairing") 								var calcRepairing: Int? = null,
	@SerializedName("defaultRepairing") 							var defaultRepairing: Int? = null,

	// צפי לאחוז שכר דירה
	@SerializedName("rentPercent") 									var rentPercent: Float? = null,
	@SerializedName("rentCustomValue") 								var rentCustomValue: Int? = null,
	@SerializedName("calcRentPercent") 								var calcRentPercent: Float? = null,
	@SerializedName("calcRent") 									var calcRent: Int? = null,
	@SerializedName("defaultRentPercent") 							var defaultRentPercent: Float? = null,
	@SerializedName("defaultRent") 									var defaultRent: Int? = null,

	// שכר דירה בניכוי הוצאות נלוות
	@SerializedName("calcRentCleaningExpenses") 					var calcRentCleaningExpenses: Int? = null,

	// ביטוח חיים
	@SerializedName("calcLifeInsurance") 							var calcLifeInsurance: Int? = null,
	@SerializedName("defaultLifeInsurance") 						var defaultLifeInsurance: Int? = null,	// [in FIXED_PARAMETERS]

	// ביטוח מבנה
	@SerializedName("calcStructureInsurance") 						var calcStructureInsurance: Int? = null,
	@SerializedName("defaultStructureInsurance") 					var defaultStructureInsurance: Int? = null,	// [in FIXED_PARAMETERS]

	// הוצאות נלוות
	@SerializedName("calcIncidentalsTotal") 						var calcIncidentalsTotal: Int? = null,

	// הון עצמי בניכוי הוצאות נלוונת
	@SerializedName("calcEquityCleaningExpenses") 					var calcEquityCleaningExpenses: Int? = null,

	// משכנתא נדרשת
	@SerializedName("calcMortgageRequired") 						var calcMortgageRequired: Int? = null,

	// החזר חודשי
	@SerializedName("calcMortgageMonthlyRepayment") 				var calcMortgageMonthlyRepayment: Int? = null,

	// תשואה חודשית
	@SerializedName("calcMortgageMonthlyYield") 					var calcMortgageMonthlyYield: Int? = null,

	// תקופת משכנתא
	@SerializedName("calcMortgagePeriod") 							var calcMortgagePeriod: Int? = null,

	// אחוז מימון מקסימלי
	@SerializedName("calcMaxPercentOfFinancing") 					var calcMaxPercentOfFinancing: Int? = null,

	// אחוז מימון בפועל
	@SerializedName("calcActualPercentOfFinancing") 				var calcActualPercentOfFinancing: Int? = null,

	// הצג ריביות
	@SerializedName("showInterestsContainer") 						var showInterestsContainer: Boolean? = null,

	// מדד
	@SerializedName("calcIndexPercent") 							var calcIndexPercent: Float? = null,
	@SerializedName("defaultIndexPercent") 							var defaultIndexPercent: Float? = null,

	// ריבית
	@SerializedName("calcInterestPercent") 							var calcInterestPercent: Float? = null,
	@SerializedName("defaultInterestPercent") 						var defaultInterestPercent: Float? = null,

	// ריבית בעוד 5 שנים
	@SerializedName("calcInterestIn5YearsPercent") 					var calcInterestIn5YearsPercent: Float? = null,
	@SerializedName("defaultInterestIn5YearsPercent") 				var defaultInterestIn5YearsPercent: Float? = null,

	// ריבית בעוד 10 שנים
	@SerializedName("calcInterestIn10YearsPercent") 				var calcInterestIn10YearsPercent: Float? = null,
	@SerializedName("defaultInterestIn10YearsPercent") 				var defaultInterestIn10YearsPercent: Float? = null,

	// ריבית ממוצעת בזמן לקיחה
	@SerializedName("calcAverageInterestAtTakingPercent") 			var calcAverageInterestAtTakingPercent: Float? = null,
	@SerializedName("defaultAverageInterestAtTakingPercent") 		var defaultAverageInterestAtTakingPercent: Float? = null,

	// ריבית ממוצעת בעת פירעון
	@SerializedName("calcAverageInterestAtMaturityPercent") 		var calcAverageInterestAtMaturityPercent: Float? = null,
	@SerializedName("defaultAverageInterestAtMaturityPercent") 		var defaultAverageInterestAtMaturityPercent: Float? = null,

	// ריבית להיוון
	@SerializedName("calcInterestToCapitalizePercent") 				var calcInterestToCapitalizePercent: Float? = null,
	@SerializedName("defaultInterestToCapitalizePercent") 			var defaultInterestToCapitalizePercent: Float? = null,

	// צפי תשואה שנתית של הנכס
	@SerializedName("calcForecastAnnualPriceIncreasePercent") 		var calcForecastAnnualPriceIncreasePercent: Float? = null,
	@SerializedName("defaultForecastAnnualPriceIncreasePercent") 	var defaultForecastAnnualPriceIncreasePercent: Float? = null,

	// עלויות מכירה
	@SerializedName("calcSalesCostsPercent")						var calcSalesCostsPercent: Float? = null,
	@SerializedName("defaultSalesCostsPercent")						var defaultSalesCostsPercent: Float? = null,

	// פחת לצורך מס
	@SerializedName("calcDepreciationForTaxPurposesPercent") 		var calcDepreciationForTaxPurposesPercent: Float? = null,
	@SerializedName("defaultDepreciationForTaxPurposesPercent") 	var defaultDepreciationForTaxPurposesPercent: Float? = null,

	// מס במכירה
	@SerializedName("calcTaxOnSale") 								var calcTaxOnSale: Int? = null,
	@SerializedName("defaultTaxOnSale") 							var defaultTaxOnSale: Int? = null,

	// תקופת שנים למכירה
	@SerializedName("calcSaleYearsPeriod") 							var calcSaleYearsPeriod: Int? = null,
	@SerializedName("defaultSaleYearsPeriod") 						var defaultSaleYearsPeriod: Int? = null,

	@SerializedName("archive")										var archive: Boolean? = null,

	@SerializedName("showMortgagePrepayment")						var showMortgagePrepayment: Boolean? = null,

	@SerializedName("calcAmortizationSchedule")						var calcAmortizationSchedule: String? = null,
	@SerializedName("calcYieldForecast")							var calcYieldForecast: String? = null

)