package org.milad.expense_share.dashboard.group.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.tabs.ExpensesTab
import org.milad.expense_share.dashboard.group.tabs.MembersTab
import org.milad.expense_share.dashboard.group.tabs.SettlementTab

@Composable
fun GroupContent(
    modifier: Modifier = Modifier,
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        GroupTabsRow(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(GroupDetailAction.SelectTab(it)) }
        )

        when (state.selectedTab) {
            GroupTab.Settlement -> SettlementTab(state, onAction)
            GroupTab.Expenses -> ExpensesTab(state, onAction)
            GroupTab.Members -> MembersTab(state, onAction)
        }
    }
}