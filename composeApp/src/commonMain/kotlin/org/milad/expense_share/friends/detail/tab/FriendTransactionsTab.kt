package org.milad.expense_share.friends.detail.tab

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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.ui.emptyState.EmptyListState
import model.Group
import org.milad.expense_share.Amount
import org.milad.expense_share.friends.model.TransactionWithGroup
import org.milad.expense_share.showSeparate

@Composable
fun FriendTransactionsTab(
    transactions: List<TransactionWithGroup>,
    onGroupClick: (Group) -> Unit
) {
    if (transactions.isEmpty()) {
        EmptyListState()
        return
    }


    val groupedByDate = transactions.groupBy {
        formatDate(it.transaction.transactionDate)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedByDate.forEach { (date, transactionsForDate) ->
            item {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(transactionsForDate) { transactionWithGroup ->
                TransactionDetailCard(
                    transactionWithGroup = transactionWithGroup,
                    onGroupClick = { onGroupClick(transactionWithGroup.group) }
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailCard(
    transactionWithGroup: TransactionWithGroup,
    onGroupClick: () -> Unit
) {
    val (transaction, group, myShare, friendShare) = transactionWithGroup

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onGroupClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${transaction.amount.showSeparate()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.status.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (transaction.status.name) {
                            "APPROVED" -> MaterialTheme.colorScheme.tertiary
                            "PENDING" -> MaterialTheme.colorScheme.primary
                            "REJECTED" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (transaction.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShareDetail(
                    label = "Your share",
                    amount = myShare,
                    isPaid = transaction.payers.any { it.user.id == 1 }
                )
                ShareDetail(
                    label = "Friend's share",
                    amount = friendShare,
                    isPaid = transaction.payers.any { it.user.id == 2 }
                )
            }


            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Paid by: ${transaction.payers.joinToString { it.user.username }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ShareDetail(
    label: String,
    amount: Amount,
    isPaid: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${amount.showSeparate()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.error
            )
            if (isPaid) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    /*val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))*/
    return "MMM dd yyyy"
}
