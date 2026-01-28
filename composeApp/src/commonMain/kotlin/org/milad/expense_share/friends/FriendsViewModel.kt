package org.milad.expense_share.friends

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import model.User
import org.milad.expense_share.dashboard.group.components.FakeDate
import usecase.friends.GetFriendsUseCase
import usecase.user.GetUserInfoUseCase

class FriendsViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
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
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { getFriends() }
            getUserInfo()
        }
    }

    private suspend fun getFriends() {
        /*getFriendsUseCase()*/

        flow {
            emit(Result.success(FakeDate.mockFriends))
        }.collect { result ->
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
    data class SelectFriend(val friend: Friend) : FriendsAction
}

data class FriendsState(
    val currentUser: User? = null,
    val listPaneLoading: Boolean = true,
    val isDetailVisible: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val selectedFriend: Friend? = null,
) : BaseViewState

sealed interface FriendsEvent : BaseViewEvent {
    data class ShowToast(val message: String) : FriendsEvent
}
