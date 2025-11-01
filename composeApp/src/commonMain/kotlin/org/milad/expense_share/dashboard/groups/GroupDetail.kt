package org.milad.expense_share.dashboard.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.dashboard.model.ExpenseItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetail() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {

        }
    }
}

@Preview
@Composable
fun GroupDetailPreview() {
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

    GroupDetailScreen(
        onBackClick = {},
        onAddExpenseClick = {},
        expenses = sampleExpenses,
        onTabSelected = {},
        selectedTab = GroupTab.Expenses
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBackClick: () -> Unit,
    selectedTab: GroupTab = GroupTab.Expenses,
    onTabSelected: (GroupTab) -> Unit,
    expenses: List<ExpenseItem>,
    onAddExpenseClick: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            AddExpenseButton(onClick = onAddExpenseClick)
        },
        topBar = {
            TopAppBar(
                title = { Text("Trip to Paris", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { },
                actions = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            GroupTabs(selectedTab, onTabSelected)

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                GroupTab.Expenses -> ExpenseList(expenses)
                GroupTab.Members -> ExpenseList(expenses)
            }
        }
    }
}

enum class GroupTab { Expenses, Members }

@Composable
fun GroupTabs(selectedTab: GroupTab, onTabSelected: (GroupTab) -> Unit) {
    val tabs = listOf(
        GroupTab.Expenses to Icons.Default.ReceiptLong,
        GroupTab.Members to Icons.Default.Person,
    )

    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, (tab, icon) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.name) },
                icon = { Icon(icon, contentDescription = tab.name) },
                selectedContentColor = Color.Red,
                unselectedContentColor = Color.Gray
            )
        }
    }
}



@Composable
fun ExpenseList(expenses: List<ExpenseItem>) {
    val grouped = expenses.groupBy { it.dateLabel }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        grouped.forEach { (label, list) ->
            item {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(list) { item ->
                ExpenseCard(item)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ExpenseCard(item: ExpenseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null)
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                )
            }
        }

        Text(
            text = item.amount,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun AddExpenseButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
//            contentColor = Color.DarkGray
//        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
        Spacer(Modifier.width(8.dp))
        Text("Add Expense")
    }
}
