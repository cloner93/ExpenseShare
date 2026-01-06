package org.milad.expense_share.dashboard.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GroupTabsRow(
    selectedTab: GroupTab,
    onTabSelected: (GroupTab) -> Unit,
) {
    val tabs = listOf(
        TabData(GroupTab.Settlement, Icons.Default.Wallet, "Settlement"),
        TabData(GroupTab.Expenses, Icons.Default.ReceiptLong, "Expenses"),
        TabData(GroupTab.Members, Icons.Default.Person, "Members"),
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
    val tab: GroupTab,
    val icon: ImageVector,
    val label: String,
)

enum class GroupTab { Settlement, Expenses, Members }