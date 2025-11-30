package org.milad.expense_share.dashboard.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.Group
import model.Transaction
import model.User
import org.jetbrains.compose.resources.painterResource
import org.milad.expense_share.dashboard.AppExtendedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBackClick: () -> Unit,
    isListAndDetailVisible: Boolean,
    isDetailVisible: Boolean,
    selectedGroup: Group?,
    onAddExpenseClick: () -> Unit,
    onAddMemberClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(GroupTab.Expenses) }

    if (selectedGroup != null)
        Scaffold(
            floatingActionButton = {
                when (selectedTab) {
                    GroupTab.Expenses -> {
                        AppExtendedButton(
                            title = "Add Expense",
                            onClick = onAddExpenseClick
                        )
                    }

                    GroupTab.Members -> {
                        AppExtendedButton(
                            title = "Add Member",
                            onClick = onAddMemberClick
                        )
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            selectedGroup.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (isDetailVisible && !isListAndDetailVisible) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {}
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                GroupTabs(selectedTab) { selectedTab = it }

                Spacer(Modifier.height(16.dp))

                when (selectedTab) {
                    GroupTab.Expenses -> ExpenseList(selectedGroup.transactions)
                    GroupTab.Members -> MemberList(selectedGroup.members)
                }
            }
        }
    else
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Select group.")
            }
        }
}

@Composable
fun MemberList(members: List<User>) {

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(members) { item ->
            MemberRow(
                item,
                onDeleteClick = {}
            )
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
    ) {
        tabs.forEachIndexed { index, (tab, icon) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.name) },
                icon = { Icon(icon, contentDescription = tab.name) },
//                selectedContentColor = MaterialTheme.colorScheme.error,
//                unselectedContentColor = Color.Gray
            )
        }
    }
}

@Composable
fun ExpenseList(expenses: List<Transaction>) {
    val grouped = expenses.groupBy { it.status }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        grouped.forEach { (label, list) ->
            item {
                Text(
                    text = label.name,
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
fun ExpenseCard(item: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .padding(bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CardDefaults.shape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Text(
                text = "$ ${item.amount}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun MemberRow(user: User, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .padding(bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.paris),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CardDefaults.shape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                )
            }
        }
    }
}
