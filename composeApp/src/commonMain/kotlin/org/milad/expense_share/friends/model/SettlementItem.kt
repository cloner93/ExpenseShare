package org.milad.expense_share.friends.model

import model.User
import org.milad.expense_share.Amount
import org.milad.expense_share.dashboard.group.components.SettlementStatus

data class SettlementItem(
    val id: String,
    val debtor: User,
    val creditor: User,
    val amount: Amount,
    val groupName: String,
    val status: SettlementStatus
)