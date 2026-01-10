package org.milad.expense_share.dashboard.group

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.Group
import model.Transaction
import model.TransactionStatus
import model.User
import org.milad.expense_share.dashboard.group.components.GroupTab
import usecase.friends.GetFriendsUseCase
import usecase.groups.DeleteGroupUseCase
import usecase.groups.UpdateGroupMembersUseCase
import usecase.transactions.ApproveTransactionUseCase
import usecase.transactions.DeleteTransactionUseCase
import usecase.transactions.RejectTransactionUseCase

class GroupDetailViewModel(
    initialGroup: Group,
    currentUser: User,
    isListAndDetailVisible: Boolean,
    isDetailVisible: Boolean,

    private val approveTransactionUseCase: ApproveTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val rejectTransactionUseCase: RejectTransactionUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val updateGroupUseCase: UpdateGroupMembersUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
) : BaseViewModel<GroupDetailAction, GroupDetailState, GroupDetailEvent>(
    initialState = GroupDetailState(
        selectedGroup = initialGroup,
        currentUser = currentUser,
        isListAndDetailVisible = isListAndDetailVisible,
        isDetailVisible = isDetailVisible
    )
) {
    override fun handle(action: GroupDetailAction) {
        when (action) {
            GroupDetailAction.DismissDialog -> setState { it.copy(dialogState = DialogState.None) }
            GroupDetailAction.NavigateBack -> postEvent(GroupDetailEvent.NavigateBack)
            GroupDetailAction.ShowMemberSheet -> setState { it.copy(dialogState = DialogState.MemberSelection) }
            GroupDetailAction.ShowDeleteGroupDialog -> setState { it.copy(dialogState = DialogState.DeleteGroup) }
            is GroupDetailAction.ApproveTransaction -> {
                approveTransaction(
                    groupId = viewState.value.selectedGroup.id.toString(),
                    transactionId = action.transactionId
                )
            }

            is GroupDetailAction.DeleteTransaction -> {
                deleteTransaction(
                    groupId = viewState.value.selectedGroup.id.toString(),
                    transactionId = action.transactionId
                )
            }

            is GroupDetailAction.RejectTransaction -> {
                rejectTransaction(
                    groupId = viewState.value.selectedGroup.id.toString(),
                    transactionId = action.transactionId
                )
            }

            is GroupDetailAction.EditTransaction -> {}

            is GroupDetailAction.DeleteGroup -> deleteGroup(action.groupId.toString())
            is GroupDetailAction.SelectTab -> {
                setState { it.copy(selectedTab = action.tab) }
                if (action.tab == GroupTab.Members && viewState.value.friends.isEmpty()) loadUserFriends()
            }

            is GroupDetailAction.ShowDeleteMemberDialog -> {
                setState { it.copy(dialogState = DialogState.DeleteMember(action.user)) }
            }

            is GroupDetailAction.UpdateMembers -> {
                updateGroupMembers(action.memberIds)
            }

            is GroupDetailAction.UpdateGroup -> {
                setState { it.copy(selectedGroup = action.group) }
            }

            is GroupDetailAction.ShowHelp -> TODO("Not yet implemented")
            is GroupDetailAction.RenameGroup -> TODO("Not yet implemented")
        }
    }

    private fun approveTransaction(groupId: String, transactionId: String) {
        viewModelScope.launch {
            setState { it.copy(transactionLoading = true, transactionError = null) }

            approveTransactionUseCase(groupId, transactionId).collect { result ->
                result.onSuccess {
                    val trx =
                        viewState.value.selectedGroup.transactions.find { it.id == transactionId.toInt() }
                            ?.copy(status = TransactionStatus.APPROVED)!!
                    setState {
                        it.copy(
                            transactionLoading = false, transactionError = null,
                            selectedGroup = it.selectedGroup.copy(
                                transactions = it.selectedGroup.transactions.map { item ->
                                    if (item.id == trx.id) trx else item
                                }
                            )
                        )
                    }
                    postEvent(
                        GroupDetailEvent.UpdateTransactionsOfCurrentGroup(
                            transactions = viewState.value.selectedGroup.transactions
                        )
                    )
                }.onFailure { e ->
                    setState { it.copy(transactionError = e, transactionLoading = false) }
                }
            }
        }
    }

    private fun rejectTransaction(groupId: String, transactionId: String) {
        viewModelScope.launch {
            setState { it.copy(transactionLoading = true, transactionError = null) }

            rejectTransactionUseCase(groupId, transactionId).collect { result ->
                result.onSuccess {
                    val trx =
                        viewState.value.selectedGroup.transactions.find { it.id == transactionId.toInt() }
                            ?.copy(status = TransactionStatus.REJECTED)!!

                    setState {
                        it.copy(
                            transactionLoading = false, transactionError = null,
                            selectedGroup = it.selectedGroup.copy(
                                transactions = it.selectedGroup.transactions.map { item ->
                                    if (item.id == trx.id) trx else item
                                }
                            )
                        )
                    }
                    postEvent(
                        GroupDetailEvent.UpdateTransactionsOfCurrentGroup(
                            transactions = viewState.value.selectedGroup.transactions
                        )
                    )
                }.onFailure { e ->
                    setState { it.copy(transactionError = e, transactionLoading = false) }
                }
            }
        }
    }

    private fun deleteTransaction(groupId: String, transactionId: String) {
        viewModelScope.launch {
            setState { it.copy(transactionLoading = true, transactionError = null) }

            deleteTransactionUseCase(groupId, transactionId).collect { result ->
                result.onSuccess {
                    setState {
                        it.copy(
                            transactionLoading = false, transactionError = null,
                            selectedGroup = it.selectedGroup.copy(
                                transactions = it.selectedGroup.transactions.filter { trx -> trx.id != transactionId.toInt() }
                            ))
                    }
                    postEvent(
                        GroupDetailEvent.UpdateTransactionsOfCurrentGroup(
                            transactions = viewState.value.selectedGroup.transactions.filter { trx -> trx.id != transactionId.toInt() })
                    )
                }.onFailure { e ->
                    setState { it.copy(transactionError = e, transactionLoading = false) }
                }
            }
        }
    }

    private fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            setState { it.copy(isLoading = true, error = null) }
            deleteGroupUseCase(groupId).collect { result ->
                result.onSuccess {
                    setState { it.copy(isLoading = false, error = null) }

                    postEvent(GroupDetailEvent.DeleteCurrentGroup)
                }.onFailure {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = it.error
                        )
                    }
                }
            }
        }
    }

    private fun updateGroupMembers(userId: List<Int>) {
        viewModelScope.launch {
            setState {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            updateGroupUseCase(
                viewState.value.selectedGroup.id.toString(),
                userId
            ).collect { result ->
                result.onSuccess {
                    var updatedMembers: List<User> = emptyList()
                    setState { state ->
                        val allPotentialUsers =
                            (state.friends + state.selectedGroup.members + state.currentUser).distinct()
                        updatedMembers = userId.mapNotNull { id ->
                            allPotentialUsers.find { user -> user.id == id }
                        }
                        state.copy(
                            isLoading = false,
                            selectedGroup = state.selectedGroup.copy(
                                members = updatedMembers
                            )
                        )
                    }
                    postEvent(GroupDetailEvent.UpdateMembersOfCurrentGroup(updatedMembers))
                }.onFailure { e ->
                    setState {
                        it.copy(
                            isLoading = false,
                            error = e
                        )
                    }
                }
            }
        }
    }

    private fun loadUserFriends() {
        viewModelScope.launch {
            setState {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            getFriendsUseCase().collect { result ->
                result.onSuccess { newFriends ->
                    setState {
                        it.copy(
                            isLoading = false,
                            friends = it.friends + newFriends,
                        )
                    }
                }.onFailure { e ->
                    setState {
                        it.copy(
                            isLoading = false,
                            error = e
                        )
                    }
                }
            }
        }
    }
}

sealed interface GroupDetailAction : BaseViewAction {
    data object NavigateBack : GroupDetailAction

    data class SelectTab(val tab: GroupTab) : GroupDetailAction

    data class ApproveTransaction(val transactionId: String) : GroupDetailAction
    data class RejectTransaction(val transactionId: String) : GroupDetailAction
    data class EditTransaction(val transactionId: String) : GroupDetailAction
    data class DeleteTransaction(val transactionId: String) : GroupDetailAction

    data object ShowMemberSheet : GroupDetailAction
    data object ShowDeleteGroupDialog : GroupDetailAction
    data class ShowDeleteMemberDialog(val user: User) : GroupDetailAction
    data class UpdateMembers(val memberIds: List<Int>) : GroupDetailAction
    data class UpdateGroup(val group: Group) : GroupDetailAction

    data class DeleteGroup(val groupId: Int) : GroupDetailAction
    data class RenameGroup(val groupId: Int) : GroupDetailAction
    data class ShowHelp(val groupId: Int) : GroupDetailAction

    data object DismissDialog : GroupDetailAction
}

data class GroupDetailState(
    val selectedGroup: Group,
    val currentUser: User,
    val selectedTab: GroupTab = GroupTab.Expenses,
    val friends: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val transactionLoading: Boolean = false,
    val error: Throwable? = null,
    val transactionError: Throwable? = null,
    val dialogState: DialogState = DialogState.None,

    val isListAndDetailVisible: Boolean = false,
    val isDetailVisible: Boolean = false,
) : BaseViewState {
    val isOwner: Boolean
        get() = selectedGroup.ownerId == currentUser.id

    val showBackButton: Boolean
        get() = isDetailVisible && !isListAndDetailVisible
}

sealed interface GroupDetailEvent : BaseViewEvent {
    data object NavigateBack : GroupDetailEvent
    data class UpdateTransactionsOfCurrentGroup(val transactions: List<Transaction>) :
        GroupDetailEvent

    data class UpdateMembersOfCurrentGroup(val memberIds: List<User>) : GroupDetailEvent
    data object DeleteCurrentGroup : GroupDetailEvent
}

sealed interface DialogState {
    data object None : DialogState
    data object DeleteGroup : DialogState
    data class DeleteMember(val user: User) : DialogState
    data object MemberSelection : DialogState
}