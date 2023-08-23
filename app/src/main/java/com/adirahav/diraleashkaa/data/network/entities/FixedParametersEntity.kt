package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class FixedParametersEntity(
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,
	@SerializedName("vat_percent") val vatPercent: Float,                           // אחוז מע"מ
	@SerializedName("mortgage_max_age") val mortgageMaxAge: Int,                  	// גיל מקסימלי לקבלת משכנתא
	@SerializedName("trial_period") val trialPeriod: String,                        // תקופת נסיון
	@SerializedName("expiration_alert") val expirationAlert: String,                // התראת פג תוקף
	@SerializedName("app_version") val appVersion: String,                          // גירסה
	@SerializedName("pay_programs") val payPrograms: String,                        // תוכניות בתשלום
	@SerializedName("google_pay") val googlePay: String,                            // GOOGLE PAY
	@SerializedName("best_yield") val bestYield: String,                            // התשואה הטובה ביותר
	@SerializedName("property_inputs") val propertyInputs: String,                  // נתוני שדות בעלי עריכת אחוזים
	@SerializedName("property_values") val propertyValues: String,                  // נתוני שדות ללא עריכת אחוזים
	@SerializedName("indexes_and_interests") val indexesAndInterests: String,       // מדדים וריביות [מתעדכן פעם בחודש]
	@SerializedName("average_interests") val averageInterests: String,              // ריבית ממוצעת לפי מסלולים [מתעדכן פעם בחודש]
	@SerializedName("additional_interests") val additionalInterests: String,        // תוספת ריבית לפי אחוז מימון
	@SerializedName("cities") val cities: String,                                   // ערים
	@SerializedName("apartment_types") val apartmentTypes: String,                  // סוג דירה
	@SerializedName("contactus") val contactUs: String?,                            //בצור קשר
	//@SerializedName("max_percent_of_financing") val maxPercentOfFinancing: String, // אחוז מימון מקסימלי
	//@SerializedName("tax_percent_on_sale") val taxPercentOnSale: String,          // מס במכירה
	//@SerializedName("transfer_tax") val transferTax: String,                      // מס רכישה  [מתעדכן פעם בשנה]
	@SerializedName("mortgage_periods") val mortgagePeriods: String,                // תקופת משכנתא
	@SerializedName("sms") val sms: String,                                			// SMS
	@SerializedName("picture") val picture: String,                                	// picture
	@SerializedName("on_error") val onError: String,                                // בעת שגיאה
)
