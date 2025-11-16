@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.expenses

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
                .fillMaxSize(),
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

            val amount = expensePrice.toDoubleOrNull() ?: 3000.0
            when (shareType) {
                ShareType.Equal -> EqualSplitSection(amount, users)
                ShareType.Percent -> PercentSplitSection(users, percents)
                ShareType.Weight -> WeightSplitSection(users, weights)
                ShareType.Manual -> ManualSplitSection(users, amounts)
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

enum class ShareType(val title: String, val icon: ImageVector) {
    Equal("Equal", Icons.Default.SouthAmerica),
    Percent("Percent", Icons.Default.Percent),
    Weight("Weight", Icons.Default.Numbers),
    Manual("Manual", Icons.Default.Edit)
}

@Composable
fun EqualSplitSection(amount: Double, users: List<User>) {
    val share = remember(users.size, amount) {
        amount / users.size
    }

    Column {
        users.forEachIndexed { index, user ->
            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Text(
                        text = share.toString(),
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
    percents: SnapshotStateMap<Int, Float>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        users.forEachIndexed { index, user ->
            val percent = percents[user.id] ?: 0f
            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Text(
                        text = "${percent.toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                bottom = {
                    Slider(
                        value = percent,
                        onValueChange = { percents[user.id] = it },
                        valueRange = 0f..100f,
                        steps = 9,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
            )
        }
    }
}

@Composable
fun WeightSplitSection(users: List<User>, weights: SnapshotStateMap<Int, Float>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        users.forEachIndexed { index, user ->
            val value = weights[user.id] ?: 1f

            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Text(
                        text = value.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                bottom = {
                    Slider(
                        value = value,
                        onValueChange = { weights[user.id] = it },
                        valueRange = 1f..3f,
                        steps = 1
                    )
                }
            )
        }
    }
}

@Composable
fun ManualSplitSection(users: List<User>, amounts: SnapshotStateMap<Int, Float>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        users.forEachIndexed { index, user ->
            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    OutlinedTextField(
                        value = amounts[user.id]?.let { if (it == 0f) "" else it.toString() } ?: "",
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        onValueChange = { input ->
                            amounts[user.id] = input.toFloatOrNull() ?: 0f
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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