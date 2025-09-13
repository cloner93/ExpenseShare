package org.milad.expense_share.presentation.groups.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String
)