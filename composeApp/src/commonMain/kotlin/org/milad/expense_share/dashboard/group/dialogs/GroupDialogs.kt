package org.milad.expense_share.dashboard.group.dialogs

import androidx.compose.runtime.Composable
import org.milad.expense_share.dashboard.group.DialogState
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState

@Composable
fun GroupDialogs(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit
) {
    when (val dialogState = state.dialogState) {
        is DialogState.None -> { /* No dialog */ }
        
        is DialogState.DeleteGroup -> {
            DeleteConfirmationSheet(
                title = "Delete the Group",
                content = "Are you sure you want to delete this group? This action cannot be undone.",
                isVisible = true,
                onConfirm = {
                    onAction(GroupDetailAction.DeleteGroup(state.selectedGroup.id))
                },
                onDismiss = { onAction(GroupDetailAction.DismissDialog) }
            )
        }
        
        is DialogState.DeleteMember -> {
            DeleteConfirmationSheet(
                title = "Remove Member",
                content = "Are you sure you want to remove ${dialogState.user.username} from this group?",
                isVisible = true,
                onConfirm = {
                    val remainingMemberIds = state.selectedGroup.members
                        .filter { it.id != dialogState.user.id }
                        .map { it.id }
                    
                    onAction(GroupDetailAction.UpdateMembers(remainingMemberIds))
                },
                onDismiss = { onAction(GroupDetailAction.DismissDialog) }
            )
        }
        
        is DialogState.MemberSelection -> {
            MemberSelectionSheet(
                visible = true,
                friends = state.friends,
                currentMembers = state.selectedGroup.members,
                onConfirm = { memberIds ->
                    onAction(GroupDetailAction.UpdateMembers(memberIds))
                },
                onDismiss = { onAction(GroupDetailAction.DismissDialog) }
            )
        }
    }
}