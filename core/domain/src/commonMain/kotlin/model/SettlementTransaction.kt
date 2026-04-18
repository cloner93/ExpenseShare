package model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount

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