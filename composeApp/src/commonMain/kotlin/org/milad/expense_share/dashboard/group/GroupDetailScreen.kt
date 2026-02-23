package org.milad.expense_share.dashboard.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pmb.common.loading.FullScreenLoading
import org.milad.expense_share.dashboard.ExtraPaneContentState
import org.milad.expense_share.dashboard.group.components.GroupContent
import org.milad.expense_share.dashboard.group.components.GroupFAB
import org.milad.expense_share.dashboard.group.components.GroupTopBar
import org.milad.expense_share.dashboard.group.dialogs.GroupDialogs

@Composable
fun GroupDetailScreen(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit,
    onExtraAction: (ExtraPaneContentState) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                    onAddExpense = { onExtraAction(ExtraPaneContentState.AddExpense) },
                    onAddMember = { onAction(GroupDetailAction.ShowMemberSheet) }
                )
            }
        ) { padding ->
            GroupContent(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding()),
                state = state,
                onAction = onAction
            )
        }
        GroupDialogs(state = state, onAction = onAction)

        if (state.isLoading)
            FullScreenLoading()
    }
}