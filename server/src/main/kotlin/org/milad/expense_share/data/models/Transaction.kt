package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable
import org.milad.expense_share.presentation.transactions.model.PayerRequest
import org.milad.expense_share.presentation.transactions.model.ShareDetailsRequest

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
    var approvedBy: Int? = null,
    val payers: List<PayerRequest>? = null,
    val shareDetails: ShareDetailsRequest? = null
)
//io.ktor.serialization.JsonConvertException: Illegal input: Field 'splitDetails' is required for type with serial name 'model.Transaction', but it was missing at path: $.data[13].transactions[0]