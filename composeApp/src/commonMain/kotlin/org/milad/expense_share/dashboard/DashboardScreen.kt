package org.milad.expense_share.dashboard

import EmptySelectionPlaceholder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.pmb.common.loading.FullScreenLoading
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.milad.expense_share.dashboard.group.GroupDetailAction
import org.milad.expense_share.dashboard.group.GroupDetailEvent
import org.milad.expense_share.dashboard.group.GroupDetailScreen
import org.milad.expense_share.dashboard.group.GroupDetailViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    navLayoutType: NavigationSuiteType,
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    shouldOpenAddGroup: Boolean = false,
    onAddGroupConsumed: () -> Unit = {},
) {
    val state by dashboardViewModel.viewState.collectAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dashboardViewModel.viewEvent.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> {
                }

                DashboardEvent.GroupCreatedSuccessful -> scope.launch { navigator.navigateBack() }
                DashboardEvent.ExtraPaneSuccessful -> {
                    scope.launch {
                        navigator.navigateBack()
                    }

                    dashboardViewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.None))
                }
            }
        }
    }

    LaunchedEffect(shouldOpenAddGroup) {
        if (shouldOpenAddGroup) {
            dashboardViewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.AddGroup))

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
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Dashboard(
                    navLayoutType = navLayoutType,
                    currentUser = state.currentUser,
                    groups = state.groups,
                    onGroupClick = { group ->
                        dashboardViewModel.handle(DashboardAction.SelectGroup(group))
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                    },
                    selectedGroup = state.selectedGroup,
                    isListAndDetailVisible = isListAndDetailVisible,
                    isDetailVisible = !isDetailVisible,
                    totalOwed = state.totalOwed,
                    totalOwe = state.totalOwe,
                    onAddGroupClick = {
                        dashboardViewModel.handle(
                            DashboardAction.ShowExtraPane(
                                ExtraPaneContentState.AddGroup
                            )
                        )
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                    }
                )
                if (state.listPaneLoading)
                    FullScreenLoading()
            }
        },
        detailPane = {
            state.selectedGroup?.let { selectedGroup ->
                val viewModel: GroupDetailViewModel = koinViewModel(
                    key = "group_${selectedGroup.id}",
                    parameters = {
                        parametersOf(
                            selectedGroup,
                            state.currentUser,
                            isListAndDetailVisible,
                            isDetailVisible
                        )
                    }
                )
                LaunchedEffect(selectedGroup) {
                    viewModel.handle(GroupDetailAction.UpdateGroup(selectedGroup))
                }

                LaunchedEffect(Unit) {
                    viewModel.viewEvent.collect { event ->
                        when (event) {
                            is GroupDetailEvent.NavigateBack -> {
                                dashboardViewModel.handle(DashboardAction.NavigateBack)
                                scope.launch { navigator.navigateBack() }
                            }
                        }
                    }
                }

                GroupDetailScreen(
                    state = viewModel.viewState.collectAsState().value,
                    onAction = viewModel::handle,
                    onExtraAction = {
                        dashboardViewModel.handle(DashboardAction.ShowExtraPane(it))
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
                    }
                )
            } ?: run {
                EmptySelectionPlaceholder()
            }
        },
        extraPane = {
            ExtraPaneContent(
                viewModel = dashboardViewModel
            ) {
                scope.launch {
                    navigator.navigateBack()
                }

                dashboardViewModel.handle(DashboardAction.ShowExtraPane(ExtraPaneContentState.None))
            }
        },
    )
}