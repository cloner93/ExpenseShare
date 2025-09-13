package org.milad.expense_share.presentation.api_model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, String>? = null
)