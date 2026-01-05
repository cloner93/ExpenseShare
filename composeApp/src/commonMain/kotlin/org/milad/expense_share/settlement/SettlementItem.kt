package org.milad.expense_share.settlement

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pmb.common.theme.AppTheme
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.settlement.FakeDate.mockSettlementItems
import org.milad.expense_share.settlement.FakeDate.userMilad
import org.milad.expense_share.showSeparate

data class SettlementItem(
    val id: String,
    val debtor: User,
    val creditor: User,
    val amount: Amount,
    val status: SettlementStatus,
)

sealed class SettlementStatus {
    object YouOwe : SettlementStatus()
    object YouPaid : SettlementStatus()
    data class TheyPaid(val payerName: String) : SettlementStatus()
    object YouAreOwed : SettlementStatus()
    object Settled : SettlementStatus()
    object Rejected : SettlementStatus()
    data class ThirdParty(val debtor: String, val creditor: String) : SettlementStatus()
}

@Composable
fun SettlementListItem(
    item: SettlementItem,
    currentUserId: Int,
    onPayClick: (SettlementItem) -> Unit = {},
    onApproveClick: (SettlementItem) -> Unit = {},
    onCorrectClick: (SettlementItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val userRole = when (currentUserId) {
        item.debtor.id -> UserRole.DEBTOR
        item.creditor.id -> UserRole.CREDITOR
        else -> UserRole.OBSERVER
    }
    val amountColor = when (userRole) {
        UserRole.DEBTOR -> MaterialTheme.colorScheme.error
        UserRole.CREDITOR -> MaterialTheme.colorScheme.tertiary
        UserRole.OBSERVER -> MaterialTheme.colorScheme.onBackground
    }
    val alpha by animateFloatAsState(
        targetValue = when (item.status) {
            is SettlementStatus.YouPaid,
                -> 0.7f

            else -> 1f
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .then(
                if (item.status is SettlementStatus.TheyPaid) {
                    Modifier.border(
                        1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = when (item.status) {
                is SettlementStatus.Settled -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserColumn(
                    name = item.debtor.username,
                    isHighlighted = userRole == UserRole.DEBTOR
                )
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$ ${item.amount.showSeparate()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(color = amountColor)
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = amountColor,
                            modifier = Modifier.size(24.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(2.dp)
                        )
                    }
                }
                UserColumn(
                    name = item.creditor.username,
                    isHighlighted = userRole == UserRole.CREDITOR
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            StatusSection(
                item = item,
                onPayClick = { onPayClick(item) },
                onApproveClick = { onApproveClick(item) }
            ) { onCorrectClick(item) }
        }
    }
}

@Composable
private fun UserColumn(
    name: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(80.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(
                if (isHighlighted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ), contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = name,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatusSection(
    item: SettlementItem,
    onPayClick: () -> Unit,
    onApproveClick: () -> Unit,
    onCorrectClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (item.status) {
            is SettlementStatus.YouOwe -> {
                StatusLabel(
                    text = "You must pay!",
                    icon = Icons.Default.Payment,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pay!")
                }
            }

            is SettlementStatus.YouPaid -> {
                StatusLabel(
                    text = "Waiting for ${item.creditor.username}'s confirmation.",
                    icon = Icons.Default.Schedule,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is SettlementStatus.TheyPaid -> {
                StatusLabel(
                    text = "${item.status.payerName} paid. Do you approve?",
                    icon = Icons.Default.Notifications,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onApproveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("approve")
                }
            }

            is SettlementStatus.YouAreOwed -> {
                StatusLabel(
                    text = "Waiting for payment",
                    icon = Icons.Default.NotificationsActive,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is SettlementStatus.Settled -> {
                StatusLabel(
                    text = "Payed",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is SettlementStatus.Rejected -> {
                StatusLabel(
                    text = "Rejected by ${item.creditor.username}",
                    icon = Icons.Default.Error,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCorrectClick, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Payments")
                }
            }

            is SettlementStatus.ThirdParty -> {
                StatusLabel(
                    text = "${item.status.debtor} to ${item.status.creditor}",
                    icon = Icons.Default.SwapHoriz,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusLabel(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.background(
            color.copy(alpha = 0.1f), MaterialTheme.shapes.small
        ).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium
        )
    }
}

private enum class UserRole {
    DEBTOR, CREDITOR, OBSERVER
}


@Preview
@Composable
fun SettlementListItemPreview() {
    AppTheme {
        Scaffold { it ->
            LazyColumn(
                modifier = Modifier.padding(it),
                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                items(mockSettlementItems) {
                    SettlementListItem(
                        item = it, currentUserId = userMilad.id
                    )
                }
            }
        }
    }
}

object FakeDate {
    val userMilad = User(id = 1, username = "Milad (Me)", phone = "09121111111")
    val userSara = User(id = 2, username = "Sara", phone = "09122222222")
    val userReza = User(id = 3, username = "Reza", phone = "09123333333")
    val userMaryam = User(id = 4, username = "Maryam", phone = "09124444444")
    val userHamid = User(id = 5, username = "Hamid", phone = "09125555555")
    val userNiloufar = User(id = 6, username = "Niloufar", phone = "09126666666")
    val userParham = User(id = 7, username = "Parham", phone = "09127777777")
    val userSaeid = User(id = 8, username = "Saeid", phone = "09128888888")
    val userNarges = User(id = 9, username = "Narges", phone = "09129999999")

    val mockSettlementItems = listOf(
        SettlementItem(
            id = "1",
            debtor = userMilad,
            creditor = userSara,
            amount = Amount(250000),
            status = SettlementStatus.YouOwe
        ),

        SettlementItem(
            id = "2",
            debtor = userMilad,
            creditor = userReza,
            amount = Amount(120000),
            status = SettlementStatus.YouPaid
        ),

        SettlementItem(
            id = "3",
            debtor = userMaryam,
            creditor = userMilad,
            amount = Amount(450000),
            status = SettlementStatus.TheyPaid(userMaryam.username)
        ),

        SettlementItem(
            id = "4",
            debtor = userHamid,
            creditor = userMilad,
            amount = Amount(85000),
            status = SettlementStatus.YouAreOwed
        ),

        SettlementItem(
            id = "5",
            debtor = userMilad,
            creditor = userNiloufar,
            amount = Amount(320000),
            status = SettlementStatus.Settled
        ),

        SettlementItem(
            id = "6",
            debtor = userMilad,
            creditor = userParham,
            amount = Amount(50000),
            status = SettlementStatus.Rejected
        ),

        SettlementItem(
            id = "7",
            debtor = userSaeid,
            creditor = userNarges,
            amount = Amount(1000000),
            status = SettlementStatus.ThirdParty(userSaeid.username, userNarges.username)
        ),
    )
}
