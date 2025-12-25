@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.milad.expense_share.Amount
import org.milad.expense_share.expenses.AnimatedLoadingButton
import org.milad.expense_share.showSeparate

@Composable
fun ExpandableExpenseCard(
    transaction: Transaction,
    currentUser: User?,
    isUserAdminOfGroup: Boolean = false,
    isExpanded: Boolean = false,
    isLoading: Boolean = false,
    currentTrxActionLoading: TrxActions? = null,
    onExpandClick: (Transaction) -> Unit = {},
    onApproveTransactionClick: (Transaction) -> Unit = {},
    onRejectTransactionClick: (Transaction) -> Unit = {},
    onEditTransactionClick: (Transaction) -> Unit = {},
    onDeleteTransactionClick: (Transaction) -> Unit = {},
    onMoreMenuTransactionClick: (Transaction) -> Unit = {},
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
                        text = "$${transaction.amount.showSeparate()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    currentUser?.let { (currentUserId, _, _) ->
                        val myShare =
                            transaction.shareDetails.members.find { it.user.id == currentUserId }?.share
                                ?: Amount(0)
                        val iPaid =
                            transaction.payers.find { it.user.id == currentUserId }?.amountPaid
                                ?: Amount(0)
                        val net = iPaid - myShare

                        if (net.isNegative()) {
                            Column(
                                modifier = Modifier
                                    .clip(CardDefaults.shape)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "You owe $${net.abs().showSeparate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else if (net.isPositive()) {
                            Column(
                                modifier = Modifier
                                    .clip(CardDefaults.shape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(8.dp),
                            ) {
                                Text(
                                    text = "You lent $${net.abs().showSeparate()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            if (isExpanded) {
                val currentUserId = currentUser?.id ?: 0
                val isCreator = transaction.createdBy == currentUserId

                val showApprovalButtons =
                    isUserAdminOfGroup && transaction.status == TransactionStatus.PENDING && !isCreator

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                if (transaction.description.isNotEmpty()) {
                    SectionRow(title = "Description", value = transaction.description)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                SectionRow(title = "Payer(s) & Amounts", value = "Remains")
                transaction.payers.forEach { payer ->
                    val name = if (payer.user.id == currentUserId) "me" else payer.user.username
                    DetailRow(
                        label = "$name:",
                        value = "+$${payer.amountPaid.showSeparate()}",
                        isBold = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionRow(title = "Split Breakdown & Debt", value = "")
                transaction.shareDetails.members.forEach { member ->
                    val name =
                        if (member.user.id == currentUserId) "me" else member.user.username
                    val share = member.share.showSeparate()
                    DetailRow(label = name, value = "$$share",isBold = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(label = "Methodology", value = transaction.shareDetails.type)

                if (isCreator || isUserAdminOfGroup) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (showApprovalButtons) {
                                AnimatedLoadingButton(
                                    text = "Approve",
                                    enabled = currentTrxActionLoading != TrxActions.Approve && isLoading,
                                    loading = currentTrxActionLoading == TrxActions.Approve && isLoading,
                                    onClick = { onApproveTransactionClick(transaction) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                )
                                AnimatedLoadingButton(
                                    text = "Reject",
                                    enabled = currentTrxActionLoading != TrxActions.Reject && isLoading,
                                    loading = currentTrxActionLoading == TrxActions.Reject && isLoading,
                                    onClick = { onRejectTransactionClick(transaction) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )

                            } else {
                                OutlinedButton(
                                    onClick = { onEditTransactionClick(transaction) },
                                ) {
                                    Text("Edit")
                                }

                                AnimatedLoadingButton(
                                    text = "Delete",
                                    enabled = currentTrxActionLoading != TrxActions.Delete && isLoading,
                                    loading = currentTrxActionLoading == TrxActions.Delete && isLoading,
                                    onClick = { onDeleteTransactionClick(transaction) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable {
                                onMoreMenuTransactionClick(transaction)
                            }
                        )
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


val t = Transaction(
    id = 1,
    groupId = 1,
    title = "Hotel Booking",
    amount = Amount(500),
    description = "2 nights stay",
    createdBy = 1,
    status = TransactionStatus.APPROVED,
    approvedBy = 1,
    createdAt = 0,
    transactionDate = 0,
    payers = listOf(
        PayerDto(
            user= User(0, "milad", "09137511005"),
            amountPaid = Amount(300),
        ),
        PayerDto(
            user= User(1, "mahdi", "09137511001"),
            amountPaid = Amount(200),
        )
    ),
    shareDetails = ShareDetailsRequest(
        type = "Equal",
        members = listOf(
            MemberShareDto(
                user= User(0, "milad", "09137511005"),
                share = Amount(250),
            ),
            MemberShareDto(
                user = User(1, "mahdi", "09137511001"),
                share = Amount(250),
            )
        )
    )
)

@Preview
@Composable
fun ExpandableExpenseCardPreview() {

    AppTheme {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
            ExpandableExpenseCard(
                transaction = t,
                currentUser = User(0, "milad", "09137511005")

            )
        }
    }
}

@Preview
@Composable
fun ExpandableExpenseCardPreview2() {
    AppTheme {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {

            ExpandableExpenseCard(
                transaction = t.copy(status = TransactionStatus.PENDING),
                currentUser = User(0, "milad", "09137511005"),
                isUserAdminOfGroup = false,
                isExpanded = true
            )
        }
    }
}

@Preview
@Composable
fun ExpandableExpenseCardPreview3() {
    AppTheme {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {

            ExpandableExpenseCard(
                transaction = t.copy(status = TransactionStatus.PENDING),
                currentUser = User(0, "milad", "09137511005"),
                isUserAdminOfGroup = true,
                isExpanded = true
            )
        }
    }
}

@Preview
@Composable
fun ExpandableExpenseCardPreview4() {
    AppTheme {
        Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {

            ExpandableExpenseCard(
                transaction = t.copy(status = TransactionStatus.APPROVED),
                currentUser = User(0, "milad", "09137511005"),
                isUserAdminOfGroup = true,
                isExpanded = true
            )
        }
    }
}