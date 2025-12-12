package org.milad.expense_share.presentation.transactions.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.User

@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Amount,
    val description: String,
    val payers: List<PayerRequest>,
    val shareDetails: ShareDetailsRequest
)

@Serializable
data class PayerRequest(
    val user: User,
    val amountPaid: Amount
)

@Serializable
data class ShareDetailsRequest(
    val type: ShareType,
    val members: List<ShareMemberRequest> = emptyList()
)

@Serializable
data class ShareMemberRequest(
    val user: User,
    val share: Amount
)

enum class ShareType { Equal, Percent, Weight, Manual }
