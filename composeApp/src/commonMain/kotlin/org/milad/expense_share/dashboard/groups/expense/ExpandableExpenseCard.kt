package org.milad.expense_share.dashboard.groups.expense


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import model.MemberShareDto
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import model.TransactionStatus
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ExpandableExpenseCard(
    transaction: Transaction,
    currentUser: User?,
    isExpanded: Boolean = false,
    onExpandClick: (Transaction) -> Unit = {},
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(CardDefaults.shape)
            .clickable { onExpandClick(transaction) }
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

                    val payerCount = transaction.payers.count()

                    Text(
                        text = "$payerCount member paid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = "$${transaction.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    currentUser?.let { (currentUserId, _, _) ->

                        val myShare =
                            transaction.shareDetails.members.find { it.userId == currentUserId }?.share
                                ?: 0.0
                        val iPaid =
                            transaction.payers.find { it.userId == currentUserId }?.amountPaid
                                ?: 0.0
                        val net = iPaid - myShare

                        if (net < 0) {
                            Column(
                                modifier = Modifier
                                    .clip(CardDefaults.shape)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "You owe $${kotlin.math.abs(net.toInt())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }

                        } else if (net > 0) {
                            Column(
                                modifier = Modifier
                                    .clip(CardDefaults.shape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "You lent $${kotlin.math.abs(net.toInt())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                        }
                    }

                }
            }

            if (isExpanded) {
                // FIXME
                val currentUserId = currentUser?.id ?: 0

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                SectionRow(title = "Payer(s) & Amounts", value = "Remains")

                transaction.payers.forEach { payer ->
                    val name = if (payer.userId == currentUserId) "me" else "User ${payer.userId}"
                    DetailRow(
                        label = "$name:",
                        value = "+$${payer.amountPaid.toInt()}",
                        isBold = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionRow(title = "Split Breakdown & Debt", value = "")

                transaction.shareDetails.members.forEach { member ->
                    val name =
                        if (member.userId == currentUserId) "me:" else "User ${member.userId}:"
                    val share =
                        (member.share ?: 0.0).toInt()
                    val displayValue =
                        if (name == "me") "$$share" else "$$share"

                    DetailRow(
                        label = name,
                        value = displayValue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(
                    label = "Methodology",
                    value = transaction.shareDetails.type
                )

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

@Preview
@Composable
fun ExpandableExpenseCardPreview() {
    val t = Transaction(
        id = 1,
        groupId = 1,
        title = "Hotel Booking",
        amount = 500.0,
        description = "2 nights stay",
        createdBy = 1,
        status = TransactionStatus.APPROVED,
        approvedBy = 1,
        createdAt = 0,
        transactionDate = 0,
        payers = listOf(
            PayerDto(
                userId = 0,
                amountPaid = 300.0,
            ),
            PayerDto(
                userId = 1,
                amountPaid = 200.0,
            )
        ),
        shareDetails = ShareDetailsRequest(
            type = "Equal",
            members = listOf(
                MemberShareDto(
                    userId = 0,
                    share = 250.0,
                ),
                MemberShareDto(
                    userId = 1,
                    share = 250.0,
                )
            )
        )
    )

    AppTheme {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
            ExpandableExpenseCard(
                transaction = t,
                currentUser = User(0, "milad", "09137511005"),
                isExpanded = false

            )
            ExpandableExpenseCard(
                transaction = t,
                currentUser = User(0, "milad", "09137511005"),
                isExpanded = true
            )
        }
    }
}