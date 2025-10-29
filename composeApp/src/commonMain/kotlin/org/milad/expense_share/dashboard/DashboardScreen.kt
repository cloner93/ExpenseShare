package org.milad.expense_share.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
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

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            DashboardList(
                appScreenSize = appScreenSize,
                taskList = taskList,
                selectedTask = { selectedTask = it },
                onAddTask = onAddTask
            )
        },
        detailPane = {
            GroupDetail(
                task = selectedTask
            )
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

@Composable
fun DashboardList(
    appScreenSize: AppScreenSize,
    taskList: List<String>,
    selectedTask: (String) -> Unit,
    onAddTask: (String) -> Unit,
) {

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Master List (Desktop/Tablet View)",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))
                TaskList(
                    taskList = taskList,
                    onTaskSelected = { selectedTask(it) }
                )
                Spacer(Modifier.height(16.dp))
                AddTaskSection(onAddTask)
            }
        }
    }
}

@Composable
fun GroupDetail(task: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (task != null) {
            Column {
                Text("Task Detail", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(task, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "This is the detail content for the selected item. The complexity of this pane scales with the screen size.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text("Select a task from the list.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun TaskList(
    taskList: List<String>,
    onTaskSelected: (String) -> Unit = {}
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