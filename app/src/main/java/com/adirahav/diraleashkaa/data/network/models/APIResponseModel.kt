package com.adirahav.diraleashkaa.data.network.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class APIResponseModel : Parcelable {
	@SerializedName("status_code")
	@Expose
	var statusCode: Int? = null

	@SerializedName("data")
	@Expose
	var data: String? = null

	protected constructor(parcel: Parcel) {
		statusCode = parcel.readInt()
		data = parcel.readString()
	}

	constructor() {}

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(parcel: Parcel, i: Int) {
		parcel.writeInt(statusCode ?: 0)
		parcel.writeString(data)
	}

	companion object {
		@SuppressLint("ParcelCreator")
		val CREATOR: Parcelable.Creator<APIResponseModel?> = object : Parcelable.Creator<APIResponseModel?> {
			override fun createFromParcel(parcel: Parcel): APIResponseModel? {
				return APIResponseModel(parcel)
			}

			override fun newArray(size: Int): Array<APIResponseModel?> {
				return arrayOfNulls(size)
			}
		}
	}
}