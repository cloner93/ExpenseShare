package org.milad.expense_share.dashboard

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.dashboard.groups.GroupDetailScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
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
            }
        }
    }

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
    val isDetailVisible =
        state.isDetailVisible || navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Dashboard(
                groups = state.groups,
                onGroupClick = { group ->
                    viewModel.handle(DashboardAction.SelectGroup(group))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                },
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
                onBackClick = {
                    viewModel.handle(DashboardAction.NavigateBack)
                    scope.launch { navigator.navigateBack() }
                },
                isListAndDetailVisible = isListAndDetailVisible,
                isDetailVisible = isDetailVisible,
                selectedGroup = state.selectedGroup,
                onAddExpenseClick = {
                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddExpense))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                },
                onAddMemberClick = {
                    viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddMember))
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                },
            )
        },
        extraPane = {
            ExtraPaneContent(
                viewModel = viewModel,
                content = state.extraPaneContentState
            ) {
                scope.launch {
                    navigator.navigateBack()
                }

                viewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.None))
            }
        },
        paneExpansionDragHandle = { expansionState ->
            val interactionSource = remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier = Modifier.paneExpansionDraggable(
                    expansionState,
                    LocalMinimumInteractiveComponentSize.current,
                    interactionSource
                ),
                interactionSource = interactionSource
            )
        },
        paneExpansionState = rememberPaneExpansionState(navigator.scaffoldValue)
    )
}