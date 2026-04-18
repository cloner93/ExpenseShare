package org.milad.expense_share.friends.detail.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import model.SettlementStatus
import model.SettlementTransaction
import org.milad.expense_share.dashboard.group.components.SettlementListItem

@Composable
fun FriendSettlementsTab(
    settlements: List<SettlementTransaction>,
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
                item = SettlementTransaction(
                    id = settlement.id,
                    debtor = settlement.debtor,
                    creditor = settlement.creditor,
                    amount = settlement.amount,
                    status = SettlementStatus.YOU_OWE,
                    groupName = settlement.groupName
                ),
                currentUserId = currentUserId
            )
        }
    }
}
