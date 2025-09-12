package org.milad.expense_share.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int,
    val groupId: Int,
    val title: String,
    val amount: Double,
    val description: String,
    val createdBy: Int,
    var status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val transactionDate: Long = System.currentTimeMillis(),
    var approvedBy: Int? = null
)