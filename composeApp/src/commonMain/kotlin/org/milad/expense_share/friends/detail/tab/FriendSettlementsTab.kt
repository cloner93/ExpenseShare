package org.milad.expense_share.friends.detail.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import org.milad.expense_share.dashboard.group.components.SettlementListItem
import org.milad.expense_share.dashboard.group.components.SettlementStatus
import org.milad.expense_share.friends.model.SettlementItem

@Composable
fun FriendSettlementsTab(
    settlements: List<SettlementItem>,
    currentUserId: Int
) {
    if (settlements.isEmpty()) {
        EmptyListState()
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(settlements) { settlement ->

            SettlementListItem(
                item = SettlementItem(
                    id = settlement.id,
                    debtor = settlement.debtor,
                    creditor = settlement.creditor,
                    amount = settlement.amount,
                    status = SettlementStatus.YouOwe,
                    groupName = settlement.groupName
                ),
                currentUserId = currentUserId
            )
        }
    }
}
