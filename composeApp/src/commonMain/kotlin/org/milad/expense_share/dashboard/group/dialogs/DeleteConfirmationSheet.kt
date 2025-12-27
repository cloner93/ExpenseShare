package org.milad.expense_share.dashboard.group.dialogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import org.milad.expense_share.dashboard.expense.ConfirmBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationSheet(
    title: String,
    content: String,
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState()

    ConfirmBottomSheet(
        title = title,
        content = content,
        sheetState = sheetState,
        onConfirmClick = {
            onConfirm()
            onDismiss()
        },
        onDismissClick = onDismiss
    )
}