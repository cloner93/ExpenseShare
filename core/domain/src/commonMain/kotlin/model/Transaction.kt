package model

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

enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
