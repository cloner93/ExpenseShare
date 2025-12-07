@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.dashboard.groups.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import model.Group
import model.Transaction
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.expenses.AnimatedLoadingButton

@Composable
fun ExpenseList(
    expenses: List<Transaction>,
    currentUser: User?,
    selectedGroup: Group,
    transactionLoading: Boolean,
    transactionError: Throwable?,
    onApproveTransactionClick: (String) -> Unit = {},
    onRejectTransactionClick: (String) -> Unit = {},
    onEditTransactionClick: (String) -> Unit = {},
    onDeleteTransactionClick: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState()
    var currentTrx by remember { mutableStateOf<Transaction?>(null) }
    var currentTrxActionLoading by remember { mutableStateOf<TrxActions?>(null) }
    var isApproveConfirmVisible by remember { mutableStateOf(false) }
    var isRejectConfirmVisible by remember { mutableStateOf(false) }
    var isDeleteConfirmVisible by remember { mutableStateOf(false) }

    val grouped = expenses.groupBy { it.status }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    if (expenses.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            grouped.forEach { (label, list) ->
                item {
                    Text(
                        text = label.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(list) { item ->
                    ExpandableExpenseCard(
                        item,
                        currentUser,
                        isExpanded = selectedTransaction == item,
                        isUserAdminOfGroup = selectedGroup.ownerId == currentUser?.id,
                        currentTrxActionLoading = currentTrxActionLoading,
                        isLoading = transactionLoading,
                        onExpandClick = {
                            if (selectedTransaction == it) {
                                selectedTransaction = null
                            } else
                                selectedTransaction = it
                        },
                        onApproveTransactionClick = {
                            currentTrx = it
                            currentTrxActionLoading = TrxActions.Approve
                            isApproveConfirmVisible = true
                        },
                        onRejectTransactionClick = {
                            currentTrx = it
                            currentTrxActionLoading = TrxActions.Reject
                            isRejectConfirmVisible = true
                        },
                        onEditTransactionClick = {
//                            currentTrx = it
//                            currentTrxActionLoading = TrxActions.Dele
//                            isDeleteConfirmVisible = true
                        },
                        onDeleteTransactionClick = {
                            currentTrx = it
                            currentTrxActionLoading = TrxActions.Delete
                            isDeleteConfirmVisible = true
                        },
                        onMoreMenuTransactionClick = {},
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    } else {
        EmptyListState()
    }

    if (isApproveConfirmVisible) {
        ConfirmBottomSheet(
            title = "Approve the TRX?",
            content = "Are you sure you want to approve this transaction?",
            sheetState = sheetState,
            onConfirmClick = {
                currentTrx?.let {
                    onApproveTransactionClick(it.id.toString())
                }
                isApproveConfirmVisible = false
            },
        ) {
            currentTrx = null
            isApproveConfirmVisible = false
            currentTrxActionLoading = null
        }
    }

    if (isRejectConfirmVisible) {
        ConfirmBottomSheet(
            title = "Reject the TRX?",
            content = "Are you sure you want to Reject this transaction?",
            sheetState = sheetState,
            onConfirmClick = {
                currentTrx?.let {
                    onRejectTransactionClick(it.id.toString())
                }
                isRejectConfirmVisible = false
            },
        ) {
            currentTrx = null
            isRejectConfirmVisible = false
            currentTrxActionLoading = null
        }
    }

    if (isDeleteConfirmVisible) {
        ConfirmBottomSheet(
            title = "Delete the TRX?",
            content = "Are you sure you want to delete this transaction?",
            sheetState = sheetState,
            onConfirmClick = {
                currentTrx?.let {
                    onDeleteTransactionClick(it.id.toString())
                }
                isDeleteConfirmVisible = false
            },
        ) {
            currentTrx = null
            isDeleteConfirmVisible = false
            currentTrxActionLoading = null
        }
    }
}

enum class TrxActions {
    Approve,
    Reject,
    Delete
}

@Composable
fun ConfirmBottomSheet(
    title: String,
    content: String,
    sheetState: SheetState,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissClick,
        sheetState = sheetState,

        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = content,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismissClick,
                ) {
                    Text("Cancel")
                }

                AnimatedLoadingButton(
                    text = "Confirm",
                    loading = false,
                    onClick = onConfirmClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ConfirmBottomSheetPreview() {
    AppTheme() {

        val sheetState = rememberModalBottomSheetState()

        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
        ) {
            ConfirmBottomSheet(
                "Approve the TRX?",
                "Are you sure you want to approve this transaction?",
                sheetState = sheetState,
                onConfirmClick = {},
            ) {}
        }
    }
}