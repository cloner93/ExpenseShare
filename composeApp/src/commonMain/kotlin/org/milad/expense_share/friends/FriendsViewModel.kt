package org.milad.expense_share.friends

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.FriendInfo
import model.User
import usecase.friends.GetAllFriendsUseCase
import usecase.user.GetUserInfoUseCase

class FriendsViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getAllFriendsUseCase: GetAllFriendsUseCase,
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
            }

            FriendsAction.NavigateBack -> setState { it .copy(selectedFriend = null, isDetailVisible = false)}
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
                        listPaneLoading = false
                    )
                }
            }.onFailure { e ->
                setState { it.copy(listPaneLoading = false) }
                postEvent(FriendsEvent.ShowToast("Error fetch friends: ${e.message}"))
            }
        }
    }

    private suspend fun getUserInfo() {
        val currentUser = getUserInfoUseCase()
        setState { it.copy(currentUser = currentUser) }
    }
}

sealed interface FriendsAction : BaseViewAction {
    data class SelectFriend(val friend: FriendInfo) : FriendsAction
    data object NavigateBack : FriendsAction
}

data class FriendsState(
    val currentUser: User? = null,
    val listPaneLoading: Boolean = true,
    val isDetailVisible: Boolean = false,
    val friends: List<FriendInfo> = emptyList(),
    val selectedFriend: FriendInfo? = null,
) : BaseViewState

sealed interface FriendsEvent : BaseViewEvent {
    data class ShowToast(val message: String) : FriendsEvent
}
