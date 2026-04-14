package org.milad.expense_share.domain.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount

@Serializable
data class SettlementTransaction(
    val fromUserId: Int,
    val toUserId: Int,
    val amount: Amount
)