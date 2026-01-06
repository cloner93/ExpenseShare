package org.milad.expense_share.dashboard.group.tabs

import androidx.compose.runtime.Composable
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.screen.ExpenseList

@Composable
fun ExpensesTab(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit
) {
    val group = state.selectedGroup
    
    ExpenseList(
        expenses = group.transactions,
        currentUser = state.currentUser,
        selectedGroup = group,
        transactionLoading = state.transactionLoading,
        transactionError = state.transactionError,
        onApproveTransactionClick = { txId ->
            onAction(GroupDetailAction.ApproveTransaction(txId))
        },
        onRejectTransactionClick = { txId ->
            onAction(GroupDetailAction.RejectTransaction(txId))
        },
        onEditTransactionClick = { txId ->
            onAction(GroupDetailAction.EditTransaction(txId))
        },
        onDeleteTransactionClick = { txId ->
            onAction(GroupDetailAction.DeleteTransaction(txId))
        }
    )
}