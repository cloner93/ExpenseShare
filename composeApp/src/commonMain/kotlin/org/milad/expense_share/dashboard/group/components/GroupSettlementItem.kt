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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import model.Settlement
import model.SettlementStatus
import org.milad.expense_share.showSeparate

@Composable
fun SettlementListItem(
    modifier: Modifier = Modifier,
    item: Settlement,
    currentUserId: Int,
    onPayClick: (Settlement) -> Unit = {},
    onApproveClick: (Settlement) -> Unit = {},
    onRejectClick: (Settlement) -> Unit = {},
    onCorrectClick: (Settlement) -> Unit = {},
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
        SettlementStatus.PAID -> AppTheme.colors.surfaceVariant
        else -> AppTheme.colors.surfaceContainerHighest
    }

    val alpha by animateFloatAsState(
        targetValue = if (item.status == SettlementStatus.PAID && currentUserId == item.debtor.id) 0.7f else 1f,
        label = "alpha_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .alpha(alpha).then(
                if (item.status == SettlementStatus.PAID) {
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
                    name = item.debtor.username + if (currentUserId == item.debtor.id) " (me)" else "",
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
                    name = item.creditor.username + if (currentUserId == item.creditor.id) " (me)" else "",
                    isHighlighted = userRole == UserRole.CREDITOR
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // فراخوانی StatusSection جدید
            StatusSection(
                item = item,
                userRole = userRole,
                onPayClick = { onPayClick(item) },
                onApproveClick = { onApproveClick(item) },
                onRejectClick = { onRejectClick(item) },
                onCorrectClick = { onCorrectClick(item) }
            )
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
    item: Settlement,
    userRole: UserRole,
    onPayClick: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCorrectClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (item.status) {
            SettlementStatus.PENDING -> {
                when (userRole) {
                    UserRole.DEBTOR -> {
                        StatusLabel("Waiting for your payment", Icons.Default.NotificationsActive, AppTheme.colors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onPayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pay!")
                        }
                    }
                    UserRole.CREDITOR -> {
                        StatusLabel("Waiting for ${item.debtor.username}'s payment", Icons.Default.NotificationsActive, AppTheme.colors.primary)
                    }
                    UserRole.OBSERVER -> {
                        StatusLabel("Pending payment from ${item.debtor.username}", Icons.Default.NotificationsActive, Color.Gray)
                    }
                }
            }

            SettlementStatus.PAID -> {
                when (userRole) {
                    UserRole.CREDITOR -> {
                        StatusLabel("${item.debtor.username} marked as paid", Icons.Default.NotificationsActive, AppTheme.colors.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // دکمه رد کردن
                            Button(
                                onClick = onRejectClick,
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.error)
                            ) {
                                // اگر آیکون Close رو ایمپورت نکردید از Icons.Default.Error استفاده کنید
                                Icon(Icons.Default.Error, contentDescription = "Reject")
                                Spacer(Modifier.width(4.dp))
                                Text("Reject")
                            }

                            // دکمه تایید کردن
                            Button(
                                onClick = onApproveClick,
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.success)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Approve")
                                Spacer(Modifier.width(4.dp))
                                Text("Approve")
                            }
                        }
                    }
                    UserRole.DEBTOR -> {
                        StatusLabel("Waiting for ${item.creditor.username} to confirm", Icons.Default.NotificationsActive, AppTheme.colors.primary)
                    }
                    UserRole.OBSERVER -> {
                        StatusLabel("Waiting for ${item.creditor.username}'s confirmation", Icons.Default.NotificationsActive, Color.Gray)
                    }
                }
            }

            SettlementStatus.CONFIRMED -> {
                // تایید نهایی برای همه یک پیام مشترک دارد (بدون دکمه)
                StatusLabel("Payment is successful", Icons.Default.Done, AppTheme.colors.success)
            }

            SettlementStatus.DISPUTED -> {
                when (userRole) {
                    UserRole.DEBTOR -> {
                        StatusLabel("Rejected by ${item.creditor.username}", Icons.Default.Error, AppTheme.colors.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onCorrectClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Edit Payments")
                        }
                    }
                    UserRole.CREDITOR -> {
                        StatusLabel("You rejected this payment", Icons.Default.Error, AppTheme.colors.error)
                    }
                    UserRole.OBSERVER -> {
                        StatusLabel("Payment disputed", Icons.Default.Error, AppTheme.colors.error)
                    }
                }
            }
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

/*@Preview
@Composable
fun PreviewPending() {
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

@Preview
@Composable
fun PreviewPaid() {
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

@Preview
@Composable
fun PreviewConfirm() {
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

@Preview
@Composable
fun PreviewDisputed() {
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
        Settlement(
            id = 0,
            debtor = userMilad,
            creditor = userSara,
            amount = Amount(250000),
            status = SettlementStatus.PENDING,
            groupId = TODO(),
            createdAt = TODO(),
            updatedAt = TODO()
        ),

        Settlement(
            id = "2",
            debtor = userMilad,
            creditor = userReza,
            amount = Amount(120000),
            status = SettlementStatus.CONFIRMED,
            groupName = "NAN"
        ),

        Settlement(
            id = "3",
            debtor = userMaryam,
            creditor = userMilad,
            amount = Amount(450000),
            status = SettlementStatus.PAID,
            groupName = "NAN"
        ),

        Settlement(
            id = "4",
            debtor = userHamid,
            creditor = userMilad,
            amount = Amount(85000),
            status = SettlementStatus.DISPUTED,
            groupName = "NAN"
        ),
    )
}
*/