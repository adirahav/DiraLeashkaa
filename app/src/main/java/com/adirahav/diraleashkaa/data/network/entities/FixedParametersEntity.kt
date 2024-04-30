package com.adirahav.diraleashkaa.data.network.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class FixedParametersEntity(
	@PrimaryKey(autoGenerate = true) var roomID: Long? = null,
	@SerializedName("vatPercent") val vatPercent: Float,                           // אחוז מע"מ
	@SerializedName("mortgageMaxAge") val mortgageMaxAge: Int,                  	// גיל מקסימלי לקבלת משכנתא
	@SerializedName("trialPeriod") val trialPeriod: String,                        // תקופת נסיון
	@SerializedName("expirationAlert") val expirationAlert: String,                // התראת פג תוקף
	@SerializedName("appVersion") val appVersion: String,                          // גירסה
	@SerializedName("payPrograms") val payPrograms: String,                        // תוכניות בתשלום
	@SerializedName("bestYield") val bestYield: String,                            // התשואה הטובה ביותר
	@SerializedName("propertyInputs") val propertyInputs: String,                  // נתוני שדות בעלי עריכת אחוזים
	@SerializedName("propertyValues") val propertyValues: String,                  // נתוני שדות ללא עריכת אחוזים
	@SerializedName("indexesAndInterests") val indexesAndInterests: String,       // מדדים וריביות [מתעדכן פעם בחודש]
	@SerializedName("averageInterests") val averageInterests: String,              // ריבית ממוצעת לפי מסלולים [מתעדכן פעם בחודש]
	@SerializedName("additionalInterests") val additionalInterests: String,        // תוספת ריבית לפי אחוז מימון
	@SerializedName("cities") val cities: String,                                   // ערים
	@SerializedName("apartmentTypes") val apartmentTypes: String,                  // סוג דירה
	@SerializedName("contactus") val contactUs: String?,                            //בצור קשר
	//@SerializedName("max_percent_of_financing") val maxPercentOfFinancing: String, // אחוז מימון מקסימלי
	//@SerializedName("tax_percent_on_sale") val taxPercentOnSale: String,          // מס במכירה
	//@SerializedName("transfer_tax") val transferTax: String,                      // מס רכישה  [מתעדכן פעם בשנה]
	@SerializedName("mortgagePeriods") val mortgagePeriods: String,                // תקופת משכנתא
	@SerializedName("sms") val sms: String,                                			// SMS
	@SerializedName("picture") val picture: String,                                	// picture
	@SerializedName("onError") val onError: String,                                // בעת שגיאה
)
