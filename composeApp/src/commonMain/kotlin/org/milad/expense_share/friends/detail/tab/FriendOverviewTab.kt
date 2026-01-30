package org.milad.expense_share.friends.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import model.Group
import model.MemberShareDto
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import model.TransactionStatus
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.friends.model.TransactionWithGroup
import org.milad.expense_share.showSeparate

@Composable
fun FriendOverviewTab(
    sharedGroups: List<Group>,
    recentTransactions: List<TransactionWithGroup>,
    onGroupClick: (Group) -> Unit
) {
    if (sharedGroups.isEmpty() && recentTransactions.isEmpty()) {
        EmptyListState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (sharedGroups.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Shared Groups",
                    count = sharedGroups.size
                )
            }

            items(sharedGroups) { group ->
                SharedGroupCard(
                    group = group,
                    onClick = { onGroupClick(group) }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        if (recentTransactions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Transactions",
                    count = recentTransactions.size
                )
            }

            items(recentTransactions) { transactionWithGroup ->
                RecentTransactionCard(transactionWithGroup)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SharedGroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${group.members.size} members • ${group.transactions.size} expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentTransactionCard(transactionWithGroup: TransactionWithGroup) {
    val (transaction, group, myShare, friendShare) = transactionWithGroup

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "in ${group.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "$${transaction.amount.showSeparate()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your share: $${myShare.showSeparate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Friend's share: $${friendShare.showSeparate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun FriendOverviewTabPreview() {
    val mockUser1 = User(1, "Ali", "09121234567")
    val mockUser2 = User(2, "Sara", "09129876543")
    
    val mockGroups = listOf(
        Group(
            id = 1,
            name = "Trip to Dubai",
            ownerId = 1,
            members = listOf(mockUser1, mockUser2),
            transactions = listOf()
        ),
        Group(
            id = 2,
            name = "Dinner Friends",
            ownerId = 1,
            members = listOf(mockUser1, mockUser2),
            transactions = listOf()
        )
    )

    val mockTransaction = Transaction(
        id = 1,
        groupId = 1,
        title = "Hotel Booking",
        amount = Amount(500000),
        description = "3 nights",
        createdBy = 1,
        status = TransactionStatus.APPROVED,
        approvedBy = 1,
        createdAt = 123456789,
        transactionDate = 123456789,
        payers = listOf(
            PayerDto(mockUser1, Amount(500000))
        ),
        shareDetails = ShareDetailsRequest(
            type = "Equal",
            members = listOf(
                MemberShareDto(mockUser1, Amount(250000)),
                MemberShareDto(mockUser2, Amount(250000))
            )
        )
    )

    val mockTransactions = listOf(
        TransactionWithGroup(
            transaction = mockTransaction,
            group = mockGroups[0],
            myShare = Amount(250000),
            friendShare = Amount(250000)
        )
    )

    AppTheme {
        Surface {
            FriendOverviewTab(
                sharedGroups = mockGroups,
                recentTransactions = mockTransactions,
                onGroupClick = {}
            )
        }
    }
}
