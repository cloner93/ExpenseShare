@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package org.milad.expense_share.dashboard

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import org.milad.expense_share.expenses.AddExpense
import org.milad.expense_share.group.AddGroupScreen

@Composable
fun ExtraPaneContent(
    viewModel: DashboardViewModel,
    content: ExtraPaneContentState,
    onBackClick: () -> Unit,
) {
    when (content) {
        ExtraPaneContentState.AddExpense -> {
            AddExpense(
                users = viewModel.viewState.value.selectedGroup?.members ?: emptyList(),
                onBackClick = onBackClick,
                onAddClick = { name, list ->
                }
            )
        }

        ExtraPaneContentState.AddGroup -> {
            AddGroupScreen(
                listOfFriends = viewModel.viewState.value.friends,
                onBackClick = onBackClick
            ) { name, list ->
                viewModel.handle(DashboardAction.AddGroup(name, list))
            }
        }

        ExtraPaneContentState.AddMember -> {
        }

        ExtraPaneContentState.None -> {
        }
    }
}