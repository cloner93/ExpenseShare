package org.milad.expense_share.dashboard.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pmb.common.theme.AppTheme
import org.milad.expense_share.dashboard.group.GroupDetailAction

@Composable
fun GroupDropdownMenu(
    isGroupOwner: Boolean,
    onAction: (GroupDetailAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        if (isGroupOwner) {
            DropdownMenuItem(
                colors = MenuDefaults.itemColors(
                    textColor = AppTheme.colors.error,
                    leadingIconColor = AppTheme.colors.error,
                ),
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    expanded = false
                    onAction(GroupDetailAction.ShowDeleteGroupDialog)
                }
            )

            DropdownMenuItem(
                text = { Text("Rename") },
                enabled = false,
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = {
                    expanded = false
                    // TODO: Implement rename
                }
            )

            HorizontalDivider()
        }

        DropdownMenuItem(
            text = { Text("Help") },
            enabled = false,
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = null) },
            trailingIcon = {
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null
                )
            },
            onClick = {
                expanded = false
                // TODO: Implement help
            }
        )
    }
}