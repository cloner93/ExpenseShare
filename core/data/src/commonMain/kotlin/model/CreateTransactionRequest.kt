package model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount


@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Amount,
    val description: String?,
    val payers: List<PayerDto>?,
    val shareDetails: ShareDetailsRequest?,
)
@Serializable
data class ShareMemberRequest(
    val userId: Int,
    val share: Amount
)
