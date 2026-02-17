package org.milad.expense_share.friends

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.FriendInfo
import model.FriendRelationStatus
import model.User
import usecase.friends.AcceptFriendRequestUseCase
import usecase.friends.CancelFriendRequestUseCase
import usecase.friends.GetAllFriendsUseCase
import usecase.friends.RejectFriendRequestUseCase
import usecase.friends.SendFriendRequestUseCase
import usecase.user.GetUserInfoUseCase

class FriendsViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getAllFriendsUseCase: GetAllFriendsUseCase,

    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val cancelFriendRequestUseCase: CancelFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
) : BaseViewModel<FriendsAction, FriendsState, FriendsEvent>(
    initialState = FriendsState()
) {
    init {
        loadData()
    }

    override fun handle(action: FriendsAction) {
        when(action) {
            is FriendsAction.SelectFriend -> {
                setState {
                    it.copy(
                        selectedFriend = action.friend,
                        isDetailVisible = true
                    )
                }
                postEvent(
                    FriendsEvent.ShowFriendDetail
                )
            }

            FriendsAction.NavigateBack -> setState { it .copy(selectedFriend = null, isDetailVisible = false)}
            FriendsAction.DismissDialog -> setState {
                it.copy(
                    friendsListDialogState = FriendsListDialogState.None,
                    selectedFriend = null
                )
            }

            is FriendsAction.CancelFriendRequest -> cancelFriendRequest(action.targetPhone)
            is FriendsAction.ShowCancelFriendRequest -> setState {
                it.copy(
                    selectedFriend = action.friend,
                    isDetailVisible = false,
                    friendsListDialogState = FriendsListDialogState.CancelRequest
                )
            }

            is FriendsAction.AcceptFriendRequest -> acceptFriendRequest(action.targetPhone)
            is FriendsAction.ShowAcceptFriendRequest -> setState {
                it.copy(
                    selectedFriend = action.friend,
                    isDetailVisible = false,
                    friendsListDialogState = FriendsListDialogState.AcceptRequest
                )
            }

            is FriendsAction.RejectFriendRequest -> rejectFriendRequest(action.targetPhone)
            is FriendsAction.ShowRejectFriendRequest -> setState {
                it.copy(
                    selectedFriend = action.friend,
                    isDetailVisible = false,
                    friendsListDialogState = FriendsListDialogState.RejectRequest
                )
            }

            FriendsAction.ShowSentRequest ->
                setState {
                    it.copy(
                        friendsListDialogState = FriendsListDialogState.NewRequest,
                    )
                }

            is FriendsAction.SentRequest -> {
                setState { it.copy(friendsListDialogState = FriendsListDialogState.NewRequest) }
                sendFriendRequest(action.targetPhone)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { getFriends() }
            getUserInfo()
        }
    }

    private suspend fun getFriends() {
        getAllFriendsUseCase().collect { result ->
            result.onSuccess { newFriends ->
                setState {
                    it.copy(
                        friends = it.friends + newFriends,
                        listPaneLoading = false,
                        listPaneError = null
                    )
                }
            }.onFailure { e ->
                setState { it.copy(listPaneLoading = false, listPaneError = e) }
                postEvent(FriendsEvent.ShowToast("Error fetch friends: ${e.message}"))
            }
        }
    }

    private suspend fun getUserInfo() {
        val currentUser = getUserInfoUseCase()
        setState { it.copy(currentUser = currentUser) }
    }

    private fun sendFriendRequest(targetPhone: String) {
        viewModelScope.launch {
//        setState { it.copy(friendActionLoading = true, friendActionError = null) }

            sendFriendRequestUseCase(targetPhone).collect { result ->
                result.onSuccess {
                    print(it) // FIXME:
                }.onFailure {
                    print(it.message)
                }
            }
        }
    }


    private fun cancelFriendRequest(targetPhone: String) {
        viewModelScope.launch {
            setState { it.copy(friendActionLoading = true, friendActionError = null) }
            cancelFriendRequestUseCase(targetPhone).collect { result ->
                result.onSuccess {
                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = null,
                            friends = it.friends.filter { friend -> friend.user.phone != targetPhone }
                        )
                    }
                }.onFailure { e ->
                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = e
                        )
                    }
                }
            }
        }
    }

    private fun acceptFriendRequest(targetPhone: String) {
        viewModelScope.launch {
            setState { it.copy(friendActionLoading = true, friendActionError = null) }
            acceptFriendRequestUseCase(targetPhone).collect { result ->
                result.onSuccess {
                    val friend = viewState.value.friends.find { it.user.phone.equals(targetPhone) }
                        ?.copy(status = FriendRelationStatus.ACCEPTED)!!

                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = null,
                            friends = it.friends.map { item ->
                                if (item.user.id == friend.user.id) friend else item
                            }

                        )
                    }
                }.onFailure { e ->
                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = e
                        )
                    }
                }
            }
        }
    }

    private fun rejectFriendRequest(targetPhone: String) {
        viewModelScope.launch {
            setState { it.copy(friendActionLoading = true, friendActionError = null) }
            rejectFriendRequestUseCase(targetPhone).collect { result ->
                result.onSuccess {
                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = null,
                            friends = it.friends.filter { friend -> friend.user.phone != targetPhone }
                        )
                    }
                }.onFailure { e ->
                    setState {
                        it.copy(
                            friendActionLoading = false,
                            friendActionError = e
                        )
                    }
                }
            }
        }
    }
}

sealed interface FriendsAction : BaseViewAction {
    data class SelectFriend(val friend: FriendInfo) : FriendsAction

    data class ShowCancelFriendRequest(val friend: FriendInfo) : FriendsAction
    data class CancelFriendRequest(val targetPhone: String) : FriendsAction

    data class ShowRejectFriendRequest(val friend: FriendInfo) : FriendsAction
    data class RejectFriendRequest(val targetPhone: String) : FriendsAction

    data class ShowAcceptFriendRequest(val friend: FriendInfo) : FriendsAction
    data class AcceptFriendRequest(val targetPhone: String) : FriendsAction

    data object DismissDialog : FriendsAction
    data object ShowSentRequest : FriendsAction
    data class SentRequest(val targetPhone: String) : FriendsAction

    data object NavigateBack : FriendsAction
}

data class FriendsState(
    val currentUser: User? = null,
    val listPaneLoading: Boolean = true,
    val listPaneError: Throwable? = null,
    val friendActionLoading: Boolean = true,
    val friendActionError: Throwable? = null,
    val isDetailVisible: Boolean = false,
    val friends: List<FriendInfo> = emptyList(),
    val selectedFriend: FriendInfo? = null,

    val friendsListDialogState: FriendsListDialogState = FriendsListDialogState.None,
) : BaseViewState

sealed interface FriendsEvent : BaseViewEvent {
    data class ShowToast(val message: String) : FriendsEvent
    data object ShowFriendDetail : FriendsEvent
}

sealed interface FriendsListDialogState {
    data object None : FriendsListDialogState
    data object NewRequest : FriendsListDialogState
    data object CancelRequest : FriendsListDialogState
    data object AcceptRequest : FriendsListDialogState
    data object RejectRequest : FriendsListDialogState
}