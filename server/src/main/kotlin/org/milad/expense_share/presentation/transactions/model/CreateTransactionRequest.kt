package org.milad.expense_share.presentation.transactions.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String,
    val payers: List<PayerRequest>,
    val shareDetails: ShareDetailsRequest
)

@Serializable
data class PayerRequest(
    val userId: Int,
    val amountPaid: Double
)

@Serializable
data class ShareDetailsRequest(
    val type: ShareType,
    val members: List<ShareMemberRequest> = emptyList()
)

@Serializable
data class ShareMemberRequest(
    val userId: Int,
    val share: Double
)

enum class ShareType { Equal, Percent, Weight, Manual }
