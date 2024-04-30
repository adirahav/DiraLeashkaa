package com.adirahav.diraleashkaa.data.network.requests

data class LoginRequest(
    val platform: String,
    val email: String?,
    val password: String?,
    val appDeviceType: String,
    val appVersion: String
)
