package org.milad.expense_share.friends.detail

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.Group
import model.User
import org.milad.expense_share.Amount
import org.milad.expense_share.friends.model.FriendBalance
import org.milad.expense_share.friends.model.FriendTab
import org.milad.expense_share.friends.model.SettlementItem
import org.milad.expense_share.friends.model.TransactionWithGroup
import usecase.groups.GetGroupsUseCase

class FriendDetailViewModel(
    private val friend: User,
    private val currentUser: User,
    private val getGroupsUseCase: GetGroupsUseCase,
) : BaseViewModel<FriendDetailAction, FriendDetailState, FriendDetailEvent>(
    initialState = FriendDetailState(
        friend = friend,
        currentUser = currentUser
    )
) {

    init {
        loadFriendDetails()
    }

    override fun handle(action: FriendDetailAction) {
        when (action) {
            is FriendDetailAction.LoadDetails -> loadFriendDetails()
            is FriendDetailAction.SelectTab -> setState { it.copy(selectedTab = action.tab) }
            is FriendDetailAction.NavigateBack -> postEvent(FriendDetailEvent.NavigateBack)
            is FriendDetailAction.OpenGroup -> postEvent(FriendDetailEvent.OpenGroup(action.group))
            is FriendDetailAction.SettleUp -> handleSettleUp()
            is FriendDetailAction.SendReminder -> handleSendReminder()
            is FriendDetailAction.UpdateFriend -> setState { it.copy(friend = action.friend) }
        }
    }

    private fun loadFriendDetails() {
        viewModelScope.launch {
            setState { it.copy(isLoading = true, error = null) }
            
            try {
                getGroupsUseCase().collect { result ->
                    result.onSuccess { allGroups ->
                        val sharedGroups = allGroups.filter { group ->
                            group.members.any { it.id == friend.id } &&
                            group.members.any { it.id == currentUser.id }
                        }
                        
                        val balance = calculateBalance(sharedGroups, currentUser.id, friend.id)
                        
                        val transactions = extractTransactions(sharedGroups, currentUser.id, friend.id)
                        
                        setState {
                            it.copy(
                                sharedGroups = sharedGroups,
                                balance = balance,
                                recentTransactions = transactions,
                                isLoading = false
                            )
                        }
                    }.onFailure { error ->
                        setState { it.copy(error = error, isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                setState { it.copy(error = e, isLoading = false) }
            }
        }
    }

    private fun calculateBalance(
        groups: List<Group>,
        currentUserId: Int,
        friendId: Int
    ): FriendBalance {
        var totalOwed = Amount(0)
        var totalOwe = Amount(0)

        groups.forEach { group ->
            group.transactions.forEach { transaction ->
                val iPaid = transaction.payers
                    .filter { it.user.id == currentUserId }
                    .sumOf { it.amountPaid.value }

                val friendPaid = transaction.payers
                    .filter { it.user.id == friendId }
                    .sumOf { it.amountPaid.value }

                val myShare = transaction.shareDetails.members
                    .filter { it.user.id == currentUserId }
                    .sumOf { it.share.value }

                val friendShare = transaction.shareDetails.members
                    .filter { it.user.id == friendId }
                    .sumOf { it.share.value }

                if (iPaid > 0 && friendShare > 0) {
                    val friendOwesMe = Amount((friendShare * iPaid / transaction.amount.value))
                    totalOwed += friendOwesMe
                }

                if (friendPaid > 0 && myShare > 0) {
                    val iOweFriend = Amount((myShare * friendPaid / transaction.amount.value))
                    totalOwe += iOweFriend
                }
            }
        }

        val net = totalOwed - totalOwe

        return FriendBalance(
            totalOwed = totalOwed,
            totalOwe = totalOwe,
            netBalance = net
        )
    }

    private fun extractTransactions(
        groups: List<Group>,
        currentUserId: Int,
        friendId: Int
    ): List<TransactionWithGroup> {
        val transactions = mutableListOf<TransactionWithGroup>()

        groups.forEach { group ->
            group.transactions
                .filter { transaction ->
                    transaction.shareDetails.members.any { it.user.id == currentUserId } &&
                    transaction.shareDetails.members.any { it.user.id == friendId }
                }
                .forEach { transaction ->
                    val myShare = transaction.shareDetails.members
                        .find { it.user.id == currentUserId }?.share ?: Amount(0)
                    
                    val friendShare = transaction.shareDetails.members
                        .find { it.user.id == friendId }?.share ?: Amount(0)

                    transactions.add(
                        TransactionWithGroup(
                            transaction = transaction,
                            group = group,
                            myShare = myShare,
                            friendShare = friendShare
                        )
                    )
                }
        }

        return transactions.sortedByDescending { it.transaction.createdAt }
    }

    private fun handleSettleUp() {
        postEvent(FriendDetailEvent.ShowSettleUpDialog(viewState.value.balance))
    }

    private fun handleSendReminder() {
        postEvent(FriendDetailEvent.ShowToast("Reminder sent to ${friend.username}"))
    }
}

data class FriendDetailState(
    val friend: User,
    val currentUser: User?,
    val balance: FriendBalance = FriendBalance(),
    val sharedGroups: List<Group> = emptyList(),
    val recentTransactions: List<TransactionWithGroup> = emptyList(),
    val pendingSettlements: List<SettlementItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val selectedTab: FriendTab = FriendTab.Overview
):BaseViewState

sealed interface FriendDetailAction : BaseViewAction {
    data object LoadDetails : FriendDetailAction
    data object NavigateBack : FriendDetailAction
    data class SelectTab(val tab: FriendTab) : FriendDetailAction
    data class OpenGroup(val group: Group) : FriendDetailAction
    data class UpdateFriend(val friend: User) : FriendDetailAction
    data object SettleUp : FriendDetailAction
    data object SendReminder : FriendDetailAction
}

sealed interface FriendDetailEvent : BaseViewEvent {
    data object NavigateBack : FriendDetailEvent
    data class OpenGroup(val group: Group) : FriendDetailEvent
    data class ShowToast(val message: String) : FriendDetailEvent
    data class ShowSettleUpDialog(val balance: FriendBalance) : FriendDetailEvent
}