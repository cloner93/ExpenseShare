package org.milad.expense_share.presentation.api_model

import kotlinx.serialization.Serializable

@Serializable
data class SuccessResponse<T>(
    val success: Boolean = true,
    val data: T,
)