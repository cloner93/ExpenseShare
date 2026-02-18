package org.milad.expense_share.friends.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.expenses.ConfirmButton

@Preview
@Composable
private fun SendNewRequestContentP() {
    AppTheme {
        Column(
            modifier = Modifier.background(AppTheme.colors.background)
        ) {
            SendNewRequestContent(
                onCancel = {},
                onConfirm = {},
                loading = false,
                error = null
            )
        }
    }
}

@Composable
private fun SendNewRequestContent(
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
    loading: Boolean,
    error: Throwable?,
) {
    var phone by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(loading) }

    val isValid = phone.length == 11 && phone.startsWith("09")
    val errorMessage = when {
        !touched -> null
        phone.isEmpty() -> "Phone is required"
        !phone.startsWith("09") -> "Phone must start with 09"
        phone.length != 11 -> "Phone must be 11 digits"
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Send new request",
            style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = phone,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(11)
                phone = filtered
                touched = true
            },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = errorMessage != null,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = if (errorMessage != null) AppTheme.colors.error else AppTheme.colors.onSurface
                    )
                    Text("${phone.length}/11")
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }

            ConfirmButton(
                "Send Request",
                loading = loading,
                hasError = error,
                enabled = isValid,
                onClick = {
                    touched = true
                    if (isValid) onConfirm(phone)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendNewRequestSheet(
    visible: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    loading: Boolean,
    error: Throwable?,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val hideAndDismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        SendNewRequestContent(
            onCancel = { hideAndDismiss() },
            onConfirm = { phone ->
                onConfirm(phone)
            },
            loading = loading, error = error
        )
    }
}