@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package org.milad.expense_share.dashboard

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.milad.expense_share.expenses.AddExpense
import org.milad.expense_share.group.AddGroupScreen

@Composable
fun ExtraPaneContent(
    viewModel: DashboardViewModel,
    onBackClick: () -> Unit,
) {

    val state by viewModel.viewState.collectAsState()
    val content = state.extraPaneContentState
    when (content) {
        ExtraPaneContentState.AddExpense -> {
            AddExpense(
                allUsers = state.selectedGroup?.members ?: emptyList(),
                onBackClick = onBackClick,
                isLoading = state.extraPaneLoading,
                hasError = state.extraPaneError
            ) { name, amount, desc, payer, shareDetails ->
                viewModel.handle(
                    DashboardAction.AddExpense(
                        name,
                        amount,
                        desc,
                        payer,
                        shareDetails
                    )
                )
            }
        }

        ExtraPaneContentState.AddGroup -> {
            AddGroupScreen(
                listOfFriends = state.friends,
                onBackClick = onBackClick,
                isLoading = state.extraPaneLoading,
                hasError = state.extraPaneError,
            ) { name, list ->
                viewModel.handle(DashboardAction.AddGroup(name, list))
            }
        }

        ExtraPaneContentState.None -> {
        }
    }
}