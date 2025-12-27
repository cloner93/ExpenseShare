package org.milad.expense_share.dashboard.group.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import model.User
import org.milad.expense_share.group.FriendSelectionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionSheet(
    visible: Boolean,
    friends: List<User>,
    currentMembers: List<User>,
    onConfirm: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    var selectedMembers by remember { mutableStateOf(currentMembers) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        MemberSelectionContent(
            friends = friends.distinctBy { it.id },
            selectedMembers = selectedMembers,
            onToggleMember = { user ->
                selectedMembers = selectedMembers.toggleMember(user)
            },
            onCancel = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onDismiss()
                }
            },
            onConfirm = {
                val memberIds = selectedMembers.map { it.id }
                onConfirm(memberIds)
                
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onDismiss()
                }
            }
        )
    }
}

@Composable
private fun MemberSelectionContent(
    friends: List<User>,
    selectedMembers: List<User>,
    onToggleMember: (User) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Friends",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(friends) { user ->
                val isSelected = selectedMembers.any { it.id == user.id }
                
                FriendSelectionRow(
                    user = user,
                    isSelected = isSelected,
                    onToggle = { onToggleMember(user) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }

            Button(onClick = onConfirm) {
                Text("Confirm (${selectedMembers.size})")
            }
        }
    }
}


private fun List<User>.toggleMember(user: User): List<User> {
    return if (any { it.id == user.id }) {
        filter { it.id != user.id }
    } else {
        this + user
    }
}