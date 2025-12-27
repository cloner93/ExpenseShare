package org.milad.expense_share.dashboard.group.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.tabs.ExpensesTab
import org.milad.expense_share.dashboard.group.tabs.FeedTab
import org.milad.expense_share.dashboard.group.tabs.MembersTab

@Composable
fun GroupContent(
    modifier: Modifier = Modifier,
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        GroupTabsRow(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(GroupDetailAction.SelectTab(it)) }
        )
        
        Spacer(Modifier.height(16.dp))
        
        when (state.selectedTab) {
            GroupTab.Expenses -> ExpensesTab(state, onAction)
            GroupTab.Members -> MembersTab(state, onAction)
            GroupTab.Feed -> FeedTab()
        }
    }
}