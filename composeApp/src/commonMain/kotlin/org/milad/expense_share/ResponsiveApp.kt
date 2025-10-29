package org.milad.expense_share

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.milad.expense_share.dashboard.DashboardScreen
import org.milad.expense_share.ui.AppScaffold
import org.milad.expense_share.ui.NavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveApp() {
    var selectedItem by remember { mutableStateOf(NavItem.Dashboard) }
    var taskList by remember { mutableStateOf(listOf("Task 1", "Task 2")) }

    AppScaffold(
        selectedItem = selectedItem,
        onItemSelected = { selectedItem = it }
    ) { appScreenSize ->
        when (selectedItem) {
            NavItem.Dashboard -> DashboardScreen(
                appScreenSize = appScreenSize,
                taskList = taskList,
                onAddTask = { task -> taskList = taskList + task }
            )

            NavItem.Settings -> Text("Settings Screen Content")
        }
    }
}