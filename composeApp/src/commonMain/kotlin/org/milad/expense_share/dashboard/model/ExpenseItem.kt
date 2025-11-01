package org.milad.expense_share.dashboard.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ExpenseItem(
    val title: String,
    val subtitle: String,
    val amount: String,
    val icon: ImageVector,
    val dateLabel: String,
)