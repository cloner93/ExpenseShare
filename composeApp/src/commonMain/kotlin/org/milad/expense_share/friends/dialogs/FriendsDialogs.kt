package org.milad.expense_share.friends.dialogs

import androidx.compose.runtime.Composable
import org.milad.expense_share.friends.FriendsAction
import org.milad.expense_share.friends.FriendsAction.AcceptFriendRequest
import org.milad.expense_share.friends.FriendsAction.CancelFriendRequest
import org.milad.expense_share.friends.FriendsAction.RejectFriendRequest
import org.milad.expense_share.friends.FriendsListDialogState
import org.milad.expense_share.friends.FriendsState

@Composable
fun FriendsDialogs(
    state: FriendsState,
    onAction: (FriendsAction) -> Unit,
) {
    when (state.friendsListDialogState) {
        is FriendsListDialogState.None -> { /* No dialog */
        }

        FriendsListDialogState.NewRequest -> {
            SendNewRequestSheet(
                visible = true,
                loading = state.friendActionLoading,
                error = state.friendActionError,
                onConfirm = { onAction(FriendsAction.SentRequest(it)) },
                onDismiss = { onAction(FriendsAction.DismissDialog) },
            )
        }

        is FriendsListDialogState.CancelRequest -> {
            ConfirmationSheet(
                title = "Cancel the request",
                content = "Are you sure you want to cancel the request to {${state.selectedFriend!!.user.username}}? This action cannot be undone.",
                isVisible = true,
                onConfirm = {
                    onAction(CancelFriendRequest(state.selectedFriend.user.phone))
                },
                onDismiss = { onAction(FriendsAction.DismissDialog) }
            )
        }

        is FriendsListDialogState.RejectRequest -> {

            ConfirmationSheet(
                title = "Reject the request",
                content = "Are you sure you want to reject the request of {${state.selectedFriend!!.user.username}}? This action cannot be undone.",
                isVisible = true,
                onConfirm = {
                    onAction(RejectFriendRequest(state.selectedFriend.user.phone))
                },
                onDismiss = { onAction(FriendsAction.DismissDialog) }
            )
        }

        is FriendsListDialogState.AcceptRequest -> {

            ConfirmationSheet(
                title = "Cancel the request",
                content = "Are you sure you want to Accept the request of {${state.selectedFriend!!.user.username}}? This action cannot be undone.",
                isVisible = true,
                onConfirm = {
                    onAction(AcceptFriendRequest(state.selectedFriend.user.phone))
                },
                onDismiss = { onAction(FriendsAction.DismissDialog) }
            )
        }

    }
}