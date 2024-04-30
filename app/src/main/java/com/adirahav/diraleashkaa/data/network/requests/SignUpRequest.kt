package com.adirahav.diraleashkaa.data.network.requests

data class SignUpRequest(
    val platform: String,
    val email: String?,
    val password: String?,
    val fullname: String?,
    val yearOfBirth: Int?,
    val equity: Int?,
    val incomes: Int?,
    val commitments: Int?,
    val termsOfUseAccept: Boolean?,
    val appDeviceId: String,
    val appDeviceType: String,
    val appVersion: String
)
