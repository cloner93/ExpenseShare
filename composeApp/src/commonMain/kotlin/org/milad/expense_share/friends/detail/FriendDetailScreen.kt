package org.milad.expense_share.friends.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pmb.common.loading.FullScreenLoading
import org.milad.expense_share.friends.detail.tab.FriendOverviewTab
import org.milad.expense_share.friends.detail.tab.FriendSettlementsTab
import org.milad.expense_share.friends.detail.tab.FriendTransactionsTab
import org.milad.expense_share.friends.model.FriendTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    state: FriendDetailState,
    showBackButton: Boolean = false,
    onAction: (FriendDetailAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.friend.user.username) },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { onAction(FriendDetailAction.NavigateBack) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* منوی تنظیمات */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                FriendBalanceHeader(
                    friend = state.friend.user,
                    balance = state.balance,
                    onSettleUp = { onAction(FriendDetailAction.SettleUp) },
                    onSendReminder = { onAction(FriendDetailAction.SendReminder) }
                )

                FriendTabsRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onAction(FriendDetailAction.SelectTab(it)) }
                )

                when (state.selectedTab) {
                    FriendTab.Overview -> {
                        FriendOverviewTab(
                            sharedGroups = state.sharedGroups,
                            recentTransactions = state.recentTransactions.take(5),
                            onGroupClick = { onAction(FriendDetailAction.OpenGroup(it)) }
                        )
                    }
                    FriendTab.Transactions -> {
                        FriendTransactionsTab(
                            transactions = state.recentTransactions,
                            onGroupClick = { onAction(FriendDetailAction.OpenGroup(it)) }
                        )
                    }
                    FriendTab.Settlements -> {
                        FriendSettlementsTab(
                            settlements = state.pendingSettlements,
                            currentUserId = state.currentUser?.id ?: 0
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            FullScreenLoading()
        }
    }
}