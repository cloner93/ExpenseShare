package org.milad.expense_share.friends.model

import org.milad.expense_share.Amount

data class FriendBalance(
    val totalOwed: Amount = Amount(0),
    val totalOwe: Amount = Amount(0),
    val netBalance: Amount = Amount(0)
) {
    val isOwed: Boolean get() = netBalance.isPositive()
    val isOwing: Boolean get() = netBalance.isNegative()
    val isSettled: Boolean get() = netBalance.isZero()
}