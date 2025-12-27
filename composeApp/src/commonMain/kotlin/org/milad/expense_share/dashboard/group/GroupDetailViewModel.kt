package org.milad.expense_share.dashboard.group

import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import model.Group
import model.User
import org.milad.expense_share.dashboard.group.components.GroupTab

class GroupDetailViewModel(
    initialGroup: Group,
) : BaseViewModel<GroupDetailAction, GroupDetailState, GroupDetailEvent>(
    initialState = GroupDetailState(
        selectedGroup = initialGroup
    )
) {
    override fun handle(action: GroupDetailAction) {
        when (action) {
            GroupDetailAction.AddExpense -> TODO()
            GroupDetailAction.DismissDialog -> TODO()
            GroupDetailAction.NavigateBack -> TODO()
            GroupDetailAction.ShowMemberSheet -> TODO()
            GroupDetailAction.ShowDeleteGroupDialog -> TODO()
            is GroupDetailAction.ApproveTransaction -> TODO()
            is GroupDetailAction.DeleteTransaction -> TODO()
            is GroupDetailAction.EditTransaction -> TODO()
            is GroupDetailAction.RejectTransaction -> TODO()
            is GroupDetailAction.DeleteGroup -> TODO()
            is GroupDetailAction.RenameGroup -> TODO()
            is GroupDetailAction.SelectTab -> TODO()
            is GroupDetailAction.ShowDeleteMemberDialog -> TODO()
            is GroupDetailAction.ShowHelp -> TODO()
            is GroupDetailAction.UpdateMembers -> TODO()
        }
    }
}

sealed interface GroupDetailAction : BaseViewAction {
    data object NavigateBack : GroupDetailAction

    data class SelectTab(val tab: GroupTab) : GroupDetailAction

    data object AddExpense : GroupDetailAction
    data class ApproveTransaction(val transactionId: String) : GroupDetailAction
    data class RejectTransaction(val transactionId: String) : GroupDetailAction
    data class EditTransaction(val transactionId: String) : GroupDetailAction
    data class DeleteTransaction(val transactionId: String) : GroupDetailAction

    data object ShowMemberSheet : GroupDetailAction
    data object ShowDeleteGroupDialog : GroupDetailAction
    data class ShowDeleteMemberDialog(val user: User) : GroupDetailAction
    data class UpdateMembers(val memberIds: List<Int>) : GroupDetailAction

    data class DeleteGroup(val groupId: Int) : GroupDetailAction
    data class RenameGroup(val groupId: Int) : GroupDetailAction
    data class ShowHelp(val groupId: Int) : GroupDetailAction

    data object DismissDialog : GroupDetailAction
}

data class GroupDetailState(
    val selectedGroup: Group,
    val currentUser: User? = null,
    val selectedTab: GroupTab = GroupTab.Expenses,
    val friends: List<User> = emptyList(),
    val transactionLoading: Boolean = false,
    val transactionError: Throwable? = null,
    val dialogState: DialogState = DialogState.None,

    val isListAndDetailVisible: Boolean = false,
    val isDetailVisible: Boolean = false,
) : BaseViewState {
    val isOwner: Boolean
        get() = selectedGroup.ownerId == currentUser?.id

    val showBackButton: Boolean
        get() = isDetailVisible && !isListAndDetailVisible
}

sealed interface GroupDetailEvent : BaseViewEvent {
}

sealed interface DialogState {
    data object None : DialogState
    data object DeleteGroup : DialogState
    data class DeleteMember(val user: User) : DialogState
    data object MemberSelection : DialogState
}