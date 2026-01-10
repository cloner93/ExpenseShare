package org.milad.expense_share.auth.register

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import usecase.auth.RegisterUserUseCase

class RegisterViewModel(
    private val registerUserUseCase: RegisterUserUseCase,
) : BaseViewModel<RegisterAction, RegisterState, RegisterEvent>(
    initialState = RegisterState()
) {

    override fun handle(action: RegisterAction) {
        when (action) {
            is RegisterAction.NavigateBack -> postEvent(RegisterEvent.NavigateToLogin)
            is RegisterAction.UpdateUserName -> setState { it.copy(userName = action.value) }
            is RegisterAction.UpdatePhone -> setState { it.copy(phone = action.value) }
            is RegisterAction.UpdatePassword -> setState { it.copy(password = action.value) }
            is RegisterAction.Register -> registerUser()
        }
    }

    private fun registerUser() {
        if (!validateForm()) return

        viewModelScope.launch {
            setState { it.copy(isLoading = true, error = null) }
            registerUserUseCase(
                phone = viewState.value.phone,
                username = viewState.value.userName,
                password = viewState.value.password
            ).collect { result ->
                result.onSuccess {
                    setState { it.copy() }
                    postEvent(RegisterEvent.RegisterSuccess)
                }.onFailure {
                    setState { it.copy(error = result.exceptionOrNull()) }
                    print(result.exceptionOrNull()?.message)
                    postEvent(
                        RegisterEvent.ShowToast(
                            "Error: ${result.exceptionOrNull()?.message}"
                        )
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val currentState = viewState.value

        if (currentState.userName.isBlank()) {
            setState { it.copy(nameNameError = "Name is required") }
            isValid = false
        }
        if (currentState.password.length < 5) {
            setState { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }
}

sealed interface RegisterAction : BaseViewAction {
    data object NavigateBack : RegisterAction
    data class UpdateUserName(val value: String) : RegisterAction
    data class UpdatePhone(val value: String) : RegisterAction
    data class UpdatePassword(val value: String) : RegisterAction
    data object Register : RegisterAction
}

data class RegisterState(
    val userName: String = "test",
    val nameNameError: String? = null,
    val phone: String = "09137511005",
    val phoneError: String? = null,
    val password: String = "milad",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
) : BaseViewState

sealed interface RegisterEvent : BaseViewEvent {
    data class ShowToast(val message: String) : RegisterEvent
    data object RegisterSuccess : RegisterEvent
    data object NavigateToLogin : RegisterEvent
}