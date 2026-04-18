package org.milad.expense_share.domain.model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount
import org.milad.expense_share.data.models.User

@Serializable
data class SettlementTransaction(
    val id: String,
    val debtor: User,
    val creditor: User,
    val amount: Amount,
    val groupName: String,
    val status: SettlementStatus
)

@Serializable
enum class SettlementStatus {
    PENDING,
    APPROVED,
    YOU_OWE,
    YOU_PAID,
    THEY_PAID,
    YOU_ARE_OWED,
    SETTLED,
    REJECTED,
    THIRD_PARTY
}