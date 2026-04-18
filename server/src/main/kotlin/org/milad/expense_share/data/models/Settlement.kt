package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount
import org.milad.expense_share.domain.model.SettlementStatus

@Serializable
data class Settlement(
    val id: Int,
    val groupId: Int,
    val debtor: User,
    val creditor: User,
    val amount: Amount,
    val status: SettlementStatus,
    val createdAt: Long,
    val updatedAt: Long,
)