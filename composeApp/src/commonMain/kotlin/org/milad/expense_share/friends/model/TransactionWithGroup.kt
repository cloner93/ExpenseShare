package org.milad.expense_share.friends.model

import model.Group
import model.Transaction
import org.milad.expense_share.Amount

data class TransactionWithGroup(
    val transaction: Transaction,
    val group: Group,
    val myShare: Amount,
    val friendShare: Amount
)