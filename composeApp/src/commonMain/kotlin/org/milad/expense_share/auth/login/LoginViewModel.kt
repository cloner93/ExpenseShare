package org.milad.expense_share.auth.login

import androidx.lifecycle.viewModelScope
import com.pmb.common.viewmodel.BaseViewAction
import com.pmb.common.viewmodel.BaseViewEvent
import com.pmb.common.viewmodel.BaseViewModel
import com.pmb.common.viewmodel.BaseViewState
import kotlinx.coroutines.launch
import usecase.auth.LoginUserUseCase

class LoginViewModel(
    private val loginUserUseCase: LoginUserUseCase,
) : BaseViewModel<LoginAction, LoginState, LoginEvent>(
    initialState = LoginState()
) {

    override fun handle(action: LoginAction) {
        when (action) {
            is LoginAction.NavigateBack -> postEvent(LoginEvent.NavigateToRegister)
            is LoginAction.UpdatePhone -> setState { it.copy(phone = action.value) }
            is LoginAction.UpdatePassword -> setState { it.copy(password = action.value) }
            is LoginAction.Login -> loginUser()
        }
    }

    private fun loginUser() {
        if (!validateForm()) return

        viewModelScope.launch {
            setState { it.copy(isLoading = true, error = null) }
            loginUserUseCase(
                phone = viewState.value.phone,
                password = viewState.value.password
            ).collect { result ->
                result.onSuccess {
                    setState { it.copy(isLoading = false) }
                    postEvent(LoginEvent.LoginSuccess)
                }.onFailure { e ->
                    setState { it.copy(isLoading = false, error = e) }
                    postEvent(LoginEvent.ShowToast("Error: ${e.message}"))
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val currentState = viewState.value

        if (currentState.phone.isBlank()) {
            setState { it.copy(phoneError = "Phone is required") }
            isValid = false
        }
        if (currentState.password.isBlank()) {
            setState { it.copy(passwordError = "Password is required") }
            isValid = false
        }

        return isValid
    }
}

sealed interface LoginAction : BaseViewAction {
    data object NavigateBack : LoginAction
    data class UpdatePhone(val value: String) : LoginAction
    data class UpdatePassword(val value: String) : LoginAction
    data object Login : LoginAction
}

data class LoginState(
    val phone: String = "09137511005",
    val phoneError: String? = null,
    val password: String = "milad",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
) : BaseViewState

sealed interface LoginEvent : BaseViewEvent {
    data class ShowToast(val message: String) : LoginEvent
    data object LoginSuccess : LoginEvent
    data object NavigateToRegister : LoginEvent
}