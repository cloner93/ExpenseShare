package org.milad.expense_share.friends.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.milad.expense_share.friends.model.FriendTab

@Composable
fun FriendTabsRow(
    selectedTab: FriendTab,
    onTabSelected: (FriendTab) -> Unit
) {
    val tabs = listOf(
        TabData(FriendTab.Overview, Icons.Default.Dashboard, "Overview"),
        TabData(FriendTab.Transactions, Icons.Default.ReceiptLong, "Transactions"),
        TabData(FriendTab.Settlements, Icons.Default.Wallet, "Settlements")
    )

    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.tab == selectedTab }
    ) {
        tabs.forEach { tabData ->
            Tab(
                selected = selectedTab == tabData.tab,
                onClick = { onTabSelected(tabData.tab) },
                text = { Text(tabData.label) },
                icon = {
                    Icon(
                        imageVector = tabData.icon,
                        contentDescription = tabData.label
                    )
                }
            )
        }
    }
}

private data class TabData(
    val tab: FriendTab,
    val icon: ImageVector,
    val label: String
)