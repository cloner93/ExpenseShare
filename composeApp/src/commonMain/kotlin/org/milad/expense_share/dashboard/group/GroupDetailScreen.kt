package org.milad.expense_share.dashboard.group

import EmptySelectionPlaceholder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import model.Group
import model.User
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.milad.expense_share.dashboard.AppExtendedButton
import org.milad.expense_share.dashboard.expense.ConfirmBottomSheet
import org.milad.expense_share.dashboard.expense.ExpenseList
import org.milad.expense_share.dashboard.expense.MemberList
import org.milad.expense_share.group.FriendSelectionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    currentUser: User?,
    isListAndDetailVisible: Boolean,
    isDetailVisible: Boolean,
    selectedGroup: Group?,
    listOfFriends: List<User>,
    transactionLoading: Boolean,
    transactionError: Throwable?,
    onAddExpenseClick: () -> Unit,
    onAddMemberClick: () -> Unit,
    onBackClick: () -> Unit,
    onGroupDeleteClick: (String) -> Unit = {},
    onGroupUpdateMember: (List<Int>) -> Unit = {},
    onGroupRenameClick: (String) -> Unit = {},
    onGroupHelpClick: (String) -> Unit = {},
    onApproveTransactionClick: (String) -> Unit = {},
    onRejectTransactionClick: (String) -> Unit = {},
    onEditTransactionClick: (String) -> Unit = {},
    onDeleteTransactionClick: (String) -> Unit = {},
) {
    val viewModel: GroupDetailViewModel = koinViewModel(/*parameters = { parametersOf("groupId") }*/)
    val state by viewModel.viewState.collectAsState()





    val sheetState = rememberModalBottomSheetState()

    var selectedTab by remember { mutableStateOf(GroupTab.Expenses) }
    var isDeleteConfirmVisible by remember { mutableStateOf(false) }
    var isDeleteUseConfirmVisible by remember { mutableStateOf(false) }
    var selectedUserForDelete: User? by remember { mutableStateOf(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var membersError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                            onClick = {
                                showBottomSheet = true
                            }
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
                            isGroupOwner = selectedGroup.ownerId == currentUser?.id,
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
                    GroupTab.Members -> MemberList(
                        selectedGroup.members,
                        currentUser,
                        selectedGroup
                    ) { user ->
                        isDeleteUseConfirmVisible = true
                        selectedUserForDelete = user

                    }
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
    if (isDeleteUseConfirmVisible) {
        ConfirmBottomSheet(
            title = "Delete the Member.",
            content = "Are you sure you want to delete this member?",
            sheetState = sheetState,
            onConfirmClick = {
                isDeleteUseConfirmVisible = false
                selectedUserForDelete?.let { user ->
                    val members = selectedGroup?.members?.filter { it.id != user.id }
                        ?.map { it.id } ?: emptyList()
                    onGroupUpdateMember(members)
                }
            },
        ) {
            isDeleteUseConfirmVisible = false
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            var tempSelected by remember {
                mutableStateOf<List<User>>(selectedGroup?.members ?: emptyList())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Friends",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(listOfFriends.distinctBy { it.id }) { user ->
                        val isUserSelected = tempSelected.any { it.id == user.id }

                        FriendSelectionRow(
                            user = user,
                            isSelected = isUserSelected,
                            onToggle = {
                                tempSelected = if (isUserSelected) {
                                    tempSelected.filter { it.id != user.id }.toMutableList()
                                } else {
                                    (tempSelected + user).toMutableList()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showBottomSheet = false
                            }
                        }
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            // ۳. استخراج آیدی تمام افراد انتخاب شده و ارسال به سرور
                            val finalMemberIds = tempSelected.map { it.id }
                            onGroupUpdateMember(finalMemberIds)

                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) showBottomSheet = false
                            }
                        }
                    ) {
                        Text("Confirm (${tempSelected.size})")
                    }
                }
            }
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
fun DropdownMenu(
    isGroupOwner: Boolean,
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
        if (isGroupOwner) {
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
        }

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