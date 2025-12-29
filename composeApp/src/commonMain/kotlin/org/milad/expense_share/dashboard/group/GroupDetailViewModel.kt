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
            GroupDetailAction.DismissDialog -> TODO()
            GroupDetailAction.NavigateBack -> postEvent(GroupDetailEvent.NavigateBack)
            GroupDetailAction.ShowMemberSheet -> TODO()
            GroupDetailAction.ShowDeleteGroupDialog -> TODO()
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
            is GroupDetailAction.DeleteGroup -> TODO()
            is GroupDetailAction.RenameGroup -> TODO()
            is GroupDetailAction.SelectTab -> setState { it.copy(selectedTab = action.tab) }
            is GroupDetailAction.ShowDeleteMemberDialog -> TODO()
            is GroupDetailAction.ShowHelp -> TODO()
            is GroupDetailAction.UpdateMembers -> TODO()
            is GroupDetailAction.UpdateGroup -> {
                setState { it.copy(selectedGroup = action.group) }
            }
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
                        GroupDetailEvent.UpdateTransactionsOfGroup(
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
                        GroupDetailEvent.UpdateTransactionsOfGroup(
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
                        GroupDetailEvent.UpdateTransactionsOfGroup(
                            transactions = viewState.value.selectedGroup.transactions.filter { trx -> trx.id != transactionId.toInt() })
                    )
                }.onFailure { e ->
                    setState { it.copy(transactionError = e, transactionLoading = false) }
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
    val transactionLoading: Boolean = false,
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
    data class UpdateTransactionsOfGroup(val transactions: List<Transaction>) : GroupDetailEvent
}

sealed interface DialogState {
    data object None : DialogState
    data object DeleteGroup : DialogState
    data class DeleteMember(val user: User) : DialogState
    data object MemberSelection : DialogState
}