package org.milad.expense_share.dashboard.groups

import EmptySelectionPlaceholder
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.Group
import model.User
import org.jetbrains.compose.resources.painterResource
import org.milad.expense_share.dashboard.AppExtendedButton
import org.milad.expense_share.dashboard.groups.expense.ConfirmBottomSheet
import org.milad.expense_share.dashboard.groups.expense.ExpenseList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    currentUser: User?,
    isListAndDetailVisible: Boolean,
    isDetailVisible: Boolean,
    selectedGroup: Group?,
    transactionLoading: Boolean,
    transactionError: Throwable?,
    onAddExpenseClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onBackClick: () -> Unit,
    onGroupDeleteClick: (String) -> Unit = {},
    onGroupRenameClick: (String) -> Unit = {},
    onGroupHelpClick: (String) -> Unit = {},
    onApproveTransactionClick: (String) -> Unit = {},
    onRejectTransactionClick: (String) -> Unit = {},
    onEditTransactionClick: (String) -> Unit = {},
    onDeleteTransactionClick: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState()

    var selectedTab by remember { mutableStateOf(GroupTab.Expenses) }
    var isDeleteConfirmVisible by remember { mutableStateOf(false) }

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

                    else -> {
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = { Text(selectedGroup.name) },
                    actions = {
                        DropdownMenu(
                            onDeleteClick = {
                                isDeleteConfirmVisible = true
                            },
                            onRenameClick = {},
                            onHelpClick = {}
                        )
                    },
                    navigationIcon = {
                        if (isDetailVisible && !isListAndDetailVisible) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
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
                    GroupTab.Expenses -> ExpenseList(
                        selectedGroup.transactions,
                        currentUser,
                        selectedGroup,
                        transactionLoading = transactionLoading,
                        transactionError = transactionError,
                        onApproveTransactionClick = { onApproveTransactionClick(it) },
                        onRejectTransactionClick = { onRejectTransactionClick(it) },
                        onEditTransactionClick = { onEditTransactionClick(it) },
                        onDeleteTransactionClick = { onDeleteTransactionClick(it) },
                    )

                    GroupTab.Members -> MemberList(selectedGroup.members)
                    GroupTab.Feed -> FeedScreen()
                }
            }
        }
    else
        EmptySelectionPlaceholder()

    if (isDeleteConfirmVisible) {
        ConfirmBottomSheet(
            title = "Delete the Group.",
            content = "Are you sure you want to delete this group?",
            sheetState = sheetState,
            onConfirmClick = {
                isDeleteConfirmVisible = false
                selectedGroup?.let { onGroupDeleteClick(it.id.toString()) }
            },
        ) {
            isDeleteConfirmVisible = false
        }
    }
}

@Composable
fun FeedScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Feed")
    }
}

@Composable
fun MemberList(members: List<User>) {

    if (members.isNotEmpty()) {
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
    } else {
        EmptyListState()
    }
}

enum class GroupTab { Expenses, Members, Feed }

@Composable
fun GroupTabs(selectedTab: GroupTab, onTabSelected: (GroupTab) -> Unit) {
    val tabs = listOf(
        GroupTab.Expenses to Icons.Default.ReceiptLong,
        GroupTab.Members to Icons.Default.Person,
        GroupTab.Feed to Icons.Default.Chat,
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

@Composable
fun DropdownMenu(
    onDeleteClick: () -> Unit,
    onRenameClick: () -> Unit,
    onHelpClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            colors = MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error,
                trailingIconColor = MaterialTheme.colorScheme.error,
            ),
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = onDeleteClick
        )
        DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            onClick = onRenameClick
        )

        HorizontalDivider()

        DropdownMenuItem(
            text = { Text("Help") },
            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = null) },
            trailingIcon = {
                Icon(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null
                )
            },
            onClick = onHelpClick
        )
    }
}