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
    val createdAt: Long ,
    val transactionDate: Long,
    var approvedBy: Int? = null
)

@Serializable
enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
