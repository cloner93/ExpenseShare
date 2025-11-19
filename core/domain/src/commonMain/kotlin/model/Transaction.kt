package model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int,
    val groupId: Int,
    val title: String,
    val amount: Double,
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
    val userId: Int,
    val amountPaid: Double,
)

@Serializable
data class ShareDetailsRequest(
    val type: String,
    val members: List<MemberShareDto>,
)

@Serializable
data class MemberShareDto(
    val userId: Int,
    val share: Double? = null,
)