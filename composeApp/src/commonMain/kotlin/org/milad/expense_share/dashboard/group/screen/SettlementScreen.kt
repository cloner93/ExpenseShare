package org.milad.expense_share.dashboard.group.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailState
import org.milad.expense_share.dashboard.group.components.FakeDate
import org.milad.expense_share.dashboard.group.components.FakeDate.mockSettlementItems
import org.milad.expense_share.dashboard.group.components.SettlementListItem
import org.milad.expense_share.showSeparate

@Composable
fun SettlementScreen(
    state: GroupDetailState,
    onAction: (GroupDetailAction) -> Unit,
) {
    Column {
        if (mockSettlementItems.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    BalanceSummaryRow(
                        totalGroupSpend = Amount(state.selectedGroup.transactions.sumOf { it.amount.value }),
                        totalBalance = Amount(mockSettlementItems.sumOf { it.amount.value })
                    )
                }
                items(mockSettlementItems) { item ->
                    SettlementListItem(item = item, currentUserId = state.currentUser.id)
                }
            }
        } else {
            EmptyListState()
        }
    }
}

@Preview
@Composable
fun PreviewSettlementScreen() {
    AppTheme(content = {
        Surface(color = AppTheme.colors.background) {
            SettlementScreen(
                state = GroupDetailState(
                    currentUser = FakeDate.userMilad,
                    selectedGroup = FakeDate.selectedGroup
                ),
                onAction = {}
            )
        }
    })
}

@Composable
fun BalanceSummaryRow(
    modifier: Modifier = Modifier,
    totalGroupSpend: Amount,
    totalBalance: Amount,
) {
    val containerColor = if (totalBalance.isNegative())
        AppTheme.colors.errorContainer
    else
        AppTheme.colors.successContainer

    val textColor = if (totalBalance.isNegative())
        AppTheme.colors.onErrorContainer
    else
        AppTheme.colors.onSuccessContainer

    Surface {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BalanceCard(
                modifier = Modifier.weight(1f),
                title = "Total Group Spend",
                amount = totalGroupSpend,
                backgroundColor = AppTheme.colors.surfaceVariant,
                textColor = AppTheme.colors.onSurfaceVariant,
                icon = Icons.Default.Wallet
            )

            BalanceCard(
                modifier = Modifier.weight(1f),
                title = "Your Total Balance",
                amount = totalBalance,
                backgroundColor = containerColor,
                textColor = textColor,
                icon = Icons.Outlined.Wallet
            )
        }
    }
}

@Composable
fun BalanceCard(
    modifier: Modifier,
    title: String,
    amount: Amount,
    backgroundColor: Color,
    textColor: Color,
    icon: ImageVector,
) {
    Column(
        modifier = modifier
            .clip(CardDefaults.shape)
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$ ${amount.showSeparate()}",
            style = AppTheme.typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = AppTheme.typography.bodyMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
    }
}