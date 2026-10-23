package com.diego_ramos.croche_api.common.result

data class ResultError(
    val code: String,
    val message: String,
    val details: List<String>? = null
)
