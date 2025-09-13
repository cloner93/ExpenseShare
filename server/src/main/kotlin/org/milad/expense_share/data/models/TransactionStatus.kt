package org.milad.expense_share.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionStatus {
    PENDING,
    APPROVED,
    REJECTED
}
