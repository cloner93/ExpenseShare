package org.milad.expense_share.friends.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.friends.friendsList.phoneMapToView
import org.milad.expense_share.friends.friendsList.upperFirstChar
import org.milad.expense_share.friends.model.FriendBalance
import org.milad.expense_share.showSeparate

@Composable
fun FriendBalanceHeader(
    friend: User,
    balance: FriendBalance,
    onSettleUp: () -> Unit,
    onSendReminder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
                ) {
                    Text(
                        text = friend.username.upperFirstChar(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = friend.phone.phoneMapToView(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))


            when {
                balance.isSettled -> {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "All settled up!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                balance.isOwed -> {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${friend.username} owes you",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "$${balance.netBalance.showSeparate()}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = onSendReminder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Text("Send Reminder")
                        }
                    }
                }
                balance.isOwing -> {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "You owe ${friend.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "$${balance.netBalance.abs().showSeparate()}",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Button(
                            onClick = onSettleUp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Settle Up")
                        }
                    }
                }
            }

            if (!balance.isSettled) {

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BalanceDetailItem(
                        label = "You paid",
                        amount = balance.totalOwed,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    BalanceDetailItem(
                        label = "${friend.username} paid",
                        amount = balance.totalOwe,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceDetailItem(
    label: String,
    amount: Amount,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$${amount.showSeparate()}",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun FriendBalanceHeaderPreview_Settled() {
    AppTheme {
        Surface {
            FriendBalanceHeader(
                friend = User(1, "Ali", "09121234567"),
                balance = FriendBalance(
                    totalOwed = Amount(0),
                    totalOwe = Amount(0),
                    netBalance = Amount(0)
                ),
                onSettleUp = {},
                onSendReminder = {}
            )
        }
    }
}

@Preview
@Composable
fun FriendBalanceHeaderPreview_Owed() {
    AppTheme {
        Surface {
            FriendBalanceHeader(
                friend = User(1, "Ali", "09121234567"),
                balance = FriendBalance(
                    totalOwed = Amount(500000),
                    totalOwe = Amount(200000),
                    netBalance = Amount(300000)
                ),
                onSettleUp = {},
                onSendReminder = {}
            )
        }
    }
}

@Preview
@Composable
fun FriendBalanceHeaderPreview_Owing() {
    AppTheme {
        Surface {
            FriendBalanceHeader(
                friend = User(1, "Sara", "09121234567"),
                balance = FriendBalance(
                    totalOwed = Amount(150000),
                    totalOwe = Amount(400000),
                    netBalance = Amount(-250000)
                ),
                onSettleUp = {},
                onSendReminder = {}
            )
        }
    }
}