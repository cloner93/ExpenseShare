@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SouthAmerica
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import model.User
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddExpense(
    users: List<User>,
    onBackClick: () -> Unit,
    onAddClick: (String, List<Int>) -> Unit,
) {
    val scrollState = rememberScrollState()
    var groupName by rememberSaveable { mutableStateOf("") }
    var expensePrice by rememberSaveable { mutableStateOf("") }
    var expenseDate by rememberSaveable { mutableStateOf("") }
    var selectedFriends by remember { mutableStateOf<List<User>>(emptyList()) }
    var shareType by remember { mutableStateOf(ShareType.Weight) }

    val percents = remember(users) {
        mutableStateMapOf<Int, Float>().apply {
            users.forEach { put(it.id, 0f) }
        }
    }

    val weights = remember(users) {
        mutableStateMapOf<Int, Float>().apply {
            users.forEach { put(it.id, 1f) }
        }
    }

    val amounts = remember(users) {
        mutableStateMapOf<Int, Float>().apply {
            users.forEach { put(it.id, 0f) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                onClick = {
                    if (groupName.isNotBlank() && selectedFriends.isNotEmpty()) {
                        onAddClick(groupName, selectedFriends.map { it.id })
                    }
                }
            ) {
                Text("Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpenseInputFields(
                groupName = groupName,
                onGroupNameChange = { groupName = it },
                expensePrice = expensePrice,
                onExpensePriceChange = { expensePrice = it },
                expenseDate = expenseDate,
                onExpenseDateChange = { expenseDate = it }
            )

            Spacer(Modifier.height(8.dp))

            ExpenseShareType(
                selectedType = shareType,
                onTypeSelected = { shareType = it }
            )

            HorizontalDivider(
                Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                DividerDefaults.Thickness,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            val amount = expensePrice.toDoubleOrNull() ?: 0.0
            when (shareType) {
                ShareType.Equal -> EqualSplitSection(amount, users)
                ShareType.Percent -> PercentSplitSection(users, amount, percents)
                ShareType.Weight -> WeightSplitSection(users, amount, weights)
                ShareType.Manual -> ManualSplitSection(users, amount, amounts)
            }
        }
    }
}

@Composable
private fun ExpenseInputFields(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    expensePrice: String,
    onExpensePriceChange: (String) -> Unit,
    expenseDate: String,
    onExpenseDateChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = groupName,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onGroupNameChange,
        label = { Text("Expense name (Trip, Dinner)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = expensePrice,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onExpensePriceChange,
        label = { Text("Expense Price (100,000)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = expenseDate,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onExpenseDateChange,
        label = { Text("Expense Date (1404.08.20)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )
}

@Composable
@Preview
fun AddExpensePreview() {
    AddExpense(
        users = listOf(
            User(0, "milad", "09137511005"),
            User(1, "Mahdi", "09103556001")
        ),
        onBackClick = {},
        onAddClick = { _, _ -> }
    )
}

@Composable
fun ExpenseShareType(
    selectedType: ShareType,
    onTypeSelected: (ShareType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Share type",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = Int.MAX_VALUE
        ) {
            ShareType.entries.forEach { type ->
                FilterChip(
                    enabled = type.enable,
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.title) },
                    leadingIcon = {
                        Icon(imageVector = type.icon, contentDescription = type.title)
                    }
                )
            }
        }
    }
}

enum class ShareType(val title: String, val icon: ImageVector, val enable: Boolean = true) {
    Equal("Equal", Icons.Default.SouthAmerica),
    Percent("Percent", Icons.Default.Percent),
    Weight("Weight", Icons.Default.Numbers),
    Manual("Manual", Icons.Default.Edit)
}

@Composable
fun EqualSplitSection(amount: Double, users: List<User>) {
    val share = remember(users.size, amount) {
        if (amount != 0.0) amount / users.size else 0.0
    }

    Column {
        users.forEachIndexed { index, user ->
            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Text(
                        text = "$ " + share.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            )
        }
    }
}

@Composable
fun PercentSplitSection(
    users: List<User>,
    amount: Double,
    percents: SnapshotStateMap<Int, Float>,
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        users.forEachIndexed { index, user ->
            val percent = percents[user.id] ?: 0f
            val userAmount = remember(percent, amount) {
                if (amount != 0.0) (amount * (percent / 100f)) else 0.0
            }

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Row {
                        Text(
                            text = "${userAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${percent.toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                bottom = {
                    Slider(
                        enabled = amount != 0.0,
                        value = percent,
                        onValueChange = { percents[user.id] = it },
                        valueRange = 0f..100f,
//                        steps = 9,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
            )
        }
    }
}

@Composable
fun WeightSplitSection(
    users: List<User>,
    amount: Double,
    weights: SnapshotStateMap<Int, Float>,
) {
    val totalWeight = remember(weights.values.toList()) {
        weights.values.sum().coerceAtLeast(1f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        users.forEachIndexed { index, user ->

            val weight = weights[user.id] ?: 1f

            val userAmount = remember(weight, totalWeight, amount) {
                if (amount != 0.0) (amount * (weight / totalWeight)) else 0.0
            }

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${userAmount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "w:${weight.toInt()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                bottom = {
                    Slider(
                        enabled = amount != 0.0,
                        value = weight,
                        onValueChange = { weights[user.id] = it },
                        valueRange = 1f..3f,
                        steps = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
            )
        }
    }
}

@Composable
fun ManualSplitSection(
    users: List<User>,
    amount: Double,
    amounts: SnapshotStateMap<Int, Float>,
) {
    val defaultShare = remember(amount, users) {
        (amount / users.size).toFloat()
    }

        users.forEach { user ->
            if ((amounts[user.id] ?: 0f) == 0f) {
                amounts[user.id] = defaultShare
            }
        }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        users.forEachIndexed { index, user ->

            val currentValue by remember { derivedStateOf { amounts[user.id] ?: 0f } }

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    TextField(
                        modifier = Modifier
                            .width(110.dp)
                            .height(50.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 4.dp),
                        value = currentValue.takeIf { it != 0f }?.toString() ?: "",
                        singleLine = true,
                        onValueChange = { input ->
                            amounts[user.id] = input.toFloatOrNull() ?: 0f
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
fun UserInfoRow(
    user: User,
    showDivider: Boolean,
    tailing: @Composable () -> Unit = {},
    bottom: @Composable () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            tailing()
        }

        bottom()

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}