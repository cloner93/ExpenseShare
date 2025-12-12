package model

import kotlinx.serialization.Serializable
import org.milad.expense_share.Amount

@Serializable
data class Transaction(
    val id: Int,
    val groupId: Int,
    val title: String,
    val amount: Amount,
    val description: String,
    val createdBy: Int,
    var status: TransactionStatus,
    val createdAt: Long,
    val transactionDate: Long,
    var approvedBy: Int? = null,
    val payers: List<PayerDto>,
    val shareDetails: ShareDetailsRequest,
)

@Serializable
enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class PayerDto(
    val user: User,
    val amountPaid: Amount,
)

@Serializable
data class ShareDetailsRequest(
    val type: String,
    val members: List<MemberShareDto>,
)

@Serializable
data class MemberShareDto(
    val user: User,
    val share: Amount = Amount(0),
)