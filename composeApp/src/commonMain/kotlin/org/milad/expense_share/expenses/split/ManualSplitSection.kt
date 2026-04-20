package org.milad.expense_share.expenses.split

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.expenses.UserInfoRow

@Composable
fun ManualSplitSection(
    users: List<User>,
    amount: Amount,
    amounts: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val defaultShare = remember(amount, users) {
        if (users.isNotEmpty()) {
            (amount.value / users.size).toFloat()
        } else {
            0f
        }
    }

    LaunchedEffect(users, defaultShare) {
        users.forEach { user ->
            if (!amounts.containsKey(user)) {
                amounts[user] = defaultShare
            }
        }
    }

    LaunchedEffect(amounts.values.toList(), amount) {
        val result = users.associateWith { user ->
            val value = (amounts[user] ?: 0f).toDouble()
            Amount(value.toLong())
        }
        onAmountsUpdated(result)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        users.forEachIndexed { index, user ->

            val currentValue = amounts[user] ?: 0f
            val displayValue = remember(currentValue) {
                if (currentValue == 0f) ""
                else if (currentValue % 1 == 0f) currentValue.toLong().toString()
                else currentValue.toString()
            }

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        TextField(
                            modifier = Modifier
                                .width(110.dp)
                                .height(50.dp)
                                .border(
                                    width = 1.dp,
                                    color = AppTheme.colors.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 4.dp),
                            value = displayValue,
                            singleLine = true,
                            onValueChange = { input ->
                                amounts[user] = input.toFloatOrNull() ?: 0f
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = AppTheme.colors.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        )

                        Spacer(Modifier.width(4.dp))

                        IconButton(onClick = { onRemoveClick(user) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "remove payer"
                            )
                        }
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManualSplitSectionPreview() {
    val users = listOf(
        User(id = 1, username = "milad", phone = "0912312312"),
        User(id = 2, username = "ali", phone = "0912312312"),
        User(id = 3, username = "javad", phone = "0912312312")
    )

    Column {
        AppTheme(darkTheme = false) {
            Surface {
                val amounts = remember {
                    mutableStateMapOf(
                        users[0] to 500f,
                        users[1] to 300f,
                        users[2] to 200f
                    )
                }
                ManualSplitSection(
                    amount = Amount(1000),
                    users = users,
                    amounts = amounts,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AppTheme(darkTheme = true) {
            Surface {
                val amounts = remember {
                    mutableStateMapOf<User, Float>()
                }
                ManualSplitSection(
                    amount = Amount(15000),
                    users = users,
                    amounts = amounts,
                    onRemoveClick = {},
                    onAmountsUpdated = {}
                )
            }
        }
    }
}
