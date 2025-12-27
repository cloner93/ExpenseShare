package org.milad.expense_share.dashboard.group

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.milad.expense_share.dashboard.group.components.GroupContent
import org.milad.expense_share.dashboard.group.components.GroupFAB
import org.milad.expense_share.dashboard.group.components.GroupTopBar
import org.milad.expense_share.dashboard.group.dialogs.GroupDialogs

@Composable
fun GroupDetailScreen(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit,
){

    Scaffold(
        topBar = {
            GroupTopBar(
                group = state.selectedGroup,
                isOwner = state.isOwner,
                showBackButton = state.showBackButton,
                onAction = onAction
            )
        },
        floatingActionButton = {
            GroupFAB(
                tab = state.selectedTab,
                onAddExpense = { onAction(GroupDetailAction.AddExpense) },
                onAddMember = { onAction(GroupDetailAction.ShowMemberSheet) }
            )
        }
    ) { padding ->
        GroupContent(
            modifier = Modifier.padding(padding),
            state = state,
            onAction = onAction
        )
    }
    GroupDialogs(state = state, onAction = onAction)
}