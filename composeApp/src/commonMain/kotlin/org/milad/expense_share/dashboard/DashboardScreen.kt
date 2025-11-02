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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import model.Group
import model.Transaction
import org.milad.expense_share.dashboard.groups.GroupDetailScreen
import org.milad.expense_share.ui.AppScreenSize

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    appScreenSize: AppScreenSize,
    groupList: List<Group>,
    onGroupSelected: (Group) -> Unit,
    selectedGroup: Group? = null,
    transactionList: List<Transaction>,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded && navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            val isDetailVisible =
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

            Dashboard(
                isListAndDetailVisible = isListAndDetailVisible,
                groups = groupList,
                onGroupClick = {
                    onGroupSelected(it)

                    scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                    }
                },
                isDetailVisible = !isDetailVisible
            )
        },
        detailPane = {
            val isDetailVisible =
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

            GroupDetailScreen(
                isListAndDetailVisible = isListAndDetailVisible,
                onBackClick = {
                    scope.launch {
                        navigator.navigateBack()
                    }
                },
                expenses = transactionList,
                selectedGroup = selectedGroup,
                isDetailVisible = isDetailVisible,
            ) {}
        },
//        modifier = TODO(),
//        extraPane = TODO(),
        paneExpansionDragHandle = { state ->
            val interactionSource =
                remember { MutableInteractionSource() }
            VerticalDragHandle(
                modifier =
                    Modifier.paneExpansionDraggable(
                        state,
                        LocalMinimumInteractiveComponentSize.current,
                        interactionSource
                    ), interactionSource = interactionSource
            )
        },
        paneExpansionState = rememberPaneExpansionState(navigator.scaffoldValue)
    )
}