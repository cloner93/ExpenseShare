package model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount

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

@Serializable
enum class SettlementStatus {
    PENDING,
    PAID,
    CONFIRMED,
    DISPUTED
}