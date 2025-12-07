package org.milad.expense_share.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.Group
import model.TransactionStatus
import model.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    navLayoutType: NavigationSuiteType,
    currentUser: User?,
    groups: List<Group>,
    onGroupClick: (Group) -> Unit,
    isListAndDetailVisible: Boolean,
    isDetailVisible: Boolean,
    onAddGroupClick: () -> Unit,
    totalOwe: Double,
    totalOwed: Double,
    selectedGroup: Group?,
) {

    Scaffold(
        floatingActionButton = {
            if (navLayoutType == NavigationSuiteType.NavigationBar)
                AppExtendedButton(
                    title = "Add Group",
                    onClick = onAddGroupClick
                )
        },
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            BalanceSummaryRow(
                modifier = Modifier,
                totalOwe = totalOwe,
                totalOwed = totalOwed
            )

            GroupSection(
                groups = groups,
                currentUser = currentUser,
                selectedGroup = selectedGroup,
                onGroupClick = onGroupClick
            )
        }
    }
}

@Composable
fun BalanceSummaryRow(
    modifier: Modifier = Modifier,
    totalOwe: Double,
    totalOwed: Double,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BalanceCard(
            modifier = Modifier.weight(1f),
            title = "You owe",
            amount = totalOwe,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        BalanceCard(
            modifier = Modifier.weight(1f),
            title = "You are owed",
            amount = totalOwed,
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun BalanceCard(
    modifier: Modifier,
    title: String,
    amount: Double,
    backgroundColor: Color,
    textColor: Color,
) {
    Column(
        modifier = modifier
            .clip(CardDefaults.shape)
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$ ${amount}",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun GroupSection(
    groups: List<Group>,
    currentUser: User?,
    onGroupClick: (Group) -> Unit,
    selectedGroup: Group?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (groups.isNotEmpty()) {
            Text(
                text = "Groups",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(groups) { group ->
                    GroupItem(
                        group = group,
                        isOpened = selectedGroup == group,
                        isAdminOfGroup = currentUser?.id == group.ownerId,
                        onClick = { onGroupClick(group) },
                        onLongClick = { println(group) }
                    )
                }
            }
        } else {
            EmptyListState()
        }
    }
}

@Composable
private fun GroupItem(
    group: Group,
    isSelected: Boolean = false,
    isOpened: Boolean = false,
    isAdminOfGroup: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val balance =
        group.transactions.filter { it.status == TransactionStatus.APPROVED }
            .sumOf { it.amount }
            .toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else if (isOpened) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.paris),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CardDefaults.shape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAdminOfGroup)
                        Icon(
                            modifier = Modifier.padding(end = 2.dp).size(18.dp),
                            imageVector = Icons.Outlined.SupervisorAccount,
                            contentDescription = "Back"
                        )
                    Text(
                        text = "${group.members.size} members",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "$$balance",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview
@Composable
private fun GroupItemPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            GroupItem(
                group = Group(
                    id = 1,
                    name = "Group 1",
                    ownerId = 1,
                    transactions = emptyList(),
                    members = emptyList()
                ),
                isOpened = true,
                isAdminOfGroup = true,
                onClick = {},
                onLongClick = {},
            )
            GroupItem(
                group = Group(
                    id = 1,
                    name = "Group 1",
                    ownerId = 1,
                    transactions = emptyList(),
                    members = emptyList()
                ),
                onClick = {},
                onLongClick = {},
            )
        }
    }
}

@Composable
fun AppExtendedButton(title: String, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = title) },
        text = { Text(title) }
    )
}