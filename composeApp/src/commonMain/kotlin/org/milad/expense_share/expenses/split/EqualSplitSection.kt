package org.milad.expense_share.expenses.split

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
fun EqualSplitSection(
    amount: Amount,
    users: List<User>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val userAmountsMap = remember(users, amount) {
        if (amount.isPositive() && users.isNotEmpty()) {
            val exactShares = amount.split(users.size)
            users.zip(exactShares).toMap()
        } else {
            users.associateWith { Amount(0) }
        }
    }

    LaunchedEffect(userAmountsMap) {
        onAmountsUpdated(userAmountsMap)
    }

    Column {
        val entries = userAmountsMap.entries.toList()

        entries.forEachIndexed { index, entry ->
            UserInfoRow(
                user = entry.key,
                showDivider = index < entries.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$ ${entry.value.showSeparate()}",
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(Modifier.width(4.dp))

                        IconButton(onClick = { onRemoveClick(entry.key) }) {
                            Icon(Icons.Default.Close, contentDescription = "remove member")
                        }
                    }
                }
            )
        }
    }
}


@Preview
@Composable
fun EqualSplitSectionPreview() {
    val users = listOf(
        User(
            id = 1,
            username = "milad",
            phone = "0912312312"
        ),
        User(
            id = 2,
            username = "ali",
            phone = "0912312312"
        ),
        User(
            id = 3,
            username = "javad",
            phone = "0912312312"
        )
    )

    Column {
        AppTheme(darkTheme = false, content = {
            Surface {

                EqualSplitSection(
                    amount = Amount(1000),
                    users = users,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        AppTheme(darkTheme = true, content = {
            Surface {
                EqualSplitSection(
                    amount = Amount(100),
                    users = users,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        })
    }
}