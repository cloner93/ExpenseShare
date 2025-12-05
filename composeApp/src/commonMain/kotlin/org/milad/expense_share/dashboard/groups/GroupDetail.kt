package org.milad.expense_share.dashboard.groups

import EmptySelectionPlaceholder
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.pmb.common.ui.emptyState.EmptyListState
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.Group
import model.Transaction
import model.User
import org.jetbrains.compose.resources.painterResource
import org.milad.expense_share.chat.ChatScreen
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

                    else -> {
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
                    GroupTab.Chat -> ChatScreen()
                }
            }
        }
    else
        EmptySelectionPlaceholder()
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

enum class GroupTab { Expenses, Members,Chat }

@Composable
fun GroupTabs(selectedTab: GroupTab, onTabSelected: (GroupTab) -> Unit) {
    val tabs = listOf(
        GroupTab.Expenses to Icons.Default.ReceiptLong,
        GroupTab.Members to Icons.Default.Person,
        GroupTab.Chat to Icons.Default.Chat,
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

    if (expenses.isNotEmpty()) {
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
                    ExpandableExpenseCard(
                        item,
                        currentUserId = 0,
                        onEditClick = {},
                        onSettleUpClick = {}
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    } else {
        EmptyListState()
    }
}

@Composable
fun ExpandableExpenseCard(
    transaction: Transaction,
    currentUserId: Int,
    onEditClick: () -> Unit,
    onSettleUpClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(CardDefaults.shape)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val payerId = transaction.payers.maxByOrNull { it.amountPaid }?.userId ?: 0
                    val payerName = if (payerId == currentUserId) "You" else "User $payerId"

                    Text(
                        text = "$payerName paid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${transaction.amount}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val myShare =
                        transaction.shareDetails.members.find { it.userId == currentUserId }?.share
                            ?: 0.0
                    val iPaid =
                        transaction.payers.find { it.userId == currentUserId }?.amountPaid ?: 0.0
                    val net = iPaid - myShare

                    if (net < 0) {
                        Text(
                            text = "You owe $${kotlin.math.abs(net)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (net > 0) {
                        Text(
                            text = "You lent $${kotlin.math.abs(net)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                SectionRow(title = "Payer(s) & Amounts", value = "Remains")

                transaction.payers.forEach { payer ->
                    val name = if (payer.userId == currentUserId) "You" else "User ${payer.userId}"
                    DetailRow(
                        label = "$name: $${payer.amountPaid}",
                        value = "+$${payer.amountPaid / 2}",
                        isBold = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionRow(title = "Split Breakdown & Debt", value = "")

                transaction.shareDetails.members.forEach { member ->
                    val name =
                        if (member.userId == currentUserId) "You" else "User ${member.userId}"
                    val share = member.share ?: 0.0
                    val displayValue = if (name == "You") "-$$share" else "+$$share"

                    DetailRow(
                        label = name,
                        value = displayValue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Methodology",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (transaction.shareDetails.type == "EQUAL") "Split equally" else transaction.shareDetails.type,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }

                    Button(
                        onClick = onSettleUpClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Settle Up")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
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
