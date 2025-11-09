package org.milad.expense_share.dashboard

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.milad.expense_share.dashboard.groups.GroupDetailScreen
import org.milad.expense_share.group.AddGroupScreen
import org.milad.expense_share.ui.AppScreenSize

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    appScreenSize: AppScreenSize,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.viewEvent.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> {
                }
            }
        }
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
    val isDetailVisible =
        state.isDetailVisible || navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column {
                CircularProgressIndicator()
                Button(
                    content = { Text("Load") },
                    onClick = {
                        viewModel.handle(DashboardAction.LoadData)
                    }
                )
            }
        }
        return
    }

    if (state.error != null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column {
                state.error?.let {
                    Text(it.cause?.message ?: "ERROR!")
                }

                Button(
                    content = { Text("Load") },
                    onClick = {
                        viewModel.handle(DashboardAction.LoadData)
                    }
                )
            }
        }
        return
    }

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
            ) {
                scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra) }
            }
        },
        detailPane = {
            GroupDetailScreen(
                isListAndDetailVisible = isListAndDetailVisible,
                onBackClick = {
                    viewModel.handle(DashboardAction.NavigateBack)
                    scope.launch { navigator.navigateBack() }
                },
                expenses = state.transactions,
                selectedGroup = state.selectedGroup,
                isDetailVisible = isDetailVisible,
            ) {}
        },
        extraPane = {
            AddGroupScreen(onBackClick = { scope.launch { navigator.navigateBack() } }) { name, list ->
                viewModel.handle(DashboardAction.AddGroup(name, list))
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