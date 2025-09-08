package org.milad.expense_share


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

sealed class NavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavItem("home", Icons.Default.Home, "home")
    object Tasks : NavItem("tasks", Icons.Default.List, "tasks")
    object Profile : NavItem("profile", Icons.Default.Person, "profile")
    object Settings : NavItem("settings", Icons.Default.Settings, "setting")
}

val navItems = listOf(
    NavItem.Home,
    NavItem.Tasks,
    NavItem.Profile,
    NavItem.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveApp() {
    var selectedItem by remember { mutableStateOf<NavItem>(NavItem.Home) }
    var taskList by remember { mutableStateOf(listOf<String>()) }

    BoxWithConstraints {
        val screenWidth = maxWidth

        when {
            // Mobile: Bottom Navigation
            screenWidth < 600.dp -> {
                MobileLayout(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    taskList = taskList,
                    onAddTask = { task -> taskList = taskList + task }
                )
            }
            // Tablet: Navigation Rail
            screenWidth < 1200.dp -> {
                TabletLayout(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    taskList = taskList,
                    onAddTask = { task -> taskList = taskList + task }
                )
            }
            // Desktop: Navigation Drawer
            else -> {
                DesktopLayout(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    taskList = taskList,
                    onAddTask = { task -> taskList = taskList + task }
                )
            }
        }
    }
}

@Composable
fun MobileLayout(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    taskList: List<String>,
    onAddTask: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = { onItemSelected(item) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        ContentArea(
            selectedItem = selectedItem,
            taskList = taskList,
            onAddTask = onAddTask,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun TabletLayout(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    taskList: List<String>,
    onAddTask: (String) -> Unit
) {
    Row {
        NavigationRail(
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(Modifier.height(16.dp))
            navItems.forEach { item ->
                NavigationRailItem(
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) }
                )
            }
        }

        ContentArea(
            selectedItem = selectedItem,
            taskList = taskList,
            onAddTask = onAddTask,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun DesktopLayout(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    taskList: List<String>,
    onAddTask: (String) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Open)

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(240.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "first page",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = selectedItem == item,
                        onClick = { onItemSelected(item) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        ContentArea(
            selectedItem = selectedItem,
            taskList = taskList,
            onAddTask = onAddTask,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ContentArea(
    selectedItem: NavItem,
    taskList: List<String>,
    onAddTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedItem) {
        NavItem.Home -> HomeScreen(modifier)
        NavItem.Tasks -> TasksScreen(taskList, onAddTask, modifier)
        NavItem.Profile -> ProfileScreen(modifier)
        NavItem.Settings -> SettingsScreen(modifier)
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "home",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "welcome back",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TasksScreen(
    taskList: List<String>,
    onAddTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "tasks",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedButton(onClick = { showDialog = true }, content = {
                Icon(Icons.Default.Add, contentDescription = "add")
            })

        }

        Spacer(Modifier.height(16.dp))

        if (taskList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "task list is empty",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(taskList) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                task,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "add new task",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        label = { Text("title of task") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                showDialog = false
                                taskText = ""
                            }
                        ) {
                            Text("cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (taskText.isNotBlank()) {
                                    onAddTask(taskText)
                                    taskText = ""
                                    showDialog = false
                                }
                            },
                            enabled = taskText.isNotBlank()
                        ) {
                            Text("add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "profile",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "profile is here",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "setting",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "setting is here",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}