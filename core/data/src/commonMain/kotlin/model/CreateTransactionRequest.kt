package model

import kotlinx.serialization.Serializable


@Serializable
data class CreateTransactionRequest(
    val title: String,
    val amount: Double,
    val description: String?,
    val payers: List<PayerDto>?,
    val splitDetails: SplitDetailsDto?,
)