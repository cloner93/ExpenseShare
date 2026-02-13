@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.expenses

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SouthAmerica
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.MemberShareDto
import model.PayerDto
import model.ShareDetailsRequest
import model.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.Amount
import org.milad.expense_share.group.FriendSelectionRow
import org.milad.expense_share.showSeparate

@Composable
fun AddExpense(
    allUsers: List<User>,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    hasError: Throwable?,
    onConfirmClick: (String, Amount, String, List<PayerDto>, ShareDetailsRequest) -> Unit,
) {
    val scrollState = rememberScrollState()
    var groupName by rememberSaveable { mutableStateOf("") }
    var expensePrice by rememberSaveable { mutableStateOf("") }
    var expenseDesc by rememberSaveable { mutableStateOf("") }
    var shareType: ShareType? by remember { mutableStateOf(null) }

    var payers by remember { mutableStateOf<List<User>>(emptyList()) }
    var members by remember { mutableStateOf<List<User>>(emptyList()) }

    val payerAmounts = remember { mutableStateMapOf<User, String>() }
    var finalPayerMap by remember { mutableStateOf<Map<User, Amount>>(emptyMap()) }

    var memberAmounts by remember { mutableStateOf<Map<User, Amount>>(emptyMap()) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var payerError by remember { mutableStateOf<String?>(null) }
    var memberError by remember { mutableStateOf<String?>(null) }
    var shareTypeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(memberAmounts) {
        memberAmounts.forEach { (id, amount) ->
            println("$id $amount")
        }
    }

    val percents = remember(members) {
        mutableStateMapOf<User, Float>().apply {
            members.forEach { put(it, 0f) }
        }
    }

    val weights = remember(members) {
        mutableStateMapOf<User, Float>().apply {
            members.forEach { put(it, 1f) }
        }
    }

    val amounts = remember(members) {
        mutableStateMapOf<User, Float>().apply {
            members.forEach { put(it, 0f) }
        }
    }

    var showPayerBottomSheet by remember { mutableStateOf(false) }
    var showMemberBottomSheet by remember { mutableStateOf(false) }


    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Add Expense", style = AppTheme.typography.titleLarge) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppTheme.colors.inverseOnSurface,
            ),
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }, bottomBar = {
        ConfirmButton(isLoading, hasError) {
            nameError = null
            priceError = null
            payerError = null
            memberError = null
            shareTypeError = null

            if (groupName.isBlank()) nameError = "Name cannot be empty"
            val priceVal = Amount(expensePrice)
            if (priceVal.isNegative() || priceVal.isZero()) priceError = "Invalid price"
            if (payers.isEmpty()) payerError = "Select at least one payer"
            if (members.isEmpty()) memberError = "Select at least one member"
            if (shareType == null) shareTypeError = "Choose a share type"

            val payerMissingAmount = finalPayerMap.any { (_, amount) ->
                amount <= 0.0
            }

            if (payerMissingAmount) {
                payerError = "One or more payers have no amount"
            }

            val payerTotal = Amount(finalPayerMap.values.sumOf { it.value })
            if (priceVal.isPositive() && payerTotal != priceVal) {
                payerError =
                    "Total payer amounts (${payerTotal}) must equal the expense price"
            }

            val isValid =
                nameError == null && priceError == null && payerError == null && memberError == null && shareTypeError == null

            if (isValid) {
                onConfirmClick(
                    groupName, priceVal,
                    expenseDesc, finalPayerMap.map { PayerDto(it.key, it.value) },
                    ShareDetailsRequest(
                        shareType!!.title,
                        members = memberAmounts.map { MemberShareDto(it.key, it.value) },
                    )
                )
            }
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .background(AppTheme.colors.inverseOnSurface)
                .padding(16.dp)
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpenseInputFields(
                groupName = groupName,
                onGroupNameChange = { groupName = it },
                nameError = nameError,
                onNameErrorChange = { nameError = it },
                expensePrice = expensePrice,
                onExpensePriceChange = { expensePrice = it },
                priceError = priceError,
                onPriceErrorChange = { priceError = it },
                expenseDesc = expenseDesc,
                onExpenseDescChange = { expenseDesc = it })

            Spacer(Modifier.height(8.dp))

            PayerOfExpense(
                payers = payers,
                payerAmounts = payerAmounts,
                onPayersClick = { showPayerBottomSheet = true },
                onRemovePayer = { user ->
                    payers = payers - user
                    payerAmounts.remove(user)
                },
                onAmountsUpdated = { updated ->
                    finalPayerMap = updated
                },
                payerError = payerError
            )

            ExpenseShareType(
                selectedType = shareType,
                onTypeSelected = {
                    shareType = it
                    shareTypeError = null
                },
                shareTypeError = shareTypeError
            )

            if (memberError != null) {
                Text(
                    memberError!!,
                    color = AppTheme.colors.error,
                    style = AppTheme.typography.bodySmall
                )
            }

            Column {
                val amount: Amount = Amount(expensePrice)
                when (shareType) {
                    ShareType.Equal -> EqualSplitSection(
                        amount,
                        members,
                        onRemoveClick = { user -> members = members - user },
                        onAmountsUpdated = { result -> memberAmounts = result }
                    )

                    ShareType.Percent -> PercentSplitSection(
                        members, amount, percents,
                        onRemoveClick = { user -> members = members - user },
                        onAmountsUpdated = { result -> memberAmounts = result }
                    )

                    ShareType.Weight -> WeightSplitSection(
                        members, amount, weights,
                        onRemoveClick = { user -> members = members - user },
                        onAmountsUpdated = { result -> memberAmounts = result }
                    )

                    ShareType.Manual -> ManualSplitSection(
                        members, amount, amounts,
                        onRemoveClick = { user -> members = members - user },
                        onAmountsUpdated = { result -> memberAmounts = result }
                    )

                    null -> {}
                }

                OutlinedButton(
                    onClick = { showMemberBottomSheet = true }, modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Members")
                }

                HorizontalDivider(
                    Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                    DividerDefaults.Thickness,
                    AppTheme.colors.outline.copy(alpha = 0.2f)
                )
            }
        }
    }

    if (showPayerBottomSheet) {
        SelectionBottomSheet(
            title = "Select Payers", items = allUsers, initiallySelected = payers, onDismiss = {
                showPayerBottomSheet = false
            }) {
            showPayerBottomSheet = false
            payers = it
            payerError = null
        }
    }
    if (showMemberBottomSheet) {
        SelectionBottomSheet(
            title = "Select Members", items = allUsers, initiallySelected = members, onDismiss = {
                showMemberBottomSheet = false
            }) {
            showMemberBottomSheet = false
            members = it
            if (shareType == null) shareType = ShareType.Equal

            memberError = null
            shareTypeError = null
        }
    }
}

@Composable
fun ConfirmButton(
    loading: Boolean,
    hasError: Throwable?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedLoadingButton(
            Modifier.fillMaxWidth(),
            text = "Save",
            loading = loading,
            onClick = onClick
        )

        AnimatedVisibility(visible = hasError != null) {
            if (hasError != null) {
                Text(
                    text = hasError.message ?: "ERROR !",
                    style = TextStyle(color = AppTheme.colors.error),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedLoadingButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    loading: Boolean,
    enabled: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        enabled = !loading || enabled,
        onClick = onClick,
        colors = colors
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + scaleIn()).togetherWith(
                    fadeOut(animationSpec = tween(300)) + scaleOut()
                )
            },
            label = "ButtonLoadingAnimation"
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    text?.let { Text(it) }

                    if (text != null && icon != null)
                        Spacer(Modifier.width(8.dp))

                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConfirmButtonP() {
    AppTheme(content = {

        Column(modifier = Modifier.background(color = Color.White)) {
            ConfirmButton(false, null) {}
            ConfirmButton(true, null) {}
            ConfirmButton(false, Throwable("Has error")) {}
        }

    })
}

@Preview
@Composable
fun LoadingButtonPreview() {
    AppTheme(content = {
        Column(modifier = Modifier.background(color = Color.White)) {
            AnimatedLoadingButton(
                text = "Title",
                loading = false,
                onClick = { }
            )
            AnimatedLoadingButton(
                text = "Title",
                loading = true,
                onClick = { }
            )
            AnimatedLoadingButton(
                text = "Title",
                loading = false,
                icon = Icons.Default.Check,
                onClick = { }
            )
            AnimatedLoadingButton(
                loading = false,
                icon = Icons.Default.Check,
                onClick = { }
            )
        }
    })
}

@Composable
fun PayerOfExpense(
    payers: List<User>,
    payerAmounts: SnapshotStateMap<User, String>,
    onPayersClick: () -> Unit,
    onRemovePayer: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
    payerError: String?,
) {

    LaunchedEffect(payerAmounts.values.toList()) {
        val result = payers.associate { user ->
            val amount = Amount(payerAmounts[user] ?: "0")
            user to amount
        }
        onAmountsUpdated(result)
    }

    Column(Modifier.fillMaxWidth()) {

        Text(
            text = "Payers",
            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        payers.forEachIndexed { index, user ->
            val paidValue = payerAmounts[user] ?: ""

            UserInfoRow(
                user = user,
                showDivider = index < payers.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        TextField(
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .border(
                                    width = 1.dp,
                                    color = AppTheme.colors.outline.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 4.dp),
                            value = paidValue,
                            singleLine = true,
                            onValueChange = { input ->
                                payerAmounts[user] = input
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

                        Spacer(Modifier.width(8.dp))

                        IconButton(onClick = { onRemovePayer(user) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "remove payer",
                                tint = AppTheme.colors.outline
                            )
                        }
                    }
                }
            )
        }

        if (payerError != null) {
            Text(
                payerError,
                color = AppTheme.colors.error,
                style = AppTheme.typography.bodySmall
            )
        }

        OutlinedButton(
            onClick = onPayersClick,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Payer")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = DividerDefaults.Thickness,
            color = AppTheme.colors.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ExpenseInputFields(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    nameError: String?,
    onNameErrorChange: (String?) -> Unit,
    expensePrice: String,
    onExpensePriceChange: (String) -> Unit,
    priceError: String?,
    onPriceErrorChange: (String?) -> Unit,
    expenseDesc: String,
    onExpenseDescChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = groupName, onValueChange = {
            onGroupNameChange(it)
            if (it.isNotBlank()) onNameErrorChange(null)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Expense name (Trip, Dinner)") }, isError = nameError != null
    )

    if (nameError != null) {
        Text(
            nameError,
            color = AppTheme.colors.error,
            style = AppTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = expensePrice,
        onValueChange = {
            onExpensePriceChange(it)
            if (it.toDoubleOrNull() != null) onPriceErrorChange(null)
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Expense Price (100,000)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = priceError != null
    )

    if (priceError != null) {
        Text(
            priceError,
            color = AppTheme.colors.error,
            style = AppTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = expenseDesc,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onExpenseDescChange,
        label = { Text("Expense Desc") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    )

    HorizontalDivider(
        Modifier.padding(vertical = 6.dp).fillMaxWidth(),
        DividerDefaults.Thickness,
        AppTheme.colors.outline.copy(alpha = 0.2f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(
    title: String,
    items: List<User>,
    initiallySelected: List<User>,
    onDismiss: () -> Unit,
    onConfirm: (List<User>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var tempSelected by remember { mutableStateOf(initiallySelected.toMutableList()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(title, style = AppTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(items) { user ->
                    FriendSelectionRow(
                        user = user, isSelected = tempSelected.contains(user), onToggle = {
                            tempSelected = if (tempSelected.contains(user)) {
                                tempSelected.filter { it != user }.toMutableList()
                            } else {
                                (tempSelected + user).toMutableList()
                            }
                        })
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }

                Button(onClick = { onConfirm(tempSelected) }) {
                    Text("Confirm (${tempSelected.size})")
                }
            }
        }
    }
}

@Composable
private fun FriendSelectionRow(user: User, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.paris),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
                    .clip(CardDefaults.shape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
            ) {
                Text(
                    text = user.username,
                    style = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.onSurface)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.phone,
                    style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface)
                )
            }

            Checkbox(
                checked = isSelected, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
fun ExpenseShareType(
    selectedType: ShareType?,
    onTypeSelected: (ShareType) -> Unit,
    shareTypeError: String?,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Share type",
            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Spacer(Modifier.height(8.dp))

        if (shareTypeError != null) {
            Text(
                shareTypeError,
                color = AppTheme.colors.error,
                style = AppTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = Int.MAX_VALUE
        ) {
            ShareType.entries.forEach { type ->
                FilterChip(
                    enabled = type.enable,
                    selected = selectedType != null && type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.title) },
                    leadingIcon = {
                        Icon(imageVector = type.icon, contentDescription = type.title)
                    })
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
fun EqualSplitSection(
    amount: Amount,
    users: List<User>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val eachUser = remember(users.size, amount) {
        if (amount.isPositive() || users.isNotEmpty()) amount / users.size else Amount(0)
    }

    LaunchedEffect(eachUser, users.size) {
        val result = users.associate { user -> user to eachUser }
        onAmountsUpdated(result)
    }

    Column {
        users.forEachIndexed { index, user ->
            UserInfoRow(
                user = user,
                showDivider = index < users.lastIndex,
                tailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${eachUser.showSeparate()}",
                            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(Modifier.width(4.dp))

                        IconButton(onClick = { onRemoveClick(user) }) {
                            Icon(Icons.Default.Close, contentDescription = "remove member")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PercentSplitSection(
    users: List<User>,
    amount: Amount,
    percents: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {

    LaunchedEffect(percents.values.toList(), amount) {
        val result = users.associateWith { user ->
            val percent = percents[user] ?: 0f
            val userAmount =
                if (amount.isPositive())
                    Amount((amount.value * (percent / 100f)).toLong())
                else
                    Amount(0)
            userAmount
        }
        onAmountsUpdated(result)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        users.forEachIndexed { index, user ->
            val percent = percents[user] ?: 0f

            val userAmount = if (amount.isPositive())
                amount * (percent / 100f).toLong()
            else Amount(0)

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

@Composable
fun WeightSplitSection(
    users: List<User>,
    amount: Amount,
    weights: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val totalWeight = remember(weights.values.toList()) {
        weights.values.sum().coerceAtLeast(1f)
    }

    LaunchedEffect(weights.values.toList(), amount) {
        val result = users.associateWith { user ->
            val w = weights[user] ?: 1f
            val userAmount =
                if (amount.isPositive()) amount * (w / totalWeight).toLong() else Amount(0)
            userAmount
        }
        onAmountsUpdated(result)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        users.forEachIndexed { index, user ->
            val weight = weights[user] ?: 1f

            val userAmount = if (amount.isPositive())
                amount * (weight / totalWeight).toLong()
            else Amount(0)

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

@Composable
fun ManualSplitSection(
    users: List<User>,
    amount: Amount,
    amounts: SnapshotStateMap<User, Float>,
    onRemoveClick: (User) -> Unit,
    onAmountsUpdated: (Map<User, Amount>) -> Unit,
) {
    val defaultShare = remember(amount, users) {
        (amount.value / users.size).toFloat()
    }

    users.forEach { user ->
        if (!amounts.containsKey(user)) {
            amounts[user] = defaultShare
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

            val currentValue by remember { derivedStateOf { amounts[user] ?: 0f } }

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
                            value = if (currentValue != 0f) currentValue.toString() else "",
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

@Composable
fun UserInfoRow(
    user: User,
    showDivider: Boolean,
    tailing: @Composable () -> Unit = {},
    bottom: @Composable () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
                    .clip(CardDefaults.shape)
                    .background(AppTheme.colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.first().uppercase(),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = user.phone,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }

            tailing()
        }

        bottom()

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = AppTheme.colors.outline.copy(alpha = 0.2f)
            )
        }
    }
}