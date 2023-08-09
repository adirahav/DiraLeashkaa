package com.adirahav.diraleashkaa.data.network.entities

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SpinnerEntity(
    var key: String? = null,
    var value: String? = null,
    var default: Boolean? = null
)