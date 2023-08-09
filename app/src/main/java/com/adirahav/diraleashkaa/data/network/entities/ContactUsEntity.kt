package com.adirahav.diraleashkaa.data.network.entities
import com.google.gson.annotations.SerializedName

data class ContactUsEntity(
    @SerializedName("mail_to") var mailTo: String? = null,
    @SerializedName("message_types") val messageTypes: List<SpinnerEntity>,
)