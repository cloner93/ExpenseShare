package model

import kotlinx.serialization.Serializable


@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String?,
    val payers: List<PayerDto>?,
    val shareDetails: ShareDetailsRequest?,
)
@Serializable
data class ShareMemberRequest(
    val userId: Int,
    val share: Double
)
