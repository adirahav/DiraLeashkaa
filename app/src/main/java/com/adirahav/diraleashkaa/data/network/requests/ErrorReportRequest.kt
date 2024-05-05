package com.adirahav.diraleashkaa.data.network.requests

data class ErrorReportRequest(
    val type: String,
    val subject: String,
    val message: String?
)
