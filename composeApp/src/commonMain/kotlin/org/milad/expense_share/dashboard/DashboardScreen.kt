package org.milad.expense_share.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.milad.expense_share.dashboard.groups.GroupDetailScreen
import org.milad.expense_share.dashboard.groups.GroupTab
import org.milad.expense_share.dashboard.model.ExpenseItem
import org.milad.expense_share.dashboard.model.GroupUiModel
import org.milad.expense_share.ui.AppScreenSize

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DashboardScreen(
    appScreenSize: AppScreenSize,
    taskList: List<String>,
    onAddTask: (String) -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded && navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded


    var selectedTask by remember { mutableStateOf<String?>(taskList.firstOrNull()) }

    val groups = listOf(
        GroupUiModel("Trip to Paris", 4, "You owe $50", false),
        GroupUiModel("Weekend Getaway", 3, "You are owed $75", true),
        GroupUiModel("Ski Trip", 5, "You owe $100", false)
    )
    val sampleExpenses = listOf(
        ExpenseItem(
            "Dinner at Le Jules Verne",
            "You paid",
            "€150.00",
            Icons.Default.Restaurant,
            "Today"
        ),
        ExpenseItem("Louvre Museum tickets", "You paid", "€50.00", Icons.Default.Museum, "Today"),
        ExpenseItem("Hotel Booking", "You paid", "€300.00", Icons.Default.Hotel, "Yesterday")
    )

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            Dashboard(
                totalOwe = 250.0,
                totalOwed = 150.0,
                groups = groups
            ) {}
        },
        detailPane = {
            GroupDetailScreen(
                onBackClick = {},
                onAddExpenseClick = {},
                expenses = sampleExpenses,
                onTabSelected = {},
                selectedTab = GroupTab.Expenses
            )
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

@Composable
fun TaskList(
    taskList: List<String>,
    onTaskSelected: (String) -> Unit = {},
) {
    LazyColumn(modifier = Modifier.Companion.fillMaxWidth()) {
        items(taskList) { task ->
            ListItem(
                headlineContent = { Text(task) },
                modifier = Modifier.Companion.clickable { onTaskSelected(task) }
            )
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

@Composable
fun AddTaskSection(onAddTask: (String) -> Unit) {
    var newTaskText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = newTaskText,
        onValueChange = { newTaskText = it },
        label = { Text("New Task") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            if (newTaskText.isNotBlank()) {
                onAddTask(newTaskText)
                newTaskText = ""
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Task")
    }
}