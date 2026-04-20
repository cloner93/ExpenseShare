package org.milad.expense_share.expenses.split

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun PercentSplitSection(
    users: List<User>,
    amount: Amount,
    percents: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val userAmountsMap = remember(users, amount, percents.values.toList()) {
        var remainingAmount = amount.value
        var remainingPercent = 100f
        val map = mutableMapOf<User, Amount>()

        users.forEach { user ->
            val p = percents[user] ?: 0f
            val userAmount = if (amount.isPositive() && remainingPercent > 0f) {
                val share = (remainingAmount * (p / remainingPercent)).toLong()
                remainingAmount -= share
                remainingPercent -= p
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        users.forEachIndexed { index, user ->
            val percent = percents[user] ?: 0f
            val userAmount = userAmountsMap[user] ?: Amount(0)

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row {
                            Text(
                                text = userAmount.showSeparate(),
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${percent.toInt()}%",
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(Modifier.width(4.dp))

                        IconButton(onClick = { onRemoveClick(user) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "remove payer"
                            )
                        }
                    }
                },
                bottom = {
                    Slider(
                        enabled = !amount.isZero(),
                        value = percent,
                        onValueChange = { newValue ->
                            percents[user] = newValue
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun PercentSplitSectionPreview() {
    val users = listOf(
        User(id = 1, username = "milad", phone = "0912312312"),
        User(id = 2, username = "ali", phone = "0912312312"),
        User(id = 3, username = "javad", phone = "0912312312")
    )

    Column {
        AppTheme(darkTheme = false) {
            Surface {
                val percents = remember {
                    mutableStateMapOf(
                        users[0] to 33.3f,
                        users[1] to 33.3f,
                        users[2] to 33.4f
                    )
                }
                PercentSplitSection(
                    amount = Amount(1000),
                    users = users,
                    onRemoveClick = {},
                    onAmountsUpdated = {},
                    percents = percents
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppTheme(darkTheme = true) {
            Surface {
                val percents = remember {
                    mutableStateMapOf(
                        users[0] to 50f,
                        users[1] to 25f,
                        users[2] to 25f
                    )
                }
                PercentSplitSection(
                    amount = Amount(100),
                    users = users,
                    onRemoveClick = {},
                    onAmountsUpdated = {},
                    percents = percents
                )
            }
        }
    }
}