package org.milad.expense_share.dashboard

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
            Dashboard(
                groups = groupList,
                onGroupClick = onGroupSelected
            )
        },
        detailPane = {
            GroupDetailScreen(
                onBackClick = {},
                onTabSelected = {},
                expenses = transactionList,
                selectedGroup = selectedGroup
            ) {}
        },
//        modifier = TODO(),
//        extraPane = TODO(),
//        paneExpansionDragHandle = { state ->
//            val interactionSource =
//                remember { MutableInteractionSource() }
//            VerticalDragHandle(
//                modifier =
//                    Modifier.paneExpansionDraggable(
//                        state,
//                        LocalMinimumInteractiveComponentSize.current,
//                        interactionSource
//                    ), interactionSource = interactionSource
//            )
//        },
        paneExpansionState = rememberPaneExpansionState(navigator.scaffoldValue)
    )
}