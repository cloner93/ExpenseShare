package org.milad.expense_share.dashboard.group.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.screen.SettlementScreen

@Composable
fun SettlementTab(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SettlementScreen(state, onAction)
    }
}