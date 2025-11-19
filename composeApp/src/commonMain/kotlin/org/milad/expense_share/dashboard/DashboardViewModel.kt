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
import usecase.friends.GetFriendsUseCase
import usecase.groups.CreateGroupUseCase
import usecase.groups.GetGroupsUseCase
import usecase.transactions.CreateTransactionUseCase
import usecase.transactions.GetTransactionsUseCase

class DashboardViewModel(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
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
            is DashboardAction.SelectGroup -> selectGroup(action.group)
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
        }
    }

    private fun createTransaction(
        title: String,
        amount: Double,
        desc: String,
        payers: List<PayerDto>?,
        shareDetails: ShareDetailsRequest?,
    ) {
        viewModelScope.launch {
            setState { it.copy(isLoading = true) }
            viewState.value.selectedGroup?.let {
                createTransactionUseCase(
                    groupId = it.id,
                    title = title,
                    amount = amount,
                    description = desc,
                    payers = payers,
                    shareDetails = shareDetails
                ).collect { result ->
                    result.onSuccess {

                        setState {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }.onFailure { e ->

                        setState { it.copy(error = e, isLoading = false) }
                        postEvent(DashboardEvent.ShowToast("Error: ${e.message}"))
                    }
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch { getGroups() }
            launch { getFriends() }
        }
    }

    private suspend fun getGroups() {
        getGroupsUseCase().collect { res ->
            setState { it.copy(isLoading = true, error = null) }

            res.onSuccess { groups ->
                setState {
                    it.copy(
                        groups = groups,
                        isLoading = false
                    )
                }

                launchTotalCalculation(groups)
            }.onFailure { e ->
                setState { it.copy(error = e, isLoading = false) }
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
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                setState { it.copy(isLoading = false) }
                postEvent(DashboardEvent.ShowToast("Error fetch friends: ${e.message}"))
            }
        }
    }

    private fun launchTotalCalculation(groups: List<Group>) {
        viewModelScope.launch(Dispatchers.Default) {
            // TODO:
            /* val userId = currentUserId() // implement this
             var totalOwed = 0.0
             var totalOwe = 0.0

             for (group in groups) {
                 for (tx in group.transactions) {
                     when (userId) {
                         tx.payerId -> totalOwed += tx.amount
                         tx.receiverId -> totalOwe += tx.amount
                     }
                 }
             }*/
            delay(2000)

            setState {
                it.copy(
                    totalOwed = 1.0,
                    totalOwe = 2.0
                )
            }
        }
    }

    private fun selectGroup(group: Group) {
        viewModelScope.launch {
            try {
                getTransactionsUseCase(group.id.toString()).collect { res ->
                    setState { it.copy(selectedGroup = group, isDetailVisible = true) }
                    res.onSuccess {
                        setState {
                            it.copy(
                                transactions = res.getOrElse { emptyList() },
                                isLoading = false
                            )
                        }
                    }.onFailure {
                        setState {
                            it.copy(
                                error = res.exceptionOrNull(),
                                isLoading = false
                            )
                        }
                        postEvent(DashboardEvent.ShowToast("Error: ${res.exceptionOrNull()?.message}"))
                    }
                }

            } catch (e: Exception) {
                setState { it.copy(isLoading = false, error = e) }
                postEvent(DashboardEvent.ShowToast("Error loading data: ${e.message}"))
            }
        }
    }

    private fun createGroup(groupName: String, members: List<Int>) {
        viewModelScope.launch {
            setState { it.copy(isLoading = true, error = null) }
            createGroupUseCase(groupName, members).collect { result ->
                result.onSuccess { newGroup ->
                    setState {
                        it.copy(
                            groups = it.groups + newGroup,
                            isLoading = false
                        )
                    }
                    postEvent(DashboardEvent.GroupCreatedSuccessful)
                }.onFailure { e ->
                    setState {
                        it.copy(
                            error = e,
                            isLoading = false
                        )
                    }
                    postEvent(DashboardEvent.ShowToast("Error creating group: ${e.message}"))
                }
            }
        }
    }

    private fun navigateBack() {
        setState { it.copy(selectedGroup = null, isDetailVisible = false) }
    }
}

sealed interface DashboardAction : BaseViewAction {
    data class ShowExtraPane(val content: ExtraPaneContentState) : DashboardAction
    data object LoadData : DashboardAction
    data class SelectGroup(val group: Group) : DashboardAction
    data object NavigateBack : DashboardAction
    data class AddGroup(val groupName: String, val members: List<Int>) : DashboardAction
    data class AddExpense(
        val expenseName: String,
        val amount: Double,
        val desc: String,
        val payers: List<PayerDto>?,
        val shareDetails: ShareDetailsRequest?,
    ) : DashboardAction
}

data class DashboardState(
    val extraPaneContentState: ExtraPaneContentState = ExtraPaneContentState.None,
    val groups: List<Group> = emptyList(),
    val friends: List<User> = emptyList(),
    val selectedGroup: Group? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val isDetailVisible: Boolean = false,
    val totalOwe: Double = 0.0,
    val totalOwed: Double = 0.0,
) : BaseViewState

sealed interface DashboardEvent : BaseViewEvent {
    data class ShowToast(val message: String) : DashboardEvent
    data object GroupCreatedSuccessful : DashboardEvent
}

sealed class ExtraPaneContentState {
    data object None : ExtraPaneContentState()
    data object AddGroup : ExtraPaneContentState()
    data object AddExpense : ExtraPaneContentState()
    data object AddMember : ExtraPaneContentState()
}