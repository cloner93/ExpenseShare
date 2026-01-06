package org.milad.expense_share.dashboard.group.tabs

import androidx.compose.runtime.Composable
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.screen.MemberList

@Composable
fun MembersTab(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit
) {
    val group = state.selectedGroup
    
    MemberList(
        members = group.members,
        currentUser = state.currentUser,
        selectedGroup = group,
        onDeleteClick = { user ->
            onAction(GroupDetailAction.ShowDeleteMemberDialog(user))
        }
    )
}