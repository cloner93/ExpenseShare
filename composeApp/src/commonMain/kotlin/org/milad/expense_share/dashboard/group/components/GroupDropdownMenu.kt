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
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.delete
import expenseshare.composeapp.generated.resources.help
import expenseshare.composeapp.generated.resources.more_options
import expenseshare.composeapp.generated.resources.rename
import org.jetbrains.compose.resources.stringResource
import org.milad.expense_share.dashboard.group.GroupDetailAction

@Composable
fun GroupDropdownMenu(
    isGroupOwner: Boolean,
    onAction: (GroupDetailAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.more_options))
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
                text = { Text(stringResource(Res.string.delete)) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    expanded = false
                    onAction(GroupDetailAction.ShowDeleteGroupDialog)
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(Res.string.rename)) },
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
            text = { Text(stringResource(Res.string.help)) },
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