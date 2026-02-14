package org.milad.expense_share.friends.friendsList

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pmb.common.theme.AppTheme
import com.pmb.common.ui.emptyState.EmptyListState
import expenseshare.composeapp.generated.resources.Res
import expenseshare.composeapp.generated.resources.paris
import model.FriendInfo
import model.FriendRelationStatus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.milad.expense_share.dashboard.group.components.FakeDate
import org.milad.expense_share.expenses.AnimatedLoadingButton
import org.milad.expense_share.friends.FriendsAction
import org.milad.expense_share.friends.FriendsState
import org.milad.expense_share.friends.dialogs.FriendsDialogs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsList(
    state: FriendsState,
    onAction: (FriendsAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Friends") })
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.friends.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(state.friends) { item ->
                        FriendRow(
                            user = item,
                            currentUserRequested = item.requestedBy == state.currentUser!!.id,
                            isOpened = state.selectedFriend == item,
                            onCancelRequest = { onAction(FriendsAction.ShowCancelFriendRequest(item)) },
                            onRejectRequest = { onAction(FriendsAction.ShowRejectFriendRequest(item)) },
                            onAcceptRequest = { onAction(FriendsAction.ShowAcceptFriendRequest(item)) },
                            onClick = { onAction(FriendsAction.SelectFriend(item)) },
                        )
                    }
                }
            } else {
                EmptyListState()
            }
        }
    }
    FriendsDialogs(state, onAction)
}

@Composable
fun FriendRow(
    user: FriendInfo,
    currentUserRequested: Boolean = false,
    isSelected: Boolean = false,
    isOpened: Boolean = false,
    onCancelRequest: () -> Unit = {},
    onRejectRequest: () -> Unit = {},
    onAcceptRequest: () -> Unit = {},
    onClick: () -> Unit = {},
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .clip(CardDefaults.shape)
            .then(
                if (user.status == FriendRelationStatus.ACCEPTED)
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onClick,
                    ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppTheme.colors.primaryContainer
            else if (isOpened) AppTheme.colors.secondaryContainer
            else AppTheme.colors.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.paris),
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CardDefaults.shape),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.user.username.upperFirstChar(),
                        color = AppTheme.colors.onSurface,
                        style = AppTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(8.dp))

                    FriendState(user.status)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.user.phone.phoneMapToView(),
                    color = AppTheme.colors.onSurfaceVariant,
                    style = AppTheme.typography.labelLarge
                )
            }
            if (user.status == FriendRelationStatus.PENDING)
            // if me requested
                if (currentUserRequested) {
                    AnimatedLoadingButton(
                        enabled = true,
                        loading = false,
                        icon = Icons.Outlined.Cancel,
                        onClick = onCancelRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.errorContainer,
                            contentColor = AppTheme.colors.onErrorContainer
                        )
                    )
                } else {
                    Row {
                        AnimatedLoadingButton(
                            enabled = true,
                            loading = false,
                            icon = Icons.Outlined.Cancel,
                            onClick = onRejectRequest,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.errorContainer,
                                contentColor = AppTheme.colors.onErrorContainer
                            )
                        )
                        Spacer(Modifier.width(4.dp))

                        AnimatedLoadingButton(
                            enabled = true,
                            loading = false,
                            icon = Icons.Default.Check,
                            onClick = onAcceptRequest,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.successContainer,
                                contentColor = AppTheme.colors.onSuccessContainer
                            )
                        )
                    }
                }


            if (user.status == FriendRelationStatus.ACCEPTED) {
                Icon(
                    imageVector = Icons.Default.ArrowRight,
                    tint = AppTheme.colors.onSurfaceVariant,
                    contentDescription = null,
                )
            }

        }
    }
}

@Composable
@Preview
fun FriendRowPreview() {
    val user = FriendInfo(
        user = FakeDate.userSaeid,
        status = FriendRelationStatus.ACCEPTED,
        requestedBy = FakeDate.userMilad.id,
        createdAt = 1,
        updatedAt = 1,
    )
    AppTheme {
        Column(
            modifier = Modifier
                .background(color = AppTheme.colors.background),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FriendRow(
                user = user
            ) {}
            FriendRow(
                user = user.copy(status = FriendRelationStatus.PENDING)
            ) {}
            FriendRow(
                user = user.copy(
                    status = FriendRelationStatus.PENDING,
                    user = FakeDate.userHamid,
                    requestedBy = FakeDate.userHamid.id
                ),
                currentUserRequested = true
            ) {}
            FriendRow(
                user = user.copy(status = FriendRelationStatus.REJECTED)
            ) {}
            FriendRow(
                user = user.copy(status = FriendRelationStatus.BLOCKED)
            ) {}
        }
    }
}

fun String.phoneMapToView(): String {
    if (this.length != 11)
        return this

    val a = this.take(4)
    val b = this.drop(4).take(3)
    val c = this.drop(7)

    return "$a $b $c"
}

fun String.upperFirstChar() =
    this.run {
        this.take(1).uppercase() + this.lowercase().drop(1)
    }

@Composable
fun FriendState(state: FriendRelationStatus) {
    val containerColor: Color
    val contentColor: Color
    val icon: ImageVector
    when (state) {
        FriendRelationStatus.ACCEPTED -> {
            containerColor = AppTheme.colors.successContainer
            contentColor = AppTheme.colors.onSuccessContainer
            icon = Icons.Default.Check
        }

        FriendRelationStatus.PENDING -> {
            containerColor = AppTheme.colors.secondaryContainer
            contentColor = AppTheme.colors.onSecondaryContainer
            icon = Icons.Default.Nightlight
        }

        FriendRelationStatus.REJECTED -> {
            containerColor = AppTheme.colors.background
            contentColor = AppTheme.colors.onBackground
            icon = Icons.Default.Cancel
        }

        FriendRelationStatus.BLOCKED -> {
            containerColor = AppTheme.colors.errorContainer
            contentColor = AppTheme.colors.onErrorContainer
            icon = Icons.Default.Block
        }
    }

    Column(
        modifier = Modifier.clip(CardDefaults.shape).background(containerColor)
            .padding(vertical = 2.dp, horizontal = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.name.upperFirstChar(),
                style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = icon,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}