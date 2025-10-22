package com.pmb.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A base class for ViewModels in a Multiplatform project, following MVI-like principles.
 *
 * @param STATE The type of the state managed by this ViewModel.
 * @param ACTION The type of the actions that can be handled by this ViewModel.
 * @param EVENT The type of the one-time events that can be emitted by this ViewModel.
 * @param initialState The initial state of the ViewModel.
 */
abstract class BaseViewModel<ACTION : BaseViewAction, STATE : BaseViewState, EVENT : BaseViewEvent>(
    initialState: STATE
) : ViewModel() {

    private val _viewState = MutableStateFlow(initialState)
    val viewState = _viewState.asStateFlow()

    private val _viewEvent = MutableSharedFlow<EVENT>()
    val viewEvent = _viewEvent.asSharedFlow()

    /**
     * The primary entry point for the UI to send actions to the ViewModel.
     */
    abstract fun handle(action: ACTION)

    /**
     * Updates the current state.
     * The lambda provides the current state for safe, atomic updates.
     */
    protected fun setState(reducer: (currentState: STATE) -> STATE) {
        _viewState.update(reducer)
    }

    /**
     * Posts a one-time event to the UI (e.g., for navigation or showing a toast).
     */
    protected fun postEvent(event: EVENT) {
        viewModelScope.launch {
            _viewEvent.emit(event)
        }
    }
}

interface BaseViewAction
interface BaseViewState
interface BaseViewEvent

