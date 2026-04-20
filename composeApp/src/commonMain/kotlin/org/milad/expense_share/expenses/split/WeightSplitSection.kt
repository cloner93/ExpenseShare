package org.milad.expense_share.expenses.split

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.expenses.UserInfoRow
import org.milad.expense_share.showSeparate

@Composable
fun WeightSplitSection(
    users: List<User>,
    amount: Amount,
    weights: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {

    val userAmountsMap = remember(users, amount, weights.values.toList()) {
        var remainingAmount = amount.value
        var remainingWeight = weights.values.sum().coerceAtLeast(1f)
        val map = mutableMapOf<User, Amount>()

        users.forEach { user ->
            val w = weights[user] ?: 1f
            val userAmount = if (amount.isPositive() && remainingWeight > 0f) {
                val share = (remainingAmount * (w / remainingWeight)).toLong()
                remainingAmount -= share
                remainingWeight -= w
                Amount(share)
            } else {
                Amount(0)
            }
            map[user] = userAmount
        }
        map
    }

    LaunchedEffect(userAmountsMap) {
        onAmountsUpdated(userAmountsMap)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        users.forEachIndexed { index, user ->
            val weight = weights[user] ?: 1f
            val userAmount = userAmountsMap[user] ?: Amount(0)

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = userAmount.showSeparate(),
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "w:${weight.toInt()}",
                                style = AppTheme.typography.bodySmall
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        IconButton(onClick = { onRemoveClick(user) }) {
                            Icon(Icons.Default.Close, contentDescription = "remove member")
                        }
                    }
                },
                bottom = {
                    Slider(
                        enabled = !amount.isZero(),
                        value = weight,
                        onValueChange = { weights[user] = it },
                        valueRange = 1f..3f,
                        steps = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun WeightSplitSectionPreview() {
    val users = listOf(
        User(id = 1, username = "milad", phone = "0912312312"),
        User(id = 2, username = "ali", phone = "0912312312"),
        User(id = 3, username = "javad", phone = "0912312312")
    )

    Column {
        AppTheme(darkTheme = false) {
            Surface {
                val weights = remember {
                    mutableStateMapOf(
                        users[0] to 1f,
                        users[1] to 2f,
                        users[2] to 3f
                    )
                }
                WeightSplitSection(
                    amount = Amount(3000),
                    users = users,
                    weights = weights,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppTheme(darkTheme = true) {
            Surface {
                val weights = remember {
                    mutableStateMapOf(
                        users[0] to 1f,
                        users[1] to 1f,
                        users[2] to 1f
                    )
                }
                WeightSplitSection(
                    amount = Amount(300),
                    users = users,
                    weights = weights,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        }
    }
}