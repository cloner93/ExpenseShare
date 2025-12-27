package org.milad.expense_share.dashboard.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import model.Group
import org.milad.expense_share.dashboard.group.GroupDetailAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupTopBar(
    group: Group,
    isOwner: Boolean,
    showBackButton: Boolean,
    onAction: (GroupDetailAction) -> Unit
) {
    TopAppBar(
        title = { Text(group.name) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { onAction(GroupDetailAction.NavigateBack) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            GroupDropdownMenu(
                isGroupOwner = isOwner,
                onAction = onAction
            )
        }
    )
}