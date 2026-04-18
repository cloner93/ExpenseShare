package org.milad.expense_share.dashboard.group.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pmb.common.theme.AppTheme
import model.Group
import model.SettlementStatus
import model.SettlementTransaction
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.showSeparate

@Composable
fun SettlementListItem(
    modifier: Modifier = Modifier,
    item: SettlementTransaction,
    currentUserId: Int,
    onPayClick: (SettlementTransaction) -> Unit = {},
    onApproveClick: (SettlementTransaction) -> Unit = {},
    onCorrectClick: (SettlementTransaction) -> Unit = {},
) {
    val userRole = when (currentUserId) {
        item.debtor.id -> UserRole.DEBTOR
        item.creditor.id -> UserRole.CREDITOR
        else -> UserRole.OBSERVER
    }
    val amountColor = when (userRole) {
        UserRole.DEBTOR -> AppTheme.colors.error
        UserRole.CREDITOR -> AppTheme.colors.success
        UserRole.OBSERVER -> AppTheme.colors.onBackground
    }
    val backgroundColor = when (item.status) {
        SettlementStatus.SETTLED -> AppTheme.colors.surfaceVariant
        else -> AppTheme.colors.surfaceContainerHighest
    }
    val alpha by animateFloatAsState(
        targetValue = when (item.status) {
            SettlementStatus.YOU_PAID -> 0.7f
            else -> 1f
        }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(alpha).then(
                if (item.status == SettlementStatus.THEY_PAID) {
                    Modifier.border(
                        1.dp,
                        AppTheme.colors.primary,
                        MaterialTheme.shapes.medium
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserColumn(
                    name = item.debtor.username + if (currentUserId == item.debtor.id) "(me)" else "", isHighlighted = userRole == UserRole.DEBTOR
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
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(color = amountColor)
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = amountColor,
                            modifier = Modifier.size(24.dp)
                                .background(backgroundColor, CircleShape)
                                .padding(2.dp)
                        )
                    }
                }
                UserColumn(
                    name = item.creditor.username + if (currentUserId == item.creditor.id) "(me)" else "", isHighlighted = userRole == UserRole.CREDITOR
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            StatusSection(
                item = item,
                onPayClick = { onPayClick(item) },
                onApproveClick = { onApproveClick(item) }) { onCorrectClick(item) }
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
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isHighlighted) AppTheme.colors.primaryContainer
                    else AppTheme.colors.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.onSurfaceVariant
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
    item: SettlementTransaction,
    onPayClick: () -> Unit,
    onApproveClick: () -> Unit,
    onCorrectClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (item.status) {
             SettlementStatus.YOU_OWE -> {
                StatusLabel(
                    text = "You must pay!",
                    icon = Icons.Default.Payment,
                    color = AppTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPayClick, colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pay!")
                }
            }

             SettlementStatus.YOU_PAID -> {
                StatusLabel(
                    text = "Waiting for ${item.creditor.username}'s confirmation.",
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.primary
                )
            }

             SettlementStatus.THEY_PAID -> {
                StatusLabel(
                    text = "${item.debtor.username} paid. Do you approve?",
                    icon = Icons.Default.Notifications,
                    color = AppTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onApproveClick, colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("approve")
                }
            }

             SettlementStatus.YOU_ARE_OWED -> {
                StatusLabel(
                    text = "Waiting for payment",
                    icon = Icons.Default.NotificationsActive,
                    color = AppTheme.colors.primary
                )
            }

             SettlementStatus.SETTLED -> {
                StatusLabel(
                    text = "Payed",
                    icon = Icons.Default.CheckCircle,
                    color = AppTheme.colors.primary
                )
            }

             SettlementStatus.REJECTED -> {
                StatusLabel(
                    text = "Rejected by ${item.creditor.username}",
                    icon = Icons.Default.Error,
                    color = AppTheme.colors.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCorrectClick, colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Payments")
                }
            }

             SettlementStatus.THIRD_PARTY -> {
                StatusLabel(
                    text = "${item.debtor.username} to ${item.creditor.username}",
                    icon = Icons.Default.SwapHoriz,
                    color = AppTheme.colors.primary
                )
            }

            SettlementStatus.APPROVED -> TODO()
            SettlementStatus.PENDING -> {
                StatusLabel(
                    text = "Waiting for payment",
                    icon = Icons.Default.NotificationsActive,
                    color = AppTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPayClick, colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pay!")
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun StatusLabel(
    text: String,
    icon: ImageVector,
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

@Preview(name = "1. You Owe")
@Composable
fun PreviewYouOwe() {
    Column {

        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[0], currentUserId = FakeDate.userMilad.id
                )
            }
        })

        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[0], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "2. You Paid (Pending)")
@Composable
fun PreviewYouPaid() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[1], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[1], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "3. They Paid (Action Required)")
@Composable
fun PreviewTheyPaid() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[2], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[2], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "4. You Are Owed")
@Composable
fun PreviewYouAreOwed() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[3], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[3], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "5. Settled")
@Composable
fun PreviewSettled() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[4], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[4], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "6. Rejected")
@Composable
fun PreviewRejected() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[5], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[5], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

@Preview(name = "7. Third Party")
@Composable
fun PreviewThirdParty() {
    Column {
        AppTheme(darkTheme = true, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[6], currentUserId = FakeDate.userMilad.id
                )
            }
        })
        AppTheme(darkTheme = false, content = {
            Surface {
                SettlementListItem(
                    item = FakeDate.mockSettlementItems[6], currentUserId = FakeDate.userMilad.id
                )
            }
        })
    }
}

object FakeDate {
    val selectedGroup = Group(
        id = 1,
        name = "Group 1",
        ownerId = 1,
        members = listOf(),
        transactions = listOf()
    )
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
        SettlementTransaction(
            id = "1",
            debtor = userMilad,
            creditor = userSara,
            amount = Amount(250000),
            status = SettlementStatus.YOU_OWE,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "2",
            debtor = userMilad,
            creditor = userReza,
            amount = Amount(120000),
            status = SettlementStatus.YOU_PAID,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "3",
            debtor = userMaryam,
            creditor = userMilad,
            amount = Amount(450000),
            status = SettlementStatus.THEY_PAID,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "4",
            debtor = userHamid,
            creditor = userMilad,
            amount = Amount(85000),
            status = SettlementStatus.YOU_ARE_OWED,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "5",
            debtor = userMilad,
            creditor = userNiloufar,
            amount = Amount(320000),
            status = SettlementStatus.SETTLED,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "6",
            debtor = userMilad,
            creditor = userParham,
            amount = Amount(50000),
            status = SettlementStatus.REJECTED,
            groupName = "NAN"
        ),

        SettlementTransaction(
            id = "7",
            debtor = userSaeid,
            creditor = userNarges,
            amount = Amount(1000000),
            status = SettlementStatus.THIRD_PARTY,
            groupName = "NAN"
        ),
    )

   /* val mockFriends = listOf(
        Friend(userSara, FriendRelationStatus.ACCEPTED),
        Friend(userReza, FriendRelationStatus.ACCEPTED),
        Friend(userMaryam, FriendRelationStatus.PENDING),
        Friend(userHamid, FriendRelationStatus.REJECTED),
        Friend(userNiloufar, FriendRelationStatus.BLOCKED),
    )*/
}
