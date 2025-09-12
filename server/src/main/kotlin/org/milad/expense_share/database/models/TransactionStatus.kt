package org.milad.expense_share.database.models

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
