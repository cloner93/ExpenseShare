package org.milad.expense_share.dashboard.group.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import org.milad.expense_share.dashboard.group.components.FakeDate.mockSettlementItems
import org.milad.expense_share.dashboard.group.components.SettlementListItem

@Composable
fun SettlementScreen() {

    if (mockSettlementItems.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mockSettlementItems) { item ->
                SettlementListItem(item = item, currentUserId = 1)
            }
        }
    } else {
        EmptyListState()
    }
}