package org.milad.expense_share.dashboard

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import model.Group
import model.Transaction
import usecase.groups.CreateGroupUseCase
import usecase.groups.GetGroupsUseCase
import usecase.transactions.GetTransactionsUseCase

class DashboardViewModel(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val createGroupUseCase: CreateGroupUseCase,
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
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                getGroupsUseCase().collect { res ->
                    setState { it.copy(isLoading = true, error = null) }
                    res.onSuccess {
                        setState {
                            it.copy(
                                groups = res.getOrElse { emptyList() },
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
                postEvent(
                    DashboardEvent.ShowToast("Error loading data: ${e.message}")
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
    data object LoadData : DashboardAction
    data class SelectGroup(val group: Group) : DashboardAction
    data object NavigateBack : DashboardAction
    data class AddGroup(val groupName: String, val members: List<Int>) : DashboardAction
}

data class DashboardState(
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val isDetailVisible: Boolean = false,
) : BaseViewState

sealed interface DashboardEvent : BaseViewEvent {
    data class ShowToast(val message: String) : DashboardEvent
}
