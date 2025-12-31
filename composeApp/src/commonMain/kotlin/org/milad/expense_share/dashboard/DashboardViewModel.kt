package org.milad.expense_share.dashboard

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Group
import model.PayerDto
import model.ShareDetailsRequest
import model.Transaction
import model.User
import org.milad.expense_share.Amount
import org.milad.expense_share.logger.AppLogger
import usecase.friends.GetFriendsUseCase
import usecase.groups.CreateGroupUseCase
import usecase.groups.GetGroupsUseCase
import usecase.transactions.CreateTransactionUseCase
import usecase.user.GetUserInfoUseCase

class DashboardViewModel(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getGroupsUseCase: GetGroupsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase,
) : BaseViewModel<DashboardAction, DashboardState, DashboardEvent>(
    initialState = DashboardState()
) {

    init {
        handle(DashboardAction.LoadData)
    }

    override fun handle(action: DashboardAction) {
        when (action) {
            is DashboardAction.LoadData -> loadData()
            is DashboardAction.SelectGroup -> setState {
                it.copy(
                    selectedGroup = action.group,
                    isDetailVisible = true,
                )
            }

            is DashboardAction.NavigateBack -> navigateBack()
            is DashboardAction.AddGroup -> createGroup(action.groupName, action.members)
            is DashboardAction.ShowExtraPane -> {
                setState { it.copy(extraPaneContentState = action.content) }
            }

            is DashboardAction.AddExpense -> createTransaction(
                action.expenseName,
                action.amount,
                action.desc,
                action.payers,
                action.shareDetails,
            )

            DashboardAction.LoadTesting -> {
                viewModelScope.launch {
                    print("strat")
                    setState { it.copy(extraPaneLoading = true) }
                    delay(1000)
                    setState { it.copy(extraPaneLoading = false) }
                    delay(1000)
                    setState { it.copy(extraPaneLoading = true) }
                    delay(1000)
                    setState { it.copy(extraPaneLoading = false) }
                    setState { it.copy(extraPaneError = Throwable("Test Error")) }
                }
            }

            DashboardAction.DeleteSelectedGroup -> deleteSelectedGroup()

            is DashboardAction.UpdateGroupMembers -> {
                updateGroupMembers(action.memberIds)
            }

            is DashboardAction.UpdateTransaction -> {
                viewState.value.selectedGroup?.let { group ->
                    val group = group.copy(transactions = action.transactions)

                    setState {
                        it.copy(
                            selectedGroup = group,
                            groups = it.groups.map { g -> if (g.id == group.id) group else g }
                        )
                    }
                    AppLogger.i("updateTransaction", "updated")
                }

                /*setState {
                    it.copy(
                        selectedGroup = action.group,
                        groups = it.groups + action.group
                    )
                }*/
            }
        }
    }

    private fun updateGroupMembers(memberIds: List<User>) {
        setState {
            it.copy(
                selectedGroup = it.selectedGroup?.copy(members = memberIds),
                groups = it.groups.map { group ->
                    if (group.id == it.selectedGroup?.id) it.selectedGroup.copy(members = memberIds)
                    else group
                }
            )
        }
    }

    private fun createTransaction(
        title: String,
        amount: Amount,
        desc: String,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?,
    ) {
        viewModelScope.launch {
            setState { it.copy(extraPaneLoading = true, extraPaneError = null) }
            viewState.value.selectedGroup?.let { group ->
                createTransactionUseCase(
                    groupId = group.id,
                    title = title,
                    amount = amount,
                    description = desc,
                    payers = payers,
                    shareDetails = shareDetails
                ).collect { result ->
                    result.onSuccess { trx ->
                        setState { state ->
                            state.copy(
                                extraPaneLoading = false,
                                extraPaneError = null,
                                selectedGroup = state.selectedGroup?.copy(
                                    transactions = state.selectedGroup.transactions + trx
                                )
                            )
                        }
                        postEvent(DashboardEvent.ExtraPaneSuccessful)
                    }.onFailure { e ->
                        setState { it.copy(extraPaneError = e, extraPaneLoading = false) }
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { getGroups() }
            launch { getFriends() }
            getUserInfo()
        }
    }

    private suspend fun getUserInfo() {
        val currentUser = getUserInfoUseCase()
        setState { it.copy(currentUser = currentUser) }
    }

    private suspend fun getGroups() {
        getGroupsUseCase().collect { res ->
            setState { it.copy(listPaneLoading = true, listPaneError = null) }

            res.onSuccess { groups ->
                setState { it ->
                    val selectedGroup = viewState.value.selectedGroup

                    it.copy(
                        groups = groups,
                        listPaneLoading = false,
                        selectedGroup = if (selectedGroup != null) groups.find { it.id == selectedGroup.id } else null
                    )
                }

                launchTotalCalculation(groups)
            }.onFailure { e ->
                setState { it.copy(listPaneError = e, listPaneLoading = false) }
                postEvent(DashboardEvent.ShowToast("Error: ${e.message}"))
            }
        }
    }

    private suspend fun getFriends() {
        getFriendsUseCase().collect { result ->
            result.onSuccess { newFriends ->
                setState {
                    it.copy(
                        friends = it.friends + newFriends,
                        listPaneLoading = false
                    )
                }
            }.onFailure { e ->
                setState { it.copy(listPaneLoading = false) }
                postEvent(DashboardEvent.ShowToast("Error fetch friends: ${e.message}"))
            }
        }
    }

    private fun launchTotalCalculation(groups: List<Group>) {
        viewModelScope.launch(Dispatchers.Default) {
            val userId = getUserInfoUseCase().id
            val balance = groups.calculateBalance(userId)

            setState {
                it.copy(
                    totalOwed = balance.owed,
                    totalOwe = balance.owe
                )
            }
        }
    }

    data class Balance(
        val owed: Amount,
        val owe: Amount,
    )

    fun List<Group>.calculateBalance(userId: Int): Balance {
        var owed = Amount(0)
        var owe = Amount(0)

        forEach { group ->
            var paidByUser = Amount(0)
            var shareOfUser = Amount(0)

            group.transactions.forEach { trx ->
                trx.payers
                    .filter { it.user.id == userId }
                    .forEach { paidByUser += it.amountPaid }

                trx.shareDetails.members
                    .filter { it.user.id == userId }
                    .forEach { shareOfUser += it.share }
            }

            val net = paidByUser - shareOfUser
            when {
                net.isPositive() -> owed += net
                net.isNegative() -> owe += net.abs()
            }
        }

        return Balance(
            owed = owed,
            owe = owe
        )
    }

    private fun createGroup(groupName: String, members: List<Int>) {
        viewModelScope.launch {
            setState { it.copy(extraPaneLoading = true, extraPaneError = null) }
            createGroupUseCase(groupName, members).collect { result ->
                result.onSuccess { newGroup ->
                    setState {
                        it.copy(
                            groups = it.groups + newGroup,
                            extraPaneLoading = false,
                            extraPaneError = null
                        )
                    }
                    postEvent(DashboardEvent.ExtraPaneSuccessful)
                }.onFailure { e ->
                    setState {
                        it.copy(
                            extraPaneError = e,
                            extraPaneLoading = false
                        )
                    }
                    postEvent(DashboardEvent.ShowToast("Error creating group: ${e.message}"))
                }
            }
        }
    }

    private fun deleteSelectedGroup() {
        setState {
            it.selectedGroup?.let { selectedGroup ->
                it.copy(
                    groups = it.groups - selectedGroup,
                    selectedGroup = null
                )
            } ?: it.copy()
        }
    }

    private fun navigateBack() {
        setState { it.copy(selectedGroup = null, isDetailVisible = false) }
    }
}

sealed interface DashboardAction : BaseViewAction {
    data class ShowExtraPane(val content: ExtraPaneContentState) : DashboardAction
    data object LoadData : DashboardAction
    data object LoadTesting : DashboardAction
    data class SelectGroup(val group: Group) : DashboardAction
    data object NavigateBack : DashboardAction
    data class AddGroup(val groupName: String, val members: List<Int>) : DashboardAction
    data class AddExpense(
        val expenseName: String,
        val amount: Amount,
        val desc: String,
        val payers: List<PayerDto>?,
        val shareDetails: ShareDetailsRequest?,
    ) : DashboardAction

    data object DeleteSelectedGroup : DashboardAction
    data class UpdateGroupMembers(val memberIds: List<User>) : DashboardAction
    data class UpdateTransaction(val transactions: List<Transaction>) : DashboardAction
}

data class DashboardState(
    val currentUser: User? = null,
    val extraPaneContentState: ExtraPaneContentState = ExtraPaneContentState.None,
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val selectedGroup: Group? = null,
//    val transactions: List<Transaction> = emptyList(),
    val listPaneLoading: Boolean = true,
    val detailPaneLoading: Boolean = false,
    val transactionLoading: Boolean = false,
    val extraPaneLoading: Boolean = false,
    val listPaneError: Throwable? = null,
    val detailPaneError: Throwable? = null,
    val transactionError: Throwable? = null,
    val extraPaneError: Throwable? = null,
    val isExtraActionSuccess: Boolean = false,
    val isDetailVisible: Boolean = false,
    val totalOwe: Amount = Amount(0),
    val totalOwed: Amount = Amount(0),
) : BaseViewState

sealed interface DashboardEvent : BaseViewEvent {
    data class ShowToast(val message: String) : DashboardEvent
    data object ExtraPaneSuccessful : DashboardEvent
}

sealed class ExtraPaneContentState {
    data object None : ExtraPaneContentState()
    data object AddGroup : ExtraPaneContentState()
    data object AddExpense : ExtraPaneContentState()
}