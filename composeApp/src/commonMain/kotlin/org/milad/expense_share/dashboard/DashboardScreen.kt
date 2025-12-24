package org.milad.expense_share.dashboard

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.dashboard.groups.GroupDetailScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    navLayoutType: NavigationSuiteType,
    viewModel: DashboardViewModel = koinViewModel(),
    shouldOpenAddGroup: Boolean = false,
    onAddGroupConsumed: () -> Unit = {},
) {
    val state by viewModel.viewState.collectAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvent.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> {
                }

                DashboardEvent.GroupCreatedSuccessful -> scope.launch { navigator.navigateBack() }
                DashboardEvent.ExtraPaneSuccessful -> {
                    scope.launch {
                        navigator.navigateBack()
                    }

                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.None))
                }
            }
        }
    }

    LaunchedEffect(shouldOpenAddGroup) {
        if (shouldOpenAddGroup) {
            viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddGroup))

            navigator.navigateTo(ListDetailPaneScaffoldRole.Extra)

            onAddGroupConsumed()
        }
    }

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
    val isDetailVisible =
        state.isDetailVisible || navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Dashboard(
                navLayoutType = navLayoutType,
                currentUser = state.currentUser,
                groups = state.groups,
                onGroupClick = { group ->
                    viewModel.handle(DashboardAction.SelectGroup(group))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                },
                selectedGroup = state.selectedGroup,
                isListAndDetailVisible = isListAndDetailVisible,
                isDetailVisible = !isDetailVisible,
                totalOwed = state.totalOwed,
                totalOwe = state.totalOwe,
                onAddGroupClick = {
                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddGroup))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                }
            )
        },
        detailPane = {
            GroupDetailScreen(
                currentUser = state.currentUser,
                isListAndDetailVisible = isListAndDetailVisible,
                isDetailVisible = isDetailVisible,
                selectedGroup = state.selectedGroup,
                transactionLoading = state.transactionLoading,
                transactionError = state.transactionError,
                onBackClick = {
                    viewModel.handle(DashboardAction.NavigateBack)
                    scope.launch { navigator.navigateBack() }
                },
                onAddExpenseClick = {
                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddExpense))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                },
                onAddMemberClick = {
                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddMember))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                },
                onGroupDeleteClick = {
                    viewModel.handle(DashboardAction.DeleteGroup(it))
                },
                onGroupRenameClick = {

                },
                onGroupHelpClick = {

                },
                onApproveTransactionClick = {
                    viewModel.handle(DashboardAction.ApproveTransaction(it))
                },
                onRejectTransactionClick = {
                    viewModel.handle(DashboardAction.RejectTransaction(it))
                },
                onEditTransactionClick = {

                },
                onDeleteTransactionClick = {
                    viewModel.handle(DashboardAction.DeleteTransaction(it))
                },
            )
        },
        extraPane = {
            ExtraPaneContent(
                viewModel = viewModel
            ) {
                scope.launch {
                    navigator.navigateBack()
                }

                viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.None))
            }
        },
    )
}