@file:OptIn(ExperimentalMaterial3Api::class)

package org.milad.expense_share.friends

import EmptySelectionPlaceholder
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.pmb.common.loading.FullScreenLoading
import com.pmb.common.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.milad.expense_share.friends.detail.FriendDetailAction
import org.milad.expense_share.friends.detail.FriendDetailEvent
import org.milad.expense_share.friends.detail.FriendDetailScreen
import org.milad.expense_share.friends.detail.FriendDetailViewModel
import org.milad.expense_share.friends.friendsList.FriendsList

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = koinViewModel(),
) {
    val state by friendsViewModel.viewState.collectAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val scope = rememberCoroutineScope()
    val loading by mutableStateOf(false)

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded && navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
    val isDetailVisible =
        state.isDetailVisible || navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    ListDetailPaneScaffold(
        modifier = Modifier.background(color = AppTheme.colors.background),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            state.currentUser?.let { currentUser ->
                FriendsList(
                    currentUser = currentUser,
                    friends = state.friends,
                    onCancelRequest = {},
                    onRejectRequest = {},
                    onAcceptRequest = {},
                    onFriendClick = {
                        friendsViewModel.handle(FriendsAction.SelectFriend(it))
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
                    },
                    selectedFriend = state.selectedFriend,
                )
                if (loading) FullScreenLoading()
            }
        },
        detailPane = {
            state.selectedFriend?.let { selectedFriend ->
                val viewModel: FriendDetailViewModel = koinViewModel(
                    key = "friend_${selectedFriend.user.id}", parameters = {
                        parametersOf(
                            selectedFriend,
                            state.currentUser,
                        )
                    })

                LaunchedEffect(selectedFriend) {
                    viewModel.handle(FriendDetailAction.UpdateFriend(selectedFriend.user))
                }

                LaunchedEffect(Unit) {
                    viewModel.viewEvent.collect { event ->
                        when (event) {
                            is FriendDetailEvent.NavigateBack -> {
                                friendsViewModel.handle(FriendsAction.NavigateBack)
                                scope.launch { navigator.navigateBack() }
                            }

                            is FriendDetailEvent.OpenGroup -> {
                            }

                            is FriendDetailEvent.ShowToast -> {
                            }

                            is FriendDetailEvent.ShowSettleUpDialog -> {
                            }
                        }
                    }
                }

                FriendDetailScreen(
                    state = viewModel.viewState.collectAsState().value,
                    showBackButton = isDetailVisible && !isListAndDetailVisible,
                    onAction = viewModel::handle
                )
            } ?: run {
                EmptySelectionPlaceholder()
            }
        },
    )
}