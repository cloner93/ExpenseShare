package org.milad.expense_share.dashboard.group.components

import androidx.compose.runtime.Composable
import org.milad.expense_share.dashboard.AppExtendedButton

@Composable
fun GroupFAB(
    tab: GroupTab,
    onAddExpense: () -> Unit,
    onAddMember: () -> Unit
) {
    when (tab) {
        GroupTab.Expenses -> {
            AppExtendedButton(
                title = "Add Expense",
                onClick = onAddExpense
            )
        }
        GroupTab.Members -> {
            AppExtendedButton(
                title = "Add Member",
                onClick = onAddMember
            )
        }
        GroupTab.Feed -> {
            // No FAB for Feed tab
        }
    }
}